package ppai.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de una recepción de bolsín registrada, que el CU 28 le pasa al CU 29
 * para notificarla por correo a la CM origen.
 *
 * @param opcionRecepcion opción de recepción que eligió el empleado (paso 7)
 * @param remitos         estado final de cada remito y de su documentación
 */
public record DatosRecepcion(int numeroBolsin, String numeroPrecinto, String cmOrigen, String cmDestino,
                             LocalDateTime fechaHora, String responsable, String opcionRecepcion,
                             List<DatosRemito> remitos) {
}
