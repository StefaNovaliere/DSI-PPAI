package ppai.entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ppai.dto.DatosDocumentacion;
import ppai.entidades.estadodocumentacion.EstadoDocumentacion;
import ppai.entidades.estadodocumentacion.Registrada;

/**
 * Patrón State - rol CONTEXTO.
 *
 * <p>La documentación conoce a su estado actual y le DELEGA cada evento de su
 * máquina de estados. No hay ningún if/switch sobre el estado: el estado
 * concreto decide si la transición es válida y cuál es el estado siguiente.
 */
public class Documentacion {

    private final int numero;
    private final String asunto;
    private final LocalDate fechaPase;
    private final TipoDocumento tipoDocumento;
    private EstadoDocumentacion estadoActual;
    private final List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    /**
     * CU 7 Registrar Documentación: new() -> Registrada.
     */
    public Documentacion(int numero, String asunto, LocalDate fechaPase, TipoDocumento tipoDocumento,
                         LocalDateTime fechaHora, Empleado responsable) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
        Registrada registrada = new Registrada();
        agregarCambioEstado(new CambioEstadoDocumentacion(fechaHora, registrada, responsable));
        setEstado(registrada);
    }

    /**
     * Materialización desde la persistencia: reconstruye la documentación con
     * su historial. El estado actual es el del cambio de estado vigente.
     */
    public Documentacion(int numero, String asunto, LocalDate fechaPase, TipoDocumento tipoDocumento,
                         List<CambioEstadoDocumentacion> historial) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
        this.cambiosEstado.addAll(historial);
        for (CambioEstadoDocumentacion cambioEstado : historial) {
            if (cambioEstado.sosActual()) {
                this.estadoActual = cambioEstado.getEstado();
            }
        }
        if (estadoActual == null) {
            throw new IllegalArgumentException("La documentación " + numero + " no tiene un cambio de estado actual");
        }
    }

    // ---------------------------------------------------------------------
    // Eventos de la máquina de estados: el contexto delega en el estado.
    // ---------------------------------------------------------------------

    /** CU 15 Generar Remito / CU 20 Modificar Bolsín. */
    public void remitar(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.remitar(this, fechaHora, responsable);
    }

    /** CU 17 Cancelar Remito. */
    public void cancelarRemito(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.cancelarRemito(this, fechaHora, responsable);
    }

    /** CU 19 Generar Bolsín. */
    public void agregarAlBolsin(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.agregarAlBolsin(this, fechaHora, responsable);
    }

    /** CU 27 Registrar el retiro de bolsines. */
    public void enviar(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.enviar(this, fechaHora, responsable);
    }

    /** CU 28 Registrar Recepción de Bolsín. */
    public void recibir(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.recibir(this, fechaHora, responsable);
    }

    /** CU 7 Registrar Documentación (volver a registrar). */
    public void registrar(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.registrar(this, fechaHora, responsable);
    }

    /** CU 9 Eliminar Documentación. */
    public void darDeBaja(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.darDeBaja(this, fechaHora, responsable);
    }

    // ---------------------------------------------------------------------
    // Métodos que usan los estados concretos para hacer la transición.
    // ---------------------------------------------------------------------

    public void setEstado(EstadoDocumentacion estado) {
        this.estadoActual = estado;
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambioEstado) {
        cambiosEstado.add(cambioEstado);
    }

    public List<CambioEstadoDocumentacion> getCambiosEstado() {
        return Collections.unmodifiableList(cambiosEstado);
    }

    // ---------------------------------------------------------------------
    // Consultas usadas por el CU 28.
    // ---------------------------------------------------------------------

    public String getAsunto() {
        return asunto;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaPase() {
        return fechaPase;
    }

    public String mostrarTipoDocumentacion() {
        return tipoDocumento.getNombre();
    }

    public EstadoDocumentacion getEstadoActual() {
        return estadoActual;
    }

    public DatosDocumentacion getDatosDocumentacion() {
        return new DatosDocumentacion(numero, getAsunto(), mostrarTipoDocumentacion(), estadoActual.getNombre());
    }
}
