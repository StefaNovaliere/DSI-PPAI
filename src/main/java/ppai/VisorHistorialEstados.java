package ppai;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ppai.boundary.RenderizadorEstado;
import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Remito;
import ppai.persistencia.Repositorio;

/**
 * Herramienta de verificación (no forma parte del CU 28): muestra el historial
 * de cambios de estado de toda la documentación, para ver cómo el patrón State
 * fue creando cada CambioEstadoDocumentacion.
 */
public class VisorHistorialEstados extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Repositorio repositorio;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[] {"Bolsín", "Estado bolsín", "Remito", "Estado remito", "N° Doc",
                "Estado documentación", "Desde", "Hasta", "Responsable"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public VisorHistorialEstados(Repositorio repositorio) {
        super(new BorderLayout(8, 8));
        this.repositorio = repositorio;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Historial de estados de la documentación (CambioEstadoDocumentacion)");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizar());
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(btnActualizar, BorderLayout.EAST);

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(20);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new RenderizadorEstado());
        add(encabezado, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        actualizar();
    }

    public void actualizar() {
        modelo.setRowCount(0);
        for (Bolsin bolsin : repositorio.getBolsines()) {
            for (Remito remito : bolsin.getRemitos()) {
                for (DetalleRemito detalle : remito.getDetallesRemito()) {
                    for (CambioEstadoDocumentacion ce : detalle.getDocumentacion().getCambiosEstado()) {
                        modelo.addRow(new Object[] {
                            bolsin.getNumeroBolsin(),
                            bolsin.getEstadoActual().getNombre(),
                            remito.obtenerNumero(),
                            remito.getEstado().getNombre(),
                            detalle.getDocumentacion().getNumero(),
                            ce.getEstado().getNombre(),
                            ce.getFechaHoraInicio().format(FORMATO),
                            ce.sosActual() ? "(actual)" : ce.getFechaHoraFin().format(FORMATO),
                            ce.getResponsableCE().getNombreCompleto()});
                    }
                }
            }
        }
    }
}
