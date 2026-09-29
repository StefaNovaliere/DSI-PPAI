package ppai.entidades;

import java.time.LocalDateTime;

/**
 * Historial de estados del Bolsín.
 */
public class CambioEstadoBolsin {

    private final LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private final Estado estado;
    private final Empleado responsableCE;

    public CambioEstadoBolsin(LocalDateTime fechaHoraInicio, Estado estado, Empleado responsableCE) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.responsableCE = responsableCE;
    }

    /** Es el cambio de estado actual si todavía no tiene fecha de fin. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public boolean sosEnviado() {
        return estado.esEnviado();
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

    public Estado getEstado() {
        return estado;
    }

    public Empleado getResponsableCE() {
        return responsableCE;
    }
}
