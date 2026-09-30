package ppai.boundary;

/**
 * Valor de una celda de estado: se muestra la descripción y se colorea según
 * el nombre del estado.
 */
record EtiquetaEstado(String nombre, String descripcion) {

    @Override
    public String toString() {
        return descripcion;
    }
}
