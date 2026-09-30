package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.ComisionMedica;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.TipoDocumento;
import ppai.entidades.Usuario;
import ppai.entidades.estadodocumentacion.DeBaja;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.EnBolsinSaliente;
import ppai.entidades.estadodocumentacion.EnRemito;
import ppai.entidades.estadodocumentacion.EstadoDocumentacion;
import ppai.entidades.estadodocumentacion.NoRecibida;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.entidades.estadodocumentacion.RecibidaYRechazada;
import ppai.entidades.estadodocumentacion.Registrada;

/**
 * Pruebas del patrón State en el CU 28: el mismo evento recibir() se resuelve
 * distinto según el objeto estado que tenga la documentación.
 */
class EstadoDocumentacionTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 1, 8, 0);
    private static final LocalDateTime RECEPCION = T0.plusDays(3);
    private final Empleado responsable = new Empleado("Pérez", "Ana", "ana@cm.gob.ar", new Usuario("aperez"),
            new ComisionMedica(1, "CM Córdoba", "", "", ""));

    /** Documentación cuyo historial termina en el estado indicado (el actual). */
    private Documentacion documentacionEn(EstadoDocumentacion estadoActual) {
        List<CambioEstadoDocumentacion> historial = new ArrayList<>();
        CambioEstadoDocumentacion anterior = new CambioEstadoDocumentacion(T0, new Registrada(), responsable);
        anterior.setFechaHoraFin(T0.plusDays(1));
        historial.add(anterior);
        historial.add(new CambioEstadoDocumentacion(T0.plusDays(1), estadoActual, responsable));
        return new Documentacion(1, "Expediente", LocalDate.of(2026, 9, 1), new TipoDocumento("Expediente", ""),
                historial);
    }

    @Test
    void enBolsinEnviadoAlRecibirseQuedaRecibidaYAceptada() {
        Documentacion doc = documentacionEn(new EnBolsinEnviado());
        doc.recibir(RECEPCION, responsable);
        assertInstanceOf(RecibidaYAceptada.class, doc.getEstadoActual());
    }

    @Test
    void paraRedirigirAlRecibirseQuedaRecibidaYAceptada() {
        Documentacion doc = documentacionEn(new ParaRedirigir());
        doc.recibir(RECEPCION, responsable);
        assertInstanceOf(RecibidaYAceptada.class, doc.getEstadoActual());
    }

    @Test
    void alRecibirseSeCierraElCambioDeEstadoActualYSeCreaUnoNuevo() {
        Documentacion doc = documentacionEn(new EnBolsinEnviado());
        CambioEstadoDocumentacion enviado = doc.getCambiosEstado().get(1);

        doc.recibir(RECEPCION, responsable);

        List<CambioEstadoDocumentacion> historial = doc.getCambiosEstado();
        assertEquals(3, historial.size());
        assertEquals(RECEPCION, enviado.getFechaHoraFin());
        CambioEstadoDocumentacion nuevo = historial.get(2);
        assertTrue(nuevo.sosActual());
        assertInstanceOf(RecibidaYAceptada.class, nuevo.getEstado());
        assertEquals(RECEPCION, nuevo.getFechaHoraInicio());
        assertSame(responsable, nuevo.getResponsableCE());
        assertEquals(1, historial.stream().filter(CambioEstadoDocumentacion::sosActual).count());
    }

    static Stream<Arguments> estadosQueNoPuedenRecibirse() {
        return Stream.<Supplier<EstadoDocumentacion>>of(Registrada::new, EnRemito::new, EnBolsinSaliente::new,
                        NoRecibida::new, RecibidaYAceptada::new, RecibidaYRechazada::new, DeBaja::new)
                .map(estado -> Arguments.of(estado.get()));
    }

    @ParameterizedTest(name = "{0} no puede recibirse")
    @MethodSource("estadosQueNoPuedenRecibirse")
    void losDemasEstadosRechazanLaRecepcion(EstadoDocumentacion estado) {
        Documentacion doc = documentacionEn(estado);
        assertThrows(IllegalStateException.class, () -> doc.recibir(RECEPCION, responsable));
        // El estado y el historial no cambian
        assertSame(estado, doc.getEstadoActual());
        assertEquals(2, doc.getCambiosEstado().size());
    }
}
