package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.SituacionDocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: ParaRedirigir.
 *
 * <p>En la máquina de estados salen de él dos transiciones con el evento
 * recibir(), según la guarda [documentación correcta]. Con otra situación (por
 * ejemplo, que no llegó) no hay transición y la recepción se rechaza sin
 * cambiar nada.
 */
public class ParaRedirigir extends Estado {

    public ParaRedirigir() {
        super(AMBITO_DOCUMENTACION, "ParaRedirigir", "Para redirigir");
    }

    @Override
    public void recibir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable,
                        SituacionDocumentacion situacion) {
        if (situacion != SituacionDocumentacion.IGUAL_A_LA_REGISTRADA
                && situacion != SituacionDocumentacion.NO_CORRESPONDE_A_LA_CM_DESTINO) {
            // Con otra situación no hay transición: se rechaza como en Estado
            super.recibir(documentacion, fechaHora, responsable, situacion);
            return;
        }
        // *sosActual(): se le pregunta a cada cambio de estado si es el actual
        CambioEstadoDocumentacion actual = null;
        for (CambioEstadoDocumentacion cambioEstado : documentacion.getCambiosEstado()) {
            if (cambioEstado.sosActual()) {
                actual = cambioEstado;
            }
        }
        actual.setFechaHoraFin(fechaHora);
        Estado proximoEstado = crearEstado(situacion);
        CambioEstadoDocumentacion nuevo = crearCambioEstado(proximoEstado, fechaHora, responsable);
        documentacion.setEstado(proximoEstado);
        documentacion.agregarCambioEstado(nuevo);
    }

    /** [documentación correcta = True] -&gt; Recibida&amp;Aceptada; [= False] -&gt; Recibida&amp;Rechazada. */
    private Estado crearEstado(SituacionDocumentacion situacion) {
        if (situacion == SituacionDocumentacion.IGUAL_A_LA_REGISTRADA) {
            return new RecibidaYAceptada();
        }
        return new RecibidaYRechazada();
    }

    private CambioEstadoDocumentacion crearCambioEstado(Estado estado, LocalDateTime fechaHora,
                                                        Empleado responsable) {
        return new CambioEstadoDocumentacion(fechaHora, estado, responsable);
    }
}
