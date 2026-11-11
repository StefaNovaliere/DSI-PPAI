package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import ppai.boundary.PantallaConsolaRecepcionBolsin;
import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.correo.ConfiguracionCorreo;
import ppai.correo.ServicioCorreo;
import ppai.correo.ServicioCorreoSimulado;
import ppai.correo.ServicioCorreoSmtp;
import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRecepcion;
import ppai.persistencia.RepositorioEnMemoria;

/**
 * Pruebas del envío de correo del CU 29 contra un servidor SMTP de prueba
 * (GreenMail), que corre en memoria durante la prueba.
 */
class NotificacionCorreoTest {

    @RegisterExtension
    static GreenMailExtension servidorSmtp = new GreenMailExtension(ServerSetupTest.SMTP);

    @TempDir
    Path carpeta;

    private final List<String> avisos = new ArrayList<>();

    private ServicioCorreoSmtp smtpDePrueba(String redirigirA) {
        return new ServicioCorreoSmtp("localhost", ServerSetupTest.SMTP.getPort(), false, false, "", "",
                "bolsines@grupo10.example", redirigirA);
    }

    private DatosRecepcion recepcion() {
        return new DatosRecepcion(101, "PR-0101", "CM Rosario", "CM Córdoba", LocalDateTime.of(2026, 11, 11, 10, 30),
                "Pérez, Ana", List.of(
                        new DatosDocumentacion(1001, "Expediente de incapacidad laboral", "Expediente",
                                "Recibida&Aceptada", "Recibida y aceptada"),
                        new DatosDocumentacion(1003, "Estudio mal derivado a Mendoza", "Estudio Complementario",
                                "Recibida&Aceptada", "Recibida y aceptada")));
    }

    private static String textoPlano(MimeMessage mensaje) throws Exception {
        MimeMultipart partes = (MimeMultipart) mensaje.getContent();
        for (int i = 0; i < partes.getCount(); i++) {
            BodyPart parte = partes.getBodyPart(i);
            if (parte.isMimeType("text/plain")) {
                return (String) parte.getContent();
            }
        }
        throw new AssertionError("El correo no tiene parte de texto plano");
    }

    @Test
    void seEnviaElCorreoConLosDatosDeLaRecepcion() throws Exception {
        GestorNotificacionCU29 gestor = new GestorNotificacionCU29(smtpDePrueba(""), Runnable::run, avisos::add);

        gestor.notificarRecepcion("ana.perez@cmcordoba.example", recepcion());

        MimeMessage[] recibidos = servidorSmtp.getReceivedMessages();
        assertEquals(1, recibidos.length);
        MimeMessage correo = recibidos[0];
        assertEquals("ana.perez@cmcordoba.example", correo.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Recepción registrada del bolsín N° 101 (precinto PR-0101)", correo.getSubject());
        String texto = textoPlano(correo);
        assertTrue(texto.contains("CM Rosario a CM Córdoba"));
        assertTrue(texto.contains("Registró: Pérez, Ana"));
        assertTrue(texto.contains("N° 1003 · Estudio Complementario · Estudio mal derivado a Mendoza · Recibida y aceptada"));
        assertTrue(avisos.get(0).startsWith(GestorNotificacionCU29.PREFIJO_ENVIANDO));
        assertTrue(avisos.get(1).contains("se envió a ana.perez@cmcordoba.example"));
    }

    @Test
    void conRedireccionSeEnviaALaDireccionDeLaDemo() throws Exception {
        GestorNotificacionCU29 gestor = new GestorNotificacionCU29(smtpDePrueba("demo@grupo10.example"), Runnable::run,
                avisos::add);

        gestor.notificarRecepcion("ana.perez@cmcordoba.example", recepcion());

        MimeMessage correo = servidorSmtp.getReceivedMessages()[0];
        assertEquals("demo@grupo10.example", correo.getRecipients(Message.RecipientType.TO)[0].toString());
        assertTrue(textoPlano(correo).contains("Destinatario original: ana.perez@cmcordoba.example"));
    }

    @Test
    void siElServidorNoRespondeSeInformaElErrorSinCortarElCasoDeUso() {
        ServicioCorreo servidorCaido = new ServicioCorreoSmtp("localhost", 1, false, false, "", "",
                "bolsines@grupo10.example", "");
        GestorNotificacionCU29 gestor = new GestorNotificacionCU29(servidorCaido, Runnable::run, avisos::add);

        gestor.notificarRecepcion("ana.perez@cmcordoba.example", recepcion());

        assertTrue(avisos.get(1).startsWith(GestorNotificacionCU29.PREFIJO_ERROR));
        assertTrue(avisos.get(1).contains("La recepción quedó registrada igual"));
    }

    @Test
    void sinServidorConfiguradoElCorreoSeGuardaEnUnArchivo() throws Exception {
        GestorNotificacionCU29 gestor = new GestorNotificacionCU29(
                new ServicioCorreoSimulado(carpeta, "no hay servidor"), Runnable::run, avisos::add);

        gestor.notificarRecepcion("ana.perez@cmcordoba.example", recepcion());

        try (var archivos = Files.list(carpeta)) {
            Path archivo = archivos.findFirst().orElseThrow();
            String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
            assertTrue(contenido.contains("Para: ana.perez@cmcordoba.example"));
            assertTrue(contenido.contains("Estudio mal derivado a Mendoza"));
        }
        assertTrue(avisos.get(1).contains("no se envió porque no hay servidor"));
    }

    @Test
    void laConfiguracionSeLeeDeCorreoProperties() throws Exception {
        assertInstanceOf(ServicioCorreoSimulado.class, ConfiguracionCorreo.cargar(carpeta.resolve("no-existe.properties")));

        Path incompleto = carpeta.resolve("incompleto.properties");
        Files.writeString(incompleto, "correo.smtp.host=smtp.gmail.com\n");
        assertInstanceOf(ServicioCorreoSimulado.class, ConfiguracionCorreo.cargar(incompleto));

        Path completo = carpeta.resolve("correo.properties");
        Files.writeString(completo, """
                correo.smtp.host=smtp.gmail.com
                correo.smtp.puerto=587
                correo.smtp.starttls=true
                correo.usuario=grupo10@gmail.com
                correo.contrasenia=abcd efgh ijkl mnop
                correo.remitente=grupo10@gmail.com
                """);
        ServicioCorreo servicio = ConfiguracionCorreo.cargar(completo);
        assertInstanceOf(ServicioCorreoSmtp.class, servicio);
        assertTrue(servicio.descripcion().contains("smtp.gmail.com:587"));
    }

    @Test
    void alRegistrarLaRecepcionSeEnviaElCorreoAlEmpleadoLogueado() throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(salida, true, StandardCharsets.UTF_8);
        PantallaConsolaRecepcionBolsin pantalla = new PantallaConsolaRecepcionBolsin(new Scanner("101\n1\nS\n"), out);
        GestorRecepcionBolsin gestor = new GestorRecepcionBolsin(pantalla, new RepositorioEnMemoria(),
                new GestorNotificacionCU29(smtpDePrueba(""), Runnable::run, out::println));
        pantalla.setGestor(gestor);

        pantalla.opcRegistrarRecBolsin();

        MimeMessage[] recibidos = servidorSmtp.getReceivedMessages();
        assertEquals(1, recibidos.length);
        assertEquals("ana.perez@cmcordoba.example", recibidos[0].getRecipients(Message.RecipientType.TO)[0].toString());
        String texto = textoPlano(recibidos[0]);
        assertTrue(texto.contains("bolsín N° 101 (precinto PR-0101), enviado desde CM Rosario a CM Córdoba"));
        assertTrue(texto.contains("N° 1001"));
        assertTrue(texto.contains("N° 1002"));
        assertTrue(texto.contains("N° 1003"));
        assertTrue(salida.toString(StandardCharsets.UTF_8).contains("se envió a ana.perez@cmcordoba.example"));
    }
}
