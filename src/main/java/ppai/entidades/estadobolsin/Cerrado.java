package ppai.entidades.estadobolsin;

/**
 * Patrón State - ESTADO CONCRETO: Cerrado (CU 39 Cerrar bolsín).
 *
 * <p>No redefine recibir(): el bolsín cerrado todavía no fue retirado por el correo.
 */
public class Cerrado extends EstadoBolsin {

    public Cerrado() {
        super("Cerrado", "Cerrado");
    }
}
