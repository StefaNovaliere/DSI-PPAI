package ppai.entidades.estadoremito;

import ppai.entidades.Remito;

/**
 * Patrón State - rol ESTADO ABSTRACTO de la máquina de estados de Remito.
 *
 * <p>Declara los dos eventos del CU 28: {@link #recibirYAceptar} (llegó toda
 * la documentación del remito) y {@link #recibirYAceptarParcial} (parte de la
 * documentación no llegó o no corresponde). Por defecto un remito NO puede
 * recibirse: sólo los redefine {@link EnBolsinEnviado}. Los eventos de los
 * otros casos de uso (cancelar, agregar o quitar de un bolsín, enviar) no se
 * implementan porque no son parte del CU 28.
 */
public abstract class EstadoRemito {

    private final String nombre;
    private final String descripcion;

    /**
     * @param nombre      nombre del estado en la máquina de estados (se guarda en la base)
     * @param descripcion texto para mostrarle al usuario
     */
    protected EstadoRemito(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** CU 28, paso 11: el remito pasa a Recibido y aceptado. */
    public void recibirYAceptar(Remito remito) {
        throw transicionInvalida(remito);
    }

    /** CU 28, flujos A3 y A4: el remito pasa a Recibido y aceptado parcial. */
    public void recibirYAceptarParcial(Remito remito) {
        throw transicionInvalida(remito);
    }

    private IllegalStateException transicionInvalida(Remito remito) {
        return new IllegalStateException("No se puede recibir el remito N° " + remito.obtenerNumero()
                + " porque está en estado «" + descripcion + "»");
    }

    /** Paso común de toda transición: el remito no guarda historial, sólo su estado actual. */
    protected void cambiarEstado(Remito remito, EstadoRemito proximoEstado) {
        remito.setEstado(proximoEstado);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
