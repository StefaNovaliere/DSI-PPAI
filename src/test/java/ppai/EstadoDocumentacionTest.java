package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.ComisionMedica;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.TipoDocumento;
import ppai.entidades.estadodocumentacion.DeBaja;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.EnBolsinSaliente;
import ppai.entidades.estadodocumentacion.EnRemito;
import ppai.entidades.estadodocumentacion.NoRecibida;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.entidades.estadodocumentacion.RecibidaYRechazada;
import ppai.entidades.estadodocumentacion.Registrada;

/** Pruebas de la máquina de estados de Documentación implementada con State. */
class EstadoDocumentacionTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 1, 8, 0);
    private Empleado empleado;
    private Documentacion documentacion;

    @BeforeEach
    void setUp() {
        ComisionMedica cm = new ComisionMedica(1, "CM Córdoba", "", "", "");
        empleado = new Empleado("Pérez", "Ana", "ana@cm.gob.ar", "aperez", cm);
        documentacion = new Documentacion(1, "Expediente", LocalDate.of(2026, 9, 1),
                new TipoDocumento("Expediente", ""), T0, empleado);
    }

    @Test
    void alCrearseQuedaRegistrada() {
        assertInstanceOf(Registrada.class, documentacion.getEstadoActual());
        assertEquals(1, documentacion.getCambiosEstado().size());
    }

    @Test
    void caminoCompletoHastaRecibidaYAceptada() {
        documentacion.remitar(T0.plusHours(1), empleado);
        assertInstanceOf(EnRemito.class, documentacion.getEstadoActual());
        documentacion.agregarAlBolsin(T0.plusHours(2), empleado);
        assertInstanceOf(EnBolsinSaliente.class, documentacion.getEstadoActual());
        documentacion.enviar(T0.plusHours(3), empleado);
        assertInstanceOf(EnBolsinEnviado.class, documentacion.getEstadoActual());
        documentacion.recibir(T0.plusHours(4), empleado);
        assertInstanceOf(RecibidaYAceptada.class, documentacion.getEstadoActual());
        assertTrue(documentacion.getEstadoActual().esFinal());

        // Historial: 5 cambios de estado y sólo el último es el actual
        List<CambioEstadoDocumentacion> historial = documentacion.getCambiosEstado();
        assertEquals(5, historial.size());
        assertEquals(1, historial.stream().filter(CambioEstadoDocumentacion::sosActual).count());
        assertTrue(historial.get(4).sosActual());
        assertEquals(T0.plusHours(4), historial.get(3).getFechaHoraFin());
    }

    @Test
    void cancelarRemitoVuelveARegistrada() {
        documentacion.remitar(T0.plusHours(1), empleado);
        documentacion.cancelarRemito(T0.plusHours(2), empleado);
        assertInstanceOf(Registrada.class, documentacion.getEstadoActual());
    }

    @Test
    void modificarBolsinVuelveAEnRemito() {
        documentacion.remitar(T0.plusHours(1), empleado);
        documentacion.agregarAlBolsin(T0.plusHours(2), empleado);
        documentacion.remitar(T0.plusHours(3), empleado);
        assertInstanceOf(EnRemito.class, documentacion.getEstadoActual());
    }

    @Test
    void paraRedirigirAlRecibirseQuedaRecibidaYAceptada() {
        CambioEstadoDocumentacion ce = new CambioEstadoDocumentacion(T0, new ParaRedirigir(), empleado);
        Documentacion redirigida = new Documentacion(2, "Estudio", LocalDate.of(2026, 9, 1),
                new TipoDocumento("Estudio", ""), List.of(ce));
        redirigida.recibir(T0.plusDays(1), empleado);
        assertInstanceOf(RecibidaYAceptada.class, redirigida.getEstadoActual());
        assertEquals(T0.plusDays(1), ce.getFechaHoraFin());
    }

    @Test
    void noRecibidaPuedeDarseDeBajaORegistrarse() {
        CambioEstadoDocumentacion ce = new CambioEstadoDocumentacion(T0, new NoRecibida(), empleado);
        Documentacion noRecibida = new Documentacion(3, "Doc", LocalDate.of(2026, 9, 1),
                new TipoDocumento("Doc", ""), List.of(ce));
        noRecibida.darDeBaja(T0.plusDays(1), empleado);
        assertInstanceOf(DeBaja.class, noRecibida.getEstadoActual());
    }

    @Test
    void transicionesInvalidasSonRechazadas() {
        // Registrada no puede recibirse ni enviarse
        assertThrows(IllegalStateException.class, () -> documentacion.recibir(T0, empleado));
        assertThrows(IllegalStateException.class, () -> documentacion.enviar(T0, empleado));
        assertInstanceOf(Registrada.class, documentacion.getEstadoActual());

        // Un estado final no admite más eventos
        documentacion.remitar(T0.plusHours(1), empleado);
        documentacion.agregarAlBolsin(T0.plusHours(2), empleado);
        documentacion.enviar(T0.plusHours(3), empleado);
        documentacion.recibir(T0.plusHours(4), empleado);
        assertThrows(IllegalStateException.class, () -> documentacion.recibir(T0.plusHours(5), empleado));
        assertThrows(IllegalStateException.class, () -> documentacion.darDeBaja(T0.plusHours(5), empleado));
    }

    private void llevarAEnBolsinEnviado() {
        documentacion.remitar(T0.plusHours(1), empleado);
        documentacion.agregarAlBolsin(T0.plusHours(2), empleado);
        documentacion.enviar(T0.plusHours(3), empleado);
    }

    @Test
    void revisionNoRecibidaVuelveARegistrarseOSeDaDeBaja() {
        llevarAEnBolsinEnviado();
        documentacion.registrarNoRecibida(T0.plusHours(4), empleado);
        assertInstanceOf(NoRecibida.class, documentacion.getEstadoActual());
        documentacion.registrar(T0.plusHours(5), empleado);
        assertInstanceOf(Registrada.class, documentacion.getEstadoActual());
    }

    @Test
    void revisionParaRedirigirLuegoRechazadaYDeBaja() {
        llevarAEnBolsinEnviado();
        documentacion.marcarParaRedirigir(T0.plusHours(4), empleado);
        assertInstanceOf(ParaRedirigir.class, documentacion.getEstadoActual());
        documentacion.rechazar(T0.plusHours(5), empleado);
        assertInstanceOf(RecibidaYRechazada.class, documentacion.getEstadoActual());
        documentacion.darDeBaja(T0.plusHours(6), empleado);
        assertInstanceOf(DeBaja.class, documentacion.getEstadoActual());
        assertTrue(documentacion.getEstadoActual().esFinal());
        assertEquals(7, documentacion.getCambiosEstado().size());
    }

    @Test
    void revisionRechazadaDesdeEnBolsinEnviado() {
        llevarAEnBolsinEnviado();
        documentacion.rechazar(T0.plusHours(4), empleado);
        assertInstanceOf(RecibidaYRechazada.class, documentacion.getEstadoActual());
    }

    @Test
    void eventosDeRevisionInvalidosSonRechazados() {
        // Una documentación Registrada no puede revisarse
        assertThrows(IllegalStateException.class, () -> documentacion.rechazar(T0, empleado));
        assertThrows(IllegalStateException.class, () -> documentacion.marcarParaRedirigir(T0, empleado));
        // ParaRedirigir no puede volver a marcarse ni registrarse como no recibida
        llevarAEnBolsinEnviado();
        documentacion.marcarParaRedirigir(T0.plusHours(4), empleado);
        assertThrows(IllegalStateException.class, () -> documentacion.marcarParaRedirigir(T0.plusHours(5), empleado));
        assertThrows(IllegalStateException.class, () -> documentacion.registrarNoRecibida(T0.plusHours(5), empleado));
    }
}
