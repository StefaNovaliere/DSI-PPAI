package ppai.entidades.estadodocumentacion;

/** Patrón State - ESTADO CONCRETO: DeBaja (estado final). */
public class DeBaja extends EstadoDocumentacion {

    public DeBaja() {
        super("DeBaja");
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
