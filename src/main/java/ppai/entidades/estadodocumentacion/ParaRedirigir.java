package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: ParaRedirigir. */
public class ParaRedirigir extends EstadoDocumentacion {

    public ParaRedirigir() {
        super("ParaRedirigir");
    }

    /**
     * CU 28 Registrar Recepción de Bolsín [documentación correcta = True]:
     * ParaRedirigir -> Recibida&amp;Aceptada.
     */
    @Override
    public void recibir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new RecibidaYAceptada(), fechaHora, responsable);
    }
}
