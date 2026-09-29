package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: NoRecibida. */
public class NoRecibida extends EstadoDocumentacion {

    public NoRecibida() {
        super("NoRecibida");
    }

    /** CU 7 Registrar Documentación: NoRecibida -> Registrada. */
    @Override
    public void registrar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new Registrada(), fechaHora, responsable);
    }

    /** CU 9 Eliminar Documentación: NoRecibida -> DeBaja. */
    @Override
    public void darDeBaja(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new DeBaja(), fechaHora, responsable);
    }
}
