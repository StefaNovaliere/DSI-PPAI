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
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.ComisionMedica;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.OpcionRecepcion;
import ppai.entidades.Remito;
import ppai.entidades.TipoDocumento;
import ppai.entidades.Usuario;
import ppai.entidades.estadobolsin.Cerrado;
import ppai.entidades.estadobolsin.Creado;
import ppai.entidades.estadobolsin.DeBaja;
import ppai.entidades.estadobolsin.Enviado;
import ppai.entidades.estadobolsin.RecibidoEnCMDestino;
import ppai.entidades.estadodocumentacion.NoRecibida;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.entidades.estadodocumentacion.Registrada;
import ppai.entidades.estadoremito.Cancelado;
import ppai.entidades.estadoremito.EnBolsinEnviado;
import ppai.entidades.estadoremito.EnBolsinSaliente;
import ppai.entidades.estadoremito.RecibidoYAceptadoParcial;
import ppai.entidades.estadoremito.RemitoRecibidoYAceptado;

/**
 * Pruebas del patrón State en Bolsín y Remito (máquinas de estado armadas con la
 * ERS). El estado del remito, además, actualiza la documentación de sus detalles.
 */
class EstadoBolsinYRemitoTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 1, 8, 0);
    private static final LocalDateTime RECEPCION = T0.plusDays(3);
    private final ComisionMedica cmc = new ComisionMedica(0, "Comisión Médica Central", "", "", "", true);
    private final ComisionMedica rosario = new ComisionMedica(2, "CM Rosario", "", "", "", false);
    private final Empleado responsable = new Empleado("Pérez", "Ana", "ana@cmc.example", new Usuario("aperez"), cmc);

    /** Bolsín cuyo historial termina en el estado indicado (el actual). */
    private Bolsin bolsinEn(Estado estadoActual) {
        Bolsin bolsin = new Bolsin(LocalDate.of(2026, 9, 1), 101, "PR-0101", 1.0, rosario, cmc);
        CambioEstadoBolsin creado = new CambioEstadoBolsin(T0, new Creado(), responsable);
        creado.setFechaHoraFin(T0.plusDays(1));
        bolsin.agregarCambioEstado(creado);
        bolsin.agregarCambioEstado(new CambioEstadoBolsin(T0.plusDays(1), estadoActual, responsable));
        bolsin.setEstado(estadoActual);
        return bolsin;
    }

    /** Remito con dos documentos (1 y 2) en estado EnBolsinEnviado. */
    private Remito remitoEn(Estado estado) {
        Remito remito = new Remito(LocalDate.of(2026, 9, 1), 5001, rosario, cmc, estado);
        remito.agregarDetalle(new DetalleRemito("Área Jurídica", documentacionEnviada(1)));
        remito.agregarDetalle(new DetalleRemito("Área de Dictámenes", documentacionEnviada(2)));
        return remito;
    }

    private Documentacion documentacionEnviada(int numero) {
        CambioEstadoDocumentacion registrada = new CambioEstadoDocumentacion(T0, new Registrada(), responsable);
        registrada.setFechaHoraFin(T0.plusDays(1));
        CambioEstadoDocumentacion enviada = new CambioEstadoDocumentacion(T0.plusDays(1),
                new ppai.entidades.estadodocumentacion.EnBolsinEnviado(), responsable);
        return new Documentacion(numero, "Expediente " + numero, LocalDate.of(2026, 9, 1),
                new TipoDocumento("Expediente", ""), List.of(registrada, enviada));
    }

    private static Estado estadoDoc(Remito remito, int indice) {
        return remito.getDetallesRemito().get(indice).getDocumentacion().getEstadoActual();
    }

    @Test
    void bolsinEnviadoAlRecibirseQuedaRecibidoEnCMDestino() {
        Bolsin bolsin = bolsinEn(new Enviado());
        assertTrue(bolsin.sosEnviado());

        bolsin.recibir(RECEPCION, responsable);

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
        return Stream.<Supplier<Estado>>of(Creado::new, Cerrado::new, RecibidoEnCMDestino::new, DeBaja::new)
                .map(estado -> Arguments.of(estado.get()));
    }

    @ParameterizedTest(name = "bolsín {0} no puede recibirse")
    @MethodSource("estadosDeBolsinQueNoPuedenRecibirse")
    void losDemasEstadosDelBolsinRechazanLaRecepcion(Estado estado) {
        Bolsin bolsin = bolsinEn(estado);
        assertFalse(bolsin.sosEnviado());
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> bolsin.recibir(RECEPCION, responsable));
        assertTrue(error.getMessage().contains("bolsín N° 101"));
        assertSame(estado, bolsin.getEstadoActual());
        assertEquals(2, bolsin.getCambiosEstado().size());
    }

    @Test
    void remitoConTodoIgualQuedaRecibidoYAceptadoYSuDocumentacionTambien() {
        Remito remito = remitoEn(new EnBolsinEnviado());
        remito.recibirYAceptar(RECEPCION, responsable, OpcionRecepcion.CONTENIDO_IGUAL, List.of());
        assertInstanceOf(RemitoRecibidoYAceptado.class, remito.getEstado());
        // El estado del remito actualiza la documentación de cada detalle
        assertInstanceOf(RecibidaYAceptada.class, estadoDoc(remito, 0));
        assertInstanceOf(RecibidaYAceptada.class, estadoDoc(remito, 1));
    }

    @Test
    void remitoConDocumentacionQueNoLlegoQuedaParcial() {
        Remito remito = remitoEn(new EnBolsinEnviado());
        remito.recibirYAceptar(RECEPCION, responsable, OpcionRecepcion.NO_SE_RECIBE_TODA, List.of(2));
        assertInstanceOf(RecibidoYAceptadoParcial.class, remito.getEstado());
        assertInstanceOf(RecibidaYAceptada.class, estadoDoc(remito, 0));
        assertInstanceOf(NoRecibida.class, estadoDoc(remito, 1));
    }

    @Test
    void remitoConDocumentacionParaRedirigirQuedaRecibidoYAceptado() {
        Remito remito = remitoEn(new EnBolsinEnviado());
        remito.recibirYAceptar(RECEPCION, responsable, OpcionRecepcion.REDIRIGIR, List.of(1));
        assertInstanceOf(RemitoRecibidoYAceptado.class, remito.getEstado());
        assertInstanceOf(ParaRedirigir.class, estadoDoc(remito, 0));
    }

    @Test
    void siLaDocumentacionIndicadaEsDeOtroRemitoEsteQuedaRecibidoYAceptado() {
        Remito remito = remitoEn(new EnBolsinEnviado());
        remito.recibirYAceptar(RECEPCION, responsable, OpcionRecepcion.NO_SE_RECIBE_TODA, List.of(99));
        assertInstanceOf(RemitoRecibidoYAceptado.class, remito.getEstado());
    }

    static Stream<Arguments> estadosDeRemitoQueNoPuedenRecibirse() {
        return Stream.<Supplier<Estado>>of(ppai.entidades.estadoremito.CreadoRemito::new, Cancelado::new,
                        EnBolsinSaliente::new, RemitoRecibidoYAceptado::new, RecibidoYAceptadoParcial::new)
                .map(estado -> Arguments.of(estado.get()));
    }

    @ParameterizedTest(name = "remito {0} no puede recibirse")
    @MethodSource("estadosDeRemitoQueNoPuedenRecibirse")
    void losDemasEstadosDelRemitoRechazanLaRecepcion(Estado estado) {
        Remito remito = remitoEn(estado);
        assertThrows(IllegalStateException.class,
                () -> remito.recibirYAceptar(RECEPCION, responsable, OpcionRecepcion.CONTENIDO_IGUAL, List.of()));
        assertSame(estado, remito.getEstado());
        // La documentación no cambia
        assertInstanceOf(ppai.entidades.estadodocumentacion.EnBolsinEnviado.class, estadoDoc(remito, 0));
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

    @Test
    void cadaEstadoSabeDeQueAmbitoEsYElContextoSoloAceptaLosSuyos() {
        assertTrue(new Enviado().esAmbitoBolsin());
        assertTrue(new EnBolsinEnviado().esAmbitoRemito());
        assertTrue(new ParaRedirigir().esAmbitoDocumentacion());
        assertFalse(new Enviado().esAmbitoRemito());

        Bolsin bolsin = bolsinEn(new Enviado());
        assertThrows(IllegalArgumentException.class, () -> bolsin.setEstado(new Cancelado()));
        assertInstanceOf(Enviado.class, bolsin.getEstadoActual());
    }

    @Test
    void losEventosDeOtrosCasosDeUsoSeRechazanPorqueNingunEstadoLosRedefine() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> new Creado().cerrar());
        assertEquals("No se puede cerrar en el estado «Creado»", error.getMessage());
        assertThrows(IllegalStateException.class, () -> new Registrada().remitar());
    }
}
