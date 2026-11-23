package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: De baja.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class DeBaja extends ppai.entidades.Estado {

    public DeBaja() {
        super(AMBITO_DOCUMENTACION, "DeBaja", "De baja");
    }
}
