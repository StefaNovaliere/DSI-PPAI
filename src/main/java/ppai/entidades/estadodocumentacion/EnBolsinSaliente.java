package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: EnBolsinSaliente. */
public class EnBolsinSaliente extends EstadoDocumentacion {

    public EnBolsinSaliente() {
        super("EnBolsinSaliente");
    }

    /** CU 20 Modificar Bolsín (se elimina o actualiza el bolsín): EnBolsinSaliente -> EnRemito. */
    @Override
    public void remitar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new EnRemito(), fechaHora, responsable);
    }

    /** CU 27 Registrar el retiro de bolsines: EnBolsinSaliente -> EnBolsinEnviado. */
    @Override
    public void enviar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new EnBolsinEnviado(), fechaHora, responsable);
    }
}
