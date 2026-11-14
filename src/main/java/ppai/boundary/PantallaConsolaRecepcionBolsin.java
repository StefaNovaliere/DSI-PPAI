package ppai.boundary;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;

/**
 * Pantalla del CU 28 implementada por consola.
 */
public class PantallaConsolaRecepcionBolsin implements PantallaRecepcionBolsin {

    private final Scanner entrada;
    private final PrintStream salida;
    private GestorRecepcionBolsin gestor;

    public PantallaConsolaRecepcionBolsin(Scanner entrada, PrintStream salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    @Override
    public void setGestor(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    /** El empleado elige la opción del menú. */
    @Override
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

    @Override
    public void mostrarCM(String nombreCM) {
        salida.println("Comisión Médica: " + nombreCM);
    }

    @Override
    public void solicitarSelBolsin(List<DatosBolsin> bolsines) {
        salida.println();
        salida.println("Bolsines enviados a su Comisión Médica:");
        salida.printf("  %-10s %-12s %-20s%n", "N° Bolsín", "N° Precinto", "CM Origen");
        for (DatosBolsin bolsin : bolsines) {
            salida.printf("  %-10d %-12s %-20s%n", bolsin.numeroBolsin(), bolsin.numeroPrecinto(), bolsin.cmOrigen());
        }
        while (true) {
            salida.print("Ingrese el N° de bolsín recibido (F para filtrar por precinto o CM origen, C para cancelar): ");
            String linea = leerLinea().trim();
            if (linea.equalsIgnoreCase("F")) {
                tomarFiltroBolsines();
                return;
            }
            if (linea.equalsIgnoreCase("C")) {
                gestor.cancelarCU();
                return;
            }
            Integer numeroBolsin = convertirNumero(linea);
            if (numeroBolsin != null) {
                tomarSeleccionBolsin(numeroBolsin);
                return;
            }
            salida.println("Debe ingresar un número, la letra F o la letra C.");
        }
    }

    private void tomarFiltroBolsines() {
        salida.print("  N° de precinto (Enter para no filtrar por precinto): ");
        String precinto = leerLinea();
        salida.print("  CM origen (Enter para no filtrar por CM origen): ");
        String cmOrigen = leerLinea();
        gestor.tomarFiltroBolsines(precinto, cmOrigen);
    }

    private void tomarSeleccionBolsin(int numeroBolsin) {
        gestor.tomarSeleccionBolsin(numeroBolsin);
    }

    @Override
    public void mostrarNroRemito(List<Integer> numerosRemito) {
        salida.println();
        salida.println("Remitos del bolsín: " + numerosRemito);
    }

    @Override
    public void mostrarDatosDocumentacion(List<DatosRemito> remitos) {
        for (DatosRemito remito : remitos) {
            salida.println();
            salida.println("Remito N° " + remito.numero() + " (" + remito.descripcionEstado() + ")");
            imprimirDocumentacion(remito.documentacion());
        }
    }

    private void imprimirDocumentacion(List<DatosDocumentacion> documentacion) {
        salida.printf("  %-8s %-24s %-40s %-20s%n", "N° Doc", "Tipo", "Asunto", "Estado");
        for (DatosDocumentacion doc : documentacion) {
            salida.printf("  %-8d %-24s %-40s %-20s%n", doc.numero(), doc.tipoDocumento(), doc.asunto(), doc.descripcionEstado());
        }
    }

    @Override
    public void solicitarSelOpcionesRecBolsin() {
        salida.println();
        salida.println("Opciones de recepción del bolsín:");
        salida.println("  1. El contenido del bolsín es igual al registrado");
        salida.println("  2. No se recibe toda la documentación asociada a los remitos que contiene el bolsín");
        salida.println("  3. Existe documentación que no corresponde al destino (su Comisión Médica)");
        salida.println("  4. La documentación se debe redirigir a otra área");
        salida.println("  0. Cancelar");
        int opcion = leerEntero("Opción: ");
        while (opcion < 0 || opcion > 4) {
            opcion = leerEntero("Opción inválida. Ingrese un número del 0 al 4: ");
        }
        switch (opcion) {
            case 0 -> gestor.cancelarCU();
            case 1 -> gestor.tomarSeleccionPrimeraOpcion();
            case 2 -> gestor.tomarSeleccionSegundaOpcion();
            case 3 -> gestor.tomarSeleccionTerceraOpcion();
            default -> gestor.tomarSeleccionCuartaOpcion();
        }
    }

    @Override
    public void solicitarSelDocumentacion(String indicacion) {
        salida.println(indicacion);
        List<Integer> numeros = null;
        while (numeros == null) {
            salida.print("N° de documentación, separados por comas (por ejemplo 1001, 1002): ");
            numeros = new ArrayList<>();
            for (String parte : leerLinea().split(",")) {
                Integer numero = convertirNumero(parte);
                if (numero != null) {
                    numeros.add(numero);
                } else if (!parte.isBlank()) {
                    salida.println("Debe ingresar números separados por comas.");
                    numeros = null;
                    break;
                }
            }
        }
        gestor.tomarSeleccionDocumentacion(numeros);
    }

    @Override
    public void solicitarConfirmacion() {
        salida.print("¿Confirma la recepción del bolsín con la opción elegida? (S/N): ");
        String respuesta = leerLinea().trim();
        tomarConfirmacion(respuesta.equalsIgnoreCase("S"));
    }

    private void tomarConfirmacion(boolean confirma) {
        gestor.tomarConfirmacion(confirma);
    }

    @Override
    public void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosRemito> remitos) {
        salida.println();
        salida.println("Recepción del bolsín N° " + numeroBolsin + " registrada. Estado de los remitos y la documentación:");
        mostrarDatosDocumentacion(remitos);
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    @Override
    public void finCU() {
        salida.println("Fin del caso de uso.");
    }

    private int leerEntero(String mensaje) {
        while (true) {
            salida.print(mensaje);
            Integer numero = convertirNumero(leerLinea());
            if (numero != null) {
                return numero;
            }
            salida.println("Debe ingresar un número.");
        }
    }

    /** El número escrito, o null si no es un número. */
    private static Integer convertirNumero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String leerLinea() {
        if (!entrada.hasNextLine()) {
            throw new IllegalStateException("Se terminó la entrada de datos.");
        }
        return entrada.nextLine();
    }
}
