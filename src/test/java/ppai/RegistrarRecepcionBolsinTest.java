package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import ppai.boundary.PantallaConsolaRecepcionBolsin;
import ppai.boundary.PantallaRecepcionBolsin;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.entidades.Bolsin;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Remito;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.persistencia.RepositorioEnMemoria;

/** Prueba de punta a punta del CU 28 con la pantalla por consola. */
class RegistrarRecepcionBolsinTest {

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();

    private void ejecutarCU(String entradaUsuario) {
        PrintStream out = new PrintStream(salida, true, StandardCharsets.UTF_8);
        PantallaRecepcionBolsin pantalla = new PantallaConsolaRecepcionBolsin(new Scanner(entradaUsuario), out);
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio,
                new GestorNotificacionCU29(out::println));
        pantalla.setGestor(gestor);
        pantalla.opcRegistrarRecBolsin();
    }

    private Bolsin bolsin(int numero) {
        return repositorio.getBolsines().stream().filter(b -> b.getNumeroBolsin() == numero).findFirst().orElseThrow();
    }

    @Test
    void registraLaRecepcionDelBolsinYSuDocumentacion() {
        ejecutarCU("101\n1\nS\n");
        String texto = salida.toString(StandardCharsets.UTF_8);

        // Sólo se listan los bolsines enviados a la CM del usuario
        assertTrue(texto.contains("CM Rosario"));
        assertTrue(texto.contains("CM Mendoza"));
        assertFalse(texto.contains("PR-0103"));
        assertFalse(texto.contains("PR-0104"));

        Bolsin recibido = bolsin(101);
        assertEquals("RecibidoEnCMDestino", recibido.getEstadoActual().getNombre());
        for (Remito remito : recibido.getRemitos()) {
            assertEquals("RecibidoYAceptado", remito.getEstado().getNombre());
            for (DetalleRemito detalle : remito.getDetallesRemito()) {
                // Tanto EnBolsinEnviado como ParaRedirigir pasan a Recibida&Aceptada
                assertInstanceOf(RecibidaYAceptada.class, detalle.getDocumentacion().getEstadoActual());
            }
        }
        assertTrue(texto.contains("[CU 29] Correo enviado a ana.perez@cm.gob.ar"));

        // El otro bolsín no se modificó
        assertEquals("Enviado", bolsin(102).getEstadoActual().getNombre());
    }

    @Test
    void siNoConfirmaNoCambiaNingunEstado() {
        ejecutarCU("102\n1\nN\n");
        assertEquals("Enviado", bolsin(102).getEstadoActual().getNombre());
        for (Remito remito : bolsin(102).getRemitos()) {
            for (DetalleRemito detalle : remito.getDetallesRemito()) {
                assertInstanceOf(EnBolsinEnviado.class, detalle.getDocumentacion().getEstadoActual());
            }
        }
        assertTrue(salida.toString(StandardCharsets.UTF_8).contains("la operación fue cancelada"));
    }

    @Test
    void bolsinInexistenteVuelveASolicitarSeleccion() {
        ejecutarCU("999\n102\n1\nS\n");
        assertTrue(salida.toString(StandardCharsets.UTF_8).contains("no está entre los bolsines enviados"));
        assertEquals("RecibidoEnCMDestino", bolsin(102).getEstadoActual().getNombre());
    }
}
