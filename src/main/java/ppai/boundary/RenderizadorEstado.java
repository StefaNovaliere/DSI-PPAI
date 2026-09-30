package ppai.boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pinta la celda de estado con un color según el estado de la documentación.
 */
class RenderizadorEstado extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                   boolean foco, int fila, int columna) {
        Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
        if (!seleccionada && valor instanceof EtiquetaEstado etiqueta) {
            celda.setBackground(colorEstado(etiqueta.nombre()));
        }
        setFont(getFont().deriveFont(Font.BOLD));
        return celda;
    }

    static Color colorEstado(String nombreEstado) {
        return switch (nombreEstado) {
            case "Recibida&Aceptada" -> new Color(0xC8E6C9);
            case "ParaRedirigir" -> new Color(0xFFE0B2);
            case "EnBolsinEnviado" -> new Color(0xBBDEFB);
            case "Recibida&Rechazada", "NoRecibida", "DeBaja" -> new Color(0xFFCDD2);
            default -> new Color(0xF0F0F0);
        };
    }
}
