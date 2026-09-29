package ppai.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Empleado de una Comisión Médica. Es el usuario del sistema y el responsable
 * de cada cambio de estado.
 */
@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String apellido;
    private String nombre;
    private String mail;
    private String usuario;
    @ManyToOne
    @JoinColumn(name = "comision_medica_id")
    private ComisionMedica comisionMedica;

    protected Empleado() {
        // Requerido por JPA
    }

    public Empleado(String apellido, String nombre, String mail, String usuario, ComisionMedica comisionMedica) {
        this.apellido = apellido;
        this.nombre = nombre;
        this.mail = mail;
        this.usuario = usuario;
        this.comisionMedica = comisionMedica;
    }

    public boolean esTuUsuario(String usuario) {
        return this.usuario.equals(usuario);
    }

    /** Devuelve la Comisión Médica en la que trabaja el empleado. */
    public ComisionMedica getCM() {
        return comisionMedica;
    }

    /** Indica si la Comisión Médica recibida es aquella en la que trabaja el empleado. */
    public boolean esTuCM(ComisionMedica cm) {
        return comisionMedica == cm;
    }

    public String getEmail() {
        return mail;
    }

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }
}
