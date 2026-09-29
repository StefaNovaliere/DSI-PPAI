package ppai.entidades;

import java.time.LocalDateTime;

import ppai.dto.DatosDocumentacion;

/**
 * Línea de un remito: una documentación y el área de la CM destino.
 */
public class DetalleRemito {

    private final String areaCMCDestino;
    private final Documentacion documentacion;

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
