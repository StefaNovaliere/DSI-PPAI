package ppai.correo;

/**
 * Interfaz que usa el sistema para enviar correos, independiente de cómo se
 * envían: por un servidor SMTP real ({@link ServicioCorreoSmtp}) o guardándolos
 * en archivos cuando no hay servidor configurado ({@link ServicioCorreoSimulado}).
 */
public interface ServicioCorreo {

    /**
     * Envía el correo.
     *
     * @return descripción de lo que se hizo, para informarle al usuario
     */
    String enviar(Correo correo) throws ErrorEnvioCorreo;

    /** Descripción breve del modo de envío (por ejemplo, el servidor SMTP). */
    String descripcion();
}
