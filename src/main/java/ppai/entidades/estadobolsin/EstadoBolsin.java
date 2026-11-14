package ppai.entidades.estadobolsin;

import java.time.LocalDateTime;

import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.Empleado;

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
    public void recibir(Bolsin bolsin, LocalDateTime fechaHora, Empleado responsable) {
        throw new IllegalStateException("No se puede recibir el bolsín N° " + bolsin.getNumeroBolsin()
                + " porque está en estado «" + descripcion + "»");
    }

    /**
     * Pasos comunes de toda transición: cierra el cambio de estado actual,
     * crea el nuevo cambio de estado y actualiza el estado del contexto.
     */
    protected void cambiarEstado(Bolsin bolsin, EstadoBolsin proximoEstado, LocalDateTime fechaHora,
                                 Empleado responsable) {
        CambioEstadoBolsin actual = buscarCambioEstadoActual(bolsin);
        if (actual != null) {
            actual.setFechaHoraFin(fechaHora);
        }
        bolsin.agregarCambioEstado(crearCambioEstado(proximoEstado, fechaHora, responsable));
        bolsin.setEstado(proximoEstado);
    }

    protected CambioEstadoBolsin buscarCambioEstadoActual(Bolsin bolsin) {
        for (CambioEstadoBolsin cambioEstado : bolsin.getCambiosEstado()) {
            if (cambioEstado.sosActual()) {
                return cambioEstado;
            }
        }
        return null;
    }

    protected CambioEstadoBolsin crearCambioEstado(EstadoBolsin estado, LocalDateTime fechaHora,
                                                   Empleado responsable) {
        return new CambioEstadoBolsin(fechaHora, estado, responsable);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
