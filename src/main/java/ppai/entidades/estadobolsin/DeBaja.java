package ppai.entidades.estadobolsin;

import ppai.entidades.Estado;

/** Patrón State - ESTADO CONCRETO: De baja (CU 21 Eliminar bolsín), estado final. */
public class DeBaja extends Estado {

    public DeBaja() {
        super(AMBITO_BOLSIN, "DeBaja", "De baja");
    }
}
