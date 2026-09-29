package ppai.entidades.estadodocumentacion;

/** Patrón State - ESTADO CONCRETO: Recibida&amp;Aceptada (estado final). */
public class RecibidaYAceptada extends EstadoDocumentacion {

    public RecibidaYAceptada() {
        super("Recibida&Aceptada");
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
