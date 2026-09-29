package ppai.dto;

/**
 * Datos de un bolsín enviado a la CM del usuario, para que el usuario lo seleccione.
 */
public record DatosBolsin(int numeroBolsin, String numeroPrecinto, String cmOrigen) {
}
