package ppai.entidades;

/**
 * Situación de una documentación al recibir el bolsín (CU 28). Son las
 * guardas de las transiciones del evento recibir() en la máquina de estados de
 * Documentación (Entrega 1): el estado actual de la documentación decide, con
 * esta situación, a qué estado pasa.
 */
public enum SituacionDocumentacion {

    /** [todo remito y documentación igual a la registrada = True] · [documentación correcta = True]. */
    IGUAL_A_LA_REGISTRADA("igual a la registrada"),
    /** [todo remito y documentación igual a la registrada = False]: la documentación no llegó. */
    NO_RECIBIDA("no recibida"),
    /** [la CM destino no es la correcta] · [documentación correcta = False]. */
    NO_CORRESPONDE_A_LA_CM_DESTINO("que no corresponde a la CM destino"),
    /** [CMDestino = CMC]: la documentación va a otra área de la CMC. */
    PARA_REDIRIGIR("para redirigir");

    private final String descripcion;

    SituacionDocumentacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
