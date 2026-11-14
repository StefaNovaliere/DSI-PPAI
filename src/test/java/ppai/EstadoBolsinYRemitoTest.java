package ppai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.ComisionMedica;
import ppai.entidades.Empleado;
import ppai.entidades.Remito;
import ppai.entidades.Usuario;
import ppai.entidades.estadobolsin.Cerrado;
import ppai.entidades.estadobolsin.Creado;
import ppai.entidades.estadobolsin.DeBaja;
import ppai.entidades.estadobolsin.Enviado;
import ppai.entidades.estadobolsin.EstadoBolsin;
import ppai.entidades.estadobolsin.RecibidoEnCMDestino;
import ppai.entidades.estadoremito.Cancelado;
import ppai.entidades.estadoremito.EnBolsinEnviado;
import ppai.entidades.estadoremito.EnBolsinSaliente;
import ppai.entidades.estadoremito.EstadoRemito;
import ppai.entidades.estadoremito.RecibidoYAceptado;
import ppai.entidades.estadoremito.RecibidoYAceptadoParcial;

/** Pruebas del patrón State en Bolsín y Remito (máquinas de estado armadas con la ERS). */
class EstadoBolsinYRemitoTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 1, 8, 0);
    private static final LocalDateTime RECEPCION = T0.plusDays(3);
    private final ComisionMedica cmc = new ComisionMedica(0, "Comisión Médica Central", "", "", "", true);
    private final ComisionMedica rosario = new ComisionMedica(2, "CM Rosario", "", "", "", false);
    private final Empleado responsable = new Empleado("Pérez", "Ana", "ana@cmc.example", new Usuario("aperez"), cmc);

    /** Bolsín cuyo historial termina en el estado indicado (el actual). */
    private Bolsin bolsinEn(EstadoBolsin estadoActual) {
        Bolsin bolsin = new Bolsin(LocalDate.of(2026, 9, 1), 101, "PR-0101", 1.0, rosario, cmc);
        CambioEstadoBolsin creado = new CambioEstadoBolsin(T0, new Creado(), responsable);
        creado.setFechaHoraFin(T0.plusDays(1));
        bolsin.agregarCambioEstado(creado);
        bolsin.agregarCambioEstado(new CambioEstadoBolsin(T0.plusDays(1), estadoActual, responsable));
        return bolsin;
    }

    private Remito remitoEn(EstadoRemito estado) {
        return new Remito(LocalDate.of(2026, 9, 1), 5001, rosario, cmc, estado);
    }

    @Test
    void bolsinEnviadoAlRecibirseQuedaRecibidoEnCMDestino() {
        Bolsin bolsin = bolsinEn(new Enviado());
        assertTrue(bolsin.sosEnviado());

        bolsin.recibirBolsin(RECEPCION, responsable);

        assertInstanceOf(RecibidoEnCMDestino.class, bolsin.getEstadoActual());
        assertFalse(bolsin.sosEnviado());
        List<CambioEstadoBolsin> historial = bolsin.getCambiosEstado();
        assertEquals(3, historial.size());
        assertEquals(RECEPCION, historial.get(1).getFechaHoraFin());
        CambioEstadoBolsin nuevo = historial.get(2);
        assertTrue(nuevo.sosActual());
        assertInstanceOf(RecibidoEnCMDestino.class, nuevo.getEstado());
        assertSame(responsable, nuevo.getResponsableCE());
    }

    static Stream<Arguments> estadosDeBolsinQueNoPuedenRecibirse() {
        return Stream.<Supplier<EstadoBolsin>>of(Creado::new, Cerrado::new, RecibidoEnCMDestino::new, DeBaja::new)
                .map(estado -> Arguments.of(estado.get()));
    }

    @ParameterizedTest(name = "bolsín {0} no puede recibirse")
    @MethodSource("estadosDeBolsinQueNoPuedenRecibirse")
    void losDemasEstadosDelBolsinRechazanLaRecepcion(EstadoBolsin estado) {
        Bolsin bolsin = bolsinEn(estado);
        assertFalse(bolsin.sosEnviado());
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> bolsin.recibirBolsin(RECEPCION, responsable));
        assertTrue(error.getMessage().contains("bolsín N° 101"));
        assertSame(estado, bolsin.getEstadoActual());
        assertEquals(2, bolsin.getCambiosEstado().size());
    }

    @Test
    void remitoEnBolsinEnviadoSeRecibeYAceptaCompletoOParcial() {
        Remito completo = remitoEn(new EnBolsinEnviado());
        completo.getEstado().recibirYAceptar(completo);
        assertInstanceOf(RecibidoYAceptado.class, completo.getEstado());

        Remito parcial = remitoEn(new EnBolsinEnviado());
        parcial.getEstado().recibirYAceptarParcial(parcial);
        assertInstanceOf(RecibidoYAceptadoParcial.class, parcial.getEstado());
    }

    static Stream<Arguments> estadosDeRemitoQueNoPuedenRecibirse() {
        return Stream.<Supplier<EstadoRemito>>of(ppai.entidades.estadoremito.Creado::new, Cancelado::new,
                        EnBolsinSaliente::new, RecibidoYAceptado::new, RecibidoYAceptadoParcial::new)
                .map(estado -> Arguments.of(estado.get()));
    }

    @ParameterizedTest(name = "remito {0} no puede recibirse")
    @MethodSource("estadosDeRemitoQueNoPuedenRecibirse")
    void losDemasEstadosDelRemitoRechazanLaRecepcion(EstadoRemito estado) {
        Remito remito = remitoEn(estado);
        assertThrows(IllegalStateException.class, () -> estado.recibirYAceptar(remito));
        assertThrows(IllegalStateException.class, () -> estado.recibirYAceptarParcial(remito));
        assertSame(estado, remito.getEstado());
    }

    @Test
    void laCMOrigenSeEncuentraSinDistinguirMayusculasNiTildes() {
        Bolsin bolsin = bolsinEn(new Enviado());
        assertTrue(bolsin.sosDeCMOrigen("rosario"));
        assertTrue(bolsin.tenesPrecinto(" pr-0101 "));
        assertFalse(bolsin.tenesPrecinto("PR-01"));
        assertTrue(new ComisionMedica(1, "CM Córdoba", "", "", "", false).coincideNombre("cordoba"));
        assertTrue(bolsin.esDestinoCMC());
    }
}
