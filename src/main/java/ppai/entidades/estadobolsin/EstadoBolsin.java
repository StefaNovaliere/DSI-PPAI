package ppai.entidades.estadobolsin;

import java.time.LocalDateTime;

import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;

/**
 * Patrón State - rol ESTADO ABSTRACTO de la máquina de estados de Bolsín.
 * Hereda de {@link Estado} el nombre y la descripción.
 *
 * <p>Declara el evento que usa el CU 28, {@link #recibir}, y la consulta
 * {@link #esEnviado()} del paso 3. Por defecto un bolsín NO puede recibirse:
 * sólo lo redefine {@link Enviado}, el único estado desde el que la máquina de
 * estados tiene la transición a Recibido en CM destino. Los eventos de los
 * otros casos de uso (cerrar, reabrir, enviar, dar de baja) no se implementan
 * porque no son parte del CU 28.
 */
public abstract class EstadoBolsin extends Estado {

    protected EstadoBolsin(String nombre, String descripcion) {
        super(nombre, descripcion);
    }

    /** CU 28, paso 3: ¿el bolsín está en estado Enviado? */
    public boolean esEnviado() {
        return false;
    }

    /** CU 28, paso 11: el bolsín pasa a Recibido en CM destino. */
    public void recibir(Bolsin bolsin, LocalDateTime fechaHora, Empleado responsable) {
        throw new IllegalStateException("No se puede recibir el bolsín N° " + bolsin.getNumeroBolsin()
                + " porque está en estado «" + getDescripcion() + "»");
    }

    // Paso que usan los estados concretos al hacer la transición

    /** Crea el cambio de estado del nuevo estado, con el empleado responsable. */
    protected CambioEstadoBolsin crearCambioEstado(EstadoBolsin estado, LocalDateTime fechaHora,
                                                   Empleado responsable) {
        return new CambioEstadoBolsin(fechaHora, estado, responsable);
    }
}
