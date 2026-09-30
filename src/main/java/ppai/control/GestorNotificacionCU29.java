package ppai.control;

import java.util.List;
import java.util.function.Consumer;

import ppai.dto.DatosDocumentacion;

/**
 * CU 29 (incluido por el CU 28): notificación por correo de la recepción.
 *
 * <p>El envío se simula: el texto del correo se entrega al canal recibido en
 * el constructor (la consola, o la ventana, que lo ofrece para verlo).
 */
public class GestorNotificacionCU29 {

    private final Consumer<String> canal;

    public GestorNotificacionCU29(Consumer<String> canal) {
        this.canal = canal;
    }

    public void notificarRecepcion(String correo, int numeroBolsin, List<DatosDocumentacion> documentacion) {
        StringBuilder correoTexto = new StringBuilder();
        correoTexto.append("[CU 29] Correo enviado a ").append(correo).append('\n');
        correoTexto.append("Asunto: Recepción del bolsín N° ").append(numeroBolsin).append('\n');
        correoTexto.append("Se registró la recepción de la siguiente documentación:\n");
        for (DatosDocumentacion doc : documentacion) {
            correoTexto.append("  - N° ").append(doc.numero()).append(" (").append(doc.tipoDocumento()).append("): ")
                    .append(doc.asunto()).append(" -> ").append(doc.descripcionEstado()).append('\n');
        }
        canal.accept(correoTexto.toString());
    }
}
