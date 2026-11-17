package ppai.entidades.estadoremito;

import java.time.LocalDateTime;
import java.util.List;

import ppai.entidades.DetalleRemito;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.OpcionRecepcion;
import ppai.entidades.Remito;

/** Patrón State - ESTADO CONCRETO: En bolsín enviado (CU 27 Registrar el retiro de bolsines). */
public class EnBolsinEnviado extends Estado {

    public EnBolsinEnviado() {
        super(AMBITO_REMITO, "EnBolsinEnviado", "En bolsín enviado");
    }

    /**
     * CU 28: el remito pasa a Recibido y aceptado (o parcial) y después se
     * actualiza el estado de la documentación de cada detalle del remito.
     */
    @Override
    public void recibirYAceptar(Remito remito, LocalDateTime fechaHora, Empleado responsable,
                                OpcionRecepcion opcion, List<Integer> documentacionIndicada) {
        Estado proximoEstado = crearEstado(remito, opcion, documentacionIndicada);
        remito.setEstado(proximoEstado);
        actualizarEstadoDocumentacion(remito.getDetallesRemito(), fechaHora, responsable, opcion,
                documentacionIndicada);
    }

    /**
     * Guardas de la máquina de estados: [toda la documentación del remito se
     * recibió y corresponde] -&gt; Recibido y aceptado; [no se recibió o no
     * corresponde parte de su documentación (opciones 2 y 3)] -&gt; Recibido y
     * aceptado parcial.
     */
    private Estado crearEstado(Remito remito, OpcionRecepcion opcion, List<Integer> documentacionIndicada) {
        if (opcion.dejaRemitoParcial() && remito.tenesDocumentacionIndicada(documentacionIndicada)) {
            return new RecibidoYAceptadoParcial();
        }
        return new RemitoRecibidoYAceptado();
    }

    private void actualizarEstadoDocumentacion(List<DetalleRemito> detallesRemito, LocalDateTime fechaHora,
                                               Empleado responsable, OpcionRecepcion opcion,
                                               List<Integer> documentacionIndicada) {
        for (DetalleRemito detalle : detallesRemito) {
            detalle.recibirYAceptarDoc(fechaHora, responsable, opcion, documentacionIndicada);
        }
    }
}
