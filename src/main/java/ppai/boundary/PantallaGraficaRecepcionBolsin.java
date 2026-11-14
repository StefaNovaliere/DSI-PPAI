package ppai.boundary;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;

/**
 * Pantalla del CU 28 Registrar Recepción de Bolsín (Swing), con un diseño
 * maestro-detalle: a la izquierda los bolsines por recibir (con un filtro) y a
 * la derecha el contenido del bolsín seleccionado.
 *
 * <p>Cada acción del usuario se traduce en el mensaje del diagrama de
 * secuencia hacia el gestor:
 * <ul>
 *   <li>elegir la opción del menú → {@code registrarNuevoRecBolsin()}</li>
 *   <li>botón "Buscar" del filtro → {@code tomarFiltroBolsines()}</li>
 *   <li>hacer clic en un bolsín de la lista → {@code tomarSeleccionBolsin()}</li>
 *   <li>elegir una de las cuatro opciones de recepción y presionar "Registrar recepción" →
 *       {@code tomarSeleccionPrimeraOpcion()} ... {@code tomarSeleccionCuartaOpcion()}</li>
 *   <li>documentación marcada en la tabla (opciones 2 a 4) → {@code tomarSeleccionDocumentacion()}</li>
 *   <li>diálogo de confirmación → {@code tomarConfirmacion()}</li>
 * </ul>
 *
 * <p>Cuando el caso de uso termina, la pantalla vuelve a cargar la lista de
 * bolsines pendientes (una nueva ejecución del CU), así el usuario puede
 * seguir con el próximo bolsín sin volver al menú.
 */
public class PantallaGraficaRecepcionBolsin extends JPanel implements PantallaRecepcionBolsin {

    private static final String LISTA = "lista";
    private static final String VACIO = "vacio";
    private static final String DETALLE = "detalle";

    private static final int COL_MARCA = 0;
    private static final int COL_DOC = 2;
    private static final int COL_ESTADO = 5;

    /** Las cuatro opciones del paso 7, con lo que se le pide marcar al usuario. */
    private enum Opcion {
        PRIMERA("1. El contenido del bolsín es igual al registrado", "",
                "Todos los remitos y documentos se registrarán como recibidos y aceptados."),
        SEGUNDA("2. No se recibe toda la documentación", "No llegó",
                "Marque en la tabla la documentación que no llegó."),
        TERCERA("3. Hay documentación que no corresponde a esta CM", "No corresponde",
                "Marque en la tabla la documentación que no corresponde a esta Comisión Médica."),
        CUARTA("4. La documentación se debe redirigir a otra área", "Redirigir",
                "Marque en la tabla la documentación que se debe redirigir a otra área de la CMC.");

        private final String texto;
        private final String columna;
        private final String indicacion;

        Opcion(String texto, String columna, String indicacion) {
            this.texto = texto;
            this.columna = columna;
            this.indicacion = indicacion;
        }
    }

    private GestorRecepcionBolsin gestor;
    private Runnable accionVolver = () -> { };

    // Encabezado y aviso de resultado
    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JPanel panelAviso = new JPanel(new BorderLayout(10, 0));
    private final JLabel lblAviso = new JLabel();
    private final List<String> lineasAviso = new ArrayList<>();

    // Maestro: filtro y bolsines por recibir
    private final JTextField txtPrecinto = new JTextField();
    private final JTextField txtCMOrigen = new JTextField();
    private final DefaultListModel<DatosBolsin> modeloBolsines = new DefaultListModel<>();
    private final JList<DatosBolsin> listaBolsines = new JList<>(modeloBolsines);
    private final JLabel lblBolsines = new JLabel("Bolsines por recibir");
    private final CardLayout tarjetasLista = new CardLayout();
    private final JPanel panelLista = new JPanel(tarjetasLista);

    // Detalle: contenido del bolsín seleccionado
    private final CardLayout tarjetasDetalle = new CardLayout();
    private final JPanel panelDetalle = new JPanel(tarjetasDetalle);
    private final JLabel lblDetalle = new JLabel(" ");
    private final JLabel lblRemitos = new JLabel(" ");
    private final DefaultTableModel modeloDocumentos = new DefaultTableModel(
            new Object[] {"", "Remito", "N° Doc", "Tipo", "Asunto", "Estado"}, 0) {
        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == COL_MARCA ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == COL_MARCA && marcadoHabilitado;
        }
    };
    private final JTable tablaDocumentos = new JTable(modeloDocumentos);

    // Opciones de recepción
    private final Map<Opcion, JRadioButton> botonesOpcion = new LinkedHashMap<>();
    private final JLabel lblIndicacion = new JLabel(" ");
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnRegistrar = new JButton("Registrar recepción");

    // Datos de la ejecución actual del caso de uso
    private final Map<Integer, DatosDocumentacion> documentos = new LinkedHashMap<>();
    private int cantidadRemitos;
    private DatosBolsin bolsinEnPantalla;
    private boolean marcadoHabilitado;
    private boolean recepcionRegistrada;
    private boolean listaMostrada;
    private boolean llenandoLista;
    private boolean seleccionAutomatica;
    private boolean reiniciando;
    private boolean filtrando;
    private boolean filtroSinResultados;
    private Integer bolsinAReseleccionar;

    public PantallaGraficaRecepcionBolsin() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel norte = new JPanel(new BorderLayout(0, 8));
        norte.add(crearEncabezado(), BorderLayout.NORTH);
        norte.add(crearAviso(), BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crearMaestro(), crearDetalle());
        centro.setDividerLocation(290);
        centro.setBorder(null);
        add(centro, BorderLayout.CENTER);
        add(crearBarraAcciones(), BorderLayout.SOUTH);

        habilitarAcciones(false);
    }

    // ------------------------------------------------------------------
    // Construcción de la pantalla
    // ------------------------------------------------------------------

    private JPanel crearEncabezado() {
        JButton btnVolver = new JButton("← Menú");
        btnVolver.addActionListener(e -> accionVolver.run());
        JLabel titulo = new JLabel("Registrar recepción de bolsín");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        lblSubtitulo.setForeground(new Color(0x555555));

        JPanel textos = new JPanel(new BorderLayout());
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(lblSubtitulo, BorderLayout.SOUTH);
        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.add(btnVolver, BorderLayout.WEST);
        encabezado.add(textos, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel crearAviso() {
        lblAviso.setVerticalAlignment(SwingConstants.TOP);
        panelAviso.add(lblAviso, BorderLayout.CENTER);
        panelAviso.setVisible(false);
        return panelAviso;
    }

    private JPanel crearMaestro() {
        listaBolsines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaBolsines.setCellRenderer(new RenderizadorBolsin());
        listaBolsines.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !llenandoLista && listaBolsines.getSelectedValue() != null) {
                tomarSeleccionBolsin(listaBolsines.getSelectedValue());
            }
        });
        JLabel sinBolsines = new JLabel("<html><div style='text-align:center'>No hay bolsines pendientes"
                + "<br>de recepción.</div></html>", SwingConstants.CENTER);
        sinBolsines.setForeground(new Color(0x777777));
        JScrollPane scrollLista = new JScrollPane(listaBolsines);
        scrollLista.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLista.add(scrollLista, LISTA);
        panelLista.add(sinBolsines, VACIO);

        lblBolsines.setFont(lblBolsines.getFont().deriveFont(Font.BOLD));
        JPanel arriba = new JPanel(new BorderLayout(0, 6));
        arriba.add(lblBolsines, BorderLayout.NORTH);
        arriba.add(crearFiltro(), BorderLayout.CENTER);

        JPanel maestro = new JPanel(new BorderLayout(0, 6));
        maestro.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        maestro.add(arriba, BorderLayout.NORTH);
        maestro.add(panelLista, BorderLayout.CENTER);
        return maestro;
    }

    /** Paso 4: filtrar por número de precinto o por CM origen. */
    private JPanel crearFiltro() {
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        btnBuscar.addActionListener(e -> tomarFiltroBolsines());
        btnLimpiar.addActionListener(e -> {
            limpiarFiltro();
            tomarFiltroBolsines();
        });
        txtPrecinto.addActionListener(e -> tomarFiltroBolsines());
        txtCMOrigen.addActionListener(e -> tomarFiltroBolsines());
        txtPrecinto.setToolTipText("Número de precinto completo, por ejemplo PR-0101");
        txtCMOrigen.setToolTipText("Nombre o parte del nombre de la CM origen, por ejemplo Rosario");

        JPanel filtro = new JPanel(new GridBagLayout());
        filtro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Filtrar"), BorderFactory.createEmptyBorder(2, 4, 4, 4)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        filtro.add(new JLabel("Precinto"), c);
        c.gridy = 1;
        filtro.add(new JLabel("CM origen"), c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        filtro.add(txtPrecinto, c);
        c.gridy = 1;
        filtro.add(txtCMOrigen, c);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnLimpiar);
        botones.add(btnBuscar);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        filtro.add(botones, c);
        return filtro;
    }

    private JPanel crearDetalle() {
        JLabel indicacion = new JLabel("Seleccione un bolsín de la lista para ver sus remitos y documentos.",
                SwingConstants.CENTER);
        indicacion.setForeground(new Color(0x777777));

        tablaDocumentos.setRowHeight(24);
        tablaDocumentos.setRowSelectionAllowed(false);
        tablaDocumentos.getTableHeader().setReorderingAllowed(false);
        int[] anchos = {95, 55, 55, 140, 240, 140};
        for (int i = 0; i < anchos.length; i++) {
            tablaDocumentos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tablaDocumentos.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new RenderizadorEstado());
        tablaDocumentos.getColumnModel().getColumn(COL_MARCA)
                .setCellRenderer(new RenderizadorMarca(tablaDocumentos.getDefaultRenderer(Boolean.class)));
        lblDetalle.setFont(lblDetalle.getFont().deriveFont(Font.BOLD));
        lblRemitos.setForeground(new Color(0x444444));

        JPanel titulos = new JPanel(new BorderLayout(0, 2));
        titulos.add(lblDetalle, BorderLayout.NORTH);
        titulos.add(lblRemitos, BorderLayout.SOUTH);
        JPanel contenidoBolsin = new JPanel(new BorderLayout(0, 6));
        contenidoBolsin.add(titulos, BorderLayout.NORTH);
        contenidoBolsin.add(new JScrollPane(tablaDocumentos), BorderLayout.CENTER);

        panelDetalle.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        panelDetalle.add(indicacion, VACIO);
        panelDetalle.add(contenidoBolsin, DETALLE);
        return panelDetalle;
    }

    /** Paso 7: las cuatro opciones de recepción y el botón para registrarla. */
    private JPanel crearBarraAcciones() {
        JPanel opciones = new JPanel(new GridLayout(2, 2, 12, 2));
        ButtonGroup grupo = new ButtonGroup();
        for (Opcion opcion : Opcion.values()) {
            JRadioButton boton = new JRadioButton(opcion.texto);
            boton.addActionListener(e -> elegirOpcion(opcion));
            grupo.add(boton);
            opciones.add(boton);
            botonesOpcion.put(opcion, boton);
        }
        JLabel titulo = new JLabel("Opción de recepción");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD));
        lblIndicacion.setForeground(new Color(0x1F3A5F));

        JPanel izquierda = new JPanel(new BorderLayout(0, 4));
        izquierda.add(titulo, BorderLayout.NORTH);
        izquierda.add(opciones, BorderLayout.CENTER);
        izquierda.add(lblIndicacion, BorderLayout.SOUTH);

        btnRegistrar.setFont(btnRegistrar.getFont().deriveFont(Font.BOLD));
        btnRegistrar.addActionListener(e -> tomarSeleccionOpcion());
        lblResumen.setForeground(new Color(0x555555));
        lblResumen.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel derecha = new JPanel(new BorderLayout(0, 6));
        derecha.add(lblResumen, BorderLayout.NORTH);
        derecha.add(btnRegistrar, BorderLayout.SOUTH);

        JPanel barra = new JPanel(new BorderLayout(16, 0));
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)));
        barra.add(izquierda, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);
        elegirOpcion(Opcion.PRIMERA);
        return barra;
    }

    // ------------------------------------------------------------------
    // Acciones del usuario -> gestor
    // ------------------------------------------------------------------

    @Override
    public void setGestor(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    public void setAccionVolver(Runnable accionVolver) {
        this.accionVolver = accionVolver;
    }

    /** El usuario eligió la opción del menú: comienza el caso de uso. */
    @Override
    public void opcRegistrarRecBolsin() {
        habilitarPantalla();
        bolsinAReseleccionar = null;
        iniciarEjecucion();
    }

    private void habilitarPantalla() {
        ocultarAviso();
        limpiarDetalle();
        tarjetasDetalle.show(panelDetalle, VACIO);
    }

    private void iniciarEjecucion() {
        listaMostrada = false;
        recepcionRegistrada = false;
        limpiarFiltro();
        habilitarAcciones(false);
        gestor.registrarNuevoRecBolsin();
    }

    /** Al terminar el CU se vuelve a ejecutar para seguir con otro bolsín, conservando el aviso. */
    private void reiniciarEjecucion() {
        reiniciando = true;
        try {
            iniciarEjecucion();
        } finally {
            reiniciando = false;
        }
    }

    private void tomarFiltroBolsines() {
        habilitarPantalla();
        habilitarAcciones(false);
        filtrando = true;
        filtroSinResultados = false;
        try {
            gestor.tomarFiltroBolsines(txtPrecinto.getText(), txtCMOrigen.getText());
        } finally {
            filtrando = false;
        }
        if (filtroSinResultados) {
            // Flujos A1 y A2: el gestor vuelve a mostrar todos los bolsines
            limpiarFiltro();
        }
    }

    private void tomarSeleccionBolsin(DatosBolsin bolsin) {
        if (!seleccionAutomatica) {
            ocultarAviso();
        }
        limpiarDetalle();
        bolsinEnPantalla = bolsin;
        lblDetalle.setText("Contenido del bolsín N° " + bolsin.numeroBolsin() + "  ·  precinto "
                + bolsin.numeroPrecinto() + "  ·  desde " + bolsin.cmOrigen());
        tarjetasDetalle.show(panelDetalle, DETALLE);
        habilitarAcciones(false);
        gestor.tomarSeleccionBolsin(bolsin.numeroBolsin());
    }

    /** Botón "Registrar recepción": le informa al gestor la opción elegida. */
    private void tomarSeleccionOpcion() {
        Opcion opcion = opcionElegida();
        habilitarAcciones(false);
        switch (opcion) {
            case PRIMERA -> gestor.tomarSeleccionPrimeraOpcion();
            case SEGUNDA -> gestor.tomarSeleccionSegundaOpcion();
            case TERCERA -> gestor.tomarSeleccionTerceraOpcion();
            case CUARTA -> gestor.tomarSeleccionCuartaOpcion();
        }
    }

    private void tomarSeleccionDocumentacion(List<Integer> numerosDocumentacion) {
        gestor.tomarSeleccionDocumentacion(numerosDocumentacion);
    }

    private void tomarConfirmacion(boolean confirma) {
        gestor.tomarConfirmacion(confirma);
    }

    // ------------------------------------------------------------------
    // Mensajes del gestor -> pantalla
    // ------------------------------------------------------------------

    @Override
    public void mostrarCM(String nombreCM) {
        lblSubtitulo.setText("Bolsines enviados a " + nombreCM + " que todavía no se recibieron. "
                + "Seleccione el que llegó y verifique su contenido.");
    }

    @Override
    public void solicitarSelBolsin(List<DatosBolsin> bolsines) {
        listaMostrada = true;
        llenandoLista = true;
        modeloBolsines.clear();
        for (DatosBolsin bolsin : bolsines) {
            modeloBolsines.addElement(bolsin);
        }
        llenandoLista = false;
        lblBolsines.setText("Bolsines por recibir (" + bolsines.size() + ")");
        tarjetasLista.show(panelLista, bolsines.isEmpty() ? VACIO : LISTA);

        // Después de un error, se vuelve a mostrar el mismo bolsín
        if (bolsinAReseleccionar != null) {
            for (int i = 0; i < modeloBolsines.size(); i++) {
                if (modeloBolsines.get(i).numeroBolsin() == bolsinAReseleccionar) {
                    seleccionAutomatica = true;
                    listaBolsines.setSelectedIndex(i);
                    seleccionAutomatica = false;
                }
            }
            bolsinAReseleccionar = null;
        }
    }

    @Override
    public void mostrarNroRemito(List<Integer> numerosRemito) {
        cantidadRemitos = numerosRemito.size();
    }

    @Override
    public void mostrarDatosDocumentacion(List<DatosRemito> remitos) {
        for (DatosRemito remito : remitos) {
            for (DatosDocumentacion doc : remito.documentacion()) {
                documentos.put(doc.numero(), doc);
                modeloDocumentos.addRow(new Object[] {
                    Boolean.FALSE, remito.numero(), doc.numero(), doc.tipoDocumento(), doc.asunto(), etiqueta(doc)});
            }
        }
        mostrarEstadoRemitos(remitos);
    }

    @Override
    public void solicitarSelOpcionesRecBolsin() {
        lblResumen.setText(cantidadRemitos + (cantidadRemitos == 1 ? " remito" : " remitos") + "  ·  "
                + documentos.size() + (documentos.size() == 1 ? " documento" : " documentos"));
        habilitarAcciones(true);
    }

    /** Opciones 2 a 4: la documentación es la que el usuario marcó en la tabla. */
    @Override
    public void solicitarSelDocumentacion(String indicacion) {
        tomarSeleccionDocumentacion(documentacionMarcada());
    }

    @Override
    public void solicitarConfirmacion() {
        Opcion opcion = opcionElegida();
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Bolsín N° ").append(bolsinEnPantalla.numeroBolsin()).append(" (precinto ")
                .append(bolsinEnPantalla.numeroPrecinto()).append("), desde ").append(bolsinEnPantalla.cmOrigen())
                .append(": ").append(lblResumen.getText().trim()).append(".\n\n");
        mensaje.append("Opción: ").append(opcion.texto.substring(3)).append(".\n");
        if (opcion == Opcion.PRIMERA) {
            mensaje.append(opcion.indicacion).append('\n');
        } else {
            mensaje.append("Documentación marcada como «").append(opcion.columna).append("»:\n");
            for (Integer numero : documentacionMarcada()) {
                mensaje.append("   • N° ").append(numero).append(" – ").append(documentos.get(numero).asunto())
                        .append('\n');
            }
            mensaje.append("El resto de la documentación se registrará como recibida y aceptada.\n");
        }
        mensaje.append("\n¿Confirma la recepción?");
        Object[] opciones = {"Registrar recepción", "Volver"};
        int respuesta = JOptionPane.showOptionDialog(this, mensaje.toString(), "Confirmar recepción",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        tomarConfirmacion(respuesta == 0);
    }

    @Override
    public void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosRemito> remitos) {
        recepcionRegistrada = true;
        List<DatosDocumentacion> documentacion = new ArrayList<>();
        for (DatosRemito remito : remitos) {
            for (DatosDocumentacion doc : remito.documentacion()) {
                documentacion.add(doc);
                documentos.put(doc.numero(), doc);
                for (int fila = 0; fila < modeloDocumentos.getRowCount(); fila++) {
                    if (modeloDocumentos.getValueAt(fila, COL_DOC).equals(doc.numero())) {
                        modeloDocumentos.setValueAt(etiqueta(doc), fila, COL_ESTADO);
                    }
                }
            }
        }
        mostrarEstadoRemitos(remitos);
        lblDetalle.setText("Bolsín N° " + numeroBolsin + "  ·  recepción registrada");
        mostrarAviso(Aviso.EXITO, "Se registró la recepción del bolsín N° " + numeroBolsin + ". "
                + resumirRemitos(remitos) + " " + resumirEstados(documentacion));
    }

    /**
     * Canal de avisos del CU 29. El correo se envía en segundo plano: primero
     * llega "Enviando…" y después el resultado, que reemplaza a esa línea.
     */
    public void mostrarNotificacionEnviada(String aviso) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> mostrarNotificacionEnviada(aviso));
            return;
        }
        String linea = aviso.startsWith(GestorNotificacionCU29.PREFIJO_ERROR)
                ? "<span style='color:#A15C00'><b>" + aviso + "</b></span>" : aviso;
        for (int i = 0; i < lineasAviso.size(); i++) {
            if (lineasAviso.get(i).startsWith(GestorNotificacionCU29.PREFIJO_ENVIANDO)) {
                lineasAviso.set(i, linea);
                actualizarTextoAviso();
                return;
            }
        }
        if (panelAviso.isVisible()) {
            agregarLineaAviso(linea);
        } else {
            mostrarAviso(Aviso.INFORMACION, linea);
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        if (filtrando) {
            filtroSinResultados = true;
        }
        if (reiniciando && panelAviso.isVisible()) {
            agregarLineaAviso(mensaje);
        } else {
            mostrarAviso(Aviso.INFORMACION, mensaje);
        }
    }

    @Override
    public void finCU() {
        habilitarAcciones(false);
        if (!listaMostrada) {
            // No hubo bolsines para elegir: se muestra la lista vacía
            modeloBolsines.clear();
            lblBolsines.setText("Bolsines por recibir (0)");
            tarjetasLista.show(panelLista, VACIO);
            return;
        }
        bolsinAReseleccionar = recepcionRegistrada || bolsinEnPantalla == null ? null : bolsinEnPantalla.numeroBolsin();
        SwingUtilities.invokeLater(this::reiniciarEjecucion);
    }

    // ------------------------------------------------------------------
    // Auxiliares de presentación
    // ------------------------------------------------------------------

    /** Al elegir otra opción se borran las marcas y cambia el título de la columna para marcar. */
    private void elegirOpcion(Opcion opcion) {
        if (tablaDocumentos.isEditing()) {
            tablaDocumentos.getCellEditor().cancelCellEditing();
        }
        botonesOpcion.get(opcion).setSelected(true);
        for (int fila = 0; fila < modeloDocumentos.getRowCount(); fila++) {
            modeloDocumentos.setValueAt(Boolean.FALSE, fila, COL_MARCA);
        }
        tablaDocumentos.getColumnModel().getColumn(COL_MARCA).setHeaderValue(opcion.columna);
        tablaDocumentos.getTableHeader().repaint();
        lblIndicacion.setText(opcion.indicacion);
        actualizarMarcado();
    }

    private Opcion opcionElegida() {
        for (Map.Entry<Opcion, JRadioButton> entrada : botonesOpcion.entrySet()) {
            if (entrada.getValue().isSelected()) {
                return entrada.getKey();
            }
        }
        return Opcion.PRIMERA;
    }

    private List<Integer> documentacionMarcada() {
        List<Integer> marcada = new ArrayList<>();
        for (int fila = 0; fila < modeloDocumentos.getRowCount(); fila++) {
            if (Boolean.TRUE.equals(modeloDocumentos.getValueAt(fila, COL_MARCA))) {
                marcada.add((Integer) modeloDocumentos.getValueAt(fila, COL_DOC));
            }
        }
        return marcada;
    }

    private void limpiarDetalle() {
        modeloDocumentos.setRowCount(0);
        documentos.clear();
        cantidadRemitos = 0;
        lblResumen.setText(" ");
        lblRemitos.setText(" ");
        elegirOpcion(Opcion.PRIMERA);
    }

    private void limpiarFiltro() {
        txtPrecinto.setText("");
        txtCMOrigen.setText("");
    }

    private void habilitarAcciones(boolean habilitar) {
        btnRegistrar.setEnabled(habilitar);
        for (JRadioButton boton : botonesOpcion.values()) {
            boton.setEnabled(habilitar);
        }
        lblIndicacion.setEnabled(habilitar);
        actualizarMarcado();
    }

    /** La columna para marcar se puede editar sólo con las opciones 2 a 4 y mientras se elige la opción. */
    private void actualizarMarcado() {
        if (tablaDocumentos.isEditing()) {
            tablaDocumentos.getCellEditor().stopCellEditing();
        }
        marcadoHabilitado = btnRegistrar.isEnabled() && opcionElegida() != Opcion.PRIMERA;
        tablaDocumentos.repaint();
    }

    private void mostrarEstadoRemitos(List<DatosRemito> remitos) {
        List<String> partes = new ArrayList<>();
        for (DatosRemito remito : remitos) {
            partes.add("Remito N° " + remito.numero() + ": <b>" + remito.descripcionEstado() + "</b>");
        }
        lblRemitos.setText("<html>" + String.join("&nbsp;&nbsp;·&nbsp;&nbsp;", partes) + "</html>");
    }

    private enum Aviso {
        EXITO(new Color(0xE8F5E9), new Color(0x66BB6A)),
        INFORMACION(new Color(0xE3F2FD), new Color(0x64B5F6));

        private final Color fondo;
        private final Color borde;

        Aviso(Color fondo, Color borde) {
            this.fondo = fondo;
            this.borde = borde;
        }
    }

    private void mostrarAviso(Aviso tipo, String mensaje) {
        lineasAviso.clear();
        lineasAviso.add(mensaje);
        panelAviso.setBackground(tipo.fondo);
        panelAviso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, tipo.borde),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        actualizarTextoAviso();
        panelAviso.setVisible(true);
    }

    private void agregarLineaAviso(String mensaje) {
        lineasAviso.add(mensaje);
        actualizarTextoAviso();
    }

    private void actualizarTextoAviso() {
        lblAviso.setText("<html>" + String.join("<br>", lineasAviso) + "</html>");
    }

    private void ocultarAviso() {
        lineasAviso.clear();
        panelAviso.setVisible(false);
    }

    private static String resumirRemitos(List<DatosRemito> remitos) {
        List<String> partes = new ArrayList<>();
        for (DatosRemito remito : remitos) {
            partes.add("N° " + remito.numero() + " «" + remito.descripcionEstado() + "»");
        }
        return (remitos.size() == 1 ? "Remito " : "Remitos ") + String.join(", ", partes) + ".";
    }

    private static String resumirEstados(List<DatosDocumentacion> documentacion) {
        Map<String, Integer> cantidadPorEstado = new LinkedHashMap<>();
        for (DatosDocumentacion doc : documentacion) {
            cantidadPorEstado.merge(doc.descripcionEstado(), 1, Integer::sum);
        }
        if (cantidadPorEstado.size() == 1) {
            String estado = cantidadPorEstado.keySet().iterator().next();
            return (documentacion.size() == 1 ? "Su documento quedó" : "Sus " + documentacion.size()
                    + " documentos quedaron") + " en estado «" + estado + "».";
        }
        List<String> partes = new ArrayList<>();
        cantidadPorEstado.forEach((estado, cantidad) -> partes.add(cantidad + " «" + estado + "»"));
        return "Documentación: " + String.join(", ", partes) + ".";
    }

    private static EtiquetaEstado etiqueta(DatosDocumentacion doc) {
        return new EtiquetaEstado(doc.estado(), doc.descripcionEstado());
    }

    /** La casilla para marcar se ve deshabilitada cuando la opción elegida no pide marcar documentación. */
    private class RenderizadorMarca implements TableCellRenderer {

        private final TableCellRenderer base;

        RenderizadorMarca(TableCellRenderer base) {
            this.base = base;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component celda = base.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            celda.setEnabled(marcadoHabilitado);
            return celda;
        }
    }

    /** Muestra cada bolsín en dos líneas: número y, debajo, precinto y CM de origen. */
    private static class RenderizadorBolsin extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean seleccionado, boolean foco) {
            DatosBolsin bolsin = (DatosBolsin) valor;
            String texto = "<html><b>Bolsín N° " + bolsin.numeroBolsin() + "</b><br>"
                    + "<span style='font-size:9px'>Precinto " + bolsin.numeroPrecinto() + "  ·  desde "
                    + bolsin.cmOrigen() + "</span></html>";
            JLabel etiqueta = (JLabel) super.getListCellRendererComponent(lista, texto, indice, seleccionado, foco);
            etiqueta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            return etiqueta;
        }
    }
}
