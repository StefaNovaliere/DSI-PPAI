package ppai.entidades.estadodocumentacion;

import ppai.entidades.Estado;

/**
 * Patrón State - ESTADO CONCRETO: Registrada.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class Registrada extends Estado {

    public Registrada() {
        super(AMBITO_DOCUMENTACION, "Registrada", "Registrada");
    }
}
