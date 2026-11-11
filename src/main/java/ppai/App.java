package ppai;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ppai.boundary.PantallaConsolaRecepcionBolsin;
import ppai.boundary.PantallaGraficaRecepcionBolsin;
import ppai.boundary.PantallaPrincipal;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.correo.ConfiguracionCorreo;
import ppai.correo.ServicioCorreo;
import ppai.entidades.Empleado;
import ppai.entidades.Usuario;
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
        ServicioCorreo servicioCorreo = ConfiguracionCorreo.cargar(Path.of(ConfiguracionCorreo.ARCHIVO_POR_DEFECTO));
        if (argumentos.contains("--consola") || GraphicsEnvironment.isHeadless()) {
            iniciarConsola(repositorio, servicioCorreo);
        } else {
            SwingUtilities.invokeLater(() -> iniciarVentana(repositorio, servicioCorreo));
        }
    }

    /** Arma la ventana principal y la devuelve (también la usan las capturas de pantalla). */
    public static JFrame iniciarVentana(Repositorio repositorio, ServicioCorreo servicioCorreo) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no se puede, queda el look and feel por defecto
        }
        PantallaGraficaRecepcionBolsin pantalla = new PantallaGraficaRecepcionBolsin();
        // El correo se envía en segundo plano para que la ventana no se congele
        Executor enSegundoPlano = Executors.newSingleThreadExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "envio-correo");
            hilo.setDaemon(true);
            return hilo;
        });
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio,
                new GestorNotificacionCU29(servicioCorreo, enSegundoPlano, pantalla::mostrarNotificacionEnviada));
        pantalla.setGestor(gestor);

        PantallaPrincipal ventana = new PantallaPrincipal(nombreUsuarioLogueado(repositorio),
                servicioCorreo.descripcion(), pantalla);
        ventana.setVisible(true);
        return ventana;
    }

    private static String nombreUsuarioLogueado(Repositorio repositorio) {
        Usuario usuario = repositorio.getSesionActual().getUsuario();
        for (Empleado empleado : repositorio.getEmpleados()) {
            if (empleado.esTuUsuario(usuario)) {
                return empleado.getNombreCompleto() + " (" + usuario.getNombre() + ")";
            }
        }
        return usuario.getNombre();
    }

    private static void iniciarConsola(Repositorio repositorio, ServicioCorreo servicioCorreo) {
        Scanner entrada = new Scanner(System.in);
        PantallaConsolaRecepcionBolsin pantalla = new PantallaConsolaRecepcionBolsin(entrada, System.out);
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, repositorio,
                new GestorNotificacionCU29(servicioCorreo, Runnable::run, System.out::println));
        pantalla.setGestor(gestor);

        while (true) {
            System.out.println();
            System.out.println("=== Sistema de Bolsines - Usuario: " + nombreUsuarioLogueado(repositorio) + " ===");
            System.out.println("Notificaciones por correo: " + servicioCorreo.descripcion());
            System.out.println("1. Registrar recepción de bolsín");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            if (!entrada.hasNextLine()) {
                return;
            }
            switch (entrada.nextLine().trim()) {
                case "1" -> pantalla.opcRegistrarRecBolsin();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opción inválida.");
            }
        }
    }
}
