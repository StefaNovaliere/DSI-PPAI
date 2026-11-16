package ppai.entidades.estadoremito;

import java.time.LocalDateTime;
import java.util.List;

import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.OpcionRecepcion;
import ppai.entidades.Remito;

/**
 * Patrón State - rol ESTADO ABSTRACTO de la máquina de estados de Remito.
 * Hereda de {@link Estado} el nombre y la descripción.
 *
 * <p>Declara el evento del CU 28, {@link #recibirYAceptar}. Por defecto un
 * remito NO puede recibirse: sólo lo redefine {@link EnBolsinEnviado}. Los
 * eventos de los otros casos de uso (cancelar, agregar o quitar de un bolsín,
 * enviar) no se implementan porque no son parte del CU 28.
 */
public abstract class EstadoRemito extends Estado {

    protected EstadoRemito(String nombre, String descripcion) {
        super(nombre, descripcion);
    }

    /**
     * CU 28: el remito se recibe y se actualiza el estado de su documentación.
     * La opción elegida y la documentación indicada por el usuario son las
     * guardas (recibido y aceptado, o parcial) y la situación de cada documento.
     */
    public void recibirYAceptar(Remito remito, LocalDateTime fechaHora, Empleado responsable,
                                OpcionRecepcion opcion, List<Integer> documentacionIndicada) {
        throw new IllegalStateException("No se puede recibir el remito N° " + remito.obtenerNumero()
                + " porque está en estado «" + getDescripcion() + "»");
    }
}
