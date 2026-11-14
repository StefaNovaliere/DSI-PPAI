package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.SituacionDocumentacion;

/**
 * Patrón State - rol ESTADO ABSTRACTO.
 *
 * <p>Declara el evento de la máquina de estados que usa el CU 28:
 * {@link #recibir}. La situación de la documentación es la guarda de la
 * transición. Por defecto un estado NO puede recibir la documentación; sólo lo
 * redefinen los estados concretos desde los que la máquina de estados de la
 * Entrega 1 tiene transiciones con recibir() (EnBolsinEnviado y ParaRedirigir).
 */
public abstract class EstadoDocumentacion {

    private final String nombre;
    private final String descripcion;

    /**
     * @param nombre      nombre del estado en la máquina de estados (se guarda en la base)
     * @param descripcion texto para mostrarle al usuario
     */
    protected EstadoDocumentacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** CU 28: recibir() con la situación de la documentación como guarda. */
    public void recibir(Documentacion documentacion, SituacionDocumentacion situacion, LocalDateTime fechaHora,
                        Empleado responsable) {
        throw recepcionInvalida(documentacion, situacion);
    }

    /** La máquina de estados no tiene una transición recibir() desde este estado con esa situación. */
    protected IllegalStateException recepcionInvalida(Documentacion documentacion, SituacionDocumentacion situacion) {
        return new IllegalStateException("No se puede recibir la documentación N° " + documentacion.getNumero()
                + " como «" + situacion.getDescripcion() + "» porque está en estado «" + descripcion + "»");
    }

    /**
     * Pasos comunes de toda transición: cierra el cambio de estado actual,
     * crea el nuevo cambio de estado y actualiza el estado del contexto.
     */
    protected void cambiarEstado(Documentacion documentacion, EstadoDocumentacion proximoEstado,
                                 LocalDateTime fechaHora, Empleado responsable) {
        CambioEstadoDocumentacion actual = buscarCambioEstadoActual(documentacion);
        if (actual != null) {
            actual.setFechaHoraFin(fechaHora);
        }
        documentacion.agregarCambioEstado(crearCambioEstado(proximoEstado, fechaHora, responsable));
        documentacion.setEstado(proximoEstado);
    }

    protected CambioEstadoDocumentacion buscarCambioEstadoActual(Documentacion documentacion) {
        for (CambioEstadoDocumentacion cambioEstado : documentacion.getCambiosEstado()) {
            if (cambioEstado.sosActual()) {
                return cambioEstado;
            }
        }
        return null;
    }

    protected CambioEstadoDocumentacion crearCambioEstado(EstadoDocumentacion estado, LocalDateTime fechaHora,
                                                          Empleado responsable) {
        return new CambioEstadoDocumentacion(fechaHora, estado, responsable);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
