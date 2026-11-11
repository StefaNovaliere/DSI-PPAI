package ppai.correo;

/**
 * Un correo listo para enviar: destinatario, asunto y cuerpo en texto plano y
 * en HTML (el cliente de correo muestra el que soporte).
 */
public record Correo(String destinatario, String asunto, String cuerpoTexto, String cuerpoHtml) {
}
