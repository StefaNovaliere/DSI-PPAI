package ppai.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de una recepción de bolsín registrada, que el CU 28 le pasa al CU 29
 * para notificarla por correo.
 */
public record DatosRecepcion(int numeroBolsin, String numeroPrecinto, String cmOrigen, String cmDestino,
                             LocalDateTime fechaHora, String responsable,
                             List<DatosDocumentacion> documentacion) {
}
