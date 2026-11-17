package ppai.entidades.estadodocumentacion;

import ppai.entidades.Estado;

/**
 * Patrón State - ESTADO CONCRETO: Recibida&amp;Aceptada. Es el estado al que
 * llega la documentación en el CU 28; no puede volver a recibirse.
 */
public class RecibidaYAceptada extends Estado {

    public RecibidaYAceptada() {
        super(AMBITO_DOCUMENTACION, "Recibida&Aceptada", "Recibida y aceptada");
    }
}
