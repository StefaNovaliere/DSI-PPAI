package ppai;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import ppai.boundary.RenderizadorEstado;
import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.Remito;
import ppai.entidades.TipoDocumento;
import ppai.persistencia.DatosDePrueba;

/**
 * Herramienta didáctica para la defensa (no forma parte del CU 28): permite
 * aplicarle a cualquier documentación cualquier evento de su máquina de
 * estados y ver cómo responde el objeto estado (patrón State).
 *
 * <p>Trabaja sobre una copia de los datos de prueba en memoria, así no
 * modifica la base de datos que usa el caso de uso.
 */
public class DemostracionPatronState extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Evento de la máquina de estados que se le envía a la documentación. */
    private interface Evento {
        void aplicar(Documentacion documentacion, LocalDateTime fechaHora, Empleado responsable);
    }

    private final List<Documentacion> documentacion = new ArrayList<>();
    private TipoDocumento tipoParaNuevas;
    private Empleado responsable;
    private int proximoNumero = 2001;

    private final DefaultTableModel modeloDocs = modeloNoEditable("N° Doc", "Asunto", "Estado actual",
            "Objeto estado (clase)");
    private final JTable tablaDocs = new JTable(modeloDocs);
    private final DefaultTableModel modeloHistorial = modeloNoEditable("Estado", "Desde", "Hasta");
    private final JLabel lblSeleccion = new JLabel("Seleccione una documentación");
    private final JTextArea areaLog = new JTextArea(8, 60);

    public DemostracionPatronState() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cargarDatos();

        JLabel titulo = new JLabel("Probar el patrón State");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        JLabel explicacion = new JLabel("<html>Elegí una documentación y enviale un evento. La <b>Documentacion</b> "
                + "(Contexto) no decide nada: delega en su <b>objeto estado</b>. Según la clase de ese objeto, el "
                + "mismo evento hace una transición o se rechaza. Esta pestaña usa una copia en memoria: no modifica "
                + "la base de datos.</html>");
        JPanel encabezado = new JPanel(new BorderLayout(4, 4));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(explicacion, BorderLayout.CENTER);
        add(encabezado, BorderLayout.NORTH);

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crearPanelDocumentacion(), crearPanelEventos());
        centro.setResizeWeight(0.62);
        add(centro, BorderLayout.CENTER);

        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Qué pasó"));
        add(scrollLog, BorderLayout.SOUTH);

        refrescarTabla(0);
    }

    private void cargarDatos() {
        documentacion.clear();
        DatosDePrueba datos = new DatosDePrueba();
        for (Bolsin bolsin : datos.getBolsines()) {
            for (Remito remito : bolsin.getRemitos()) {
                for (DetalleRemito detalle : remito.getDetallesRemito()) {
                    documentacion.add(detalle.getDocumentacion());
                }
            }
        }
        tipoParaNuevas = datos.getTiposDocumento().get(0);
        String usuario = datos.getSesionActual().getUsuario();
        for (Empleado empleado : datos.getEmpleados()) {
            if (empleado.esTuUsuario(usuario)) {
                responsable = empleado;
            }
        }
        proximoNumero = 2001;
    }

    private JPanel crearPanelDocumentacion() {
        tablaDocs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaDocs.setRowHeight(22);
        tablaDocs.getColumnModel().getColumn(1).setPreferredWidth(220);
        tablaDocs.getColumnModel().getColumn(2).setCellRenderer(new RenderizadorEstado());
        tablaDocs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarSeleccionada();
            }
        });

        JButton btnNueva = new JButton("Nueva documentación (CU 7: new)");
        btnNueva.addActionListener(e -> crearDocumentacion());
        JButton btnReiniciar = new JButton("Reiniciar demo");
        btnReiniciar.addActionListener(e -> {
            cargarDatos();
            areaLog.setText("");
            refrescarTabla(0);
        });
        JButton btnMaquina = new JButton("Ver máquina de estados");
        btnMaquina.addActionListener(e -> mostrarMaquinaDeEstados());
        JPanel botones = new JPanel(new GridLayout(1, 3, 6, 0));
        botones.add(btnNueva);
        botones.add(btnMaquina);
        botones.add(btnReiniciar);

        JTable tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setRowHeight(20);
        tablaHistorial.getColumnModel().getColumn(0).setCellRenderer(new RenderizadorEstado());
        JScrollPane scrollHistorial = new JScrollPane(tablaHistorial);
        scrollHistorial.setBorder(BorderFactory.createTitledBorder("Historial de la documentación seleccionada"));
        scrollHistorial.setPreferredSize(new Dimension(300, 150));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Documentación (Contexto)"));
        panel.add(new JScrollPane(tablaDocs), BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout(5, 5));
        sur.add(botones, BorderLayout.NORTH);
        sur.add(scrollHistorial, BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelEventos() {
        JPanel botones = new JPanel(new GridLayout(0, 1, 4, 4));
        agregarEvento(botones, "remitar()", "CU 15 Generar Remito / CU 20 Modificar Bolsín", Documentacion::remitar);
        agregarEvento(botones, "cancelarRemito()", "CU 17 Cancelar Remito", Documentacion::cancelarRemito);
        agregarEvento(botones, "agregarAlBolsin()", "CU 19 Generar Bolsín", Documentacion::agregarAlBolsin);
        agregarEvento(botones, "enviar()", "CU 27 Registrar retiro de bolsines", Documentacion::enviar);
        agregarEvento(botones, "recibir()", "CU 28 Registrar Recepción de Bolsín", Documentacion::recibir);
        agregarEvento(botones, "registrarNoRecibida()", "CU 31 [todo igual a lo registrado = False]",
                Documentacion::registrarNoRecibida);
        agregarEvento(botones, "marcarParaRedirigir()", "CU 31 [CMDestino = CMC]",
                Documentacion::marcarParaRedirigir);
        agregarEvento(botones, "rechazar()", "CU 31 [CMDestino no es la correcta / doc. incorrecta]", Documentacion::rechazar);
        agregarEvento(botones, "registrar()", "CU 7 Registrar Documentación", Documentacion::registrar);
        agregarEvento(botones, "darDeBaja()", "CU 9 Eliminar Documentación", Documentacion::darDeBaja);

        lblSeleccion.setFont(lblSeleccion.getFont().deriveFont(Font.BOLD));
        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Eventos que se le envían"));
        panel.add(lblSeleccion, BorderLayout.NORTH);
        panel.add(botones, BorderLayout.CENTER);
        return panel;
    }

    private void agregarEvento(JPanel panel, String nombre, String casoDeUso, Evento evento) {
        JButton boton = new JButton("<html><b>" + nombre + "</b> &nbsp;<font size=-2>" + casoDeUso + "</font></html>");
        boton.setHorizontalAlignment(JButton.LEFT);
        boton.addActionListener(e -> aplicarEvento(nombre, evento));
        panel.add(boton);
    }

    private void aplicarEvento(String nombreEvento, Evento evento) {
        int fila = tablaDocs.getSelectedRow();
        if (fila < 0) {
            log("Primero seleccione una documentación.");
            return;
        }
        Documentacion doc = documentacion.get(fila);
        String estadoAntes = doc.getEstadoActual().getNombre();
        String claseAntes = doc.getEstadoActual().getClass().getSimpleName();
        try {
            evento.aplicar(doc, LocalDateTime.now().withNano(0), responsable);
            log("OK        Doc " + doc.getNumero() + " · " + nombreEvento + " → el objeto " + claseAntes
                    + " hizo la transición " + estadoAntes + " → " + doc.getEstadoActual().getNombre());
        } catch (IllegalStateException ex) {
            log("RECHAZADO Doc " + doc.getNumero() + " · " + nombreEvento + " → el objeto " + claseAntes
                    + " no tiene esa transición (" + ex.getMessage() + ")");
        }
        refrescarTabla(fila);
    }

    private void crearDocumentacion() {
        Documentacion nueva = new Documentacion(proximoNumero++, "Documentación nueva de prueba", LocalDate.now(),
                tipoParaNuevas, LocalDateTime.now().withNano(0), responsable);
        documentacion.add(nueva);
        log("OK        Doc " + nueva.getNumero() + " · new() → nace en estado " + nueva.getEstadoActual().getNombre());
        refrescarTabla(documentacion.size() - 1);
    }

    private void refrescarTabla(int filaSeleccionada) {
        modeloDocs.setRowCount(0);
        for (Documentacion doc : documentacion) {
            modeloDocs.addRow(new Object[] {doc.getNumero(), doc.getAsunto(), doc.getEstadoActual().getNombre(),
                doc.getEstadoActual().getClass().getSimpleName()});
        }
        if (filaSeleccionada >= 0 && filaSeleccionada < modeloDocs.getRowCount()) {
            tablaDocs.setRowSelectionInterval(filaSeleccionada, filaSeleccionada);
        }
        mostrarSeleccionada();
    }

    private void mostrarSeleccionada() {
        modeloHistorial.setRowCount(0);
        int fila = tablaDocs.getSelectedRow();
        if (fila < 0) {
            lblSeleccion.setText("Seleccione una documentación");
            return;
        }
        Documentacion doc = documentacion.get(fila);
        lblSeleccion.setText("<html>Doc " + doc.getNumero() + " · estado actual: <font color='#1565C0'>"
                + doc.getEstadoActual().getNombre() + "</font><br>objeto de la clase <i>"
                + doc.getEstadoActual().getClass().getSimpleName() + "</i></html>");
        for (CambioEstadoDocumentacion ce : doc.getCambiosEstado()) {
            modeloHistorial.addRow(new Object[] {ce.getEstado().getNombre(), ce.getFechaHoraInicio().format(FORMATO),
                ce.sosActual() ? "ACTUAL" : ce.getFechaHoraFin().format(FORMATO)});
        }
    }

    /** Muestra la máquina de estados de Documentación modelada en el análisis (Entrega 1). */
    private void mostrarMaquinaDeEstados() {
        java.net.URL imagen = getClass().getResource("/imagenes/maquina_estados_documentacion.png");
        if (imagen == null) {
            log("No se encontró la imagen de la máquina de estados.");
            return;
        }
        JLabel diagrama = new JLabel(new ImageIcon(imagen));
        JScrollPane scroll = new JScrollPane(diagrama);
        scroll.setPreferredSize(new Dimension(1000, 560));
        JOptionPane.showMessageDialog(this, scroll, "Máquina de estados de la clase Documentación (análisis)",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void log(String mensaje) {
        areaLog.append(mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
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
