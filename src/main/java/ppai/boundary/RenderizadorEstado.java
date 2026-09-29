package ppai.boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pinta la celda de estado de una tabla según el estado de la documentación.
 */
public class RenderizadorEstado extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                   boolean foco, int fila, int columna) {
        Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
        if (!seleccionada) {
            celda.setBackground(colorEstado(String.valueOf(valor)));
        }
        setFont(getFont().deriveFont(Font.BOLD));
        return celda;
    }

    private static Color colorEstado(String estado) {
        return switch (estado) {
            case "Recibida&Aceptada" -> new Color(0xC8E6C9);
            case "ParaRedirigir" -> new Color(0xFFE0B2);
            case "EnBolsinEnviado" -> new Color(0xBBDEFB);
            case "Recibida&Rechazada", "NoRecibida", "DeBaja" -> new Color(0xFFCDD2);
            default -> Color.WHITE;
        };
    }
}
