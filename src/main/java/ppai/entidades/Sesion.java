package ppai.entidades;

import java.time.LocalDateTime;

/**
 * Sesión del usuario que está operando el sistema.
 */
public class Sesion {

    private final LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private final String usuario;

    public Sesion(LocalDateTime fechaHoraInicio, String usuario) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.usuario = usuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void cerrar(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
}
