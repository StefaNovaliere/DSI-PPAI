package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.SituacionDocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: ParaRedirigir.
 *
 * <p>En la máquina de estados salen de él dos transiciones con el evento
 * recibir(), según la guarda [documentación correcta]. Con otra situación (por
 * ejemplo, que no llegó) no hay transición y la recepción se rechaza sin
 * cambiar nada.
 */
public class ParaRedirigir extends EstadoDocumentacion {

    public ParaRedirigir() {
        super("ParaRedirigir", "Para redirigir");
    }

    @Override
    public void recibir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable,
                        SituacionDocumentacion situacion) {
        if (situacion != SituacionDocumentacion.IGUAL_A_LA_REGISTRADA
                && situacion != SituacionDocumentacion.NO_CORRESPONDE_A_LA_CM_DESTINO) {
            throw recepcionInvalida(documentacion, situacion);
        }
        CambioEstadoDocumentacion actual = buscarCambioEstadoActual(documentacion);
        actual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion proximoEstado = crearEstado(situacion);
        CambioEstadoDocumentacion nuevo = crearCambioEstado(proximoEstado, fechaHora, responsable);
        documentacion.setEstado(proximoEstado);
        documentacion.agregarCambioEstado(nuevo);
    }

    /** [documentación correcta = True] -&gt; Recibida&amp;Aceptada; [= False] -&gt; Recibida&amp;Rechazada. */
    private EstadoDocumentacion crearEstado(SituacionDocumentacion situacion) {
        if (situacion == SituacionDocumentacion.IGUAL_A_LA_REGISTRADA) {
            return new RecibidaYAceptada();
        }
        return new RecibidaYRechazada();
    }
}
