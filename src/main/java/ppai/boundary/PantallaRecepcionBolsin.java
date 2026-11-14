package ppai.boundary;

import java.util.List;

import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosRemito;

/**
 * Pantalla (boundary) del CU 28 Registrar Recepción de Bolsín.
 *
 * <p>Son los mensajes que el gestor le envía a la pantalla en el diagrama de
 * secuencia. Hay una implementación por consola y otra gráfica (Swing); el
 * gestor funciona igual con cualquiera de las dos.
 */
public interface PantallaRecepcionBolsin {

    void setGestor(GestorRecepcionBolsin gestor);

    /** El empleado elige la opción "Registrar recepción de bolsín". */
    void opcRegistrarRecBolsin();

    void mostrarCM(String nombreCM);

    /** Paso 4: muestra los bolsines y permite seleccionar uno o filtrar por precinto o CM origen. */
    void solicitarSelBolsin(List<DatosBolsin> bolsines);

    /** Números de los remitos que contiene el bolsín seleccionado. */
    void mostrarNroRemito(List<Integer> numerosRemito);

    /** Documentación del bolsín seleccionado, agrupada por remito. */
    void mostrarDatosDocumentacion(List<DatosRemito> remitos);

    /** Paso 7: las cuatro opciones de recepción del bolsín. */
    void solicitarSelOpcionesRecBolsin();

    /** Flujos A3 a A5: pide la documentación que no llegó, no corresponde o se redirige. */
    void solicitarSelDocumentacion(String indicacion);

    void solicitarConfirmacion();

    /** Estado final de cada remito y de su documentación. */
    void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosRemito> remitos);

    void mostrarMensaje(String mensaje);

    void finCU();
}
