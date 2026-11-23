package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: De baja.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class DeBaja extends EstadoDocumentacion {

    public DeBaja() {
        super("DeBaja", "De baja");
    }
}
