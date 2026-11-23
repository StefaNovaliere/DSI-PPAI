package ppai.boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.function.IntSupplier;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/** Renderiza el estado como una pill, sin pintar su fondo en toda la celda. */
class RenderizadorEstado extends JPanel implements TableCellRenderer {

    private final IntSupplier filaHover;
    private final PillLabel badge = new PillLabel();

    RenderizadorEstado(IntSupplier filaHover) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.filaHover = filaHover;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        add(badge);
    }

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                   boolean foco, int fila, int columna) {
        setBackground(fila == filaHover.getAsInt() ? TemaBolsines.LISTA_HOVER : TemaBolsines.SUPERFICIE);
        if (valor instanceof EtiquetaEstado etiqueta) {
            badge.setText(etiqueta.descripcion());
            badge.setBackground(colorEstado(etiqueta.nombre()));
            badge.setForeground(colorTextoEstado(etiqueta.nombre()));
        } else {
            badge.setText(valor == null ? "" : valor.toString());
            badge.setBackground(new Color(0xEFF2F5));
            badge.setForeground(TemaBolsines.TEXTO);
        }
        return this;
    }

    private static Color colorEstado(String nombreEstado) {
        return switch (nombreEstado) {
            case "EnBolsinEnviado" -> new Color(0xEFF6FF);
            case "ParaRedirigir" -> new Color(0xFFFBEB);
            case "Recibida&Aceptada" -> new Color(0xE1F0E8);
            case "Recibida&Rechazada", "NoRecibida", "DeBaja" -> new Color(0xF9E5E3);
            default -> new Color(0xEFF2F5);
        };
    }

    private static Color colorTextoEstado(String nombreEstado) {
        return switch (nombreEstado) {
            case "EnBolsinEnviado" -> new Color(0x1D4ED8);
            case "ParaRedirigir" -> new Color(0xB45309);
            case "Recibida&Aceptada" -> new Color(0x28634A);
            case "Recibida&Rechazada", "NoRecibida", "DeBaja" -> new Color(0x873F3B);
            default -> TemaBolsines.TEXTO;
        };
    }

    private static class PillLabel extends JLabel {
        PillLabel() {
            setFont(TemaBolsines.fuente(12f, java.awt.Font.PLAIN)
                    .deriveFont(Map.of(TextAttribute.WEIGHT, TextAttribute.WEIGHT_MEDIUM)));
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
