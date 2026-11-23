package ppai.entidades.estadobolsin;

/** Patrón State - ESTADO CONCRETO: De baja (CU 21 Eliminar bolsín), estado final. */
public class DeBaja extends ppai.entidades.Estado {

    public DeBaja() {
        super(AMBITO_BOLSIN, "DeBaja", "De baja");
    }
}
