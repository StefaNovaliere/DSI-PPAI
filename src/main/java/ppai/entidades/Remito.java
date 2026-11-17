package ppai.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ppai.dto.DatosDocumentacion;
import ppai.persistencia.ConversorEstadoRemito;

/**
 * Remito de documentación entre Comisiones Médicas.
 *
 * <p>Patrón State - rol CONTEXTO: conoce su estado y le DELEGA la recepción.
 * El estado concreto decide si el remito puede recibirse y cuál es su estado
 * siguiente.
 */
@Entity
@Table(name = "remito")
public class Remito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private int numero;
    @ManyToOne
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica origen;
    @ManyToOne
    @JoinColumn(name = "cm_destino_id")
    private ComisionMedica destino;
    /** Se guarda el nombre del estado; lo convierte ConversorEstadoRemito. */
    @Column(name = "estado", nullable = false)
    @Convert(converter = ConversorEstadoRemito.class)
    private Estado estado;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "remito_id")
    @OrderBy("id")
    private List<DetalleRemito> detallesRemito = new ArrayList<>();

    protected Remito() {
        // Requerido por JPA
    }

    public Remito(LocalDate fecha, int numero, ComisionMedica origen, ComisionMedica destino, Estado estado) {
        this.fecha = fecha;
        this.numero = numero;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado;
    }

    public void agregarDetalle(DetalleRemito detalle) {
        detallesRemito.add(detalle);
    }

    public int obtenerNumero() {
        return numero;
    }

    /** Datos de cada documentación incluida en el remito. */
    public List<DatosDocumentacion> obtenerDatosRemito() {
        List<DatosDocumentacion> documentacion = new ArrayList<>();
        for (DetalleRemito detalle : detallesRemito) {
            documentacion.add(detalle.obtenerDocumentacion());
        }
        return documentacion;
    }

    public String obtenerDescripcionEstado() {
        return estado.getDescripcion();
    }

    /** ¿La documentación con ese número está en este remito? */
    public boolean contenesDocumentacion(int numeroDocumentacion) {
        for (DetalleRemito detalle : detallesRemito) {
            if (detalle.esDocumentacion(numeroDocumentacion)) {
                return true;
            }
        }
        return false;
    }

    /**
     * CU 28, paso 11 y flujos A3 a A5: delega en el estado actual, que hace la
     * transición (Recibido y aceptado, o parcial) y actualiza el estado de la
     * documentación del remito (patrón State).
     */
    public void recibirYAceptar(LocalDateTime fechaHora, Empleado responsable, OpcionRecepcion opcion,
                                List<Integer> documentacionIndicada) {
        estado.recibirYAceptar(this, fechaHora, responsable, opcion, documentacionIndicada);
    }

    /** ¿El usuario indicó documentación de este remito (la que no llegó, no corresponde o se redirige)? */
    public boolean tenesDocumentacionIndicada(List<Integer> documentacionIndicada) {
        for (DetalleRemito detalle : detallesRemito) {
            if (detalle.esDocumentacionIndicada(documentacionIndicada)) {
                return true;
            }
        }
        return false;
    }

    /** Lo usa el estado concreto al hacer la transición. */
    public void setEstado(Estado estado) {
        if (!estado.esAmbitoRemito()) {
            throw new IllegalArgumentException("El estado «" + estado + "» no es un estado de remito");
        }
        this.estado = estado;
    }

    public Estado getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ComisionMedica getOrigen() {
        return origen;
    }

    public ComisionMedica getDestino() {
        return destino;
    }

    public List<DetalleRemito> getDetallesRemito() {
        return Collections.unmodifiableList(detallesRemito);
    }
}
