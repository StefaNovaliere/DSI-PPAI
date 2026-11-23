package ppai.entidades.estadobolsin;

import java.time.LocalDateTime;
import java.util.List;

import ppai.entidades.Bolsin;
import ppai.entidades.Estado;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.Empleado;
import ppai.entidades.OpcionRecepcion;
import ppai.entidades.Remito;

/** Patrón State - ESTADO CONCRETO: Enviado (CU 27 Registrar el retiro de bolsines). */
public class Enviado extends ppai.entidades.Estado {

    public Enviado() {
        super(AMBITO_BOLSIN, "Enviado", "Enviado");
    }

    @Override
    public boolean esEnviado() {
        return true;
    }

    /**
     * CU 28 Registrar Recepción de Bolsín, paso 11:
     * Enviado -&gt; Recibido en CM destino, registrando el empleado responsable.
     */
    @Override
    public void recibir(Bolsin bolsin, LocalDateTime fechaHora, Empleado responsable, OpcionRecepcion opcion,
                        List<Integer> documentacionIndicada) {
        CambioEstadoBolsin actual = buscarCambioEstadoActual(bolsin);
        actual.setFechaHoraFin(fechaHora);
        Estado proximoEstado = crearEstado();
        CambioEstadoBolsin nuevo = crearCambioEstadoBolsin(proximoEstado, fechaHora, responsable);
        bolsin.setEstado(proximoEstado);
        bolsin.agregarCambioEstado(nuevo);

        actualizarEstadoRemito(bolsin, fechaHora, responsable, opcion, documentacionIndicada);
    }

    /** El estado Enviado coordina la recepción de los remitos del bolsín. */
    private void actualizarEstadoRemito(Bolsin bolsin, LocalDateTime fechaHora, Empleado responsable,
                                        OpcionRecepcion opcion, List<Integer> documentacionIndicada) {
        for (Remito remito : bolsin.getRemitos()) {
            remito.recibirYAceptar(fechaHora, responsable, opcion, documentacionIndicada);
        }
    }

    private Estado crearEstado() {
        return new RecibidoEnCMDestino();
    }
}

