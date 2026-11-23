package ppai.boundary;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/** Paleta y detalles comunes de las pantallas del Sistema de Bolsines. */
public final class TemaBolsines {

    private static final Font PLUS_JAKARTA_REGULAR = cargarFuente("PlusJakartaSans-Regular.ttf");
    private static final Font PLUS_JAKARTA_SEMIBOLD = cargarFuente("PlusJakartaSans-SemiBold.ttf");
    private static final Font PLUS_JAKARTA_BOLD = PLUS_JAKARTA_SEMIBOLD.deriveFont(Font.BOLD);

    static final Color FONDO = new Color(0xF8FAFC);
    static final Color SUPERFICIE = Color.WHITE;
    static final Color BORDE = new Color(0xE2E8F0);
    static final Color AZUL = new Color(0x0F4C5C);
    static final Color AZUL_HOVER = new Color(0x0A3642);
    static final Color TURQUESA = new Color(0x0F4C5C);
    static final Color TURQUESA_HOVER = new Color(0x0A3642);
    static final Color TEXTO = new Color(0x1E293B);
    static final Color SECUNDARIO = new Color(0x475569);
    static final Color RESALTADO = new Color(0xE6F0F2);
    static final Color LISTA_SELECCIONADA = new Color(0xF0FDFA);
    static final Color LISTA_HOVER = new Color(0xF8FAFC);
    static final Color SOMBRA = new Color(0x0F, 0x17, 0x2A, 13);

    private static final Font INTER = cargarFuente("Inter.ttf");
    private static final Font JETBRAINS_MONO = cargarFuente("JetBrainsMono.ttf");

    private TemaBolsines() {
    }

    public static void configurarUIManager() {
        Font fuenteBase = fuente(14f, Font.PLAIN);
        UIManager.put("defaultFont", fuenteBase);
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("CheckBox.arc", 8);
        UIManager.put("CheckBox.icon.selectedBackground", AZUL);
        UIManager.put("CheckBox.icon.selectedBorderColor", AZUL);
        UIManager.put("CheckBox.icon.checkmarkColor", Color.WHITE);
        UIManager.put("Component.focusColor", new Color(0x62AEB0));
        UIManager.put("Component.borderColor", BORDE);
        UIManager.put("Component.focusedBorderColor", TURQUESA);
        UIManager.put("Component.disabledForeground", new Color(0x405566));
        UIManager.put("Button.disabledBackground", new Color(0xE1E9EE));
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("TitlePane.background", AZUL);
        UIManager.put("TitlePane.foreground", Color.WHITE);
        UIManager.put("TitlePane.font", fuente(12f, Font.PLAIN));
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Label.font", fuenteBase);
        UIManager.put("Button.font", fuenteBase);
        UIManager.put("Button.disabledText", new Color(0x405566));
        UIManager.put("ToggleButton.font", fuenteBase);
        UIManager.put("CheckBox.font", fuenteBase);
        UIManager.put("ComboBox.font", fuenteBase);
        UIManager.put("TextField.font", fuenteBase);
        UIManager.put("TextField.background", SUPERFICIE);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.caretForeground", TURQUESA);
        UIManager.put("FormattedTextField.font", fuenteBase);
        UIManager.put("PasswordField.font", fuenteBase);
        UIManager.put("TextArea.font", fuenteBase);
        UIManager.put("TextPane.font", fuenteBase);
        UIManager.put("EditorPane.font", fuenteBase);
        UIManager.put("RadioButton.font", fuenteBase);
        UIManager.put("RadioButton.foreground", TEXTO);
        UIManager.put("RadioButton.disabledText", new Color(0x405566));
        UIManager.put("List.font", fuenteBase);
        UIManager.put("Table.font", fuenteBase);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.background", SUPERFICIE);
        UIManager.put("Table.gridColor", new Color(0xE7EDF1));
        UIManager.put("TableHeader.font", fuenteBase);
        UIManager.put("TableHeader.background", new Color(0xEDF2F6));
        UIManager.put("TableHeader.foreground", new Color(0x34495B));
        UIManager.put("Table.selectionBackground", new Color(0xDDECEF));
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("List.selectionBackground", RESALTADO);
        UIManager.put("List.selectionForeground", AZUL);
        UIManager.put("Menu.font", fuenteBase);
        UIManager.put("MenuItem.font", fuenteBase);
        UIManager.put("CheckBoxMenuItem.font", fuenteBase);
        UIManager.put("RadioButtonMenuItem.font", fuenteBase);
        UIManager.put("TabbedPane.font", fuenteBase);
        UIManager.put("ToolTip.font", fuente(12f, Font.PLAIN));
        UIManager.put("TitledBorder.font", fuente(13f, Font.PLAIN));
        UIManager.put("Spinner.font", fuenteBase);
        UIManager.put("ProgressBar.font", fuenteBase);
        UIManager.put("OptionPane.messageFont", fuenteBase);
        UIManager.put("OptionPane.buttonFont", fuenteBase);
    }

    static Font fuente(float tamano, int estilo) {
        boolean titulo = (estilo & Font.BOLD) != 0;
        Font base = titulo ? PLUS_JAKARTA_BOLD : INTER;
        Font fuente = base.deriveFont(estilo & Font.ITALIC, tamano);
        return titulo ? fuente.deriveFont(Map.of(TextAttribute.TRACKING, -0.02f)) : fuente;
    }

    static Font fuenteTitulo(float tamano) {
        Font peso = tamano >= 24f ? PLUS_JAKARTA_BOLD.deriveFont(Font.BOLD, tamano)
                : PLUS_JAKARTA_SEMIBOLD.deriveFont(Font.PLAIN, tamano);
        return peso
                .deriveFont(Map.of(TextAttribute.TRACKING, -0.02f));
    }

    static Font fuenteCodigo(float tamano) {
        return JETBRAINS_MONO.deriveFont(Map.of(
                TextAttribute.SIZE, tamano, TextAttribute.WEIGHT, TextAttribute.WEIGHT_MEDIUM));
    }

    private static Font cargarFuente(String nombreArchivo) {
        try (InputStream archivo = TemaBolsines.class.getResourceAsStream("/fonts/" + nombreArchivo)) {
            if (archivo == null) {
                throw new IllegalStateException("No se encontró la fuente " + nombreArchivo);
            }
            return Font.createFont(Font.TRUETYPE_FONT, archivo);
        } catch (java.awt.FontFormatException | IOException e) {
            throw new IllegalStateException("No se pudo cargar " + nombreArchivo, e);
        }
    }

    static void tarjeta(JComponent componente) {
        componente.setBackground(SUPERFICIE);
        componente.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
    }

    static JPanel crearTarjeta(LayoutManager layout) {
        return new JPanel(layout) {
            {
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int ancho = getWidth() - 1;
                int alto = getHeight() - 5;
                g.setColor(SOMBRA);
                g.fillRoundRect(1, 4, ancho - 1, alto, 12, 12);
                g.setColor(SUPERFICIE);
                g.fillRoundRect(0, 0, ancho - 1, alto - 4, 12, 12);
                g.setColor(BORDE);
                g.drawRoundRect(0, 0, ancho - 2, alto - 5, 12, 12);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
    }

    static JPanel crearTarjetaModal(LayoutManager layout) {
        return new JPanel(layout) {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int capa = 10; capa >= 1; capa--) {
                    int alpha = Math.max(2, 40 - capa * 3);
                    int expansion = capa / 3;
                    g.setColor(new Color(0x0F, 0x17, 0x2A, alpha));
                    g.fillRoundRect(4 - expansion, 8 + capa / 2,
                            getWidth() - 8 + expansion * 2, getHeight() - 18 + expansion * 2, 16, 16);
                }
                g.setColor(SUPERFICIE);
                g.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 16, 16, 16);
                g.setColor(BORDE);
                g.drawRoundRect(4, 4, getWidth() - 8, getHeight() - 16, 16, 16);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
    }

    static JPanel crearHero(LayoutManager layout) {
        return new JPanel(layout) {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new java.awt.GradientPaint(0, 0, AZUL, getWidth(), getHeight(), new Color(0x176C72)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
    }

    static void botonPrimario(JButton boton) {
        boton.setFont(fuente(13f, Font.PLAIN));
        boton.setForeground(Color.WHITE);
        boton.setBackground(TURQUESA);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(TURQUESA_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(boton.isEnabled() ? TURQUESA : new Color(0xE1E9EE));
            }
        });
    }

    static void botonSecundario(JButton boton) {
        boton.setFont(fuente(12f, Font.PLAIN));
        boton.setForeground(AZUL);
        boton.setBackground(SUPERFICIE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(RESALTADO);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(SUPERFICIE);
            }
        });
    }

    static void campo(JTextField campo) {
        campo.setFont(fuente(13f, Font.PLAIN));
        campo.setForeground(TEXTO);
        campo.setBackground(SUPERFICIE);
        campo.setCaretColor(TURQUESA);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(8, 9, 8, 9)));
    }
}
