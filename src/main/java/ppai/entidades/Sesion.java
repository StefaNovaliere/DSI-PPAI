package ppai.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Sesión del usuario que está operando el sistema.
 */
@Entity
@Table(name = "sesion")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    protected Sesion() {
        // Requerido por JPA
    }

    public Sesion(LocalDateTime fechaHoraInicio, Usuario usuario) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void cerrar(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
}
