package ppai.entidades.estadoremito;

import java.time.LocalDateTime;
import java.util.List;

import ppai.entidades.Empleado;
import ppai.entidades.OpcionRecepcion;
import ppai.entidades.Remito;

/**
 * Patrón State - rol ESTADO ABSTRACTO de la máquina de estados de Remito.
 *
 * <p>Declara el evento del CU 28, {@link #recibirYAceptar}. Por defecto un
 * remito NO puede recibirse: sólo lo redefine {@link EnBolsinEnviado}. Los
 * eventos de los otros casos de uso (cancelar, agregar o quitar de un bolsín,
 * enviar) no se implementan porque no son parte del CU 28.
 */
public abstract class EstadoRemito {

    private final String nombre;
    private final String descripcion;

    /**
     * @param nombre      nombre del estado en la máquina de estados (se guarda en la base)
     * @param descripcion texto para mostrarle al usuario
     */
    protected EstadoRemito(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * CU 28: el remito se recibe y se actualiza el estado de su documentación.
     * La opción elegida y la documentación indicada por el usuario son las
     * guardas (recibido y aceptado, o parcial) y la situación de cada documento.
     */
    public void recibirYAceptar(Remito remito, LocalDateTime fechaHora, Empleado responsable,
                                OpcionRecepcion opcion, List<Integer> documentacionIndicada) {
        throw new IllegalStateException("No se puede recibir el remito N° " + remito.obtenerNumero()
                + " porque está en estado «" + descripcion + "»");
    }

    @Override
    public String toString() {
        return nombre;
    }
}
