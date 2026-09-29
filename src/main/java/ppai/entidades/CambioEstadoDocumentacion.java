package ppai.entidades;

import java.time.LocalDateTime;

import ppai.entidades.estadodocumentacion.EstadoDocumentacion;

/**
 * Historial de estados de la Documentación. Referencia al estado concreto
 * (patrón State) que tuvo la documentación en ese período.
 */
public class CambioEstadoDocumentacion {

    private final LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private final EstadoDocumentacion estado;
    private final Empleado responsableCE;

    public CambioEstadoDocumentacion(LocalDateTime fechaHoraInicio, EstadoDocumentacion estado,
                                     Empleado responsableCE) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.responsableCE = responsableCE;
    }

    /** Es el cambio de estado actual si todavía no tiene fecha de fin. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public EstadoDocumentacion getEstado() {
        return estado;
    }

    public Empleado getResponsableCE() {
        return responsableCE;
    }
}
