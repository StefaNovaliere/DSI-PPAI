package ppai.entidades;

/**
 * Estado de Bolsín y de Remito, tal como fue modelado en el análisis
 * (entidad con ámbito).
 *
 * <p>Documentación NO usa esta clase: sus estados se modelan con el patrón
 * State (ver paquete {@code ppai.entidades.estadodocumentacion}).
 */
public class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";

    private String ambito;
    private String nombre;
    private String descripcion;

    public Estado(String ambito, String nombre, String descripcion) {
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equals(ambito);
    }

    public boolean esAmbitoRemito() {
        return AMBITO_REMITO.equals(ambito);
    }

    public boolean esEnviado() {
        return "Enviado".equals(nombre);
    }

    public boolean esRecibidoEnCMDestino() {
        return "RecibidoEnCMDestino".equals(nombre);
    }

    public boolean esRecibidoYAceptado() {
        return "RecibidoYAceptado".equals(nombre);
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
