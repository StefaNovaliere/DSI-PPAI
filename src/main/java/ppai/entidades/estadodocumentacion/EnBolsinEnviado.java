package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.SituacionDocumentacion;

/**
 * Patrón State - ESTADO CONCRETO: EnBolsinEnviado.
 *
 * <p>Es el estado de la documentación que viaja en el bolsín. En la máquina de
 * estados salen de él cuatro transiciones con el evento recibir(); la guarda
 * (la situación de la documentación) decide cuál se toma.
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    public EnBolsinEnviado() {
        super("EnBolsinEnviado", "En bolsín enviado");
    }

    @Override
    public void recibir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable,
                        SituacionDocumentacion situacion) {
        // *sosActual(): se le pregunta a cada cambio de estado si es el actual
        CambioEstadoDocumentacion actual = null;
        for (CambioEstadoDocumentacion cambioEstado : documentacion.getCambiosEstado()) {
            if (cambioEstado.sosActual()) {
                actual = cambioEstado;
            }
        }
        actual.setFechaHoraFin(fechaHora);
        EstadoDocumentacion proximoEstado = crearEstado(situacion);
        CambioEstadoDocumentacion nuevo = crearCambioEstado(proximoEstado, fechaHora, responsable);
        documentacion.setEstado(proximoEstado);
        documentacion.agregarCambioEstado(nuevo);
    }

    /** Las guardas de las transiciones recibir() que salen de EnBolsinEnviado. */
    private EstadoDocumentacion crearEstado(SituacionDocumentacion situacion) {
        return switch (situacion) {
            // [todo remito y documentación igual a la registrada = True]
            case IGUAL_A_LA_REGISTRADA -> new RecibidaYAceptada();
            // [todo remito y documentación igual a la registrada = False]
            case NO_RECIBIDA -> new NoRecibida();
            // [Si la CMDestino no es la correcta]
            case NO_CORRESPONDE_A_LA_CM_DESTINO -> new RecibidaYRechazada();
            // [CMDestino = CMC]
            case PARA_REDIRIGIR -> new ParaRedirigir();
        };
    }
}
