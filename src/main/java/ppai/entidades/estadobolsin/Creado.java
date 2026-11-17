package ppai.entidades.estadobolsin;

import ppai.entidades.Estado;

/**
 * Patrón State - ESTADO CONCRETO: Creado (CU 19 Generar bolsín).
 *
 * <p>No redefine recibir(): un bolsín que todavía no se cerró ni se envió no
 * puede recibirse.
 */
public class Creado extends Estado {

    public Creado() {
        super(AMBITO_BOLSIN, "Creado", "Creado");
    }
}
