package ppai.entidades.estadobolsin;

import ppai.entidades.Estado;

/**
 * Patrón State - ESTADO CONCRETO: Cerrado (CU 39 Cerrar bolsín).
 *
 * <p>No redefine recibir(): el bolsín cerrado todavía no fue retirado por el correo.
 */
public class Cerrado extends Estado {

    public Cerrado() {
        super(AMBITO_BOLSIN, "Cerrado", "Cerrado");
    }
}
