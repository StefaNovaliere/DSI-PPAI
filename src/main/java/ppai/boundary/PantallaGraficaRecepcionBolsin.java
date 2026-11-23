package ppai.boundary;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.RootPaneContainer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ppai.control.GestorNotificacionCU29;
import ppai.control.GestorRecepcionBolsin;
import ppai.dto.DatosBolsin;
import ppai.dto.DatosDocumentacion;
import ppai.dto.DatosRemito;

/**
 * Pantalla del CU 28 Registrar Recepción de Bolsín (Swing), con un diseño
 * maestro-detalle: a la izquierda los bolsines por recibir (con un filtro) y a
 * la derecha el contenido del bolsín seleccionado.
 *
 * <p>Cada acción del usuario se traduce en el mensaje del diagrama de
 * secuencia hacia el gestor:
 * <ul>
 *   <li>elegir la opción del menú → {@code registrarNuevoRecBolsin()}</li>
 *   <li>botón "Buscar" del filtro → {@code tomarFiltroBolsines()}</li>
 *   <li>hacer clic en un bolsín de la lista → {@code tomarSeleccionBolsin()}</li>
 *   <li>elegir una de las cuatro opciones de recepción y presionar "Registrar recepción" →
 *       {@code tomarSeleccionPrimeraOpcion()} ... {@code tomarSeleccionCuartaOpcion()}</li>
 *   <li>documentación marcada en la tabla (opciones 2 a 4) → {@code tomarSeleccionDocumentacion()}</li>
 *   <li>diálogo de confirmación → {@code tomarConfirmacion()}</li>
 * </ul>
 *
 * <p>Cuando el caso de uso termina, la pantalla vuelve a cargar la lista de
 * bolsines pendientes (una nueva ejecución del CU), así el usuario puede
 * seguir con el próximo bolsín sin volver al menú.
 */
public class PantallaGraficaRecepcionBolsin extends JPanel implements PantallaRecepcionBolsin {

    private static final String LISTA = "lista";
    private static final String VACIO = "vacio";
    private static final String DETALLE = "detalle";

    private static final int COL_MARCA = 0;
    private static final int COL_DOC = 2;
    private static final int COL_ESTADO = 5;

    /** Las cuatro opciones del paso 7, con lo que se le pide marcar al usuario. */
    private enum Opcion {
        PRIMERA("1. El contenido del bolsín es igual al registrado", "",
                "Todos los remitos y documentos se registrarán como recibidos y aceptados.",
                "Contenido completo", "Registrar todo como recibido y aceptado."),
        SEGUNDA("2. No se recibe toda la documentación", "No llegó",
                "Marque en la tabla la documentación que no llegó.",
                "Falta documentación", "Marcar los documentos que no llegaron."),
        TERCERA("3. Hay documentación que no corresponde a esta CM", "No corresponde",
                "Marque en la tabla la documentación que no corresponde a esta Comisión Médica.",
                "No corresponde", "Marcar los documentos que pertenecen a otra CM."),
        CUARTA("4. La documentación se debe redirigir a otra área", "Redirigir",
                "Marque en la tabla la documentación que se debe redirigir a otra área de la CMC.",
                "Redirigir", "Marcar los documentos que deben ir a otra área.");

        private final String texto;
        private final String columna;
        private final String indicacion;
        private final String tituloCorto;
        private final String descripcionCorta;

        Opcion(String texto, String columna, String indicacion, String tituloCorto, String descripcionCorta) {
            this.texto = texto;
            this.columna = columna;
            this.indicacion = indicacion;
            this.tituloCorto = tituloCorto;
            this.descripcionCorta = descripcionCorta;
        }
    }

    private GestorRecepcionBolsin gestor;
    private Runnable accionVolver = () -> { };

    // Encabezado y aviso de resultado
    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JPanel panelAviso = new JPanel(new BorderLayout(10, 0));
    private final JLabel lblAviso = new JLabel();
    private final List<String> lineasAviso = new ArrayList<>();

    // Maestro: filtro y bolsines por recibir
    private final JTextField txtPrecinto = new JTextField();
    private final JTextField txtCMOrigen = new JTextField();
    private final DefaultListModel<DatosBolsin> modeloBolsines = new DefaultListModel<>();
    private final JList<DatosBolsin> listaBolsines = new JList<>(modeloBolsines);
    private int indiceBolsinHover = -1;
    private final JLabel lblBolsines = new JLabel("Bolsines por recibir");
    private final CardLayout tarjetasLista = new CardLayout();
    private final JPanel panelLista = new JPanel(tarjetasLista);

    // Detalle: contenido del bolsín seleccionado
    private final CardLayout tarjetasDetalle = new CardLayout();
    private final JPanel panelDetalle = new JPanel(tarjetasDetalle);
    private final JLabel lblDetalle = new JLabel(" ");
    private final JPanel panelRemitos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private int filaHoverTabla = -1;
    private JPanel tarjetaDetalleBolsin;
    private JScrollPane scrollTablaDocumentos;
    private final DefaultTableModel modeloDocumentos = new DefaultTableModel(
            new Object[] {"", "Remito", "N° Doc", "Tipo", "Asunto", "Estado"}, 0) {
        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == COL_MARCA ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == COL_MARCA && marcadoHabilitado;
        }
    };
    private final JTable tablaDocumentos = new JTable(modeloDocumentos);

    // Opciones de recepción
    private final Map<Opcion, JRadioButton> botonesOpcion = new LinkedHashMap<>();
    private final JLabel lblIndicacion = new JLabel(" ");
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnRegistrar = new JButton("Registrar recepción");

    // Datos de la ejecución actual del caso de uso
    private final Map<Integer, DatosDocumentacion> documentos = new LinkedHashMap<>();
    private int cantidadRemitos;
    private DatosBolsin bolsinEnPantalla;
    private boolean marcadoHabilitado;
    private boolean recepcionRegistrada;
    private boolean listaMostrada;
    private boolean llenandoLista;
    private boolean seleccionAutomatica;
    private boolean reiniciando;
    private boolean filtrando;
    private boolean filtroSinResultados;
    private Integer bolsinAReseleccionar;

    public PantallaGraficaRecepcionBolsin() {
        super(new BorderLayout());
        setBackground(TemaBolsines.FONDO);

        JPanel norte = new JPanel(new BorderLayout(0, 8));
        norte.add(crearEncabezado(), BorderLayout.NORTH);
        norte.add(crearAviso(), BorderLayout.SOUTH);

        JPanel columnas = new JPanel(new BorderLayout(18, 0));
        columnas.setOpaque(false);
        JPanel maestro = crearMaestro();
        maestro.setPreferredSize(new Dimension(360, 0));
        columnas.add(maestro, BorderLayout.WEST);

        JPanel columnaDerecha = new PanelColumnaDesplazable();
        columnaDerecha.setLayout(new javax.swing.BoxLayout(columnaDerecha, javax.swing.BoxLayout.Y_AXIS));
        columnaDerecha.setOpaque(false);
        columnaDerecha.add(crearDetalle());
        columnaDerecha.add(javax.swing.Box.createVerticalStrut(14));
        JPanel barraAcciones = crearBarraAcciones();
        barraAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        java.awt.Dimension tamanoBarra = barraAcciones.getPreferredSize();
        barraAcciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, tamanoBarra.height));
        columnaDerecha.add(barraAcciones);
        columnaDerecha.add(javax.swing.Box.createVerticalGlue());
        JScrollPane scrollDerecha = new JScrollPane(columnaDerecha,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDerecha.setBorder(BorderFactory.createEmptyBorder());
        scrollDerecha.setOpaque(false);
        scrollDerecha.getViewport().setOpaque(false);
        columnas.add(scrollDerecha, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        contenido.setMaximumSize(new Dimension(1400, Integer.MAX_VALUE));
        contenido.add(norte, BorderLayout.NORTH);
        contenido.add(columnas, BorderLayout.CENTER);

        JPanel centrado = new JPanel(new GridBagLayout());
        centrado.setBackground(TemaBolsines.FONDO);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        centrado.add(contenido, constraints);
        add(centrado, BorderLayout.CENTER);

        habilitarAcciones(false);
    }

    // ------------------------------------------------------------------
    // Construcción de la pantalla
    // ------------------------------------------------------------------

    private JPanel crearEncabezado() {
        JButton btnVolver = new JButton("←  Menú principal");
        TemaBolsines.botonSecundario(btnVolver);
        btnVolver.addActionListener(e -> accionVolver.run());
        JLabel sobrelinea = new JLabel("OPERACIONES  /  CU 28");
        sobrelinea.setForeground(TemaBolsines.TURQUESA);
        sobrelinea.setFont(TemaBolsines.fuente(10f, Font.PLAIN));
        JLabel titulo = new JLabel("Registrar recepción de bolsín");
        titulo.setFont(TemaBolsines.fuenteTitulo(24f));
        titulo.setForeground(TemaBolsines.AZUL);
        lblSubtitulo.setForeground(TemaBolsines.SECUNDARIO);
        lblSubtitulo.setFont(TemaBolsines.fuente(12f, Font.PLAIN));

        JPanel titulos = new JPanel(new BorderLayout(0, 4));
        titulos.setOpaque(false);
        titulos.add(titulo, BorderLayout.NORTH);
        titulos.add(lblSubtitulo, BorderLayout.CENTER);
        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);
        textos.add(sobrelinea, BorderLayout.NORTH);
        textos.add(titulos, BorderLayout.CENTER);
        JPanel encabezado = new JPanel(new BorderLayout(18, 0));
        encabezado.setOpaque(false);
        encabezado.add(btnVolver, BorderLayout.WEST);
        encabezado.add(textos, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel crearAviso() {
        lblAviso.setVerticalAlignment(SwingConstants.TOP);
        lblAviso.setFont(TemaBolsines.fuente(12f, Font.PLAIN));
        panelAviso.add(lblAviso, BorderLayout.CENTER);
        panelAviso.setOpaque(true);
        panelAviso.setVisible(false);
        return panelAviso;
    }

    private JPanel crearMaestro() {
        listaBolsines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaBolsines.setFixedCellHeight(76);
        listaBolsines.setBackground(TemaBolsines.SUPERFICIE);
        listaBolsines.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        listaBolsines.setToolTipText("Elegí un bolsín para consultar su contenido");
        listaBolsines.setCellRenderer(new RenderizadorBolsin());
        listaBolsines.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int nuevoIndice = listaBolsines.locationToIndex(e.getPoint());
                if (nuevoIndice >= 0 && !listaBolsines.getCellBounds(nuevoIndice, nuevoIndice).contains(e.getPoint())) {
                    nuevoIndice = -1;
                }
                if (indiceBolsinHover != nuevoIndice) {
                    indiceBolsinHover = nuevoIndice;
                    listaBolsines.repaint();
                }
            }
        });
        listaBolsines.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                indiceBolsinHover = -1;
                listaBolsines.repaint();
            }
        });
        listaBolsines.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !llenandoLista && listaBolsines.getSelectedValue() != null) {
                tomarSeleccionBolsin(listaBolsines.getSelectedValue());
            }
        });
        JLabel sinBolsines = new JLabel("<html><div style='text-align:center'>No hay bolsines pendientes"
                + "<br>de recepción.</div></html>", SwingConstants.CENTER);
        sinBolsines.setForeground(TemaBolsines.SECUNDARIO);
        sinBolsines.setFont(TemaBolsines.fuente(13f, Font.PLAIN));
        JScrollPane scrollLista = new JScrollPane(listaBolsines);
        scrollLista.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLista.setBorder(BorderFactory.createEmptyBorder());
        panelLista.setBackground(TemaBolsines.SUPERFICIE);
        panelLista.setBorder(BorderFactory.createLineBorder(TemaBolsines.BORDE));
        panelLista.add(scrollLista, LISTA);
        panelLista.add(sinBolsines, VACIO);

        lblBolsines.setFont(TemaBolsines.fuente(13f, Font.PLAIN));
        lblBolsines.setForeground(TemaBolsines.AZUL);
        JPanel arriba = new JPanel(new BorderLayout(0, 9));
        arriba.setOpaque(false);
        arriba.add(lblBolsines, BorderLayout.NORTH);
        arriba.add(crearFiltro(), BorderLayout.CENTER);

        JPanel maestro = new JPanel(new BorderLayout(0, 10));
        maestro.setBackground(TemaBolsines.FONDO);
        maestro.setBorder(BorderFactory.createEmptyBorder());
        maestro.add(arriba, BorderLayout.NORTH);
        maestro.add(panelLista, BorderLayout.CENTER);
        return maestro;
    }

    /** Paso 4: filtrar por número de precinto o por CM origen. */
    private JPanel crearFiltro() {
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        TemaBolsines.botonPrimario(btnBuscar);
        TemaBolsines.botonSecundario(btnLimpiar);
        TemaBolsines.campo(txtPrecinto);
        txtPrecinto.setFont(TemaBolsines.fuenteCodigo(13f));
        TemaBolsines.campo(txtCMOrigen);
        txtPrecinto.putClientProperty("JTextField.placeholderText", "Ej: PR-0101");
        txtCMOrigen.putClientProperty("JTextField.placeholderText", "Nombre o parte del nombre");
        txtPrecinto.setToolTipText("Número de precinto completo, por ejemplo PR-0101");
        txtCMOrigen.setToolTipText("Nombre o parte del nombre de la CM origen");
        btnBuscar.addActionListener(e -> tomarFiltroBolsines());
        btnLimpiar.addActionListener(e -> {
            limpiarFiltro();
            tomarFiltroBolsines();
        });
        txtPrecinto.addActionListener(e -> tomarFiltroBolsines());
        txtCMOrigen.addActionListener(e -> tomarFiltroBolsines());

        JPanel filtro = TemaBolsines.crearTarjeta(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 2, 4, 2);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel titulo = new JLabel("Filtrar bolsines");
        titulo.setFont(TemaBolsines.fuenteTitulo(15f));
        titulo.setForeground(TemaBolsines.AZUL);
        filtro.add(titulo, c);
        c.gridy = 1;
        JLabel etiquetaPrecinto = new JLabel("Precinto");
        etiquetaPrecinto.setFont(TemaBolsines.fuente(11f, Font.PLAIN));
        etiquetaPrecinto.setForeground(TemaBolsines.SECUNDARIO);
        filtro.add(etiquetaPrecinto, c);
        c.gridy = 2;
        filtro.add(txtPrecinto, c);
        c.gridy = 3;
        JLabel etiquetaOrigen = new JLabel("Comisión médica origen");
        etiquetaOrigen.setFont(TemaBolsines.fuente(11f, Font.PLAIN));
        etiquetaOrigen.setForeground(TemaBolsines.SECUNDARIO);
        filtro.add(etiquetaOrigen, c);
        c.gridy = 4;
        filtro.add(txtCMOrigen, c);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnLimpiar);
        botones.add(btnBuscar);
        c.gridy = 5;
        c.insets = new Insets(8, 2, 2, 2);
        filtro.add(botones, c);
        return filtro;
    }

    private JPanel crearDetalle() {
        tablaDocumentos.setRowHeight(52);
        tablaDocumentos.setRowSelectionAllowed(false);
        tablaDocumentos.setFillsViewportHeight(true);
        tablaDocumentos.setShowVerticalLines(false);
        tablaDocumentos.setShowHorizontalLines(true);
        tablaDocumentos.setIntercellSpacing(new Dimension(0, 1));
        tablaDocumentos.getTableHeader().setReorderingAllowed(false);
        tablaDocumentos.getTableHeader().setPreferredSize(new Dimension(1, 36));
        tablaDocumentos.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaBolsines.BORDE));
        tablaDocumentos.setDefaultRenderer(Object.class, new RenderizadorCelda());
        tablaDocumentos.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int nuevaFila = tablaDocumentos.rowAtPoint(e.getPoint());
                if (filaHoverTabla != nuevaFila) {
                    filaHoverTabla = nuevaFila;
                    tablaDocumentos.repaint();
                }
            }
        });
        tablaDocumentos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                filaHoverTabla = -1;
                tablaDocumentos.repaint();
            }
        });
        int[] anchos = {95, 55, 55, 140, 240, 140};
        for (int i = 0; i < anchos.length; i++) {
            tablaDocumentos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tablaDocumentos.getColumnModel().getColumn(COL_ESTADO)
                .setCellRenderer(new RenderizadorEstado(() -> filaHoverTabla));
        tablaDocumentos.getColumnModel().getColumn(1).setCellRenderer(new RenderizadorCodigo());
        tablaDocumentos.getColumnModel().getColumn(COL_DOC).setCellRenderer(new RenderizadorCodigo());
        tablaDocumentos.getColumnModel().getColumn(COL_MARCA)
                .setCellRenderer(new RenderizadorMarca(tablaDocumentos.getDefaultRenderer(Boolean.class)));
        lblDetalle.setFont(TemaBolsines.fuenteTitulo(19f));
        lblDetalle.setForeground(TemaBolsines.AZUL);
        panelRemitos.setOpaque(false);

        JPanel titulos = new JPanel(new BorderLayout(0, 8));
        titulos.setOpaque(false);
        titulos.add(lblDetalle, BorderLayout.NORTH);
        titulos.add(panelRemitos, BorderLayout.CENTER);
        tarjetaDetalleBolsin = TemaBolsines.crearTarjeta(new BorderLayout(0, 12));
        tarjetaDetalleBolsin.add(titulos, BorderLayout.NORTH);
        scrollTablaDocumentos = new JScrollPane(tablaDocumentos);
        scrollTablaDocumentos.setBorder(BorderFactory.createLineBorder(TemaBolsines.BORDE));
        tarjetaDetalleBolsin.add(scrollTablaDocumentos, BorderLayout.CENTER);

        JPanel detalleApilado = new JPanel();
        detalleApilado.setLayout(new javax.swing.BoxLayout(detalleApilado, javax.swing.BoxLayout.Y_AXIS));
        detalleApilado.setOpaque(false);
        detalleApilado.add(tarjetaDetalleBolsin);
        detalleApilado.add(javax.swing.Box.createVerticalGlue());

        panelDetalle.setBackground(TemaBolsines.FONDO);
        panelDetalle.setBorder(BorderFactory.createEmptyBorder());
        panelDetalle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelDetalle.add(crearEstadoSinSeleccion(), VACIO);
        panelDetalle.add(detalleApilado, DETALLE);
        return panelDetalle;
    }

    private JPanel crearEstadoSinSeleccion() {
        JLabel paso = new JLabel("SIGUIENTE PASO");
        paso.setForeground(TemaBolsines.TURQUESA);
        paso.setFont(TemaBolsines.fuente(10f, Font.BOLD));
        JLabel titulo = new JLabel("Elegí un bolsín de la lista");
        titulo.setForeground(TemaBolsines.AZUL);
        titulo.setFont(TemaBolsines.fuenteTitulo(21f));
        JLabel descripcion = new JLabel("Acá vas a ver sus remitos y documentos para registrar la recepción.");
        descripcion.setForeground(TemaBolsines.SECUNDARIO);
        descripcion.setFont(TemaBolsines.fuente(13f, Font.PLAIN));

        JPanel textos = new JPanel(new BorderLayout(0, 7));
        textos.setOpaque(false);
        textos.add(paso, BorderLayout.NORTH);
        JPanel tituloYDescripcion = new JPanel(new BorderLayout(0, 6));
        tituloYDescripcion.setOpaque(false);
        tituloYDescripcion.add(titulo, BorderLayout.NORTH);
        tituloYDescripcion.add(descripcion, BorderLayout.CENTER);
        textos.add(tituloYDescripcion, BorderLayout.CENTER);

        JLabel numeroPaso = new JLabel("01", SwingConstants.CENTER);
        numeroPaso.setOpaque(true);
        numeroPaso.setBackground(TemaBolsines.RESALTADO);
        numeroPaso.setForeground(TemaBolsines.TURQUESA);
        numeroPaso.setFont(TemaBolsines.fuente(17f, Font.BOLD));
        numeroPaso.setPreferredSize(new Dimension(50, 50));
        numeroPaso.setBorder(BorderFactory.createLineBorder(new Color(0xC8E0E2)));

        JPanel contenido = new JPanel(new BorderLayout(16, 0));
        contenido.setOpaque(false);
        contenido.add(numeroPaso, BorderLayout.WEST);
        contenido.add(textos, BorderLayout.CENTER);

        JPanel tarjeta = TemaBolsines.crearTarjeta(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(20, 28, 20, 28);
        tarjeta.add(contenido, constraints);
        return tarjeta;
    }

    /** Paso 7: las cuatro opciones de recepción y el botón para registrarla. */
    private JPanel crearBarraAcciones() {
        JPanel opciones = new JPanel(new GridLayout(2, 2, 12, 10));
        opciones.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (Opcion opcion : Opcion.values()) {
            JRadioButton boton = new JRadioButton("<html><b>" + (opcion.ordinal() + 1) + " · "
                    + opcion.tituloCorto + "</b><br><span style='color:#475569'>"
                    + opcion.descripcionCorta + "</span></html>");
            boton.setFont(TemaBolsines.fuente(12f, Font.PLAIN));
            boton.setForeground(TemaBolsines.TEXTO);
            boton.setBackground(TemaBolsines.SUPERFICIE);
            boton.setOpaque(true);
            boton.setHorizontalAlignment(SwingConstants.LEFT);
            boton.setRolloverEnabled(true);
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(TemaBolsines.BORDE),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            boton.addActionListener(e -> elegirOpcion(opcion));
            boton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    actualizarEstiloOpciones();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    actualizarEstiloOpciones();
                }
            });
            grupo.add(boton);
            opciones.add(boton);
            botonesOpcion.put(opcion, boton);
        }
        JLabel titulo = new JLabel("Seleccioná cómo llegó el contenido");
        titulo.setFont(TemaBolsines.fuenteTitulo(17f));
        titulo.setForeground(TemaBolsines.AZUL);
        lblIndicacion.setForeground(TemaBolsines.TURQUESA);
        lblIndicacion.setFont(TemaBolsines.fuente(11f, Font.PLAIN));

        TemaBolsines.botonPrimario(btnRegistrar);
        btnRegistrar.addActionListener(e -> tomarSeleccionOpcion());
        lblResumen.setForeground(TemaBolsines.SECUNDARIO);
        lblResumen.setFont(TemaBolsines.fuente(12f, Font.BOLD));
        lblResumen.setHorizontalAlignment(SwingConstants.LEFT);
        JPanel informacion = new JPanel(new BorderLayout(0, 4));
        informacion.setOpaque(false);
        informacion.add(lblResumen, BorderLayout.NORTH);
        informacion.add(lblIndicacion, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(16, 0));
        pie.setOpaque(false);
        pie.add(informacion, BorderLayout.CENTER);
        pie.add(btnRegistrar, BorderLayout.EAST);

        JPanel barra = TemaBolsines.crearTarjeta(new BorderLayout(0, 14));
        barra.add(titulo, BorderLayout.NORTH);
        barra.add(opciones, BorderLayout.CENTER);
        barra.add(pie, BorderLayout.SOUTH);
        elegirOpcion(Opcion.PRIMERA);
        return barra;
    }

    // ------------------------------------------------------------------
    // Acciones del usuario -> gestor
    // ------------------------------------------------------------------

    @Override
    public void setGestor(GestorRecepcionBolsin gestor) {
        this.gestor = gestor;
    }

    public void setAccionVolver(Runnable accionVolver) {
        this.accionVolver = accionVolver;
    }

    /** El usuario eligió la opción del menú: comienza el caso de uso. */
    @Override
    public void opcRegistrarRecBolsin() {
        habilitarPantalla();
        bolsinAReseleccionar = null;
        iniciarEjecucion();
    }

    private void habilitarPantalla() {
        ocultarAviso();
        limpiarDetalle();
        tarjetasDetalle.show(panelDetalle, VACIO);
    }

    private void iniciarEjecucion() {
        listaMostrada = false;
        recepcionRegistrada = false;
        limpiarFiltro();
        habilitarAcciones(false);
        gestor.registrarNuevoRecBolsin();
    }

    /** Al terminar el CU se vuelve a ejecutar para seguir con otro bolsín, conservando el aviso. */
    private void reiniciarEjecucion() {
        reiniciando = true;
        try {
            iniciarEjecucion();
        } finally {
            reiniciando = false;
        }
    }

    private void tomarFiltroBolsines() {
        habilitarPantalla();
        habilitarAcciones(false);
        filtrando = true;
        filtroSinResultados = false;
        try {
            gestor.tomarFiltroBolsines(txtPrecinto.getText(), txtCMOrigen.getText());
        } finally {
            filtrando = false;
        }
        if (filtroSinResultados) {
            // Flujos A1 y A2: el gestor vuelve a mostrar todos los bolsines
            limpiarFiltro();
        }
    }

    private void tomarSeleccionBolsin(DatosBolsin bolsin) {
        if (!seleccionAutomatica) {
            ocultarAviso();
        }
        limpiarDetalle();
        bolsinEnPantalla = bolsin;
        lblDetalle.setText("Contenido del bolsín N° " + bolsin.numeroBolsin() + "  ·  precinto "
                + bolsin.numeroPrecinto() + "  ·  desde " + bolsin.cmOrigen());
        tarjetasDetalle.show(panelDetalle, DETALLE);
        habilitarAcciones(false);
        gestor.tomarSeleccionBolsin(bolsin.numeroBolsin());
    }

    /** Botón "Registrar recepción": le informa al gestor la opción elegida. */
    private void tomarSeleccionOpcion() {
        Opcion opcion = opcionElegida();
        habilitarAcciones(false);
        switch (opcion) {
            case PRIMERA -> gestor.tomarSeleccionPrimeraOpcion();
            case SEGUNDA -> gestor.tomarSeleccionSegundaOpcion();
            case TERCERA -> gestor.tomarSeleccionTerceraOpcion();
            case CUARTA -> gestor.tomarSeleccionCuartaOpcion();
        }
    }

    private void tomarSeleccionDocumentacion(List<Integer> numerosDocumentacion) {
        gestor.tomarSeleccionDocumentacion(numerosDocumentacion);
    }

    private void tomarConfirmacion(boolean confirma) {
        gestor.tomarConfirmacion(confirma);
    }

    // ------------------------------------------------------------------
    // Mensajes del gestor -> pantalla
    // ------------------------------------------------------------------

    @Override
    public void mostrarCM(String nombreCM) {
        lblSubtitulo.setText("Bolsines enviados a " + nombreCM + " que todavía no se recibieron. "
                + "Seleccione el que llegó y verifique su contenido.");
    }

    @Override
    public void solicitarSelBolsin(List<DatosBolsin> bolsines) {
        listaMostrada = true;
        llenandoLista = true;
        modeloBolsines.clear();
        for (DatosBolsin bolsin : bolsines) {
            modeloBolsines.addElement(bolsin);
        }
        llenandoLista = false;
        lblBolsines.setText("Bolsines por recibir (" + bolsines.size() + ")");
        tarjetasLista.show(panelLista, bolsines.isEmpty() ? VACIO : LISTA);

        // Después de un error, se vuelve a mostrar el mismo bolsín
        if (bolsinAReseleccionar != null) {
            for (int i = 0; i < modeloBolsines.size(); i++) {
                if (modeloBolsines.get(i).numeroBolsin() == bolsinAReseleccionar) {
                    seleccionAutomatica = true;
                    listaBolsines.setSelectedIndex(i);
                    seleccionAutomatica = false;
                }
            }
            bolsinAReseleccionar = null;
        }
    }

    @Override
    public void mostrarNroRemito(List<Integer> numerosRemito) {
        cantidadRemitos = numerosRemito.size();
    }

    @Override
    public void mostrarDatosDocumentacion(List<DatosRemito> remitos) {
        for (DatosRemito remito : remitos) {
            for (DatosDocumentacion doc : remito.documentacion()) {
                documentos.put(doc.numero(), doc);
                modeloDocumentos.addRow(new Object[] {
                    Boolean.FALSE, remito.numero(), doc.numero(), doc.tipoDocumento(), doc.asunto(), etiqueta(doc)});
            }
        }
        ajustarAlturaTarjetaDetalle();
        mostrarEstadoRemitos(remitos);
    }

    private void ajustarAlturaTarjetaDetalle() {
        int altoTabla = 36 + tablaDocumentos.getRowCount() * tablaDocumentos.getRowHeight() + 2;
        int altoMaximoTabla = 420;
        int altoVisibleTabla = Math.min(altoTabla, altoMaximoTabla);
        int altoTarjeta = altoVisibleTabla + 112;
        scrollTablaDocumentos.setPreferredSize(new Dimension(500, altoVisibleTabla));
        tarjetaDetalleBolsin.setPreferredSize(new Dimension(700, altoTarjeta));
        tarjetaDetalleBolsin.setMaximumSize(new Dimension(Integer.MAX_VALUE, altoTarjeta));
        tarjetaDetalleBolsin.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelDetalle.setPreferredSize(new Dimension(700, altoTarjeta));
        panelDetalle.setMaximumSize(new Dimension(Integer.MAX_VALUE, altoTarjeta));
        panelDetalle.revalidate();
    }

    @Override
    public void solicitarSelOpcionesRecBolsin() {
        lblResumen.setText(cantidadRemitos + (cantidadRemitos == 1 ? " remito" : " remitos") + "  ·  "
                + documentos.size() + (documentos.size() == 1 ? " documento" : " documentos"));
        habilitarAcciones(true);
    }

    /** Opciones 2 a 4: la documentación es la que el usuario marcó en la tabla. */
    @Override
    public void solicitarSelDocumentacion(String indicacion) {
        tomarSeleccionDocumentacion(documentacionMarcada());
    }

    @Override
    public void solicitarConfirmacion() {
        Opcion opcion = opcionElegida();
        StringBuilder detalle = new StringBuilder();
        if (opcion == Opcion.PRIMERA) {
            detalle.append(opcion.indicacion).append('\n');
        } else {
            detalle.append("Documentación marcada como «").append(opcion.columna).append("»:\n");
            for (Integer numero : documentacionMarcada()) {
                detalle.append("  · N° ").append(numero).append(" — ").append(documentos.get(numero).asunto())
                        .append('\n');
            }
            detalle.append("El resto se registrará como recibido y aceptado.\n");
        }
        tomarConfirmacion(mostrarDialogoConfirmacion(detalle.toString()));
    }

    private boolean mostrarDialogoConfirmacion(String detalle) {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Confirmar recepción",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);
        dialogo.setBackground(new Color(0, 0, 0, 0));
        dialogo.getRootPane().setOpaque(false);
        dialogo.getLayeredPane().setOpaque(false);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel contenido = TemaBolsines.crearTarjetaModal(new BorderLayout(0, 18));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel sobrelinea = new JLabel("RECEPCIÓN  ·  REVISIÓN FINAL");
        sobrelinea.setForeground(TemaBolsines.TURQUESA);
        sobrelinea.setFont(TemaBolsines.fuente(10f, Font.PLAIN));
        JLabel titulo = new JLabel("Confirmar recepción");
        titulo.setForeground(TemaBolsines.AZUL);
        titulo.setFont(TemaBolsines.fuenteTitulo(25f));
        JLabel descripcion = new JLabel("Revisá el resumen antes de registrar los cambios.");
        descripcion.setForeground(TemaBolsines.SECUNDARIO);
        descripcion.setFont(TemaBolsines.fuente(13f, Font.PLAIN));

        JPanel textos = new JPanel(new BorderLayout(0, 5));
        textos.setOpaque(false);
        textos.add(sobrelinea, BorderLayout.NORTH);
        JPanel tituloYDescripcion = new JPanel(new BorderLayout(0, 5));
        tituloYDescripcion.setOpaque(false);
        tituloYDescripcion.add(titulo, BorderLayout.NORTH);
        tituloYDescripcion.add(descripcion, BorderLayout.CENTER);
        textos.add(tituloYDescripcion, BorderLayout.CENTER);

        JButton cerrar = new JButton("×");
        TemaBolsines.botonSecundario(cerrar);
        cerrar.setFont(TemaBolsines.fuente(18f, Font.PLAIN));
        cerrar.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        cerrar.setToolTipText("Cerrar y volver a revisar");
        cerrar.addActionListener(e -> dialogo.dispose());
        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setOpaque(false);
        encabezado.add(textos, BorderLayout.CENTER);
        encabezado.add(cerrar, BorderLayout.EAST);

        JLabel identificacion = new JLabel("Bolsín N° " + bolsinEnPantalla.numeroBolsin());
        identificacion.setForeground(TemaBolsines.AZUL);
        identificacion.setFont(TemaBolsines.fuente(14f, Font.PLAIN));
        JPanel metadatos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        metadatos.setOpaque(false);
        metadatos.add(new PillMetadato("Precinto " + bolsinEnPantalla.numeroPrecinto()));
        metadatos.add(new PillMetadato("Origen · CM " + normalizarOrigenCM(bolsinEnPantalla.cmOrigen())));
        metadatos.add(new PillMetadato(cantidadRemitos + (cantidadRemitos == 1 ? " remito" : " remitos")));
        metadatos.add(new PillMetadato(documentos.size()
                + (documentos.size() == 1 ? " documento" : " documentos")));

        JTextArea resumen = new JTextArea(detalle, Math.min(7, Math.max(3, detalle.split("\\n").length)), 48);
        resumen.setFont(TemaBolsines.fuente(12f, Font.PLAIN));
        resumen.setForeground(TemaBolsines.TEXTO);
        resumen.setBackground(Color.WHITE);
        resumen.setEditable(false);
        resumen.setLineWrap(true);
        resumen.setWrapStyleWord(true);
        resumen.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JPanel tarjetaResumen = TemaBolsines.crearTarjeta(new BorderLayout(0, 8));
        tarjetaResumen.setBorder(BorderFactory.createEmptyBorder(14, 15, 14, 15));
        tarjetaResumen.add(identificacion, BorderLayout.NORTH);
        JPanel contenidoResumen = new JPanel(new BorderLayout(0, 10));
        contenidoResumen.setOpaque(false);
        contenidoResumen.add(metadatos, BorderLayout.NORTH);
        JPanel opcionDestacada = new JPanel(new BorderLayout(0, 4));
        opcionDestacada.setOpaque(true);
        opcionDestacada.setBackground(TemaBolsines.LISTA_SELECCIONADA);
        opcionDestacada.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, TemaBolsines.AZUL),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        JLabel opcionEtiqueta = new JLabel("OPCIÓN ELEGIDA");
        opcionEtiqueta.setForeground(TemaBolsines.SECUNDARIO);
        opcionEtiqueta.setFont(TemaBolsines.fuente(10f, Font.PLAIN));
        JLabel opcionTexto = new JLabel((opcionElegida().ordinal() + 1) + " · " + opcionElegida().tituloCorto);
        opcionTexto.setForeground(TemaBolsines.AZUL);
        opcionTexto.setFont(TemaBolsines.fuenteTitulo(14f));
        JPanel textosOpcion = new JPanel(new BorderLayout(0, 3));
        textosOpcion.setOpaque(false);
        textosOpcion.add(opcionEtiqueta, BorderLayout.NORTH);
        textosOpcion.add(opcionTexto, BorderLayout.CENTER);
        opcionDestacada.add(textosOpcion, BorderLayout.CENTER);
        contenidoResumen.add(opcionDestacada, BorderLayout.CENTER);
        contenidoResumen.add(resumen, BorderLayout.SOUTH);
        tarjetaResumen.add(contenidoResumen, BorderLayout.CENTER);

        JLabel pregunta = new JLabel("¿Confirmás que el contenido recibido es correcto?");
        pregunta.setForeground(TemaBolsines.TEXTO);
        pregunta.setFont(TemaBolsines.fuente(13f, Font.PLAIN));
        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        cuerpo.add(tarjetaResumen, BorderLayout.CENTER);
        cuerpo.add(pregunta, BorderLayout.SOUTH);

        boolean[] confirma = {false};
        JButton volver = new JButton("Volver a revisar");
        TemaBolsines.botonSecundario(volver);
        volver.addActionListener(e -> dialogo.dispose());
        JButton registrar = new JButton("Confirmar recepción");
        TemaBolsines.botonPrimario(registrar);
        registrar.addActionListener(e -> {
            confirma[0] = true;
            dialogo.dispose();
        });
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        acciones.setOpaque(false);
        acciones.add(volver);
        acciones.add(registrar);

        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(cuerpo, BorderLayout.CENTER);
        contenido.add(acciones, BorderLayout.SOUTH);
        dialogo.setContentPane(contenido);
        dialogo.getRootPane().setDefaultButton(registrar);
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                javax.swing.KeyStroke.getKeyStroke("ESCAPE"), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.setMinimumSize(new Dimension(560, 340));
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        EstadoBackdrop backdrop = instalarBackdrop();
        try {
            dialogo.setVisible(true);
        } finally {
            restaurarBackdrop(backdrop);
        }
        return confirma[0];
    }

    private EstadoBackdrop instalarBackdrop() {
        Window ventanaPropietaria = SwingUtilities.getWindowAncestor(this);
        if (!(ventanaPropietaria instanceof RootPaneContainer contenedorRaiz)) {
            return null;
        }
        JRootPane raiz = contenedorRaiz.getRootPane();
        Component anterior = raiz.getGlassPane();
        boolean visibleAnterior = anterior.isVisible();
        BufferedImage imagenDifuminada = capturarYDifuminar(raiz.getLayeredPane());
        JPanel overlay = new JPanel() {
            {
                setOpaque(false);
                addMouseListener(new MouseAdapter() { });
            }

            @Override
            protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                if (imagenDifuminada != null) {
                    g.drawImage(imagenDifuminada, 0, 0, getWidth(), getHeight(), null);
                }
                g.setColor(new Color(15, 23, 42, 115));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        raiz.setGlassPane(overlay);
        overlay.setVisible(true);
        return new EstadoBackdrop(raiz, anterior, visibleAnterior);
    }

    private static BufferedImage capturarYDifuminar(javax.swing.JLayeredPane capa) {
        int ancho = capa.getWidth();
        int alto = capa.getHeight();
        if (ancho <= 0 || alto <= 0) {
            return null;
        }
        try {
            BufferedImage captura = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = captura.createGraphics();
            capa.printAll(g);
            g.dispose();

            int tamano = 9;
            float sigma = 2.0f;
            float[] pesos = new float[tamano * tamano];
            float suma = 0f;
            int radio = tamano / 2;
            for (int y = -radio; y <= radio; y++) {
                for (int x = -radio; x <= radio; x++) {
                    float peso = (float) Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                    pesos[(y + radio) * tamano + x + radio] = peso;
                    suma += peso;
                }
            }
            for (int i = 0; i < pesos.length; i++) {
                pesos[i] /= suma;
            }
            Kernel kernel = new Kernel(tamano, tamano, pesos);
            return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null).filter(captura, null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void restaurarBackdrop(EstadoBackdrop estado) {
        if (estado == null) {
            return;
        }
        estado.raiz().setGlassPane(estado.componenteAnterior());
        estado.componenteAnterior().setVisible(estado.visibleAnterior());
    }

    private static String normalizarOrigenCM(String origen) {
        String valor = origen == null ? "" : origen.trim();
        if (valor.regionMatches(true, 0, "CM ", 0, 3)) {
            return valor.substring(3).trim();
        }
        return valor;
    }

    private record EstadoBackdrop(JRootPane raiz, Component componenteAnterior, boolean visibleAnterior) { }

    private static class PillMetadato extends JLabel {
        PillMetadato(String texto) {
            super(texto);
            setFont(TemaBolsines.fuente(11f, Font.PLAIN));
            setForeground(TemaBolsines.AZUL);
            setBorder(BorderFactory.createEmptyBorder(6, 11, 6, 11));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0xF8FAFC));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 999, 999);
            g.setColor(TemaBolsines.BORDE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 999, 999);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    @Override
    public void mostrarRecepcionRegistrada(int numeroBolsin, List<DatosRemito> remitos) {
        recepcionRegistrada = true;
        List<DatosDocumentacion> documentacion = new ArrayList<>();
        for (DatosRemito remito : remitos) {
            for (DatosDocumentacion doc : remito.documentacion()) {
                documentacion.add(doc);
                documentos.put(doc.numero(), doc);
                for (int fila = 0; fila < modeloDocumentos.getRowCount(); fila++) {
                    if (modeloDocumentos.getValueAt(fila, COL_DOC).equals(doc.numero())) {
                        modeloDocumentos.setValueAt(etiqueta(doc), fila, COL_ESTADO);
                    }
                }
            }
        }
        mostrarEstadoRemitos(remitos);
        lblDetalle.setText("Bolsín N° " + numeroBolsin + "  ·  recepción registrada");
        mostrarAviso(Aviso.EXITO, "Se registró la recepción del bolsín N° " + numeroBolsin + ". "
                + resumirRemitos(remitos) + " " + resumirEstados(documentacion));
    }

    /**
     * Canal de avisos del CU 29. El correo se envía en segundo plano: primero
     * llega "Enviando…" y después el resultado, que reemplaza a esa línea.
     */
    public void mostrarNotificacionEnviada(String aviso) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> mostrarNotificacionEnviada(aviso));
            return;
        }
        String linea = aviso.startsWith(GestorNotificacionCU29.PREFIJO_ERROR)
                ? "<span style='color:#A15C00'><b>" + aviso + "</b></span>" : aviso;
        for (int i = 0; i < lineasAviso.size(); i++) {
            if (lineasAviso.get(i).startsWith(GestorNotificacionCU29.PREFIJO_ENVIANDO)) {
                lineasAviso.set(i, linea);
                actualizarTextoAviso();
                return;
            }
        }
        if (panelAviso.isVisible()) {
            agregarLineaAviso(linea);
        } else {
            mostrarAviso(Aviso.INFORMACION, linea);
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        if (filtrando) {
            filtroSinResultados = true;
        }
        if (reiniciando && panelAviso.isVisible()) {
            agregarLineaAviso(mensaje);
        } else {
            mostrarAviso(Aviso.INFORMACION, mensaje);
        }
    }

    @Override
    public void finCU() {
        habilitarAcciones(false);
        if (!listaMostrada) {
            // No hubo bolsines para elegir: se muestra la lista vacía
            modeloBolsines.clear();
            lblBolsines.setText("Bolsines por recibir (0)");
            tarjetasLista.show(panelLista, VACIO);
            return;
        }
        bolsinAReseleccionar = recepcionRegistrada || bolsinEnPantalla == null ? null : bolsinEnPantalla.numeroBolsin();
        SwingUtilities.invokeLater(this::reiniciarEjecucion);
    }

    // ------------------------------------------------------------------
    // Auxiliares de presentación
    // ------------------------------------------------------------------

    /** Al elegir otra opción se borran las marcas y cambia el título de la columna para marcar. */
    private void elegirOpcion(Opcion opcion) {
        if (tablaDocumentos.isEditing()) {
            tablaDocumentos.getCellEditor().cancelCellEditing();
        }
        botonesOpcion.get(opcion).setSelected(true);
        actualizarEstiloOpciones();
        for (int fila = 0; fila < modeloDocumentos.getRowCount(); fila++) {
            modeloDocumentos.setValueAt(Boolean.FALSE, fila, COL_MARCA);
        }
        tablaDocumentos.getColumnModel().getColumn(COL_MARCA).setHeaderValue(opcion.columna);
        tablaDocumentos.getTableHeader().repaint();
        lblIndicacion.setText("<html>" + opcion.indicacion + "</html>");
        actualizarMarcado();
    }

    private void actualizarEstiloOpciones() {
        for (Map.Entry<Opcion, JRadioButton> entrada : botonesOpcion.entrySet()) {
            JRadioButton boton = entrada.getValue();
            boolean seleccionada = boton.isSelected();
            boolean hover = boton.getModel().isRollover() && !seleccionada;
            Color borde = seleccionada ? TemaBolsines.AZUL
                    : hover ? new Color(0xCBD5E1) : TemaBolsines.BORDE;
            boton.setBackground(seleccionada ? TemaBolsines.LISTA_SELECCIONADA
                    : hover ? TemaBolsines.LISTA_HOVER : TemaBolsines.SUPERFICIE);
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borde),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        }
    }

    private Opcion opcionElegida() {
        for (Map.Entry<Opcion, JRadioButton> entrada : botonesOpcion.entrySet()) {
            if (entrada.getValue().isSelected()) {
                return entrada.getKey();
            }
        }
        return Opcion.PRIMERA;
    }

    private List<Integer> documentacionMarcada() {
        List<Integer> marcada = new ArrayList<>();
        for (int fila = 0; fila < modeloDocumentos.getRowCount(); fila++) {
            if (Boolean.TRUE.equals(modeloDocumentos.getValueAt(fila, COL_MARCA))) {
                marcada.add((Integer) modeloDocumentos.getValueAt(fila, COL_DOC));
            }
        }
        return marcada;
    }

    private void limpiarDetalle() {
        modeloDocumentos.setRowCount(0);
        documentos.clear();
        cantidadRemitos = 0;
        lblResumen.setText(" ");
        panelRemitos.removeAll();
        panelRemitos.revalidate();
        panelRemitos.repaint();
        bolsinEnPantalla = null;
        panelDetalle.setPreferredSize(new Dimension(700, 0));
        panelDetalle.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        elegirOpcion(Opcion.PRIMERA);
    }

    private void limpiarFiltro() {
        txtPrecinto.setText("");
        txtCMOrigen.setText("");
    }

    private void habilitarAcciones(boolean habilitar) {
        btnRegistrar.setEnabled(habilitar);
        btnRegistrar.setBackground(habilitar ? TemaBolsines.TURQUESA : new Color(0xE1E9EE));
        btnRegistrar.setForeground(habilitar ? Color.WHITE : TemaBolsines.TEXTO);
        for (JRadioButton boton : botonesOpcion.values()) {
            boton.setEnabled(habilitar);
        }
        lblIndicacion.setEnabled(habilitar);
        if (habilitar) {
            lblIndicacion.setText(opcionElegida().indicacion);
            lblIndicacion.setForeground(TemaBolsines.TURQUESA);
            lblIndicacion.setVisible(true);
        } else if (bolsinEnPantalla == null) {
            lblIndicacion.setText("Seleccioná un bolsín para habilitar el registro.");
            lblIndicacion.setForeground(TemaBolsines.SECUNDARIO);
            lblIndicacion.setVisible(true);
        } else {
            lblIndicacion.setText(" ");
            lblIndicacion.setVisible(false);
        }
        actualizarMarcado();
    }

    /** La columna para marcar se puede editar sólo con las opciones 2 a 4 y mientras se elige la opción. */
    private void actualizarMarcado() {
        if (tablaDocumentos.isEditing()) {
            tablaDocumentos.getCellEditor().stopCellEditing();
        }
        marcadoHabilitado = btnRegistrar.isEnabled() && opcionElegida() != Opcion.PRIMERA;
        tablaDocumentos.repaint();
    }

    private void mostrarEstadoRemitos(List<DatosRemito> remitos) {
        panelRemitos.removeAll();
        for (DatosRemito remito : remitos) {
            JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
                @Override
                protected void paintComponent(java.awt.Graphics graphics) {
                    java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                    g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(0xF8FAFC));
                    g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 999, 999);
                    g.setColor(TemaBolsines.BORDE);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 999, 999);
                    g.dispose();
                    super.paintComponent(graphics);
                }
            };
            pill.setOpaque(false);
            pill.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            JLabel texto = new JLabel("Remito N° " + remito.numero() + "  ·  " + remito.descripcionEstado());
            texto.setFont(TemaBolsines.fuente(11f, Font.PLAIN));
            texto.setForeground(TemaBolsines.SECUNDARIO);
            pill.add(texto);
            panelRemitos.add(pill);
        }
        panelRemitos.revalidate();
        panelRemitos.repaint();
    }

    private enum Aviso {
        EXITO(new Color(0xE8F3EE), new Color(0x23856D)),
        INFORMACION(new Color(0xE8F1F7), new Color(0x4F8FB2));

        private final Color fondo;
        private final Color borde;

        Aviso(Color fondo, Color borde) {
            this.fondo = fondo;
            this.borde = borde;
        }
    }

    private void mostrarAviso(Aviso tipo, String mensaje) {
        lineasAviso.clear();
        lineasAviso.add(mensaje);
        panelAviso.setBackground(tipo.fondo);
        panelAviso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, tipo.borde),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        actualizarTextoAviso();
        panelAviso.setVisible(true);
    }

    private void agregarLineaAviso(String mensaje) {
        lineasAviso.add(mensaje);
        actualizarTextoAviso();
    }

    private void actualizarTextoAviso() {
        lblAviso.setText("<html>" + String.join("<br>", lineasAviso) + "</html>");
    }

    private void ocultarAviso() {
        lineasAviso.clear();
        panelAviso.setVisible(false);
    }

    private static String resumirRemitos(List<DatosRemito> remitos) {
        List<String> partes = new ArrayList<>();
        for (DatosRemito remito : remitos) {
            partes.add("N° " + remito.numero() + " «" + remito.descripcionEstado() + "»");
        }
        return (remitos.size() == 1 ? "Remito " : "Remitos ") + String.join(", ", partes) + ".";
    }

    private static String resumirEstados(List<DatosDocumentacion> documentacion) {
        Map<String, Integer> cantidadPorEstado = new LinkedHashMap<>();
        for (DatosDocumentacion doc : documentacion) {
            cantidadPorEstado.merge(doc.descripcionEstado(), 1, Integer::sum);
        }
        if (cantidadPorEstado.size() == 1) {
            String estado = cantidadPorEstado.keySet().iterator().next();
            return (documentacion.size() == 1 ? "Su documento quedó" : "Sus " + documentacion.size()
                    + " documentos quedaron") + " en estado «" + estado + "».";
        }
        List<String> partes = new ArrayList<>();
        cantidadPorEstado.forEach((estado, cantidad) -> partes.add(cantidad + " «" + estado + "»"));
        return "Documentación: " + String.join(", ", partes) + ".";
    }

    private static EtiquetaEstado etiqueta(DatosDocumentacion doc) {
        return new EtiquetaEstado(doc.estado(), doc.descripcionEstado());
    }

    /** La casilla para marcar se ve deshabilitada cuando la opción elegida no pide marcar documentación. */
    private static class PanelColumnaDesplazable extends JPanel implements javax.swing.Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 48;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return Math.max(48, visibleRect.height - 48);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof javax.swing.JViewport viewport
                    && getPreferredSize().height <= viewport.getHeight();
        }
    }

    private class RenderizadorMarca extends JPanel implements TableCellRenderer {

        private final TableCellRenderer base;

        RenderizadorMarca(TableCellRenderer base) {
            this.base = base;
            setLayout(new GridBagLayout());
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            removeAll();
            Component celda = base.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            celda.setEnabled(marcadoHabilitado);
            if (celda instanceof javax.swing.JComponent componente) {
                componente.setOpaque(false);
                componente.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            }
            setBackground(fila == filaHoverTabla ? TemaBolsines.LISTA_HOVER : TemaBolsines.SUPERFICIE);
            add(celda);
            return this;
        }

        @Override
        protected void paintChildren(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            if (!marcadoHabilitado) {
                g.setComposite(java.awt.AlphaComposite.SrcOver.derive(0.4f));
            }
            super.paintChildren(g);
            g.dispose();
        }
    }

    private class RenderizadorCelda extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco,
                    fila, columna);
            celda.setBackground(fila == filaHoverTabla ? TemaBolsines.LISTA_HOVER : TemaBolsines.SUPERFICIE);
            celda.setForeground(TemaBolsines.TEXTO);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            return celda;
        }
    }

    /** Números de remito y documento con cifras tabulares y espaciado estable. */
    private class RenderizadorCodigo extends DefaultTableCellRenderer {
        RenderizadorCodigo() {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco,
                    fila, columna);
            celda.setFont(TemaBolsines.fuenteCodigo(12f));
            celda.setBackground(fila == filaHoverTabla ? TemaBolsines.LISTA_HOVER : TemaBolsines.SUPERFICIE);
            celda.setForeground(TemaBolsines.TEXTO);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            return celda;
        }
    }

    /** Muestra cada bolsín en dos líneas: número y, debajo, precinto y CM de origen. */
    private class RenderizadorBolsin extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean seleccionado, boolean foco) {
            DatosBolsin bolsin = (DatosBolsin) valor;
            String texto = "<html><b>Bolsín N° " + bolsin.numeroBolsin() + "</b><br>"
                    + "<span style='font-size:9px'>Precinto " + bolsin.numeroPrecinto() + "  ·  desde "
                    + bolsin.cmOrigen() + "</span></html>";
            JLabel etiqueta = (JLabel) super.getListCellRendererComponent(lista, texto, indice, seleccionado, foco);
            etiqueta.setFont(TemaBolsines.fuente(12f, Font.PLAIN));
            etiqueta.setForeground(seleccionado ? TemaBolsines.AZUL : TemaBolsines.TEXTO);
            boolean hover = indice == indiceBolsinHover && !seleccionado;
            etiqueta.setBackground(seleccionado ? TemaBolsines.LISTA_SELECCIONADA
                    : hover ? TemaBolsines.LISTA_HOVER : TemaBolsines.SUPERFICIE);
            etiqueta.setOpaque(true);
            etiqueta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 1, 0,
                            seleccionado ? TemaBolsines.AZUL : TemaBolsines.BORDE),
                    BorderFactory.createEmptyBorder(9, 11, 9, 9)));
            return etiqueta;
        }
    }
}
