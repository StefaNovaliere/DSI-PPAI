package ppai.entidades;

/**
 * Tipo de la documentación que viaja en los remitos (expediente, dictamen, etc.).
 */
public class TipoDocumento {

    private String nombre;
    private String descripcion;

    public TipoDocumento(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
