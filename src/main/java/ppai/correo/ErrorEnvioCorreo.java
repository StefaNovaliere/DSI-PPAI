package ppai.correo;

/**
 * No se pudo enviar el correo (servidor inaccesible, credenciales inválidas,
 * destinatario rechazado, etc.).
 */
public class ErrorEnvioCorreo extends Exception {

    public ErrorEnvioCorreo(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
