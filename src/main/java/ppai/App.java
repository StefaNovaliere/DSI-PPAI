package ppai;

import java.util.Scanner;

import ppai.boundary.PantallaRecepcionBolsin;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Remito;
import ppai.persistencia.Repositorio;
import ppai.persistencia.RepositorioEnMemoria;

/**
 * Punto de entrada: arma las capas y muestra el menú principal.
 */
public class App {

    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        Repositorio repositorio = new RepositorioEnMemoria();
        PantallaRecepcionBolsin pantalla = new PantallaRecepcionBolsin(entrada, System.out);
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio,
                new GestorNotificacionCU29(System.out));
        pantalla.setGestor(gestor);

        while (true) {
            System.out.println();
            System.out.println("=== Sistema de Bolsines - Usuario: " + repositorio.getSesionActual().getUsuario() + " ===");
            System.out.println("1. Registrar recepción de bolsín (CU 28)");
            System.out.println("2. Ver bolsines y estados de la documentación");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            if (!entrada.hasNextLine()) {
                return;
            }
            switch (entrada.nextLine().trim()) {
                case "1" -> pantalla.opcRegistrarRecBolsin();
                case "2" -> listarEstados(repositorio);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private static void listarEstados(Repositorio repositorio) {
        for (Bolsin bolsin : repositorio.getBolsines()) {
            System.out.println();
            System.out.println("Bolsín " + bolsin.getNumeroBolsin() + " (" + bolsin.obtenerCMOrigen() + " -> "
                    + bolsin.obtenerCMDestino().getNombre() + ") estado: " + bolsin.getEstadoActual().getNombre());
            for (Remito remito : bolsin.getRemitos()) {
                System.out.println("  Remito " + remito.obtenerNumero() + " estado: " + remito.getEstado().getNombre());
                for (DetalleRemito detalle : remito.getDetallesRemito()) {
                    System.out.println("    Doc " + detalle.getDocumentacion().getNumero() + " historial:");
                    for (CambioEstadoDocumentacion ce : detalle.getDocumentacion().getCambiosEstado()) {
                        System.out.println("      " + ce.getEstado().getNombre() + "  desde " + ce.getFechaHoraInicio()
                                + (ce.sosActual() ? "  (actual)" : "  hasta " + ce.getFechaHoraFin()));
                    }
                }
            }
        }
    }
}
