package ppai.entidades.estadodocumentacion;

import java.time.LocalDateTime;

import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;

/**
 * Patrón State - ESTADO CONCRETO: EnBolsinEnviado.
 *
 * <p>Es el estado con más transiciones de salida: la del CU 28 (recibir) y
 * las tres del CU 31 Registrar Revisión de Documentación. En la máquina de
 * estados las del CU 31 son recibir() con distintas condiciones de guarda; en
 * el diseño cada una es un evento, así el estado no necesita un if por guarda.
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

    /** CU 31 [todo remito y documentación igual a la registrada = False]: EnBolsinEnviado -> NoRecibida. */
    @Override
    public void registrarNoRecibida(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new NoRecibida(), fechaHora, responsable);
    }

    /** CU 31 [CMDestino = CMC]: EnBolsinEnviado -> ParaRedirigir. */
    @Override
    public void marcarParaRedirigir(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new ParaRedirigir(), fechaHora, responsable);
    }

    /** CU 31 [si la CMDestino no es la correcta]: EnBolsinEnviado -> Recibida&amp;Rechazada. */
    @Override
    public void rechazar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable) {
        cambiarEstado(documentacion, new RecibidaYRechazada(), fechaHora, responsable);
    }
}
