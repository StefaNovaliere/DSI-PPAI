package ppai.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Empleado de una Comisión Médica. Opera el sistema con su Usuario y es el
 * responsable de cada cambio de estado.
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
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @ManyToOne
    @JoinColumn(name = "comision_medica_id")
    private ComisionMedica comisionMedica;

    protected Empleado() {
        // Requerido por JPA
    }

    public Empleado(String apellido, String nombre, String mail, Usuario usuario, ComisionMedica comisionMedica) {
        this.apellido = apellido;
        this.nombre = nombre;
        this.mail = mail;
        this.usuario = usuario;
        this.comisionMedica = comisionMedica;
    }

    /** Indica si el usuario recibido es el del empleado. */
    public boolean esTuUsuario(Usuario usuario) {
        return this.usuario == usuario;
    }

    /** Devuelve el nombre de la Comisión Médica en la que trabaja el empleado. */
    public String getCM() {
        return comisionMedica.getNombre();
    }

    /** Indica si la Comisión Médica recibida (por su nombre) es aquella en la que trabaja el empleado. */
    public boolean esTuCM(String nombreCM) {
        return comisionMedica.getNombre().equals(nombreCM);
    }

    public String getEmail() {
        return mail;
    }

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }
}
