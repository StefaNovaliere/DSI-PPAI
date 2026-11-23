package ppai.boundary;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Ventana principal: encabezado con el usuario y un menú de opciones. Al
 * elegir "Registrar recepción de bolsín" se muestra la pantalla del CU 28.
 */
public class PantallaPrincipal extends JFrame {

    private static final String MENU = "menu";
    private static final String RECEPCION = "recepcion";

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final PantallaGraficaRecepcionBolsin pantallaRecepcion;

    /**
     * @param estadoCorreo cómo se envían las notificaciones por correo (se muestra en el menú)
     */
    public PantallaPrincipal(String nombreUsuario, String estadoCorreo,
                             PantallaGraficaRecepcionBolsin pantallaRecepcion) {
        super("Sistema de Bolsines");
        this.pantallaRecepcion = pantallaRecepcion;
        pantallaRecepcion.setAccionVolver(() -> tarjetas.show(contenido, MENU));

        contenido.add(crearMenu(estadoCorreo), MENU);
        contenido.add(pantallaRecepcion, RECEPCION);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaBolsines.FONDO);
        raiz.add(crearEncabezado(nombreUsuario), BorderLayout.NORTH);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1100, pantalla.width), Math.min(760, pantalla.height - 40));
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado(String nombreUsuario) {
        JLabel marca = new JLabel("B", javax.swing.SwingConstants.CENTER);
        marca.setOpaque(true);
        marca.setBackground(TemaBolsines.TURQUESA);
        marca.setForeground(Color.WHITE);
        marca.setFont(TemaBolsines.fuente(19f, Font.BOLD));
        marca.setPreferredSize(new Dimension(38, 38));

        JLabel sistema = new JLabel("Sistema de Bolsines");
        sistema.setForeground(Color.WHITE);
        sistema.setFont(TemaBolsines.fuente(15f, Font.PLAIN));
        JLabel area = new JLabel("GESTIÓN DE RECEPCIONES");
        area.setForeground(new Color(0xB8CAD7));
        area.setFont(TemaBolsines.fuente(10f, Font.PLAIN));

        JPanel textos = new JPanel(new BorderLayout(0, 2));
        textos.setOpaque(false);
        textos.add(sistema, BorderLayout.CENTER);
        textos.add(area, BorderLayout.SOUTH);
        JPanel identidad = new JPanel(new BorderLayout(11, 0));
        identidad.setOpaque(false);
        identidad.add(marca, BorderLayout.WEST);
        identidad.add(textos, BorderLayout.CENTER);

        JLabel etiquetaUsuario = new JLabel("SESIÓN ACTIVA");
        etiquetaUsuario.setForeground(new Color(0xB8CAD7));
        etiquetaUsuario.setFont(TemaBolsines.fuente(10f, Font.PLAIN));
        JLabel usuario = new JLabel(nombreUsuario);
        usuario.setForeground(Color.WHITE);
        usuario.setFont(TemaBolsines.fuente(13f, Font.PLAIN));
        JPanel sesion = new JPanel(new BorderLayout(0, 3));
        sesion.setOpaque(false);
        sesion.add(etiquetaUsuario, BorderLayout.NORTH);
        sesion.add(usuario, BorderLayout.SOUTH);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(TemaBolsines.AZUL);
        encabezado.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        encabezado.add(identidad, BorderLayout.WEST);
        encabezado.add(sesion, BorderLayout.EAST);

        JPanel marco = new JPanel(new BorderLayout());
        marco.add(encabezado, BorderLayout.CENTER);
        JPanel acento = new JPanel();
        acento.setBackground(TemaBolsines.TURQUESA);
        acento.setPreferredSize(new Dimension(1, 3));
        marco.add(acento, BorderLayout.SOUTH);
        return marco;
    }

    private JPanel crearMenu(String estadoCorreo) {
        JLabel seccion = new JLabel("GESTIÓN DE ENVÍOS  ·  CU 28");
        seccion.setForeground(new Color(0xF2FAFC));
        seccion.setFont(TemaBolsines.fuente(10f, Font.PLAIN));
        JLabel titulo = new JLabel("Recepción de bolsines");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(TemaBolsines.fuenteTitulo(29f));
        JLabel descripcion = new JLabel("Registre la llegada y verifique la documentación recibida.");
        descripcion.setForeground(new Color(0xE0EBF1));
        descripcion.setFont(TemaBolsines.fuente(13f, Font.PLAIN));

        JPanel textos = new JPanel(new BorderLayout(0, 9));
        textos.setOpaque(false);
        textos.add(seccion, BorderLayout.NORTH);
        JPanel tituloYDescripcion = new JPanel(new BorderLayout(0, 8));
        tituloYDescripcion.setOpaque(false);
        tituloYDescripcion.add(titulo, BorderLayout.NORTH);
        tituloYDescripcion.add(descripcion, BorderLayout.CENTER);
        textos.add(tituloYDescripcion, BorderLayout.CENTER);

        JPanel hero = TemaBolsines.crearHero(new BorderLayout(20, 0));
        hero.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 24));
        hero.add(textos, BorderLayout.CENTER);
        hero.add(new IlustracionRecepcion(), BorderLayout.EAST);

        JButton iniciar = new JButton("Iniciar recepción  →");
        TemaBolsines.botonPrimario(iniciar);
        iniciar.setPreferredSize(new Dimension(220, 46));
        iniciar.setHorizontalAlignment(JButton.CENTER);
        iniciar.addActionListener(e -> {
            tarjetas.show(contenido, RECEPCION);
            pantallaRecepcion.opcRegistrarRecBolsin();
        });

        boolean correoConfigurado = estadoCorreo != null && estadoCorreo.toLowerCase().contains("smtp");
        JLabel correoIcono = new JLabel("●");
        correoIcono.setForeground(correoConfigurado ? new Color(0x16836A) : TemaBolsines.SECUNDARIO);
        correoIcono.setFont(TemaBolsines.fuente(10f, Font.PLAIN));
        JLabel correo = new JLabel(correoConfigurado
                ? "Notificaciones por correo activas" : "Correo en modo de prueba");
        correo.setForeground(correoConfigurado ? new Color(0x17664F) : TemaBolsines.SECUNDARIO);
        correo.setFont(TemaBolsines.fuente(12f, Font.PLAIN));
        JPanel estadoCorreoPanel = new JPanel(new BorderLayout(7, 0)) {
            {
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            }

            @Override
            protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(correoConfigurado ? new Color(0xEAF6F1) : new Color(0xF1F5F9));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 999, 999);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        estadoCorreoPanel.add(correoIcono, BorderLayout.WEST);
        estadoCorreoPanel.add(correo, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(20, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(18, 4, 2, 4));
        pie.add(iniciar, BorderLayout.WEST);
        JPanel estadoConSeparador = new JPanel(new BorderLayout(14, 0));
        estadoConSeparador.setOpaque(false);
        estadoConSeparador.add(estadoCorreoPanel, BorderLayout.CENTER);
        pie.add(estadoConSeparador, BorderLayout.CENTER);

        JPanel contenidoTarjeta = TemaBolsines.crearTarjeta(new BorderLayout());
        contenidoTarjeta.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));
        contenidoTarjeta.add(hero, BorderLayout.CENTER);
        contenidoTarjeta.add(pie, BorderLayout.SOUTH);

        JPanel menu = new JPanel(new GridBagLayout());
        menu.setBackground(TemaBolsines.FONDO);
        contenidoTarjeta.setPreferredSize(new Dimension(680, 310));
        menu.add(contenidoTarjeta);
        return menu;
    }

    /** Ilustración vectorial de un bolsín y la recepción de un envío. */
    private static class IlustracionRecepcion extends JPanel {
        IlustracionRecepcion() {
            setOpaque(false);
            setPreferredSize(new Dimension(142, 124));
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255, 32));
            g.fillOval(10, 2, 116, 116);
            g.setColor(new Color(255, 255, 255, 225));
            int[] x = {35, 69, 103, 103, 69, 35};
            int[] y = {43, 28, 43, 83, 98, 83};
            g.fillPolygon(x, y, x.length);
            g.setColor(TemaBolsines.AZUL);
            g.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            g.drawLine(69, 29, 69, 98);
            g.drawLine(36, 44, 69, 59);
            g.drawLine(102, 44, 69, 59);
            g.setColor(TemaBolsines.TURQUESA);
            g.setStroke(new java.awt.BasicStroke(3.2f, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            g.drawLine(69, 10, 69, 34);
            g.drawLine(60, 25, 69, 34);
            g.drawLine(78, 25, 69, 34);
            g.dispose();
        }
    }
}
