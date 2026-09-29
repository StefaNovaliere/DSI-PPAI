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
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;

/**
 * Remito de documentación entre Comisiones Médicas.
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
    @ManyToOne
    @JoinColumn(name = "estado_id")
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

    /** Número del remito y datos de cada documentación incluida. */
    public DatosRemito obtenerDatosRemito() {
        List<DatosDocumentacion> documentacion = new ArrayList<>();
        for (DetalleRemito detalle : detallesRemito) {
            documentacion.add(detalle.obtenerDocumentacion());
        }
        return new DatosRemito(obtenerNumero(), documentacion);
    }

    /**
     * CU 28: el remito pasa a RecibidoYAceptado y le pide a cada detalle que
     * actualice el estado de su documentación.
     */
    public void recibirYAceptar(Estado recibidoYAceptado, LocalDateTime fechaHora, Empleado responsable) {
        setEstado(recibidoYAceptado);
        for (DetalleRemito detalle : detallesRemito) {
            detalle.actualizarEstadoDoc(fechaHora, responsable);
        }
    }

    public void setEstado(Estado estado) {
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
