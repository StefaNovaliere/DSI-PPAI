package ppai.entidades.estadoremito;

import ppai.entidades.Remito;

/** Patrón State - ESTADO CONCRETO: En bolsín enviado (CU 27 Registrar el retiro de bolsines). */
public class EnBolsinEnviado extends EstadoRemito {

    public EnBolsinEnviado() {
        super("EnBolsinEnviado", "En bolsín enviado");
    }

    /** CU 28, paso 11: En bolsín enviado -&gt; Recibido y aceptado. */
    @Override
    public void recibirYAceptar(Remito remito) {
        cambiarEstado(remito, new RecibidoYAceptado());
    }

    /** CU 28, flujos A3 y A4: En bolsín enviado -&gt; Recibido y aceptado parcial. */
    @Override
    public void recibirYAceptarParcial(Remito remito) {
        cambiarEstado(remito, new RecibidoYAceptadoParcial());
    }
}
