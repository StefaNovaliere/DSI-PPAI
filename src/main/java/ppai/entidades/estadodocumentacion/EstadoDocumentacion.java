package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/**
 * Patrón State - rol ESTADO ABSTRACTO.
 *
 * <p>Declara un método por cada evento de la máquina de estados de
 * Documentación. La implementación por defecto rechaza el evento: cada estado
 * concreto redefine sólo las transiciones que salen de él.
 */
public abstract class EstadoDocumentacion {

    private final String nombre;

    protected EstadoDocumentacion(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void remitar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("remitar");
    }

    public void cancelarRemito(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("cancelar el remito de");
    }

    public void agregarAlBolsin(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("agregar al bolsín");
    }

    public void enviar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("enviar");
    }

    public void recibir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("recibir");
    }

    /** CU 31: la documentación no llegó o no coincide con lo registrado. */
    public void registrarNoRecibida(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("registrar como no recibida");
    }

    /** CU 31: la documentación debe redirigirse a otra CM. */
    public void marcarParaRedirigir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("marcar para redirigir");
    }

    /** CU 31: la documentación se recibe pero se rechaza. */
    public void rechazar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("rechazar");
    }

    public void registrar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("registrar");
    }

    public void darDeBaja(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        throw transicionInvalida("dar de baja");
    }

    /** Un estado final no tiene transiciones de salida. */
    public boolean esFinal() {
        return false;
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

    private IllegalStateException transicionInvalida(String accion) {
        return new IllegalStateException(
                "No se puede " + accion + " una documentación en estado " + nombre);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
