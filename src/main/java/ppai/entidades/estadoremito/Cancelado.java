package ppai.entidades.estadoremito;

import ppai.entidades.Estado;

/** Patrón State - ESTADO CONCRETO: Cancelado (CU 17 Cancelar remito), estado final. */
public class Cancelado extends Estado {

    public Cancelado() {
        super(AMBITO_REMITO, "Cancelado", "Cancelado");
    }
}
