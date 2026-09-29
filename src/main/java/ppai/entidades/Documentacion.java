package ppai.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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
@Entity
@Table(name = "documentacion")
public class Documentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;
    private String asunto;
    private LocalDate fechaPase;
    @ManyToOne
    @JoinColumn(name = "tipo_documento_id")
    private TipoDocumento tipoDocumento;
    /** No se guarda como columna: se reconstruye desde el cambio de estado actual (ver restaurarEstadoActual). */
    @Transient
    private EstadoDocumentacion estadoActual;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "documentacion_id")
    @OrderBy("id")
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    protected Documentacion() {
        // Requerido por JPA
    }

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
     * Reconstruye una documentación a partir de su historial de estados
     * (se usa para cargar datos de prueba). El estado actual es el del cambio
     * de estado vigente.
     */
    public Documentacion(int numero, String asunto, LocalDate fechaPase, TipoDocumento tipoDocumento,
                         List<CambioEstadoDocumentacion> historial) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumento = tipoDocumento;
        this.cambiosEstado.addAll(historial);
        restaurarEstadoActual();
    }

    /**
     * Al materializar la documentación desde la base de datos, el estado
     * actual (objeto del patrón State) es el del cambio de estado vigente.
     */
    @PostLoad
    void restaurarEstadoActual() {
        estadoActual = null;
        for (CambioEstadoDocumentacion cambioEstado : cambiosEstado) {
            if (cambioEstado.sosActual()) {
                estadoActual = cambioEstado.getEstado();
            }
        }
        if (estadoActual == null) {
            throw new IllegalStateException("La documentación " + numero + " no tiene un cambio de estado actual");
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

    /** CU 31 Registrar Revisión: no llegó o no coincide con lo registrado. */
    public void registrarNoRecibida(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.registrarNoRecibida(this, fechaHora, responsable);
    }

    /** CU 31 Registrar Revisión: la documentación debe redirigirse. */
    public void marcarParaRedirigir(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.marcarParaRedirigir(this, fechaHora, responsable);
    }

    /** CU 31 Registrar Revisión: la documentación se rechaza. */
    public void rechazar(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.rechazar(this, fechaHora, responsable);
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
