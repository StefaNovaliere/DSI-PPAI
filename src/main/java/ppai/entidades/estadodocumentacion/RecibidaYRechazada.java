package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: Recibida&amp;Rechazada. */
public class RecibidaYRechazada extends EstadoDocumentacion {

    public RecibidaYRechazada() {
        super("Recibida&Rechazada");
    }

    /** CU 7 Registrar Documentación: Recibida&amp;Rechazada -> Registrada. */
    @Override
    public void registrar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new Registrada(), fechaHora, responsable);
    }

    /** CU 9 Eliminar Documentación: Recibida&amp;Rechazada -> DeBaja. */
    @Override
    public void darDeBaja(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new DeBaja(), fechaHora, responsable);
    }
}
