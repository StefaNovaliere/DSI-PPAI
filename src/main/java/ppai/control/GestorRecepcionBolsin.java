package ppai.control;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ppai.boundary.PantallaRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;
import ppai.entidades.Bolsin;
import ppai.entidades.ComisionMedica;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.persistencia.Repositorio;

/**
 * Controlador del CU 28 Registrar Recepción de Bolsín.
 *
 * <p>Con el patrón State el gestor ya no busca el estado Recibida&amp;Aceptada
 * de la documentación (se eliminó buscarEstadoRecibidaYAceptada() y su loop
 * sobre Estado): cada documentación resuelve su propia transición.
 */
public class GestorRecepcionBolsin {

    private final PantallaRecepcionBolsin pantalla;
    private final Repositorio repositorio;
    private final GestorNotificacionCU29 gestorCU29;

    private Empleado empleadoLogueado;
    private ComisionMedica cmUsuario;
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
        if (cmUsuario == null) {
            pantalla.mostrarMensaje("No se encontró la Comisión Médica del usuario logueado.");
            finCU();
            return;
        }
        pantalla.mostrarCM(cmUsuario.getNombre());

        buscarBolsinesEnviadosCM();
        if (bolsinesEnviados.isEmpty()) {
            pantalla.mostrarMensaje("No hay bolsines enviados pendientes de recepción para " + cmUsuario.getNombre() + ".");
            finCU();
            return;
        }
        pantalla.solicitarSelBolsin(buscarCMOrigenBolsines());
    }

    private void buscarCMUsuarioLogged() {
        String usuario = repositorio.getSesionActual().getUsuario();
        empleadoLogueado = null;
        cmUsuario = null;
        for (Empleado empleado : repositorio.getEmpleados()) {
            if (empleado.esTuUsuario(usuario)) {
                empleadoLogueado = empleado;
                cmUsuario = empleado.getCM();
            }
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
            pantalla.mostrarMensaje("El bolsín N° " + numeroBolsin + " no está entre los bolsines enviados a su CM.");
            pantalla.solicitarSelBolsin(buscarCMOrigenBolsines());
            return;
        }
        for (DatosRemito remito : buscarInformacionRemito()) {
            pantalla.mostrarNroRemito(remito.numero());
            pantalla.mostrarDatosDocumentacion(remito.documentacion());
        }
        pantalla.solicitarSelOpcionesRecBolsin();
    }

    private List<DatosRemito> buscarInformacionRemito() {
        return bolsinSeleccionado.obtenerInformacionRemito();
    }

    /** Primera opción: todo remito y documentación coincide con lo registrado. */
    public void tomarSeleccionPrimerOpcion() {
        pantalla.solicitarConfirmacion();
    }

    /** Hay diferencias con lo registrado: corresponde el CU 31 (fuera de alcance). */
    public void tomarSeleccionSegundaOpcion() {
        pantalla.mostrarMensaje("Existen diferencias con lo registrado: corresponde ejecutar el "
                + "CU 31 Registrar Revisión de Documentación (fuera del alcance de esta implementación).");
        finCU();
    }

    public void tomarConfirmacion(boolean confirma) {
        if (!confirma) {
            pantalla.mostrarMensaje("Operación cancelada. No se registró la recepción.");
            finCU();
            return;
        }
        fechaYHora = getFechaYHoraActual();

        Estado recibidoEnCMDestino = buscarEstadoRecibidoEnCMDestino();
        Estado recibidoYAceptado = buscarEstadoRecibidoYAceptado();

        bolsinSeleccionado.recibirBolsin(fechaYHora, recibidoEnCMDestino, empleadoLogueado);
        // Remito -> DetalleRemito -> Documentacion.recibir() -> estadoActual.recibir(): patrón State
        bolsinSeleccionado.recibirYAceptarRemito(recibidoYAceptado, fechaYHora, empleadoLogueado);

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

    private void llamarCU29() {
        gestorCU29.notificarRecepcion(correoEmpleado, bolsinSeleccionado.getNumeroBolsin(), documentacionRecibida);
    }

    private void finCU() {
        pantalla.finCU();
    }

    public Bolsin getBolsinSeleccionado() {
        return bolsinSeleccionado;
    }
}
