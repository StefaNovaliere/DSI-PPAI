package ppai.dto;

import java.util.List;

/**
 * Datos de un remito (número, estado y documentación incluida) que se muestran en pantalla.
 *
 * @param descripcionEstado texto del estado del remito (por ejemplo "En bolsín enviado")
 */
public record DatosRemito(int numero, String descripcionEstado, List<DatosDocumentacion> documentacion) {
}
