package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/** Patrón State - ESTADO CONCRETO: EnBolsinEnviado. */
public class EnBolsinEnviado extends EstadoDocumentacion {

    public EnBolsinEnviado() {
        super("EnBolsinEnviado", "En bolsín enviado");
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
