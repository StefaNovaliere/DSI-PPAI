package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

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
    public void recibir(Documentacion documentacion, SituacionDocumentacion situacion, LocalDateTime fechaHora,
                        Empleado responsable) {
        EstadoDocumentacion proximoEstado = switch (situacion) {
            // [todo remito y documentación igual a la registrada = True] / recibir()
            case IGUAL_A_LA_REGISTRADA -> new RecibidaYAceptada();
            // [todo remito y documentación igual a la registrada = False] / recibir()
            case NO_RECIBIDA -> new NoRecibida();
            // [Si la CMDestino no es la correcta] / recibir()
            case NO_CORRESPONDE_A_LA_CM_DESTINO -> new RecibidaYRechazada();
            // [CMDestino = CMC] / recibir()
            case PARA_REDIRIGIR -> new ParaRedirigir();
        };
        cambiarEstado(documentacion, proximoEstado, fechaHora, responsable);
    }
}
