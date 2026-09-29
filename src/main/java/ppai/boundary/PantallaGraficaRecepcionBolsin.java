package ppai.boundary;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;

/**
 * Pantalla del CU 28 implementada con Swing.
 *
 * <p>Es orientada a eventos: cuando el gestor "solicita" algo, la pantalla
 * habilita el control correspondiente y el evento del usuario (click) dispara
 * el "tomar..." hacia el gestor, igual que en el diagrama de secuencia.
 */
public class PantallaGraficaRecepcionBolsin extends JPanel implements PantallaRecepcionBolsin {

    private GestorRecepcionBolsin gestor;

    private final JLabel lblCM = new JLabel("Comisión Médica: -");
    private final JButton btnOpcRecepcionBolsin = new JButton("Registrar recepción de bolsín");

    private final DefaultTableModel modeloBolsines =
            modeloNoEditable("N° Bolsín", "N° Precinto", "CM Origen");
    private final JTable tablaBolsines = new JTable(modeloBolsines);
    private final JButton btnSeleccionarBolsin = new JButton("Seleccionar bolsín");

    private final DefaultTableModel modeloDocumentacion =
            modeloNoEditable("N° Remito", "N° Doc", "Tipo", "Asunto", "Estado");
    private final JTable tablaDocumentacion = new JTable(modeloDocumentacion);
    private int remitoActual;

    private final JRadioButton rbPrimerOpcion =
            new JRadioButton("Todo remito y documentación coincide con lo registrado", true);
    private final JRadioButton rbSegundaOpcion = new JRadioButton("Hay diferencias con lo registrado");
    private final JButton btnConfirmacion = new JButton("Registrar recepción");

    private final JTextArea areaMensajes = new JTextArea(7, 60);

    public PantallaGraficaRecepcionBolsin() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearMensajes(), BorderLayout.SOUTH);

        btnOpcRecepcionBolsin.addActionListener(e -> opcRegistrarRecBolsin());
        btnSeleccionarBolsin.addActionListener(e -> tomarSeleccionBolsin());
        btnConfirmacion.addActionListener(e -> tomarSeleccionOpcion());
        habilitarControles(false, false);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("CU 28 - Registrar Recepción de Bolsín");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        lblCM.setFont(lblCM.getFont().deriveFont(Font.PLAIN, 13f));
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(lblCM);
        panel.add(textos, BorderLayout.WEST);
        panel.add(btnOpcRecepcionBolsin, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearCuerpo() {
        tablaBolsines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaBolsines.setRowHeight(22);
        JPanel panelBolsines = new JPanel(new BorderLayout(5, 5));
        panelBolsines.setBorder(BorderFactory.createTitledBorder("1. Bolsines enviados a su Comisión Médica"));
        JScrollPane scrollBolsines = new JScrollPane(tablaBolsines);
        scrollBolsines.setPreferredSize(new Dimension(300, 110));
        panelBolsines.add(scrollBolsines, BorderLayout.CENTER);
        panelBolsines.add(alinearDerecha(btnSeleccionarBolsin), BorderLayout.SOUTH);

        tablaDocumentacion.setRowHeight(22);
        tablaDocumentacion.getColumnModel().getColumn(3).setPreferredWidth(260);
        tablaDocumentacion.getColumnModel().getColumn(4).setCellRenderer(new RenderizadorEstado());
        JPanel panelDocumentacion = new JPanel(new BorderLayout(5, 5));
        panelDocumentacion.setBorder(BorderFactory.createTitledBorder("2. Remitos y documentación del bolsín"));
        JScrollPane scrollDocumentacion = new JScrollPane(tablaDocumentacion);
        scrollDocumentacion.setPreferredSize(new Dimension(300, 150));
        panelDocumentacion.add(scrollDocumentacion, BorderLayout.CENTER);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbPrimerOpcion);
        grupo.add(rbSegundaOpcion);
        JPanel panelOpciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelOpciones.setBorder(BorderFactory.createTitledBorder("3. Opción de recepción"));
        panelOpciones.add(rbPrimerOpcion);
        panelOpciones.add(rbSegundaOpcion);
        panelOpciones.add(Box.createHorizontalStrut(20));
        panelOpciones.add(btnConfirmacion);

        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.add(panelBolsines);
        cuerpo.add(panelDocumentacion);
        cuerpo.add(panelOpciones);
        return cuerpo;
    }

    private JScrollPane crearMensajes() {
        areaMensajes.setEditable(false);
        areaMensajes.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(areaMensajes);
        scroll.setBorder(BorderFactory.createTitledBorder("Mensajes"));
        return scroll;
    }

    // ------------------------------------------------------------------
    // Eventos del usuario -> gestor
    // ------------------------------------------------------------------

    @Override
    public void setGestor(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    @Override
    public void opcRegistrarRecBolsin() {
        habilitarPantalla();
        gestor.registrarNuevoRecBolsin();
    }

    private void habilitarPantalla() {
        modeloBolsines.setRowCount(0);
        modeloDocumentacion.setRowCount(0);
        areaMensajes.setText("");
        rbPrimerOpcion.setSelected(true);
        btnOpcRecepcionBolsin.setEnabled(false);
    }

    private void tomarSeleccionBolsin() {
        int fila = tablaBolsines.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Seleccione un bolsín de la lista.");
            return;
        }
        modeloDocumentacion.setRowCount(0);
        gestor.tomarSeleccionBolsin((Integer) modeloBolsines.getValueAt(fila, 0));
    }

    private void tomarSeleccionOpcion() {
        habilitarControles(false, false);
        if (rbPrimerOpcion.isSelected()) {
            gestor.tomarSeleccionPrimerOpcion();
        } else {
            gestor.tomarSeleccionSegundaOpcion();
        }
    }

    // ------------------------------------------------------------------
    // Mensajes del gestor -> pantalla
    // ------------------------------------------------------------------

    @Override
    public void mostrarCM(String nombreCM) {
        lblCM.setText("Comisión Médica: " + nombreCM);
    }

    @Override
    public void solicitarSelBolsin(List<DatosBolsin> bolsines) {
        modeloBolsines.setRowCount(0);
        for (DatosBolsin bolsin : bolsines) {
            modeloBolsines.addRow(new Object[] {bolsin.numeroBolsin(), bolsin.numeroPrecinto(), bolsin.cmOrigen()});
        }
        if (!bolsines.isEmpty()) {
            tablaBolsines.setRowSelectionInterval(0, 0);
        }
        habilitarControles(true, false);
    }

    @Override
    public void mostrarNroRemito(int numeroRemito) {
        remitoActual = numeroRemito;
    }

    @Override
    public void mostrarDatosDocumentacion(List<DatosDocumentacion> documentacion) {
        for (DatosDocumentacion doc : documentacion) {
            modeloDocumentacion.addRow(new Object[] {
                remitoActual, doc.numero(), doc.tipoDocumento(), doc.asunto(), doc.estado()});
        }
    }

    @Override
    public void solicitarSelOpcionesRecBolsin() {
        habilitarControles(true, true);
    }

    @Override
    public void solicitarConfirmacion() {
        Object[] opciones = {"Confirmar", "Cancelar"};
        int respuesta = JOptionPane.showOptionDialog(this,
                "¿Confirma la recepción del bolsín seleccionado?",
                "Confirmar recepción", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);
        tomarConfirmacion(respuesta == 0);
    }

    private void tomarConfirmacion(boolean confirma) {
        gestor.tomarConfirmacion(confirma);
    }

    @Override
    public void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosDocumentacion> documentacion) {
        // Se actualiza la columna Estado con el nuevo estado de cada documentación
        for (DatosDocumentacion doc : documentacion) {
            for (int fila = 0; fila < modeloDocumentacion.getRowCount(); fila++) {
                if (modeloDocumentacion.getValueAt(fila, 1).equals(doc.numero())) {
                    modeloDocumentacion.setValueAt(doc.estado(), fila, 4);
                }
            }
        }
        mostrarMensaje("Recepción del bolsín N° " + numeroBolsin + " registrada correctamente.");
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        areaMensajes.append(mensaje + "\n");
        areaMensajes.setCaretPosition(areaMensajes.getDocument().getLength());
    }

    @Override
    public void finCU() {
        mostrarMensaje("Fin del caso de uso.");
        habilitarControles(false, false);
        btnOpcRecepcionBolsin.setEnabled(true);
    }

    /** Salida que escribe en el área de mensajes (la usa el CU 29 para mostrar el mail). */
    public PrintStream getSalidaMensajes() {
        OutputStream destino = new OutputStream() {
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    String linea = buffer.toString(StandardCharsets.UTF_8);
                    buffer.reset();
                    SwingUtilities.invokeLater(() -> mostrarMensaje(linea));
                } else {
                    buffer.write(b);
                }
            }
        };
        return new PrintStream(destino, true, StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------

    private void habilitarControles(boolean seleccionBolsin, boolean opciones) {
        tablaBolsines.setEnabled(seleccionBolsin);
        btnSeleccionarBolsin.setEnabled(seleccionBolsin);
        rbPrimerOpcion.setEnabled(opciones);
        rbSegundaOpcion.setEnabled(opciones);
        btnConfirmacion.setEnabled(opciones);
    }

    private static JPanel alinearDerecha(Component componente) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.add(componente);
        return panel;
    }

    private static DefaultTableModel modeloNoEditable(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }
}
