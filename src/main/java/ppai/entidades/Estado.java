package ppai.entidades;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Patrón State - rol ESTADO ABSTRACTO.
 *
 * <p>Es la clase de la que heredan los estados de las tres máquinas de estado
 * del CU 28: Bolsín, Remito y Documentación. El ámbito dice a qué máquina de
 * estados pertenece cada estado.
 *
 * <p>Declara los eventos de las máquinas de estado y, por defecto, los rechaza:
 * sólo los redefinen los estados concretos desde los que la máquina de estados
 * tiene esa transición. Los eventos de los otros casos de uso (modificar,
 * cerrar, enviar, etc.) están declarados, pero ningún estado los redefine
 * porque no son parte del CU 28.
 */
public abstract class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";
    public static final String AMBITO_DOCUMENTACION = "Documentacion";

    private final String ambito;
    private final String descripcion;
    private final String nombre;

    /**
     * @param ambito      máquina de estados a la que pertenece el estado
     * @param nombre      nombre del estado en la máquina de estados (se guarda en la base)
     * @param descripcion texto para mostrarle al usuario
     */
    protected Estado(String ambito, String nombre, String descripcion) {
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equals(ambito);
    }

    public boolean esAmbitoDocumentacion() {
        return AMBITO_DOCUMENTACION.equals(ambito);
    }

    public boolean esAmbitoRemito() {
        return AMBITO_REMITO.equals(ambito);
    }

    /** CU 28, paso 3: ¿el bolsín está en estado Enviado? */
    public boolean esEnviado() {
        return false;
    }

    // Eventos del CU 28

    /** Bolsín: Enviado -&gt; Recibido en CM destino. */
    public void recibir(Bolsin bolsin, LocalDateTime fechaHora, Empleado responsable) {
        throw new IllegalStateException("No se puede recibir el bolsín N° " + bolsin.getNumeroBolsin()
                + " porque está en estado «" + descripcion + "»");
    }

    /** Documentación: la situación de la documentación es la guarda de la transición. */
    public void recibir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable,
                        SituacionDocumentacion situacion) {
        throw new IllegalStateException("No se puede recibir la documentación N° " + documentacion.getNumero()
                + " como «" + situacion.getDescripcion() + "» porque está en estado «" + descripcion + "»");
    }

    /**
     * Remito: se recibe y se actualiza el estado de su documentación. La opción
     * elegida y la documentación indicada por el usuario son las guardas.
     */
    public void recibirYAceptar(Remito remito, LocalDateTime fechaHora, Empleado responsable,
                                OpcionRecepcion opcion, List<Integer> documentacionIndicada) {
        throw new IllegalStateException("No se puede recibir el remito N° " + remito.obtenerNumero()
                + " porque está en estado «" + descripcion + "»");
    }

    // Eventos de los otros casos de uso: ningún estado los redefine porque no son parte del CU 28

    public void modificar() {
        throw eventoNoPermitido("modificar");
    }

    public void darDeBaja() {
        throw eventoNoPermitido("dar de baja");
    }

    public void cerrar() {
        throw eventoNoPermitido("cerrar");
    }

    public void reabrir() {
        throw eventoNoPermitido("reabrir");
    }

    public void enviar() {
        throw eventoNoPermitido("enviar");
    }

    public void remitar() {
        throw eventoNoPermitido("remitar");
    }

    public void cancelarRemito() {
        throw eventoNoPermitido("cancelar el remito");
    }

    public void agregarAlBolsin() {
        throw eventoNoPermitido("agregar al bolsín");
    }

    public void registrar() {
        throw eventoNoPermitido("registrar");
    }

    private IllegalStateException eventoNoPermitido(String evento) {
        return new IllegalStateException("No se puede " + evento + " en el estado «" + descripcion + "»");
    }

    @Override
    public String toString() {
        return nombre;
    }
}
