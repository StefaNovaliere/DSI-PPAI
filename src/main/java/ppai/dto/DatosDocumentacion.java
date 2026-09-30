package ppai.dto;

/**
 * Datos de una documentación que el gestor le pasa a la pantalla.
 *
 * @param estado            nombre del estado actual (por ejemplo "EnBolsinEnviado")
 * @param descripcionEstado texto del estado para el usuario (por ejemplo "En bolsín enviado")
 */
public record DatosDocumentacion(int numero, String asunto, String tipoDocumento, String estado,
                                 String descripcionEstado) {
}
