package ppai.entidades;

/**
 * Comisión Médica: origen o destino de los bolsines y remitos.
 */
public class ComisionMedica {

    private final int codigo;
    private final String nombre;
    private final String direccion;
    private final String email;
    private final String telefono;

    public ComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getEmail() {
        return email;
    }
}
