package ppai.entidades.estadobolsin;

/**
 * Patrón State - ESTADO CONCRETO: Recibido en CM destino (estado final).
 *
 * <p>No redefine recibir(): un bolsín no se puede recibir dos veces.
 */
public class RecibidoEnCMDestino extends EstadoBolsin {

    public RecibidoEnCMDestino() {
        super("RecibidoEnCMDestino", "Recibido en CM destino");
    }
}
