package ppai.entidades.estadoremito;

import ppai.entidades.Estado;

/**
 * Patrón State - ESTADO CONCRETO: En bolsín saliente (CU 19 Generar bolsín).
 *
 * <p>No puede recibirse: el bolsín todavía no fue retirado por el correo.
 */
public class EnBolsinSaliente extends Estado {

    public EnBolsinSaliente() {
        super(AMBITO_REMITO, "EnBolsinSaliente", "En bolsín saliente");
    }
}
