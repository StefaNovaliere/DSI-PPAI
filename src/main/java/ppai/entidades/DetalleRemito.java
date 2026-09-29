package ppai.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import ppai.dto.DatosDocumentacion;

/**
 * Línea de un remito: una documentación y el área de la CM destino.
 */
@Entity
@Table(name = "detalle_remito")
public class DetalleRemito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String areaCMCDestino;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "documentacion_id")
    private Documentacion documentacion;

    protected DetalleRemito() {
        // Requerido por JPA
    }

    public DetalleRemito(String areaCMCDestino, Documentacion documentacion) {
        this.areaCMCDestino = areaCMCDestino;
        this.documentacion = documentacion;
    }

    /** Datos de la documentación del detalle para mostrar en pantalla. */
    public DatosDocumentacion obtenerDocumentacion() {
        return documentacion.getDatosDocumentacion();
    }

    /**
     * CU 28: registra la recepción de la documentación. El detalle no sabe en
     * qué estado está la documentación ni a cuál pasa: eso lo resuelve el
     * estado concreto (patrón State).
     */
    public void actualizarEstadoDoc(LocalDateTime fechaHora, Empleado responsable) {
        documentacion.recibir(fechaHora, responsable);
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }

    public String getAreaCMCDestino() {
        return areaCMCDestino;
    }
}
