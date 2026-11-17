package ppai.entidades.estadodocumentacion;

import ppai.entidades.Estado;

/**
 * Patrón State - ESTADO CONCRETO: No recibida.
 *
 * <p>No redefine recibir(): en la máquina de estados las transiciones que
 * salen de este estado son de otros casos de uso (registrar() y darDeBaja()).
 */
public class NoRecibida extends Estado {

    public NoRecibida() {
        super(AMBITO_DOCUMENTACION, "NoRecibida", "No recibida");
    }
}
