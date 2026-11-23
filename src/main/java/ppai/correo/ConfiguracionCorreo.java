package ppai.correo;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lee la configuración del servidor de correo desde {@code correo.properties}
 * (en la carpeta desde donde se ejecuta el programa) y crea el servicio de
 * correo que corresponde. Si el archivo no existe o está incompleto, los
 * correos se guardan como archivos en lugar de enviarse.
 */
public final class ConfiguracionCorreo {

    public static final String ARCHIVO_POR_DEFECTO = "correo.properties";
    public static final Path CARPETA_SIMULADOS = Path.of("correos-simulados");

    private ConfiguracionCorreo() {
    }

    public static ServicioCorreo cargar(Path archivo) {
        if (!Files.exists(archivo)) {
            return new ServicioCorreoSimulado(CARPETA_SIMULADOS, "no hay un servidor de correo configurado (falta "
                    + archivo.getFileName() + ")");
        }
        Properties propiedades = new Properties();
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            propiedades.load(lector);
        } catch (IOException e) {
            return new ServicioCorreoSimulado(CARPETA_SIMULADOS, "no se pudo leer " + archivo.getFileName());
        }
        String host = valor(propiedades, "correo.smtp.host");
        String puerto = valor(propiedades, "correo.smtp.puerto");
        String remitente = valor(propiedades, "correo.remitente");
        if (host.isEmpty() || puerto.isEmpty() || remitente.isEmpty()) {
            return new ServicioCorreoSimulado(CARPETA_SIMULADOS, archivo.getFileName()
                    + " está incompleto: faltan correo.smtp.host, correo.smtp.puerto o correo.remitente");
        }
        int numeroPuerto;
        try {
            numeroPuerto = Integer.parseInt(puerto);
        } catch (NumberFormatException e) {
            return new ServicioCorreoSimulado(CARPETA_SIMULADOS, "correo.smtp.puerto no es un número");
        }
        String contrasenia = valor(propiedades, "correo.contrasenia");
        if (host.equalsIgnoreCase("smtp.gmail.com")) {
            // Gmail muestra la contraseña de aplicación con espacios, pero se usa sin ellos
            contrasenia = contrasenia.replace(" ", "");
        }
        return new ServicioCorreoSmtp(host, numeroPuerto,
                Boolean.parseBoolean(valor(propiedades, "correo.smtp.starttls")),
                Boolean.parseBoolean(valor(propiedades, "correo.smtp.ssl")),
                valor(propiedades, "correo.usuario"),
                contrasenia,
                remitente,
                valor(propiedades, "correo.redirigirA"));
    }

    private static String valor(Properties propiedades, String clave) {
        return propiedades.getProperty(clave, "").trim();
    }
}
