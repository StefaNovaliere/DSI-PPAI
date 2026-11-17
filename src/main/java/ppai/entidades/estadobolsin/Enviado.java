package ppai.entidades.estadobolsin;

import java.time.LocalDateTime;

import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: Enviado (CU 27 Registrar el retiro de bolsines). */
public class Enviado extends EstadoBolsin {

    public Enviado() {
        super("Enviado", "Enviado");
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
        EstadoBolsin proximoEstado = crearEstado();
        CambioEstadoBolsin nuevo = crearCambioEstado(proximoEstado, fechaHora, responsable);
        bolsin.setEstado(proximoEstado);
        bolsin.agregarCambioEstado(nuevo);
    }

    private EstadoBolsin crearEstado() {
        return new RecibidoEnCMDestino();
    }
}
