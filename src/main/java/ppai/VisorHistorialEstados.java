package ppai;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ppai.boundary.RenderizadorEstado;
import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Remito;
import ppai.persistencia.Repositorio;

/**
 * Herramienta de verificación (no forma parte del CU 28): muestra el historial
 * de cambios de estado de la documentación guardado en la base, para ver los
 * CambioEstadoDocumentacion que fue creando el patrón State.
 */
public class VisorHistorialEstados extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String TODOS = "Todos";
    private static final int COLUMNA_HASTA = 6;

    private final Repositorio repositorio;
    private final JComboBox<String> comboBolsin = new JComboBox<>();
    private final JLabel lblResumen = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[] {"Bolsín", "Remito", "N° Doc", "Asunto", "Estado documentación", "Desde", "Hasta",
                "Responsable"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private boolean actualizando;

    public VisorHistorialEstados(Repositorio repositorio) {
        super(new BorderLayout(8, 8));
        this.repositorio = repositorio;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Historial de estados de la documentación (guardado en la base de datos)");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        JLabel explicacion = new JLabel("<html>Cada fila es un <b>CambioEstadoDocumentacion</b>. Los crea el objeto "
                + "estado de la documentación cuando resuelve una transición (patrón State). "
                + "La fila marcada <b>ACTUAL</b> es el estado vigente.</html>");
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filtro.add(new JLabel("Bolsín:"));
        filtro.add(comboBolsin);
        filtro.add(lblResumen);
        comboBolsin.addActionListener(e -> {
            if (!actualizando) {
                cargarTabla();
            }
        });

        JPanel encabezado = new JPanel(new BorderLayout(4, 4));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(explicacion, BorderLayout.CENTER);
        encabezado.add(filtro, BorderLayout.SOUTH);

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(20);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new RenderizadorEstado());
        tabla.getColumnModel().getColumn(COLUMNA_HASTA).setCellRenderer(new RenderizadorActual());
        add(encabezado, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        actualizar();
    }

    /** Vuelve a leer los bolsines y recarga el filtro y la tabla. */
    public void actualizar() {
        actualizando = true;
        Object seleccionado = comboBolsin.getSelectedItem();
        comboBolsin.removeAllItems();
        comboBolsin.addItem(TODOS);
        for (Bolsin bolsin : repositorio.getBolsines()) {
            comboBolsin.addItem(String.valueOf(bolsin.getNumeroBolsin()));
        }
        comboBolsin.setSelectedItem(seleccionado == null ? TODOS : seleccionado);
        actualizando = false;
        cargarTabla();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        String filtro = (String) comboBolsin.getSelectedItem();
        List<String> resumen = new ArrayList<>();
        for (Bolsin bolsin : repositorio.getBolsines()) {
            if (filtro != null && !TODOS.equals(filtro) && !filtro.equals(String.valueOf(bolsin.getNumeroBolsin()))) {
                continue;
            }
            resumen.add("Bolsín " + bolsin.getNumeroBolsin() + ": " + bolsin.getEstadoActual().getNombre());
            for (Remito remito : bolsin.getRemitos()) {
                for (DetalleRemito detalle : remito.getDetallesRemito()) {
                    for (CambioEstadoDocumentacion ce : detalle.getDocumentacion().getCambiosEstado()) {
                        modelo.addRow(new Object[] {
                            bolsin.getNumeroBolsin(),
                            remito.obtenerNumero(),
                            detalle.getDocumentacion().getNumero(),
                            detalle.getDocumentacion().getAsunto(),
                            ce.getEstado().getNombre(),
                            ce.getFechaHoraInicio().format(FORMATO),
                            ce.sosActual() ? "ACTUAL" : ce.getFechaHoraFin().format(FORMATO),
                            ce.getResponsableCE().getNombreCompleto()});
                    }
                }
            }
        }
        lblResumen.setText(filtro == null || TODOS.equals(filtro) ? "" : "   " + String.join(" · ", resumen));
    }

    /** Resalta el cambio de estado vigente. */
    private static class RenderizadorActual extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            setFont(getFont().deriveFont("ACTUAL".equals(valor) ? Font.BOLD : Font.PLAIN));
            return celda;
        }
    }
}
