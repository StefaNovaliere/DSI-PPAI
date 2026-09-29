package ppai.control;

import java.io.PrintStream;
import java.util.List;

import ppai.dto.DatosDocumentacion;

/**
 * CU 29 (incluido por el CU 28): notificación por correo de la recepción.
 * Se simula el envío mostrando el mensaje por consola.
 */
public class GestorNotificacionCU29 {

    private final PrintStream salida;

    public GestorNotificacionCU29(PrintStream salida) {
        this.salida = salida;
    }

    public void notificarRecepcion(String correo, int numeroBolsin, List<DatosDocumentacion> documentacion) {
        salida.println();
        salida.println("[CU 29] Enviando correo a " + correo);
        salida.println("        Asunto: Recepción del bolsín N° " + numeroBolsin);
        for (DatosDocumentacion doc : documentacion) {
            salida.println("        - Doc " + doc.numero() + " (" + doc.tipoDocumento() + "): "
                    + doc.asunto() + " -> " + doc.estado());
        }
    }
}
