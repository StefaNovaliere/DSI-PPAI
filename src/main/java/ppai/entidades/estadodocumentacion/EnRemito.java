package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: EnRemito. */
public class EnRemito extends EstadoDocumentacion {

    public EnRemito() {
        super("EnRemito");
    }

    /** CU 17 Cancelar Remito: EnRemito -> Registrada. */
    @Override
    public void cancelarRemito(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new Registrada(), fechaHora, responsable);
    }

    /** CU 19 Generar Bolsín: EnRemito -> EnBolsinSaliente. */
    @Override
    public void agregarAlBolsin(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new EnBolsinSaliente(), fechaHora, responsable);
    }
}
