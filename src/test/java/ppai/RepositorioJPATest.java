package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ppai.boundary.PantallaConsolaRecepcionBolsin;
import ppai.boundary.PantallaRecepcionBolsin;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.correo.ServicioCorreoSimulado;
import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Documentacion;
import ppai.entidades.Remito;
import ppai.entidades.estadobolsin.Enviado;
import ppai.entidades.estadobolsin.RecibidoEnCMDestino;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.NoRecibida;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.entidades.estadoremito.RemitoRecibidoYAceptado;
import ppai.entidades.estadoremito.RemitoRecibidoYAceptadoParcial;
import ppai.persistencia.RepositorioJPA;

/** Prueba de la persistencia en SQLite con JPA/Hibernate. */
class RepositorioJPATest {

    @TempDir
    Path carpeta;

    private String ejecutarCU(RepositorioJPA repositorio, String entradaUsuario) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(salida, true, StandardCharsets.UTF_8);
        PantallaRecepcionBolsin pantalla = new PantallaConsolaRecepcionBolsin(new Scanner(entradaUsuario), out);
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio, new GestorNotificacionCU29(
                new ServicioCorreoSimulado(carpeta.resolve("correos"), "es una prueba"), Runnable::run, out::println));
        pantalla.setGestor(gestor);
        pantalla.opcRegistrarRecBolsin();
        return salida.toString(StandardCharsets.UTF_8);
    }

    private static Bolsin bolsin(RepositorioJPA repositorio, int numero) {
        return repositorio.getBolsines().stream().filter(b -> b.getNumeroBolsin() == numero).findFirst().orElseThrow();
    }

    private static Documentacion documentacion(Bolsin bolsin, int numeroRemito, int indice) {
        Remito remito = bolsin.getRemitos().stream().filter(r -> r.obtenerNumero() == numeroRemito).findFirst()
                .orElseThrow();
        return remito.getDetallesRemito().get(indice).getDocumentacion();
    }

    @Test
    void laBaseSeCargaConLosDatosDePrueba() {
        String archivo = carpeta.resolve("prueba.db").toString();
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, true)) {
            assertEquals(7, repositorio.getBolsines().size());
            assertEquals("aperez", repositorio.getSesionActual().getUsuario().getNombre());
            // Los conversores materializan el objeto del estado concreto de cada clase
            Bolsin bolsin101 = bolsin(repositorio, 101);
            assertInstanceOf(Enviado.class, bolsin101.getEstadoActual());
            assertEquals(3, bolsin101.getCambiosEstado().size());
            assertInstanceOf(ppai.entidades.estadoremito.EnBolsinEnviado.class,
                    bolsin101.getRemitos().get(0).getEstado());
            Documentacion redirigida = documentacion(bolsin101, 5002, 0);
            assertInstanceOf(ParaRedirigir.class, redirigida.getEstadoActual());
            assertEquals(5, redirigida.getCambiosEstado().size());
        }
    }

    @Test
    void laRecepcionQuedaGuardadaEnLaBase() {
        String archivo = carpeta.resolve("prueba.db").toString();
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, true)) {
            ejecutarCU(repositorio, "101\n1\nS\n");
        }

        // Se abre de nuevo la base (sin reiniciar): los objetos se materializan desde SQLite
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            Bolsin recibido = bolsin(repositorio, 101);
            assertInstanceOf(RecibidoEnCMDestino.class, recibido.getEstadoActual());
            assertEquals(4, recibido.getCambiosEstado().size());
            assertEquals("Pérez, Ana", recibido.getCambiosEstado().get(3).getResponsableCE().getNombreCompleto());
            for (Remito remito : recibido.getRemitos()) {
                assertInstanceOf(RemitoRecibidoYAceptado.class, remito.getEstado());
                for (DetalleRemito detalle : remito.getDetallesRemito()) {
                    Documentacion doc = detalle.getDocumentacion();
                    assertInstanceOf(RecibidaYAceptada.class, doc.getEstadoActual());
                    long actuales = doc.getCambiosEstado().stream().filter(CambioEstadoDocumentacion::sosActual).count();
                    assertEquals(1, actuales);
                    assertEquals("Pérez, Ana",
                            doc.getCambiosEstado().get(doc.getCambiosEstado().size() - 1).getResponsableCE().getNombreCompleto());
                }
            }
            // El bolsín ya recibido no vuelve a listarse; el 102 sigue pendiente
            assertInstanceOf(Enviado.class, bolsin(repositorio, 102).getEstadoActual());
        }
    }

    @Test
    void laRecepcionParcialQuedaGuardadaEnLaBase() {
        String archivo = carpeta.resolve("prueba.db").toString();
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, true)) {
            ejecutarCU(repositorio, "105\n2\n1009\nS\n");
        }
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            Bolsin recibido = bolsin(repositorio, 105);
            assertInstanceOf(RecibidoEnCMDestino.class, recibido.getEstadoActual());
            assertInstanceOf(RemitoRecibidoYAceptadoParcial.class, recibido.getRemitos().get(0).getEstado());
            assertInstanceOf(RemitoRecibidoYAceptado.class, recibido.getRemitos().get(1).getEstado());
            assertInstanceOf(NoRecibida.class, documentacion(recibido, 9001, 1).getEstadoActual());
            assertInstanceOf(RecibidaYAceptada.class, documentacion(recibido, 9001, 0).getEstadoActual());
        }
    }

    @Test
    void siUnEstadoRechazaLaTransicionNoSeGuardaNada() {
        String archivo = carpeta.resolve("prueba.db").toString();
        String texto;
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, true)) {
            // La 1003 está Para redirigir: en la máquina de estados no tiene transición a No recibida
            texto = ejecutarCU(repositorio, "101\n2\n1003\nS\nC\n");
        }
        assertTrue(texto.contains("No se puede recibir la documentación N° 1003 como «no recibida» porque está "
                + "en estado «Para redirigir». No se guardó ningún cambio."));
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            Bolsin pendiente = bolsin(repositorio, 101);
            assertInstanceOf(Enviado.class, pendiente.getEstadoActual());
            assertEquals(3, pendiente.getCambiosEstado().size());
            assertInstanceOf(ppai.entidades.estadoremito.EnBolsinEnviado.class,
                    pendiente.getRemitos().get(0).getEstado());
            assertInstanceOf(EnBolsinEnviado.class, documentacion(pendiente, 5001, 0).getEstadoActual());
            assertInstanceOf(ParaRedirigir.class, documentacion(pendiente, 5002, 0).getEstadoActual());
        }
    }

    @Test
    void siSeCancelaNoSeGuardaNada() {
        String archivo = carpeta.resolve("prueba.db").toString();
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, true)) {
            ejecutarCU(repositorio, "102\n1\nN\n0\n");
        }
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            Bolsin pendiente = bolsin(repositorio, 102);
            assertInstanceOf(Enviado.class, pendiente.getEstadoActual());
            assertInstanceOf(EnBolsinEnviado.class, documentacion(pendiente, 6001, 0).getEstadoActual());
        }
    }

    @Test
    void unaBaseDeUnaVersionAnteriorSeVuelveACrear() throws Exception {
        String archivo = carpeta.resolve("vieja.db").toString();
        try (Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + archivo);
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("create table estado (id integer primary key, ambito varchar, nombre varchar)");
        }
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            assertEquals(7, repositorio.getBolsines().size());
        }
        // La segunda vez ya es de esta versión: no se vuelve a crear
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            ejecutarCU(repositorio, "101\n1\nS\n");
        }
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            assertInstanceOf(RecibidoEnCMDestino.class, bolsin(repositorio, 101).getEstadoActual());
        }
    }
}
