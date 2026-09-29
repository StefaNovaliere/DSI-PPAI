package ppai.dto;

/**
 * Datos de una documentación que el gestor le pasa a la pantalla.
 */
public record DatosDocumentacion(int numero, String asunto, String tipoDocumento, String estado) {
}
