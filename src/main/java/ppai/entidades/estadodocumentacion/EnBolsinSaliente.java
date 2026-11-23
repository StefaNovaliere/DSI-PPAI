package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: En bolsín saliente.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class EnBolsinSaliente extends ppai.entidades.Estado {

    public EnBolsinSaliente() {
        super(AMBITO_DOCUMENTACION, "EnBolsinSaliente", "En bolsín saliente");
    }
}
