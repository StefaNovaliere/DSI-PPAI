package ppai.correo;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * Envía los correos por un servidor SMTP real (Gmail, Mailtrap, el servidor de
 * la institución, etc.) usando Jakarta Mail.
 */
public class ServicioCorreoSmtp implements ServicioCorreo {

    private static final String NOMBRE_REMITENTE = "Sistema de Bolsines";
    private static final String TIEMPO_ESPERA_MS = "15000";

    private final String host;
    private final int puerto;
    private final boolean starttls;
    private final boolean ssl;
    private final String usuario;
    private final String contrasenia;
    private final String remitente;
    private final String redirigirA;

    /**
     * @param usuario    usuario SMTP; vacío o null si el servidor no pide autenticación
     * @param redirigirA si no es vacío, todos los correos se envían a esta dirección
     *                   (útil para la demo, porque los empleados de prueba no tienen correos reales)
     */
    public ServicioCorreoSmtp(String host, int puerto, boolean starttls, boolean ssl, String usuario,
                              String contrasenia, String remitente, String redirigirA) {
        this.host = host;
        this.puerto = puerto;
        this.starttls = starttls;
        this.ssl = ssl;
        this.usuario = usuario;
        this.contrasenia = contrasenia;
        this.remitente = remitente;
        this.redirigirA = redirigirA;
    }

    @Override
    public String enviar(Correo correo) throws ErrorEnvioCorreo {
        Correo aEnviar = aplicarRedireccion(correo);
        try {
            MimeMessage mensaje = new MimeMessage(crearSesion());
            mensaje.setFrom(new InternetAddress(remitente, NOMBRE_REMITENTE, StandardCharsets.UTF_8.name()));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(aEnviar.destinatario()));
            mensaje.setSubject(aEnviar.asunto(), StandardCharsets.UTF_8.name());
            mensaje.setSentDate(new Date());

            MimeBodyPart texto = new MimeBodyPart();
            texto.setText(aEnviar.cuerpoTexto(), StandardCharsets.UTF_8.name());
            MimeBodyPart html = new MimeBodyPart();
            html.setContent(aEnviar.cuerpoHtml(), "text/html; charset=UTF-8");
            MimeMultipart alternativas = new MimeMultipart("alternative");
            alternativas.addBodyPart(texto);
            alternativas.addBodyPart(html);
            mensaje.setContent(alternativas);

            Transport.send(mensaje);
            return "se envió a " + aEnviar.destinatario() + " (servidor " + host + ")";
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new ErrorEnvioCorreo(explicar(e), e);
        }
    }

    @Override
    public String descripcion() {
        return "se envían por SMTP (" + host + ":" + puerto + ")" + (redirigirA == null || redirigirA.isBlank()
                ? "" : ", todas a " + redirigirA);
    }

    private Session crearSesion() {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", host);
        propiedades.put("mail.smtp.port", String.valueOf(puerto));
        propiedades.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        propiedades.put("mail.smtp.starttls.required", String.valueOf(starttls));
        propiedades.put("mail.smtp.ssl.enable", String.valueOf(ssl));
        propiedades.put("mail.smtp.connectiontimeout", TIEMPO_ESPERA_MS);
        propiedades.put("mail.smtp.timeout", TIEMPO_ESPERA_MS);
        propiedades.put("mail.smtp.writetimeout", TIEMPO_ESPERA_MS);
        boolean conAutenticacion = usuario != null && !usuario.isBlank();
        propiedades.put("mail.smtp.auth", String.valueOf(conAutenticacion));
        if (!conAutenticacion) {
            return Session.getInstance(propiedades);
        }
        return Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, contrasenia);
            }
        });
    }

    /** En modo demo, envía todo a una sola dirección y aclara a quién iba dirigido. */
    private Correo aplicarRedireccion(Correo correo) {
        if (redirigirA == null || redirigirA.isBlank()) {
            return correo;
        }
        String nota = "[Correo redirigido para la demo. Destinatario original: " + correo.destinatario() + "]";
        return new Correo(redirigirA, correo.asunto(), nota + "\n\n" + correo.cuerpoTexto(),
                "<p style=\"color:#666;font-size:12px\">" + nota + "</p>" + correo.cuerpoHtml());
    }

    /** Traduce los errores más comunes a un mensaje que el usuario pueda resolver. */
    private String explicar(Exception e) {
        String detalle = String.valueOf(e.getMessage());
        Throwable causa = e;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        String tipo = causa.getClass().getSimpleName();
        if (e instanceof jakarta.mail.AuthenticationFailedException) {
            return "el servidor " + host + " rechazó el usuario o la contraseña";
        }
        if (tipo.contains("UnknownHost")) {
            return "no se encontró el servidor " + host;
        }
        if (tipo.contains("ConnectException") || tipo.contains("SocketTimeout")) {
            return "no se pudo conectar con " + host + ":" + puerto + " (¿la red bloquea el puerto?)";
        }
        if (detalle.contains("[EOF]") || tipo.contains("EOFException")) {
            return "el servidor " + host + ":" + puerto + " cortó la conexión sin responder. Revise en "
                    + "correo.properties que el puerto coincida con el cifrado (587 con starttls=true y ssl=false, "
                    + "o 465 con ssl=true y starttls=false); si coinciden, la red o el antivirus bloquean el envío";
        }
        return detalle;
    }
}
