package ppai.boundary;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;

/**
 * Pantalla del CU 28 Registrar Recepción de Bolsín (Swing), con un diseño
 * maestro-detalle: a la izquierda los bolsines por recibir y a la derecha el
 * contenido del bolsín seleccionado.
 *
 * <p>Cada acción del usuario se traduce en el mensaje del diagrama de
 * secuencia hacia el gestor:
 * <ul>
 *   <li>elegir la opción del menú → {@code registrarNuevoRecBolsin()}</li>
 *   <li>hacer clic en un bolsín de la lista → {@code tomarSeleccionBolsin()}</li>
 *   <li>botón "Confirmar recepción" → {@code tomarSeleccionPrimerOpcion()}</li>
 *   <li>botón "Informar diferencias" → {@code tomarSeleccionSegundaOpcion()}</li>
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

    private GestorRecepcionBolsin gestor;
    private Runnable accionVolver = () -> { };

    // Encabezado y aviso de resultado
    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JPanel panelAviso = new JPanel(new BorderLayout(10, 0));
    private final JLabel lblAviso = new JLabel();
    private final List<String> lineasAviso = new ArrayList<>();

    // Maestro: bolsines por recibir
    private final DefaultListModel<DatosBolsin> modeloBolsines = new DefaultListModel<>();
    private final JList<DatosBolsin> listaBolsines = new JList<>(modeloBolsines);
    private final JLabel lblBolsines = new JLabel("Bolsines por recibir");
    private final CardLayout tarjetasLista = new CardLayout();
    private final JPanel panelLista = new JPanel(tarjetasLista);

    // Detalle: contenido del bolsín seleccionado
    private final CardLayout tarjetasDetalle = new CardLayout();
    private final JPanel panelDetalle = new JPanel(tarjetasDetalle);
    private final JLabel lblDetalle = new JLabel(" ");
    private final DefaultTableModel modeloDocumentos =
            modeloNoEditable("Remito", "N° Doc", "Tipo", "Asunto", "Estado");
    private final JTable tablaDocumentos = new JTable(modeloDocumentos);
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnDiferencias = new JButton("Informar diferencias");
    private final JButton btnConfirmar = new JButton("Confirmar recepción");

    // Datos de la ejecución actual del caso de uso
    private final Map<Integer, DatosDocumentacion> documentos = new LinkedHashMap<>();
    private int remitoActual;
    private int cantidadRemitos;
    private DatosBolsin bolsinEnPantalla;
    private boolean recepcionRegistrada;
    private boolean listaMostrada;
    private boolean llenandoLista;
    private boolean seleccionAutomatica;
    private boolean reiniciando;
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
        JPanel maestro = new JPanel(new BorderLayout(0, 6));
        maestro.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        maestro.add(lblBolsines, BorderLayout.NORTH);
        maestro.add(panelLista, BorderLayout.CENTER);
        return maestro;
    }

    private JPanel crearDetalle() {
        JLabel indicacion = new JLabel("Seleccione un bolsín de la lista para ver sus remitos y documentos.",
                SwingConstants.CENTER);
        indicacion.setForeground(new Color(0x777777));

        tablaDocumentos.setRowHeight(24);
        tablaDocumentos.setRowSelectionAllowed(false);
        tablaDocumentos.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaDocumentos.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaDocumentos.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaDocumentos.getColumnModel().getColumn(3).setPreferredWidth(260);
        tablaDocumentos.getColumnModel().getColumn(4).setPreferredWidth(150);
        tablaDocumentos.getColumnModel().getColumn(4).setCellRenderer(new RenderizadorEstado());
        lblDetalle.setFont(lblDetalle.getFont().deriveFont(Font.BOLD));

        JPanel contenidoBolsin = new JPanel(new BorderLayout(0, 6));
        contenidoBolsin.add(lblDetalle, BorderLayout.NORTH);
        contenidoBolsin.add(new JScrollPane(tablaDocumentos), BorderLayout.CENTER);

        panelDetalle.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        panelDetalle.add(indicacion, VACIO);
        panelDetalle.add(contenidoBolsin, DETALLE);
        return panelDetalle;
    }

    private JPanel crearBarraAcciones() {
        btnConfirmar.setFont(btnConfirmar.getFont().deriveFont(Font.BOLD));
        btnConfirmar.setToolTipText("Todos los remitos y documentos llegaron y coinciden con lo registrado");
        btnDiferencias.setToolTipText("Falta algo o no coincide con lo registrado");
        btnConfirmar.addActionListener(e -> tomarSeleccionPrimerOpcion());
        btnDiferencias.addActionListener(e -> tomarSeleccionSegundaOpcion());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnDiferencias);
        botones.add(btnConfirmar);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDDDDD)),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)));
        barra.add(lblResumen, BorderLayout.WEST);
        barra.add(botones, BorderLayout.EAST);
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

    private void tomarSeleccionPrimerOpcion() {
        habilitarAcciones(false);
        gestor.tomarSeleccionPrimerOpcion();
    }

    private void tomarSeleccionSegundaOpcion() {
        habilitarAcciones(false);
        gestor.tomarSeleccionSegundaOpcion();
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
        tarjetasLista.show(panelLista, LISTA);

        // Después de cancelar o informar diferencias, se vuelve a mostrar el mismo bolsín
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
    public void mostrarNroRemito(int numeroRemito) {
        remitoActual = numeroRemito;
        cantidadRemitos++;
    }

    @Override
    public void mostrarDatosDocumentacion(List<DatosDocumentacion> documentacion) {
        for (DatosDocumentacion doc : documentacion) {
            documentos.put(doc.numero(), doc);
            modeloDocumentos.addRow(new Object[] {
                remitoActual, doc.numero(), doc.tipoDocumento(), doc.asunto(), etiqueta(doc)});
        }
    }

    @Override
    public void solicitarSelOpcionesRecBolsin() {
        lblResumen.setText(cantidadRemitos + (cantidadRemitos == 1 ? " remito" : " remitos") + "  ·  "
                + documentos.size() + (documentos.size() == 1 ? " documento" : " documentos"));
        habilitarAcciones(true);
    }

    @Override
    public void solicitarConfirmacion() {
        String mensaje = "Se registrará la recepción del bolsín N° " + bolsinEnPantalla.numeroBolsin()
                + " (precinto " + bolsinEnPantalla.numeroPrecinto() + "),\ncon " + lblResumen.getText().trim()
                + ".\n\n¿Confirma que todo llegó y coincide con lo registrado?";
        Object[] opciones = {"Registrar recepción", "Volver"};
        int respuesta = JOptionPane.showOptionDialog(this, mensaje, "Confirmar recepción",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        tomarConfirmacion(respuesta == 0);
    }

    @Override
    public void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosDocumentacion> documentacion) {
        recepcionRegistrada = true;
        for (DatosDocumentacion doc : documentacion) {
            documentos.put(doc.numero(), doc);
            for (int fila = 0; fila < modeloDocumentos.getRowCount(); fila++) {
                if (modeloDocumentos.getValueAt(fila, 1).equals(doc.numero())) {
                    modeloDocumentos.setValueAt(etiqueta(doc), fila, 4);
                }
            }
        }
        lblDetalle.setText("Bolsín N° " + numeroBolsin + "  ·  recepción registrada");
        mostrarAviso(Aviso.EXITO, "Se registró la recepción del bolsín N° " + numeroBolsin + ". "
                + resumirEstados(documentacion));
    }

    /** Canal del CU 29 (simulado): informa que se envió la notificación por correo. */
    public void mostrarNotificacionEnviada(String textoCorreo) {
        agregarLineaAviso("Se envió la notificación de la recepción por correo electrónico (CU 29).");
    }

    @Override
    public void mostrarMensaje(String mensaje) {
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

    private void limpiarDetalle() {
        modeloDocumentos.setRowCount(0);
        documentos.clear();
        cantidadRemitos = 0;
        lblResumen.setText(" ");
    }

    private void habilitarAcciones(boolean habilitar) {
        btnConfirmar.setEnabled(habilitar);
        btnDiferencias.setEnabled(habilitar);
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
        return "Estado de la documentación: " + String.join(", ", partes) + ".";
    }

    private static EtiquetaEstado etiqueta(DatosDocumentacion doc) {
        return new EtiquetaEstado(doc.estado(), doc.descripcionEstado());
    }

    private static DefaultTableModel modeloNoEditable(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
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
