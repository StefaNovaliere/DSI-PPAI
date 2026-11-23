package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: En remito.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class EnRemito extends ppai.entidades.Estado {

    public EnRemito() {
        super(AMBITO_DOCUMENTACION, "EnRemito", "En remito");
    }
}
