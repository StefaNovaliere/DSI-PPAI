package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: Recibida y rechazada.
 *
 * <p>No redefine recibir(): en la máquina de estados las transiciones que
 * salen de este estado son de otros casos de uso (registrar() y darDeBaja()).
 */
public class RecibidaYRechazada extends ppai.entidades.Estado {

    public RecibidaYRechazada() {
        super(AMBITO_DOCUMENTACION, "Recibida&Rechazada", "Recibida y rechazada");
    }
}
