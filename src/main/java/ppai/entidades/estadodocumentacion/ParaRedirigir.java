package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.SituacionDocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: ParaRedirigir.
 *
 * <p>En la máquina de estados salen de él dos transiciones con el evento
 * recibir(), según la guarda [documentación correcta]. Con otra situación (por
 * ejemplo, que no llegó) no hay transición y la recepción se rechaza.
 */
public class ParaRedirigir extends EstadoDocumentacion {

    public ParaRedirigir() {
        super("ParaRedirigir", "Para redirigir");
    }

    @Override
    public void recibir(Documentacion documentacion, SituacionDocumentacion situacion, LocalDateTime fechaHora,
                        Empleado responsable) {
        if (situacion == SituacionDocumentacion.IGUAL_A_LA_REGISTRADA) {
            // [documentación correcta = True] / recibir()
            cambiarEstado(documentacion, new RecibidaYAceptada(), fechaHora, responsable);
        } else if (situacion == SituacionDocumentacion.NO_CORRESPONDE_A_LA_CM_DESTINO) {
            // [documentación correcta = False] / recibir()
            cambiarEstado(documentacion, new RecibidaYRechazada(), fechaHora, responsable);
        } else {
            throw recepcionInvalida(documentacion, situacion);
        }
    }
}
