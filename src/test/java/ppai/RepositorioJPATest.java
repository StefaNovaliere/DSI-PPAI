package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ppai.boundary.PantallaConsolaRecepcionBolsin;
import ppai.boundary.PantallaRecepcionBolsin;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Documentacion;
import ppai.entidades.Remito;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.persistencia.RepositorioJPA;

/** Prueba de la persistencia en SQLite con JPA/Hibernate. */
class RepositorioJPATest {

    @TempDir
    Path carpeta;

    private void ejecutarCU(RepositorioJPA repositorio, String entradaUsuario) {
        PrintStream out = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        PantallaRecepcionBolsin pantalla = new PantallaConsolaRecepcionBolsin(new Scanner(entradaUsuario), out);
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio, new GestorNotificacionCU29(out::println));
        pantalla.setGestor(gestor);
        pantalla.opcRegistrarRecBolsin();
    }

    private static Bolsin bolsin(RepositorioJPA repositorio, int numero) {
        return repositorio.getBolsines().stream().filter(b -> b.getNumeroBolsin() == numero).findFirst().orElseThrow();
    }

    @Test
    void laBaseSeCargaConLosDatosDePrueba() {
        String archivo = carpeta.resolve("prueba.db").toString();
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, true)) {
            assertEquals(4, repositorio.getBolsines().size());
            assertEquals("aperez", repositorio.getSesionActual().getUsuario().getNombre());
            // El conversor materializa el objeto del estado concreto
            Documentacion redirigida = bolsin(repositorio, 101).getRemitos().get(1)
                    .getDetallesRemito().get(0).getDocumentacion();
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
            assertEquals("RecibidoEnCMDestino", recibido.getEstadoActual().getNombre());
            assertEquals(3, recibido.getCambiosEstado().size());
            for (Remito remito : recibido.getRemitos()) {
                assertEquals("RecibidoYAceptado", remito.getEstado().getNombre());
                for (DetalleRemito detalle : remito.getDetallesRemito()) {
                    Documentacion doc = detalle.getDocumentacion();
                    assertInstanceOf(RecibidaYAceptada.class, doc.getEstadoActual());
                    long actuales = doc.getCambiosEstado().stream().filter(CambioEstadoDocumentacion::sosActual).count();
                    assertEquals(1, actuales);
                    assertEquals("Pérez, Ana",
                            doc.getCambiosEstado().get(doc.getCambiosEstado().size() - 1).getResponsableCE().getNombreCompleto());
                }
            }
            // Flujo alternativo: el bolsín ya recibido no vuelve a listarse; el 102 sigue pendiente
            assertEquals("Enviado", bolsin(repositorio, 102).getEstadoActual().getNombre());
        }
    }

    @Test
    void siSeCancelaNoSeGuardaNada() {
        String archivo = carpeta.resolve("prueba.db").toString();
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, true)) {
            ejecutarCU(repositorio, "102\n1\nN\n");
        }
        try (RepositorioJPA repositorio = new RepositorioJPA(archivo, false)) {
            Bolsin pendiente = bolsin(repositorio, 102);
            assertEquals("Enviado", pendiente.getEstadoActual().getNombre());
            Documentacion doc = pendiente.getRemitos().get(0).getDetallesRemito().get(0).getDocumentacion();
            assertInstanceOf(EnBolsinEnviado.class, doc.getEstadoActual());
        }
    }
}
