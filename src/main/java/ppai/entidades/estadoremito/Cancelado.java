package ppai.entidades.estadoremito;

/** Patrón State - ESTADO CONCRETO: Cancelado (CU 17 Cancelar remito), estado final. */
public class Cancelado extends ppai.entidades.Estado {

    public Cancelado() {
        super(AMBITO_REMITO, "Cancelado", "Cancelado");
    }
}
