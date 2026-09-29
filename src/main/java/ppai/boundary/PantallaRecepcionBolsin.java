package ppai.boundary;

import java.util.List;

import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;

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

    void solicitarSelBolsin(List<DatosBolsin> bolsines);

    void mostrarNroRemito(int numeroRemito);

    void mostrarDatosDocumentacion(List<DatosDocumentacion> documentacion);

    void solicitarSelOpcionesRecBolsin();

    void solicitarConfirmacion();

    void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosDocumentacion> documentacion);

    void mostrarMensaje(String mensaje);

    void finCU();
}
