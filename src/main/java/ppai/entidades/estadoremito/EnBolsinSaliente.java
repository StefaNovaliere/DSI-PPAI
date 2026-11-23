package ppai.entidades.estadoremito;

/**
 * Patrón State - ESTADO CONCRETO: En bolsín saliente (CU 19 Generar bolsín).
 *
 * <p>No puede recibirse: el bolsín todavía no fue retirado por el correo.
 */
public class EnBolsinSaliente extends EstadoRemito {

    public EnBolsinSaliente() {
        super("EnBolsinSaliente", "En bolsín saliente");
    }
}
