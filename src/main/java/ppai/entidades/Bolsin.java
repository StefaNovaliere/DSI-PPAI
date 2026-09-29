package ppai.entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;

/**
 * Bolsín: envío físico precintado de remitos entre Comisiones Médicas.
 */
public class Bolsin {

    private final LocalDate fecha;
    private final int numeroBolsin;
    private final String numeroPrecinto;
    private final double peso;
    private final ComisionMedica origen;
    private final ComisionMedica destino;
    private final List<Remito> remitos = new ArrayList<>();
    private final List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();

    public Bolsin(LocalDate fecha, int numeroBolsin, String numeroPrecinto, double peso,
                  ComisionMedica origen, ComisionMedica destino) {
        this.fecha = fecha;
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.peso = peso;
        this.origen = origen;
        this.destino = destino;
    }

    public void asociarRemito(Remito remito) {
        remitos.add(remito);
    }

    public void agregarCambioEstado(CambioEstadoBolsin cambioEstado) {
        cambiosEstado.add(cambioEstado);
    }

    /** Indica si el estado actual del bolsín es Enviado. */
    public boolean sosEnviado() {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        return actual != null && actual.sosEnviado();
    }

    public ComisionMedica obtenerCMDestino() {
        return destino;
    }

    public String obtenerCMOrigen() {
        return origen.getNombre();
    }

    public int getNumeroBolsin() {
        return numeroBolsin;
    }

    public String getNumeroPrecinto() {
        return numeroPrecinto;
    }

    /** Número y documentación de cada remito del bolsín. */
    public List<DatosRemito> obtenerInformacionRemito() {
        List<DatosRemito> informacion = new ArrayList<>();
        for (Remito remito : remitos) {
            informacion.add(remito.obtenerDatosRemito());
        }
        return informacion;
    }

    /** CU 28: el bolsín pasa a RecibidoEnCMDestino. */
    public void recibirBolsin(LocalDateTime fechaHora, Estado recibidoEnCMDestino, Empleado responsable) {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHora);
        }
        crearCEBolsin(fechaHora, recibidoEnCMDestino, responsable);
    }

    private void crearCEBolsin(LocalDateTime fechaHora, Estado estado, Empleado responsable) {
        agregarCambioEstado(new CambioEstadoBolsin(fechaHora, estado, responsable));
    }

    /** CU 28: recibe y acepta cada remito del bolsín. */
    public void recibirYAceptarRemito(Estado recibidoYAceptado, LocalDateTime fechaHora, Empleado responsable) {
        for (Remito remito : remitos) {
            remito.recibirYAceptar(recibidoYAceptado, fechaHora, responsable);
        }
    }

    /** Datos (con el estado vigente) de toda la documentación del bolsín. */
    public List<DatosDocumentacion> obtenerInformacionDocumentacion() {
        List<DatosDocumentacion> informacion = new ArrayList<>();
        for (DatosRemito datosRemito : obtenerInformacionRemito()) {
            informacion.addAll(datosRemito.documentacion());
        }
        return informacion;
    }

    public Estado getEstadoActual() {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        return actual == null ? null : actual.getEstado();
    }

    private CambioEstadoBolsin buscarCambioEstadoActual() {
        for (CambioEstadoBolsin cambioEstado : cambiosEstado) {
            if (cambioEstado.sosActual()) {
                return cambioEstado;
            }
        }
        return null;
    }

    public List<Remito> getRemitos() {
        return Collections.unmodifiableList(remitos);
    }

    public List<CambioEstadoBolsin> getCambiosEstado() {
        return Collections.unmodifiableList(cambiosEstado);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getPeso() {
        return peso;
    }
}
