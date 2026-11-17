package ppai.entidades.estadodocumentacion;

import ppai.entidades.Estado;

/**
 * Patrón State - ESTADO CONCRETO: En bolsín saliente.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class EnBolsinSaliente extends Estado {

    public EnBolsinSaliente() {
        super(AMBITO_DOCUMENTACION, "EnBolsinSaliente", "En bolsín saliente");
    }
}
