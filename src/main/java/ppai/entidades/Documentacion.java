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

/**
 * Patrón State - rol CONTEXTO.
 *
 * <p>La documentación conoce a su estado actual y le DELEGA el evento de su
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
    /** No se guarda como columna: se obtiene del cambio de estado actual (ver restaurarEstadoActual). */
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
     * Crea la documentación con su historial de estados. La documentación la
     * registran y la envían otros casos de uso (CU 7, 15, 19, 27); este
     * constructor se usa para cargar los datos de prueba.
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
     * El estado actual (objeto del patrón State) es el del cambio de estado
     * vigente. Se ejecuta también al materializar la documentación desde la base.
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

    /** CU 28 Registrar Recepción de Bolsín: delega en el estado actual. */
    public void recibir(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.recibir(this, fechaHora, responsable);
    }

    // Métodos que usa el estado concreto para hacer la transición

    public void setEstado(EstadoDocumentacion estado) {
        this.estadoActual = estado;
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambioEstado) {
        cambiosEstado.add(cambioEstado);
    }

    public List<CambioEstadoDocumentacion> getCambiosEstado() {
        return Collections.unmodifiableList(cambiosEstado);
    }

    // Consultas usadas por el CU 28

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
        return new DatosDocumentacion(numero, getAsunto(), mostrarTipoDocumentacion(), estadoActual.getNombre(),
                estadoActual.getDescripcion());
    }
}
