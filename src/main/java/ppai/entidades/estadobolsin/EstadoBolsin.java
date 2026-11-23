package ppai.entidades.estadobolsin;

import java.time.LocalDateTime;
import java.util.List;

import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.Empleado;
import ppai.entidades.OpcionRecepcion;

/**
 * Patrón State - rol ESTADO ABSTRACTO de la máquina de estados de Bolsín.
 *
 * <p>Declara el evento que usa el CU 28, {@link #recibir}, y la consulta
 * {@link #esEnviado()} del paso 3. Por defecto un bolsín NO puede recibirse:
 * sólo lo redefine {@link Enviado}, el único estado desde el que la máquina de
 * estados tiene la transición a Recibido en CM destino. Los eventos de los
 * otros casos de uso (cerrar, reabrir, enviar, dar de baja) no se implementan
 * porque no son parte del CU 28.
 */
public abstract class EstadoBolsin {

    private final String nombre;
    private final String descripcion;

    /**
     * @param nombre      nombre del estado en la máquina de estados (se guarda en la base)
     * @param descripcion texto para mostrarle al usuario
     */
    protected EstadoBolsin(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** CU 28, paso 3: ¿el bolsín está en estado Enviado? */
    public boolean esEnviado() {
        return false;
    }

    /** CU 28, paso 11: el bolsín pasa a Recibido en CM destino. */
    public void recibir(Bolsin bolsin, LocalDateTime fechaHora, Empleado responsable, OpcionRecepcion opcion,
                        List<Integer> documentacionIndicada) {
        throw new IllegalStateException("No se puede recibir el bolsín N° " + bolsin.getNumeroBolsin()
                + " porque está en estado «" + descripcion + "»");
    }

    // Pasos que usan los estados concretos al hacer la transición

    /** Busca en el historial del bolsín el cambio de estado vigente (*sosActual()). */
    protected CambioEstadoBolsin buscarCambioEstadoActual(Bolsin bolsin) {
        for (CambioEstadoBolsin cambioEstado : bolsin.getCambiosEstado()) {
            if (cambioEstado.sosActual()) {
                return cambioEstado;
            }
        }
        return null;
    }

    /** Crea el cambio de estado del nuevo estado, con el empleado responsable. */
    protected CambioEstadoBolsin crearCambioEstado(EstadoBolsin estado, LocalDateTime fechaHora,
                                                   Empleado responsable) {
        return new CambioEstadoBolsin(fechaHora, estado, responsable);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
