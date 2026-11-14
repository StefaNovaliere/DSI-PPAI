package ppai.entidades.estadobolsin;

/**
 * Patrón State - ESTADO CONCRETO: Creado (CU 19 Generar bolsín).
 *
 * <p>No redefine recibir(): un bolsín que todavía no se cerró ni se envió no
 * puede recibirse.
 */
public class Creado extends EstadoBolsin {

    public Creado() {
        super("Creado", "Creado");
    }
}
