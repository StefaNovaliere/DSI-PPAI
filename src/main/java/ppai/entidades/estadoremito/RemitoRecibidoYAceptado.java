package ppai.entidades.estadoremito;

/** Patrón State - ESTADO CONCRETO: Recibido y aceptado, estado final. */
public class RemitoRecibidoYAceptado extends ppai.entidades.Estado {

    public RemitoRecibidoYAceptado() {
        super(AMBITO_REMITO, "RecibidoYAceptado", "Recibido y aceptado");
    }
}
