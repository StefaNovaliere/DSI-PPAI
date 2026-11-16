package ppai.entidades;

/**
 * Clase general de los estados de las tres máquinas de estado del CU 28.
 *
 * <p>Tiene lo que comparten todos los estados: el nombre y la descripción. De
 * ella heredan los estados abstractos de cada ámbito ({@code EstadoBolsin},
 * {@code EstadoRemito} y {@code EstadoDocumentacion}), y cada uno declara los
 * eventos de su propia máquina de estados.
 */
public abstract class Estado {

    private final String nombre;
    private final String descripcion;

    /**
     * @param nombre      nombre del estado en la máquina de estados (se guarda en la base)
     * @param descripcion texto para mostrarle al usuario
     */
    protected Estado(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
