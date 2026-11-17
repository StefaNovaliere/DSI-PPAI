package ppai.entidades.estadoremito;

import ppai.entidades.Estado;

/** Patrón State - ESTADO CONCRETO: Recibido y aceptado parcial, estado final. */
public class RecibidoYAceptadoParcial extends Estado {

    public RecibidoYAceptadoParcial() {
        super(AMBITO_REMITO, "RecibidoYAceptadoParcial", "Recibido y aceptado parcial");
    }
}
