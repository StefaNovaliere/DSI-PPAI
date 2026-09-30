package ppai.control;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ppai.boundary.PantallaRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;
import ppai.entidades.Bolsin;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.Usuario;
import ppai.persistencia.Repositorio;

/**
 * Controlador del CU 28 Registrar Recepción de Bolsín.
 *
 * <p>Con el patrón State el gestor no busca ni decide el estado siguiente de
 * la documentación (se eliminó buscarEstadoRecibidaYAceptada() y su loop sobre
 * Estado): le pide a cada documentación que se reciba y el objeto estado de
 * esa documentación resuelve la transición.
 */
public class GestorRecepcionBolsin {

    private final PantallaRecepcionBolsin pantalla;
    private final Repositorio repositorio;
    private final GestorNotificacionCU29 gestorCU29;

    private Empleado empleadoLogueado;
    private String nombreCMUsuario;
    private LocalDateTime fechaYHora;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    private Bolsin bolsinSeleccionado;
    private String correoEmpleado;
    private List<DatosDocumentacion> documentacionRecibida = new ArrayList<>();

    public GestorRecepcionBolsin(PantallaRecepcionBolsin pantalla, Repositorio repositorio,
                                 GestorNotificacionCU29 gestorCU29) {
        this.pantalla = pantalla;
        this.repositorio = repositorio;
        this.gestorCU29 = gestorCU29;
    }

    /** Paso 1: el empleado elige la opción "Registrar recepción de bolsín". */
    public void registrarNuevoRecBolsin() {
        bolsinSeleccionado = null;
        documentacionRecibida = new ArrayList<>();

        buscarCMUsuarioLogged();
        if (nombreCMUsuario == null) {
            pantalla.mostrarMensaje("No se encontró la Comisión Médica del usuario logueado.");
            finCU();
            return;
        }
        pantalla.mostrarCM(nombreCMUsuario);

        buscarBolsinesEnviadosCM();
        if (bolsinesEnviados.isEmpty()) {
            pantalla.mostrarMensaje("No hay bolsines enviados pendientes de recepción para " + nombreCMUsuario + ".");
            finCU();
            return;
        }
        pantalla.solicitarSelBolsin(buscarCMOrigenBolsines());
    }

    private void buscarCMUsuarioLogged() {
        Usuario usuario = repositorio.getSesionActual().getUsuario();
        empleadoLogueado = null;
        nombreCMUsuario = null;
        for (Empleado empleado : repositorio.getEmpleados()) {
            if (empleado.esTuUsuario(usuario)) {
                empleadoLogueado = empleado;
            }
        }
        if (empleadoLogueado != null) {
            nombreCMUsuario = empleadoLogueado.getCM();
        }
    }

    /** Bolsines en estado Enviado cuyo destino es la CM del usuario logueado. */
    private void buscarBolsinesEnviadosCM() {
        bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : repositorio.getBolsines()) {
            if (bolsin.sosEnviado() && empleadoLogueado.esTuCM(bolsin.obtenerCMDestino())) {
                bolsinesEnviados.add(bolsin);
            }
        }
    }

    private List<DatosBolsin> buscarCMOrigenBolsines() {
        List<DatosBolsin> datos = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            datos.add(new DatosBolsin(bolsin.getNumeroBolsin(), bolsin.getNumeroPrecinto(), bolsin.obtenerCMOrigen()));
        }
        return datos;
    }

    /** El empleado selecciona el bolsín recibido. */
    public void tomarSeleccionBolsin(int numeroBolsin) {
        bolsinSeleccionado = null;
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.getNumeroBolsin() == numeroBolsin) {
                bolsinSeleccionado = bolsin;
            }
        }
        if (bolsinSeleccionado == null) {
            pantalla.mostrarMensaje("El bolsín N° " + numeroBolsin + " no está entre los bolsines enviados a su "
                    + "Comisión Médica.");
            pantalla.solicitarSelBolsin(buscarCMOrigenBolsines());
            return;
        }
        List<DatosRemito> remitos = buscarInformacionRemito();
        List<Integer> numerosRemito = new ArrayList<>();
        for (DatosRemito remito : remitos) {
            numerosRemito.add(remito.numero());
        }
        pantalla.mostrarNroRemito(numerosRemito);
        pantalla.mostrarDatosDocumentacion(remitos);
        pantalla.solicitarSelOpcionesRecBolsin();
    }

    private List<DatosRemito> buscarInformacionRemito() {
        return bolsinSeleccionado.obtenerInformacionRemito();
    }

    /** Primera opción: todo remito y documentación coincide con lo registrado. */
    public void tomarSeleccionPrimeraOpcion() {
        pantalla.solicitarConfirmacion();
    }

    /**
     * Segunda opción: hay diferencias con lo registrado. La recepción no se
     * registra aquí sino con el CU 31 Registrar Revisión de Documentación.
     */
    public void tomarSeleccionSegundaOpcion() {
        pantalla.mostrarMensaje("No se registró la recepción del bolsín N° " + bolsinSeleccionado.getNumeroBolsin()
                + " porque hay diferencias con lo registrado. Regístrelas con la opción "
                + "\"Registrar revisión de documentación\" (CU 31).");
        finCU();
    }

    public void tomarConfirmacion(boolean confirma) {
        if (bolsinSeleccionado == null) {
            pantalla.mostrarMensaje("Seleccione primero el bolsín recibido.");
            return;
        }
        if (!confirma) {
            pantalla.mostrarMensaje("No se registró la recepción del bolsín N° "
                    + bolsinSeleccionado.getNumeroBolsin() + ": la operación fue cancelada.");
            finCU();
            return;
        }
        fechaYHora = getFechaYHoraActual();

        Estado recibidoEnCMDestino = buscarEstadoRecibidoEnCMDestino();
        Estado recibidoYAceptado = buscarEstadoRecibidoYAceptado();

        repositorio.iniciarTransaccion();
        try {
            bolsinSeleccionado.recibirBolsin(fechaYHora, recibidoEnCMDestino, empleadoLogueado);
            // Remito -> DetalleRemito -> Documentacion.recibir() -> estadoActual.recibir(): patrón State
            bolsinSeleccionado.recibirYAceptarRemito(recibidoYAceptado, fechaYHora, empleadoLogueado);
            repositorio.actualizar(bolsinSeleccionado);
            repositorio.confirmarTransaccion();
        } catch (RuntimeException e) {
            repositorio.deshacerTransaccion();
            pantalla.mostrarMensaje("No se pudo registrar la recepción del bolsín N° "
                    + bolsinSeleccionado.getNumeroBolsin() + " (" + e.getMessage() + "). No se guardó ningún cambio.");
            finCU();
            return;
        }

        buscarInformacionDocumentacion();
        pantalla.mostrarRecepcionRegistrada(bolsinSeleccionado.getNumeroBolsin(), documentacionRecibida);

        buscarCorreoCM();
        llamarCU29();
        finCU();
    }

    private LocalDateTime getFechaYHoraActual() {
        return LocalDateTime.now().withNano(0);
    }

    private Estado buscarEstadoRecibidoEnCMDestino() {
        for (Estado estado : repositorio.getEstados()) {
            if (estado.esAmbitoBolsin() && estado.esRecibidoEnCMDestino()) {
                return estado;
            }
        }
        throw new IllegalStateException("No existe el estado RecibidoEnCMDestino para Bolsín");
    }

    private Estado buscarEstadoRecibidoYAceptado() {
        for (Estado estado : repositorio.getEstados()) {
            if (estado.esAmbitoRemito() && estado.esRecibidoYAceptado()) {
                return estado;
            }
        }
        throw new IllegalStateException("No existe el estado RecibidoYAceptado para Remito");
    }

    private void buscarInformacionDocumentacion() {
        documentacionRecibida = bolsinSeleccionado.obtenerInformacionDocumentacion();
    }

    private void buscarCorreoCM() {
        correoEmpleado = empleadoLogueado.getEmail();
    }

    /** Include del CU 29: el gestor del CU 28 le pasa el control al gestor del CU 29. */
    private void llamarCU29() {
        gestorCU29.notificarRecepcion(correoEmpleado, bolsinSeleccionado.getNumeroBolsin(), documentacionRecibida);
    }

    private void finCU() {
        pantalla.finCU();
    }
}
