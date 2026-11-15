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
import java.util.List;

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

    public boolean esDocumentacion(int numeroDocumentacion) {
        return documentacion.getNumero() == numeroDocumentacion;
    }

    /** ¿El usuario indicó esta documentación (la que no llegó, no corresponde o se redirige)? */
    public boolean esDocumentacionIndicada(List<Integer> documentacionIndicada) {
        return documentacionIndicada.contains(documentacion.getNumero());
    }

    /**
     * CU 28, paso 11 y flujos A3 a A5: registra la recepción de la documentación.
     * La documentación que indicó el usuario tiene la situación de la opción
     * elegida (no llegó, no corresponde o se redirige); el resto es igual a la
     * registrada. El detalle no sabe en qué estado está la documentación ni a
     * cuál pasa: eso lo resuelve su estado actual (patrón State).
     */
    public void recibirYAceptarDoc(LocalDateTime fechaHora, Empleado responsable, OpcionRecepcion opcion,
                                   List<Integer> documentacionIndicada) {
        SituacionDocumentacion situacion = esDocumentacionIndicada(documentacionIndicada)
                ? opcion.getSituacionDocumentacionIndicada()
                : SituacionDocumentacion.IGUAL_A_LA_REGISTRADA;
        documentacion.recibir(fechaHora, responsable, situacion);
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }

    public String getAreaCMCDestino() {
        return areaCMCDestino;
    }
}
