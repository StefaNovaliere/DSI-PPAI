package ppai.dto;

import java.util.List;

/**
 * Datos de un remito (número y documentación incluida) que se muestran en pantalla.
 */
public record DatosRemito(int numero, List<DatosDocumentacion> documentacion) {
}
