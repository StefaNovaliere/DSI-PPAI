package ppai.entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;

/**
 * Remito de documentación entre Comisiones Médicas.
 */
public class Remito {

    private final LocalDate fecha;
    private final int numero;
    private final ComisionMedica origen;
    private final ComisionMedica destino;
    private Estado estado;
    private final List<DetalleRemito> detallesRemito = new ArrayList<>();

    public Remito(LocalDate fecha, int numero, ComisionMedica origen, ComisionMedica destino, Estado estado) {
        this.fecha = fecha;
        this.numero = numero;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado;
    }

    public void agregarDetalle(DetalleRemito detalle) {
        detallesRemito.add(detalle);
    }

    public int obtenerNumero() {
        return numero;
    }

    /** Número del remito y datos de cada documentación incluida. */
    public DatosRemito obtenerDatosRemito() {
        List<DatosDocumentacion> documentacion = new ArrayList<>();
        for (DetalleRemito detalle : detallesRemito) {
            documentacion.add(detalle.obtenerDocumentacion());
        }
        return new DatosRemito(obtenerNumero(), documentacion);
    }

    /**
     * CU 28: el remito pasa a RecibidoYAceptado y le pide a cada detalle que
     * actualice el estado de su documentación.
     */
    public void recibirYAceptar(Estado recibidoYAceptado, LocalDateTime fechaHora, Empleado responsable) {
        setEstado(recibidoYAceptado);
        for (DetalleRemito detalle : detallesRemito) {
            detalle.actualizarEstadoDoc(fechaHora, responsable);
        }
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Estado getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ComisionMedica getOrigen() {
        return origen;
    }

    public ComisionMedica getDestino() {
        return destino;
    }

    public List<DetalleRemito> getDetallesRemito() {
        return Collections.unmodifiableList(detallesRemito);
    }
}
