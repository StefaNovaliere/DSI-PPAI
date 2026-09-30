package ppai.boundary;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
    private static final Color AZUL_OSCURO = new Color(0x1F3A5F);

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final PantallaGraficaRecepcionBolsin pantallaRecepcion;

    public PantallaPrincipal(String nombreUsuario, PantallaGraficaRecepcionBolsin pantallaRecepcion) {
        super("Sistema de Bolsines");
        this.pantallaRecepcion = pantallaRecepcion;
        pantallaRecepcion.setAccionVolver(() -> tarjetas.show(contenido, MENU));

        contenido.add(crearMenu(), MENU);
        contenido.add(pantallaRecepcion, RECEPCION);

        JPanel raiz = new JPanel(new BorderLayout());
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
        JLabel sistema = new JLabel("Sistema de Bolsines");
        sistema.setForeground(Color.WHITE);
        sistema.setFont(sistema.getFont().deriveFont(Font.BOLD, 15f));
        JLabel usuario = new JLabel("Usuario: " + nombreUsuario);
        usuario.setForeground(new Color(0xDDE6F0));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(AZUL_OSCURO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        encabezado.add(sistema, BorderLayout.WEST);
        encabezado.add(usuario, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearMenu() {
        JLabel titulo = new JLabel("Seleccione una tarea");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JButton opcionRecepcion = new JButton("<html><div style='padding:6px; width:300px'>"
                + "<span style='font-size:13px'><b>Registrar recepción de bolsín</b></span><br><br>"
                + "Registre la llegada de los bolsines que otras Comisiones Médicas enviaron a la suya "
                + "y verifique su contenido.</div></html>");
        opcionRecepcion.setPreferredSize(new Dimension(420, 110));
        opcionRecepcion.setMaximumSize(new Dimension(420, 110));
        opcionRecepcion.setHorizontalAlignment(JButton.LEFT);
        opcionRecepcion.addActionListener(e -> {
            tarjetas.show(contenido, RECEPCION);
            pantallaRecepcion.opcRegistrarRecBolsin();
        });

        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        opcionRecepcion.setAlignmentX(LEFT_ALIGNMENT);
        columna.add(titulo);
        columna.add(javax.swing.Box.createVerticalStrut(16));
        columna.add(opcionRecepcion);

        JPanel menu = new JPanel(new GridBagLayout());
        menu.add(columna);
        return menu;
    }
}
