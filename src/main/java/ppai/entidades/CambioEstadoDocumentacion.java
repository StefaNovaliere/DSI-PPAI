package ppai.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import ppai.persistencia.ConversorEstadoDocumentacion;


/**
 * Historial de estados de la Documentación. Referencia al estado concreto
 * (patrón State) que tuvo la documentación en ese período.
 */
@Entity
@Table(name = "cambio_estado_documentacion")
public class CambioEstadoDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    /** Se guarda el nombre del estado; lo convierte ConversorEstadoDocumentacion. */
    @Column(name = "estado", nullable = false)
    @Convert(converter = ConversorEstadoDocumentacion.class)
    private Estado estado;
    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Empleado responsableCE;

    protected CambioEstadoDocumentacion() {
        // Requerido por JPA
    }

    public CambioEstadoDocumentacion(LocalDateTime fechaHoraInicio, Estado estado,
                                     Empleado responsableCE) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.responsableCE = responsableCE;
    }

    /** Es el cambio de estado actual si todavía no tiene fecha de fin. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public void setFechaHoraFin(LocalDateTime fechaHora) {
        this.fechaHoraFin = fechaHora;
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
