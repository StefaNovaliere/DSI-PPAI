package ppai.entidades.estadoremito;

import ppai.entidades.Estado;

/** Patrón State - ESTADO CONCRETO: Recibido y aceptado, estado final. */
public class RemitoRecibidoYAceptado extends Estado {

    public RemitoRecibidoYAceptado() {
        super(AMBITO_REMITO, "RecibidoYAceptado", "Recibido y aceptado");
    }
}
