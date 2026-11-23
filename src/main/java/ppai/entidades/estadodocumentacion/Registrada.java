package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: Registrada.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class Registrada extends EstadoDocumentacion {

    public Registrada() {
        super("Registrada", "Registrada");
    }
}
