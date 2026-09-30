package ppai.entidades.estadodocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: Recibida y rechazada.
 *
 * <p>No redefine recibir(): desde este estado la documentación no puede
 * recibirse (la máquina de estados no tiene esa transición).
 */
public class RecibidaYRechazada extends EstadoDocumentacion {

    public RecibidaYRechazada() {
        super("Recibida&Rechazada", "Recibida y rechazada");
    }
}
