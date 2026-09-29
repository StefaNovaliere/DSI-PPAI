package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/**
 * Patrón State - ESTADO CONCRETO: EnBolsinEnviado.
 *
 * <p>Las transiciones de CU 31 (Registrar Revisión de Documentación) hacia
 * NoRecibida, ParaRedirigir y Recibida&amp;Rechazada quedan fuera del alcance
 * del CU 28; se agregarían redefiniendo el evento correspondiente aquí.
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    public EnBolsinEnviado() {
        super("EnBolsinEnviado");
    }

    /**
     * CU 28 Registrar Recepción de Bolsín
     * [todo remito y documentación igual a la registrada = True]:
     * EnBolsinEnviado -> Recibida&amp;Aceptada.
     */
    @Override
    public void recibir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new RecibidaYAceptada(), fechaHora, responsable);
    }
}
