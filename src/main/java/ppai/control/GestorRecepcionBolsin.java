package ppai.control;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ppai.boundary.PantallaRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosRecepcion;
import ppai.dto.DatosRemito;
import ppai.entidades.Bolsin;
import ppai.entidades.Empleado;
import ppai.entidades.OpcionRecepcion;
import ppai.entidades.Usuario;
import ppai.persistencia.Repositorio;

/**
 * Controlador del CU 28 Registrar Recepción de Bolsín.
 *
 * <p>Con el patrón State el gestor no busca ni decide el estado siguiente del
 * bolsín, de los remitos ni de la documentación (no hay loops sobre los
 * estados): le pide a cada objeto que se reciba, y su objeto estado resuelve
 * la transición o la rechaza si no corresponde.
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
    private OpcionRecepcion opcionSeleccionada;
    private List<Integer> documentacionIndicada = new ArrayList<>();
    private String correoCMOrigen;
    private List<DatosRemito> remitosRecibidos = new ArrayList<>();

    public GestorRecepcionBolsin(PantallaRecepcionBolsin pantalla, Repositorio repositorio,
                                 GestorNotificacionCU29 gestorCU29) {
        this.pantalla = pantalla;
        this.repositorio = repositorio;
        this.gestorCU29 = gestorCU29;
    }

    /** Paso 1: el empleado elige la opción "Registrar recepción de bolsín". */
    public void registrarNuevoRecBolsin() {
        bolsinSeleccionado = null;
        limpiarOpcion();
        remitosRecibidos = new ArrayList<>();

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
        pantalla.solicitarSelBolsin(buscarCMOrigenBolsines(bolsinesEnviados));
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

    /** Paso 3: bolsines en estado Enviado cuyo destino es la CM del usuario logueado. */
    private void buscarBolsinesEnviadosCM() {
        bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : repositorio.getBolsines()) {
            if (bolsin.sosEnviado() && empleadoLogueado.esTuCM(bolsin.obtenerCMDestino())) {
                bolsinesEnviados.add(bolsin);
            }
        }
    }

    /** Paso 4: CM origen y número de precinto de cada bolsín. */
    private List<DatosBolsin> buscarCMOrigenBolsines(List<Bolsin> bolsines) {
        List<DatosBolsin> datos = new ArrayList<>();
        for (Bolsin bolsin : bolsines) {
            datos.add(new DatosBolsin(bolsin.getNumeroBolsin(), bolsin.getNumeroPrecinto(), bolsin.obtenerCMOrigen()));
        }
        return datos;
    }

    /**
     * Paso 4: el empleado filtra los bolsines por número de precinto, por CM
     * origen o por los dos. Un dato vacío no filtra. Si ningún bolsín coincide
     * (flujos A1 y A2) se informa y se vuelven a mostrar todos.
     */
    public void tomarFiltroBolsines(String precinto, String cmOrigen) {
        String filtroPrecinto = precinto == null ? "" : precinto.trim();
        String filtroCMOrigen = cmOrigen == null ? "" : cmOrigen.trim();
        bolsinSeleccionado = null;
        limpiarOpcion();

        List<Bolsin> encontrados = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            if ((filtroPrecinto.isEmpty() || bolsin.tenesPrecinto(filtroPrecinto))
                    && (filtroCMOrigen.isEmpty() || bolsin.sosDeCMOrigen(filtroCMOrigen))) {
                encontrados.add(bolsin);
            }
        }
        if (encontrados.isEmpty()) {
            pantalla.mostrarMensaje(armarMensajeSinBolsines(filtroPrecinto, filtroCMOrigen));
            pantalla.solicitarSelBolsin(buscarCMOrigenBolsines(bolsinesEnviados));
            return;
        }
        pantalla.solicitarSelBolsin(buscarCMOrigenBolsines(encontrados));
    }

    /** Flujos A1 (precinto) y A2 (CM origen). */
    private String armarMensajeSinBolsines(String precinto, String cmOrigen) {
        String criterio;
        if (!precinto.isEmpty() && !cmOrigen.isEmpty()) {
            criterio = "con el número de precinto «" + precinto + "» desde la CM de origen «" + cmOrigen + "»";
        } else if (!precinto.isEmpty()) {
            criterio = "con el número de precinto «" + precinto + "»";
        } else {
            criterio = "desde la CM de origen «" + cmOrigen + "»";
        }
        return "No se encontró ningún bolsín enviado a " + nombreCMUsuario + " " + criterio
                + ". Se muestran todos los bolsines pendientes.";
    }

    /** Paso 5: el empleado selecciona el bolsín recibido. */
    public void tomarSeleccionBolsin(int numeroBolsin) {
        bolsinSeleccionado = null;
        limpiarOpcion();
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.getNumeroBolsin() == numeroBolsin) {
                bolsinSeleccionado = bolsin;
            }
        }
        if (bolsinSeleccionado == null) {
            pantalla.mostrarMensaje("El bolsín N° " + numeroBolsin + " no está entre los bolsines enviados a su "
                    + "Comisión Médica.");
            pantalla.solicitarSelBolsin(buscarCMOrigenBolsines(bolsinesEnviados));
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

    /** Paso 8: primera opción, el contenido del bolsín es igual al registrado. */
    public void tomarSeleccionPrimeraOpcion() {
        if (!hayBolsinSeleccionado()) {
            return;
        }
        opcionSeleccionada = OpcionRecepcion.CONTENIDO_IGUAL;
        documentacionIndicada = new ArrayList<>();
        pantalla.solicitarConfirmacion();
    }

    /** Flujo A3: segunda opción, no se recibe toda la documentación. */
    public void tomarSeleccionSegundaOpcion() {
        tomarOpcionConDocumentacion(OpcionRecepcion.NO_SE_RECIBE_TODA,
                "Indique la documentación que no se recibió.");
    }

    /** Flujo A4: tercera opción, existe documentación que no corresponde al destino. */
    public void tomarSeleccionTerceraOpcion() {
        tomarOpcionConDocumentacion(OpcionRecepcion.NO_CORRESPONDE,
                "Indique la documentación que no corresponde a " + nombreCMUsuario + ".");
    }

    /** Flujo A5: cuarta opción, la documentación se debe redirigir a otra área (sólo en la CMC). */
    public void tomarSeleccionCuartaOpcion() {
        if (!hayBolsinSeleccionado()) {
            return;
        }
        if (!bolsinSeleccionado.esDestinoCMC()) {
            pantalla.mostrarMensaje("La opción «" + OpcionRecepcion.REDIRIGIR.getDescripcion() + "» corresponde "
                    + "sólo cuando la CM destino es la Comisión Médica Central.");
            pantalla.solicitarSelOpcionesRecBolsin();
            return;
        }
        tomarOpcionConDocumentacion(OpcionRecepcion.REDIRIGIR,
                "Indique la documentación que se debe redirigir a otra área.");
    }

    private void tomarOpcionConDocumentacion(OpcionRecepcion opcion, String indicacion) {
        if (!hayBolsinSeleccionado()) {
            return;
        }
        opcionSeleccionada = opcion;
        documentacionIndicada = new ArrayList<>();
        pantalla.solicitarSelDocumentacion(indicacion);
    }

    /** Flujos A3 a A5: el empleado indica la documentación (por número). */
    public void tomarSeleccionDocumentacion(List<Integer> numerosDocumentacion) {
        if (!hayBolsinSeleccionado()) {
            return;
        }
        if (opcionSeleccionada == null || opcionSeleccionada == OpcionRecepcion.CONTENIDO_IGUAL) {
            pantalla.mostrarMensaje("Seleccione primero la opción de recepción.");
            pantalla.solicitarSelOpcionesRecBolsin();
            return;
        }
        if (numerosDocumentacion.isEmpty()) {
            pantalla.mostrarMensaje("No se indicó ninguna documentación para la opción «"
                    + opcionSeleccionada.getDescripcion() + "».");
            pantalla.solicitarSelOpcionesRecBolsin();
            return;
        }
        for (Integer numero : numerosDocumentacion) {
            if (!bolsinSeleccionado.contenesDocumentacion(numero)) {
                pantalla.mostrarMensaje("La documentación N° " + numero + " no está en el bolsín N° "
                        + bolsinSeleccionado.getNumeroBolsin() + ".");
                pantalla.solicitarSelOpcionesRecBolsin();
                return;
            }
        }
        documentacionIndicada = new ArrayList<>(numerosDocumentacion);
        pantalla.solicitarConfirmacion();
    }

    /** Pasos 10 y 11 (y flujo A6 si el empleado no confirma). */
    public void tomarConfirmacion(boolean confirma) {
        if (!hayBolsinSeleccionado()) {
            return;
        }
        if (opcionSeleccionada == null) {
            pantalla.mostrarMensaje("Seleccione primero la opción de recepción.");
            pantalla.solicitarSelOpcionesRecBolsin();
            return;
        }
        if (!confirma) {
            // Flujo A6: se vuelve a las opciones de recepción (paso 7)
            pantalla.mostrarMensaje("No se registró la recepción del bolsín N° "
                    + bolsinSeleccionado.getNumeroBolsin() + " porque no se confirmó. Puede revisar la opción "
                    + "elegida y volver a registrarla.");
            limpiarOpcion();
            pantalla.solicitarSelOpcionesRecBolsin();
            return;
        }
        fechaYHora = getFechaYHoraActual();

        repositorio.iniciarTransaccion();
        try {
            // Cada objeto delega la transición en su estado actual: patrón State
            bolsinSeleccionado.recibir(fechaYHora, empleadoLogueado);
            bolsinSeleccionado.recibirYAceptarRemito(fechaYHora, empleadoLogueado, opcionSeleccionada,
                    documentacionIndicada);
            repositorio.actualizar(bolsinSeleccionado);
            repositorio.confirmarTransaccion();
        } catch (RuntimeException e) {
            repositorio.deshacerTransaccion();
            pantalla.mostrarMensaje("No se pudo registrar la recepción del bolsín N° "
                    + bolsinSeleccionado.getNumeroBolsin() + ": " + e.getMessage() + ". No se guardó ningún cambio.");
            finCU();
            return;
        }

        buscarInformacionDocumentacion();
        pantalla.mostrarRecepcionRegistrada(bolsinSeleccionado.getNumeroBolsin(), remitosRecibidos);

        buscarCorreoCM();
        llamarCU29();
        finCU();
    }

    /** Observación 1: el empleado cancela el caso de uso cuando lo decide. */
    public void cancelarCU() {
        pantalla.mostrarMensaje("Se canceló el registro de la recepción. No se guardó ningún cambio.");
        finCU();
    }

    private boolean hayBolsinSeleccionado() {
        if (bolsinSeleccionado == null) {
            pantalla.mostrarMensaje("Seleccione primero el bolsín recibido.");
            return false;
        }
        return true;
    }

    private void limpiarOpcion() {
        opcionSeleccionada = null;
        documentacionIndicada = new ArrayList<>();
    }

    private LocalDateTime getFechaYHoraActual() {
        return LocalDateTime.now().withNano(0);
    }

    /** Estado final de cada remito y de su documentación. */
    private void buscarInformacionDocumentacion() {
        remitosRecibidos = bolsinSeleccionado.obtenerInformacionRemito();
    }

    /** CU 29: la notificación va a la CM origen del bolsín. */
    private void buscarCorreoCM() {
        correoCMOrigen = bolsinSeleccionado.obtenerCorreoCMOrigen();
    }

    /**
     * Paso 12, include del CU 29: el gestor del CU 28 le pasa el control al
     * gestor del CU 29, que le envía el correo a la CM origen con la situación
     * de la documentación recibida.
     */
    private void llamarCU29() {
        DatosRecepcion recepcion = new DatosRecepcion(bolsinSeleccionado.getNumeroBolsin(),
                bolsinSeleccionado.getNumeroPrecinto(), bolsinSeleccionado.obtenerCMOrigen(), nombreCMUsuario,
                fechaYHora, empleadoLogueado.getNombreCompleto(), opcionSeleccionada.getDescripcion(),
                remitosRecibidos);
        gestorCU29.notificarRecepcion(correoCMOrigen, recepcion);
    }

    private void finCU() {
        pantalla.finCU();
    }
}
