package ppai.entidades;

/**
 * Empleado de una Comisión Médica. Es el usuario del sistema y el responsable
 * de cada cambio de estado.
 */
public class Empleado {

    private final String apellido;
    private final String nombre;
    private final String mail;
    private final String usuario;
    private final ComisionMedica comisionMedica;

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
