package ppai.boundary;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;

/**
 * Pantalla (boundary) del CU 28, implementada por consola.
 */
public class PantallaRecepcionBolsin {

    private final Scanner entrada;
    private final PrintStream salida;
    private GestorRecepcionBolsin gestor;

    public PantallaRecepcionBolsin(Scanner entrada, PrintStream salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public void setGestor(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    /** El empleado elige la opción del menú. */
    public void opcRegistrarRecBolsin() {
        habilitarPantalla();
        gestor.registrarNuevoRecBolsin();
    }

    private void habilitarPantalla() {
        salida.println();
        salida.println("==============================================");
        salida.println("   CU 28 - Registrar Recepción de Bolsín");
        salida.println("==============================================");
    }

    public void mostrarCM(String nombreCM) {
        salida.println("Comisión Médica: " + nombreCM);
    }

    public void solicitarSelBolsin(List<DatosBolsin> bolsines) {
        salida.println();
        salida.println("Bolsines enviados a su Comisión Médica:");
        salida.printf("  %-10s %-12s %-20s%n", "N° Bolsín", "N° Precinto", "CM Origen");
        for (DatosBolsin bolsin : bolsines) {
            salida.printf("  %-10d %-12s %-20s%n", bolsin.numeroBolsin(), bolsin.numeroPrecinto(), bolsin.cmOrigen());
        }
        tomarSeleccionBolsin(leerEntero("Ingrese el N° de bolsín recibido: "));
    }

    private void tomarSeleccionBolsin(int numeroBolsin) {
        gestor.tomarSeleccionBolsin(numeroBolsin);
    }

    public void mostrarNroRemito(int numeroRemito) {
        salida.println();
        salida.println("Remito N° " + numeroRemito);
    }

    public void mostrarDatosDocumentacion(List<DatosDocumentacion> documentacion) {
        salida.printf("  %-8s %-24s %-40s %-18s%n", "N° Doc", "Tipo", "Asunto", "Estado");
        for (DatosDocumentacion doc : documentacion) {
            salida.printf("  %-8d %-24s %-40s %-18s%n", doc.numero(), doc.tipoDocumento(), doc.asunto(), doc.estado());
        }
    }

    public void solicitarSelOpcionesRecBolsin() {
        salida.println();
        salida.println("Seleccione una opción:");
        salida.println("  1. Todo remito y documentación coincide con lo registrado");
        salida.println("  2. Hay diferencias con lo registrado");
        int opcion = leerEntero("Opción: ");
        while (opcion != 1 && opcion != 2) {
            opcion = leerEntero("Opción inválida. Ingrese 1 o 2: ");
        }
        if (opcion == 1) {
            tomarSeleccionPrimerOpcion();
        } else {
            gestor.tomarSeleccionSegundaOpcion();
        }
    }

    private void tomarSeleccionPrimerOpcion() {
        gestor.tomarSeleccionPrimerOpcion();
    }

    public void solicitarConfirmacion() {
        salida.print("¿Confirma la recepción del bolsín? (S/N): ");
        String respuesta = leerLinea().trim();
        tomarConfirmacion(respuesta.equalsIgnoreCase("S"));
    }

    private void tomarConfirmacion(boolean confirma) {
        gestor.tomarConfirmacion(confirma);
    }

    public void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosDocumentacion> documentacion) {
        salida.println();
        salida.println("Recepción del bolsín N° " + numeroBolsin + " registrada. Estado de la documentación:");
        mostrarDatosDocumentacion(documentacion);
    }

    public void mostrarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public void finCU() {
        salida.println("Fin del caso de uso.");
    }

    private int leerEntero(String mensaje) {
        while (true) {
            salida.print(mensaje);
            String linea = leerLinea().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                salida.println("Debe ingresar un número.");
            }
        }
    }

    private String leerLinea() {
        if (!entrada.hasNextLine()) {
            throw new IllegalStateException("Se terminó la entrada de datos.");
        }
        return entrada.nextLine();
    }
}
