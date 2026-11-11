package ppai.control;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import ppai.correo.Correo;
import ppai.correo.ErrorEnvioCorreo;
import ppai.correo.Html;
import ppai.correo.ServicioCorreo;
import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRecepcion;

/**
 * CU 29 (incluido por el CU 28): notifica por correo electrónico la recepción
 * de un bolsín.
 *
 * <p>Arma el correo con los datos de la recepción y lo envía con el
 * {@link ServicioCorreo} configurado. El envío se hace con el {@link Executor}
 * recibido: en la ventana, en segundo plano para que la pantalla no se
 * congele mientras se conecta con el servidor. El resultado (enviado o error)
 * se informa por el canal de avisos. Si el envío falla, la recepción ya quedó
 * registrada: la notificación no la deshace.
 */
public class GestorNotificacionCU29 {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final String PREFIJO_ENVIANDO = "Enviando la notificación por correo";
    public static final String PREFIJO_ERROR = "No se pudo enviar la notificación por correo";

    private final ServicioCorreo servicioCorreo;
    private final Executor ejecutor;
    private final Consumer<String> avisos;

    /**
     * @param servicioCorreo cómo se envían los correos
     * @param ejecutor       dónde se ejecuta el envío ({@code Runnable::run} para hacerlo en el momento)
     * @param avisos         a quién se le informa el resultado del envío
     */
    public GestorNotificacionCU29(ServicioCorreo servicioCorreo, Executor ejecutor, Consumer<String> avisos) {
        this.servicioCorreo = servicioCorreo;
        this.ejecutor = ejecutor;
        this.avisos = avisos;
    }

    /** Arma el correo de la recepción y lo envía al destinatario. */
    public void notificarRecepcion(String correoDestino, DatosRecepcion recepcion) {
        Correo correo = armarCorreo(correoDestino, recepcion);
        avisos.accept(PREFIJO_ENVIANDO + " a " + correoDestino + "…");
        ejecutor.execute(() -> enviar(correo, recepcion.numeroBolsin()));
    }

    private void enviar(Correo correo, int numeroBolsin) {
        try {
            String resultado = servicioCorreo.enviar(correo);
            avisos.accept("Notificación por correo de la recepción del bolsín N° " + numeroBolsin + " (CU 29): "
                    + resultado + ".");
        } catch (ErrorEnvioCorreo e) {
            avisos.accept(PREFIJO_ERROR + " del bolsín N° " + numeroBolsin
                    + " (CU 29): " + e.getMessage() + ". La recepción quedó registrada igual.");
        }
    }

    private Correo armarCorreo(String destinatario, DatosRecepcion r) {
        String asunto = "Recepción registrada del bolsín N° " + r.numeroBolsin() + " (precinto "
                + r.numeroPrecinto() + ")";
        String fecha = r.fechaHora().format(FORMATO_FECHA);

        StringBuilder texto = new StringBuilder();
        texto.append("Se registró la recepción del bolsín N° ").append(r.numeroBolsin())
                .append(" (precinto ").append(r.numeroPrecinto()).append("), enviado desde ")
                .append(r.cmOrigen()).append(" a ").append(r.cmDestino()).append(".\n\n");
        texto.append("Fecha y hora: ").append(fecha).append('\n');
        texto.append("Registró: ").append(r.responsable()).append("\n\n");
        texto.append("Documentación recibida:\n");
        for (DatosDocumentacion doc : r.documentacion()) {
            texto.append("  - N° ").append(doc.numero()).append(" · ").append(doc.tipoDocumento()).append(" · ")
                    .append(doc.asunto()).append(" · ").append(doc.descripcionEstado()).append('\n');
        }
        texto.append("\nEste es un mensaje automático del Sistema de Bolsines.\n");

        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family:Arial,sans-serif;font-size:14px;color:#222\">");
        html.append("<h2 style=\"color:#1F3A5F;margin:0 0 12px\">Recepción de bolsín registrada</h2>");
        html.append("<p>Se registró la recepción del <b>bolsín N° ").append(r.numeroBolsin()).append("</b> (precinto ")
                .append(Html.escapar(r.numeroPrecinto())).append("), enviado desde <b>")
                .append(Html.escapar(r.cmOrigen())).append("</b> a <b>").append(Html.escapar(r.cmDestino()))
                .append("</b>.</p>");
        html.append("<p>Fecha y hora: ").append(fecha).append("<br>Registró: ")
                .append(Html.escapar(r.responsable())).append("</p>");
        html.append("<table style=\"border-collapse:collapse\" cellpadding=\"6\">");
        html.append("<tr style=\"background:#1F3A5F;color:#fff\"><th align=\"left\">N° Doc</th>"
                + "<th align=\"left\">Tipo</th><th align=\"left\">Asunto</th><th align=\"left\">Estado</th></tr>");
        for (DatosDocumentacion doc : r.documentacion()) {
            html.append("<tr style=\"border-bottom:1px solid #ddd\"><td>").append(doc.numero()).append("</td><td>")
                    .append(Html.escapar(doc.tipoDocumento())).append("</td><td>")
                    .append(Html.escapar(doc.asunto())).append("</td><td>")
                    .append(Html.escapar(doc.descripcionEstado())).append("</td></tr>");
        }
        html.append("</table>");
        html.append("<p style=\"color:#777;font-size:12px;margin-top:16px\">"
                + "Este es un mensaje automático del Sistema de Bolsines.</p></div>");

        return new Correo(destinatario, asunto, texto.toString(), html.toString());
    }
}
