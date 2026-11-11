package ppai.correo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Se usa cuando no hay un servidor de correo configurado: en lugar de enviar
 * el correo, lo guarda como archivo HTML para poder abrirlo y ver su contenido.
 */
public class ServicioCorreoSimulado implements ServicioCorreo {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final Path carpeta;
    private final String motivo;

    /**
     * @param carpeta dónde se guardan los correos
     * @param motivo  por qué no se envían de verdad (se muestra al usuario)
     */
    public ServicioCorreoSimulado(Path carpeta, String motivo) {
        this.carpeta = carpeta;
        this.motivo = motivo;
    }

    @Override
    public String enviar(Correo correo) throws ErrorEnvioCorreo {
        try {
            Files.createDirectories(carpeta);
            Path archivo = carpeta.resolve("correo-" + LocalDateTime.now().format(FORMATO_ARCHIVO) + ".html");
            String contenido = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>"
                    + Html.escapar(correo.asunto()) + "</title></head><body style=\"font-family:sans-serif\">"
                    + "<p style=\"color:#666\">Para: " + Html.escapar(correo.destinatario()) + "<br>Asunto: "
                    + Html.escapar(correo.asunto()) + "</p><hr>" + correo.cuerpoHtml() + "</body></html>";
            Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
            return "no se envió porque " + motivo + "; se guardó en " + archivo;
        } catch (IOException e) {
            throw new ErrorEnvioCorreo("no se pudo guardar el correo en " + carpeta.toAbsolutePath(), e);
        }
    }

    @Override
    public String descripcion() {
        return "no se envían porque " + motivo + "; se guardan en la carpeta " + carpeta;
    }
}
