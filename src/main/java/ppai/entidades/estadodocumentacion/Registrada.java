package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: Registrada. */
public class Registrada extends EstadoDocumentacion {

    public Registrada() {
        super("Registrada");
    }

    /** CU 15 Generar Remito: Registrada -> EnRemito. */
    @Override
    public void remitar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new EnRemito(), fechaHora, responsable);
    }
}
