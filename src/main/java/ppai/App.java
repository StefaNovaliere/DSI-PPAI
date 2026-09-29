package ppai;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ppai.boundary.PantallaConsolaRecepcionBolsin;
import ppai.boundary.PantallaGraficaRecepcionBolsin;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Remito;
import ppai.persistencia.Repositorio;
import ppai.persistencia.RepositorioJPA;

/**
 * Punto de entrada: arma las capas y abre la interfaz gráfica.
 * Argumentos: {@code --consola} usa la pantalla por consola (también si no hay
 * entorno gráfico); {@code --reiniciar-datos} vuelve la base a los datos de prueba.
 */
public class App {

    public static void main(String[] args) {
        List<String> argumentos = Arrays.asList(args);
        Repositorio repositorio = new RepositorioJPA(RepositorioJPA.ARCHIVO_BD_POR_DEFECTO,
                argumentos.contains("--reiniciar-datos"));
        if (argumentos.contains("--consola") || GraphicsEnvironment.isHeadless()) {
            iniciarConsola(repositorio);
        } else {
            SwingUtilities.invokeLater(() -> iniciarVentana(repositorio));
        }
    }

    /** Arma la ventana principal y la devuelve (también la usa la captura de pantallas). */
    public static JFrame iniciarVentana(Repositorio repositorio) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no se puede, queda el look and feel por defecto
        }
        PantallaGraficaRecepcionBolsin pantalla = new PantallaGraficaRecepcionBolsin();
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio,
                new GestorNotificacionCU29(pantalla.getSalidaMensajes()));
        pantalla.setGestor(gestor);

        VisorHistorialEstados visor = new VisorHistorialEstados(repositorio);
        JTabbedPane pestanias = new JTabbedPane();
        pestanias.addTab("Registrar recepción de bolsín", pantalla);
        pestanias.addTab("Historial de estados", visor);
        pestanias.addTab("Probar el patrón State", new DemostracionPatronState());
        pestanias.addChangeListener(e -> visor.actualizar());

        JFrame ventana = new JFrame("Sistema de Bolsines - Usuario: " + repositorio.getSesionActual().getUsuario());
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setContentPane(pestanias);
        ventana.setMinimumSize(new Dimension(900, 640));
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        return ventana;
    }

    private static void iniciarConsola(Repositorio repositorio) {
        Scanner entrada = new Scanner(System.in);
        PantallaConsolaRecepcionBolsin pantalla = new PantallaConsolaRecepcionBolsin(entrada, System.out);
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
