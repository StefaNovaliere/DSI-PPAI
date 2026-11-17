package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ppai.boundary.PantallaConsolaRecepcionBolsin;
import ppai.boundary.PantallaRecepcionBolsin;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.correo.ServicioCorreoSimulado;
import ppai.entidades.Bolsin;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.Remito;
import ppai.entidades.Sesion;
import ppai.entidades.Usuario;
import ppai.entidades.estadobolsin.Enviado;
import ppai.entidades.estadobolsin.RecibidoEnCMDestino;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.NoRecibida;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.entidades.estadodocumentacion.RecibidaYRechazada;
import ppai.entidades.estadoremito.RemitoRecibidoYAceptado;
import ppai.entidades.estadoremito.RecibidoYAceptadoParcial;
import ppai.persistencia.DatosDePrueba;
import ppai.persistencia.Repositorio;
import ppai.persistencia.RepositorioEnMemoria;

/** Pruebas de punta a punta del CU 28 con la pantalla por consola. */
class RegistrarRecepcionBolsinTest {

    @TempDir
    Path carpeta;

    private Repositorio repositorio = new RepositorioEnMemoria();
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();

    private String ejecutarCU(String entradaUsuario) {
        PrintStream out = new PrintStream(salida, true, StandardCharsets.UTF_8);
        PantallaRecepcionBolsin pantalla = new PantallaConsolaRecepcionBolsin(new Scanner(entradaUsuario), out);
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio,
                new GestorNotificacionCU29(new ServicioCorreoSimulado(carpeta, "es una prueba"), Runnable::run, out::println));
        pantalla.setGestor(gestor);
        pantalla.opcRegistrarRecBolsin();
        return salida.toString(StandardCharsets.UTF_8);
    }

    private Bolsin bolsin(int numero) {
        return repositorio.getBolsines().stream().filter(b -> b.getNumeroBolsin() == numero).findFirst().orElseThrow();
    }

    private Remito remito(int numeroBolsin, int numeroRemito) {
        return bolsin(numeroBolsin).getRemitos().stream().filter(r -> r.obtenerNumero() == numeroRemito)
                .findFirst().orElseThrow();
    }

    private Documentacion documentacion(int numeroBolsin, int numeroDocumentacion) {
        for (Remito remito : bolsin(numeroBolsin).getRemitos()) {
            for (DetalleRemito detalle : remito.getDetallesRemito()) {
                if (detalle.esDocumentacion(numeroDocumentacion)) {
                    return detalle.getDocumentacion();
                }
            }
        }
        throw new AssertionError("No está la documentación " + numeroDocumentacion);
    }

    /** Texto del último listado de bolsines que mostró la consola. */
    private static String ultimoListado(String texto) {
        int inicio = texto.lastIndexOf("Bolsines enviados a su Comisión Médica:");
        return texto.substring(inicio, texto.indexOf("Ingrese el N° de bolsín", inicio));
    }

    @Test
    void primeraOpcionRecibeYAceptaTodo() {
        String texto = ejecutarCU("101\n1\nS\n");

        // Sólo se listan los bolsines enviados a la CM del usuario
        String listado = ultimoListado(texto);
        assertTrue(listado.contains("PR-0101") && listado.contains("PR-0102") && listado.contains("PR-0105"));
        assertFalse(listado.contains("PR-0103"));
        assertFalse(listado.contains("PR-0104"));

        Bolsin recibido = bolsin(101);
        assertInstanceOf(RecibidoEnCMDestino.class, recibido.getEstadoActual());
        for (Remito remito : recibido.getRemitos()) {
            assertInstanceOf(RemitoRecibidoYAceptado.class, remito.getEstado());
            for (DetalleRemito detalle : remito.getDetallesRemito()) {
                // EnBolsinEnviado y ParaRedirigir pasan a Recibida&Aceptada (máquina de estados de la Entrega 1)
                assertInstanceOf(RecibidaYAceptada.class, detalle.getDocumentacion().getEstadoActual());
            }
        }
        // CU 29: se notifica a la CM origen
        assertTrue(texto.contains("Enviando la notificación por correo a CM Rosario (mesa.entradas@cmrosario.example)"));
        // El otro bolsín no se modificó
        assertInstanceOf(Enviado.class, bolsin(102).getEstadoActual());
    }

    @Test
    void segundaOpcionLaDocumentacionQueNoLlegoQuedaNoRecibidaYElRemitoParcial() {
        String texto = ejecutarCU("105\n2\n1009\nS\n");

        assertTrue(texto.contains("Indique la documentación que no se recibió."));
        assertInstanceOf(RecibidoEnCMDestino.class, bolsin(105).getEstadoActual());
        assertInstanceOf(NoRecibida.class, documentacion(105, 1009).getEstadoActual());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(105, 1008).getEstadoActual());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(105, 1010).getEstadoActual());
        // El remito que tiene la documentación que no llegó queda parcial; el otro, aceptado
        assertInstanceOf(RecibidoYAceptadoParcial.class, remito(105, 9001).getEstado());
        assertInstanceOf(RemitoRecibidoYAceptado.class, remito(105, 9002).getEstado());
        assertTrue(texto.contains("Remito N° 9001 (Recibido y aceptado parcial)"));
        assertTrue(texto.contains("CM Córdoba (mesa.entradas@cmcordoba.example)"));
    }

    @Test
    void terceraOpcionLaDocumentacionQueNoCorrespondeQuedaRechazada() {
        ejecutarCU("102\n3\n1005\nS\n");

        assertInstanceOf(RecibidaYRechazada.class, documentacion(102, 1005).getEstadoActual());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(102, 1004).getEstadoActual());
        assertInstanceOf(RecibidoYAceptadoParcial.class, remito(102, 6001).getEstado());
        assertInstanceOf(RecibidoEnCMDestino.class, bolsin(102).getEstadoActual());
    }

    @Test
    void cuartaOpcionLaDocumentacionQuedaParaRedirigirYElRemitoAceptado() {
        ejecutarCU("105\n4\n1008, 1010\nS\n");

        assertInstanceOf(ParaRedirigir.class, documentacion(105, 1008).getEstadoActual());
        assertInstanceOf(ParaRedirigir.class, documentacion(105, 1010).getEstadoActual());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(105, 1009).getEstadoActual());
        assertInstanceOf(RemitoRecibidoYAceptado.class, remito(105, 9001).getEstado());
        assertInstanceOf(RemitoRecibidoYAceptado.class, remito(105, 9002).getEstado());
    }

    @Test
    void cuartaOpcionSoloCorrespondeSiLaCMDestinoEsLaCMC() {
        // Usuario de CM Rosario: recibe el bolsín 103, que viene de la CMC
        repositorio = new RepositorioConUsuario("jgomez");
        String texto = ejecutarCU("103\n4\n1\nS\n");

        assertTrue(texto.contains("corresponde sólo cuando la CM destino es la Comisión Médica Central"));
        // Después de la advertencia eligió la primera opción
        assertInstanceOf(RecibidoEnCMDestino.class, bolsin(103).getEstadoActual());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(103, 1006).getEstadoActual());
    }

    @Test
    void filtroPorPrecintoSinResultadosInformaYMuestraTodos() {
        String texto = ejecutarCU("F\nPR-9999\n\nC\n");

        assertTrue(texto.contains("No se encontró ningún bolsín enviado a Comisión Médica Central con el número "
                + "de precinto «PR-9999». Se muestran todos los bolsines pendientes."));
        assertTrue(ultimoListado(texto).contains("PR-0105"));
    }

    @Test
    void filtroPorCMOrigenSinResultadosInforma() {
        String texto = ejecutarCU("F\n\nSalta\nC\n");
        assertTrue(texto.contains("desde la CM de origen «Salta»"));
    }

    @Test
    void filtroPorCMOrigenMuestraSoloEsosBolsines() {
        String texto = ejecutarCU("F\n\nrosario\n101\n1\nS\n");

        String filtrado = texto.substring(texto.indexOf("CM origen (Enter"));
        String listado = filtrado.substring(0, filtrado.indexOf("Ingrese el N° de bolsín"));
        assertTrue(listado.contains("PR-0101"));
        assertFalse(listado.contains("PR-0102"));
        assertFalse(listado.contains("PR-0105"));
        assertInstanceOf(RecibidoEnCMDestino.class, bolsin(101).getEstadoActual());
    }

    @Test
    void filtroPorPrecintoEncuentraElBolsin() {
        String texto = ejecutarCU("F\npr-0102\n\nC\n");
        String filtrado = texto.substring(texto.indexOf("CM origen (Enter"));
        assertTrue(filtrado.contains("PR-0102"));
        assertFalse(filtrado.contains("PR-0101"));
    }

    @Test
    void siNoConfirmaVuelveALasOpcionesSinCambiarNingunEstado() {
        String texto = ejecutarCU("102\n1\nN\n0\n");

        assertTrue(texto.contains("porque no se confirmó"));
        assertTrue(texto.contains("Se canceló el registro de la recepción"));
        assertInstanceOf(Enviado.class, bolsin(102).getEstadoActual());
        for (Remito remito : bolsin(102).getRemitos()) {
            for (DetalleRemito detalle : remito.getDetallesRemito()) {
                assertInstanceOf(EnBolsinEnviado.class, detalle.getDocumentacion().getEstadoActual());
            }
        }
    }

    @Test
    void laDocumentacionIndicadaTieneQueEstarEnElBolsin() {
        String texto = ejecutarCU("105\n2\n1001\n2\n\n0\n");

        assertTrue(texto.contains("La documentación N° 1001 no está en el bolsín N° 105."));
        assertTrue(texto.contains("No se indicó ninguna documentación"));
        assertInstanceOf(Enviado.class, bolsin(105).getEstadoActual());
    }

    @Test
    void bolsinInexistenteVuelveASolicitarSeleccion() {
        String texto = ejecutarCU("999\n102\n1\nS\n");
        assertTrue(texto.contains("no está entre los bolsines enviados"));
        assertInstanceOf(RecibidoEnCMDestino.class, bolsin(102).getEstadoActual());
    }

    /** Los mismos datos de prueba, con la sesión de otro usuario. */
    private static class RepositorioConUsuario implements Repositorio {

        private final DatosDePrueba datos = new DatosDePrueba();
        private final Sesion sesion;

        RepositorioConUsuario(String nombreUsuario) {
            Usuario usuario = datos.getUsuarios().stream().filter(u -> u.getNombre().equals(nombreUsuario))
                    .findFirst().orElseThrow();
            sesion = new Sesion(LocalDateTime.now(), usuario);
        }

        @Override
        public Sesion getSesionActual() {
            return sesion;
        }

        @Override
        public List<Empleado> getEmpleados() {
            return datos.getEmpleados();
        }

        @Override
        public List<Bolsin> getBolsines() {
            return datos.getBolsines();
        }

        @Override
        public void iniciarTransaccion() {
        }

        @Override
        public void actualizar(Bolsin bolsin) {
        }

        @Override
        public void confirmarTransaccion() {
        }

        @Override
        public void deshacerTransaccion() {
        }
    }
}
