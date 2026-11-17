package ppai.entidades.estadobolsin;

import java.time.LocalDateTime;

import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;

/** Patrón State - ESTADO CONCRETO: Enviado (CU 27 Registrar el retiro de bolsines). */
public class Enviado extends Estado {

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
    public void recibir(Bolsin bolsin, LocalDateTime fechaHora, Empleado responsable) {
        // *sosActual(): se le pregunta a cada cambio de estado si es el actual
        CambioEstadoBolsin actual = null;
        for (CambioEstadoBolsin cambioEstado : bolsin.getCambiosEstado()) {
            if (cambioEstado.sosActual()) {
                actual = cambioEstado;
            }
        }
        actual.setFechaHoraFin(fechaHora);
        Estado proximoEstado = crearEstado();
        CambioEstadoBolsin nuevo = crearCambioEstado(proximoEstado, fechaHora, responsable);
        bolsin.setEstado(proximoEstado);
        bolsin.agregarCambioEstado(nuevo);
    }

    private Estado crearEstado() {
        return new RecibidoEnCMDestino();
    }

    private CambioEstadoBolsin crearCambioEstado(Estado estado, LocalDateTime fechaHora, Empleado responsable) {
        return new CambioEstadoBolsin(fechaHora, estado, responsable);
    }
}
