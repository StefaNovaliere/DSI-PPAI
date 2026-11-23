package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: No recibida.
 *
 * <p>No redefine recibir(): en la máquina de estados las transiciones que
 * salen de este estado son de otros casos de uso (registrar() y darDeBaja()).
 */
public class NoRecibida extends EstadoDocumentacion {

    public NoRecibida() {
        super("NoRecibida", "No recibida");
    }
}
