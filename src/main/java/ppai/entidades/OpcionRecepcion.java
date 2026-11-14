package ppai.entidades;

/**
 * Opciones de recepción del bolsín que el sistema muestra en el paso 7 del
 * CU 28. Cada una indica la situación de la documentación que marca el
 * usuario; la que no marca es igual a la registrada.
 */
public enum OpcionRecepcion {

    /** Paso 8: el contenido del bolsín es igual al registrado. */
    CONTENIDO_IGUAL("El contenido del bolsín es igual al registrado",
            SituacionDocumentacion.IGUAL_A_LA_REGISTRADA, false),
    /** Flujo A3: la documentación marcada no llegó. */
    NO_SE_RECIBE_TODA("No se recibe toda la documentación asociada a los remitos que contiene el bolsín",
            SituacionDocumentacion.NO_RECIBIDA, true),
    /** Flujo A4: la documentación marcada no corresponde a la CM destino. */
    NO_CORRESPONDE("Existe documentación que no corresponde al destino",
            SituacionDocumentacion.NO_CORRESPONDE_A_LA_CM_DESTINO, true),
    /** Flujo A5: la documentación marcada se redirige a otra área de la CMC. */
    REDIRIGIR("La documentación se debe redirigir a otra área", SituacionDocumentacion.PARA_REDIRIGIR, false);

    private final String descripcion;
    private final SituacionDocumentacion situacionDocumentacionIndicada;
    private final boolean dejaRemitoParcial;

    OpcionRecepcion(String descripcion, SituacionDocumentacion situacionDocumentacionIndicada,
                    boolean dejaRemitoParcial) {
        this.descripcion = descripcion;
        this.situacionDocumentacionIndicada = situacionDocumentacionIndicada;
        this.dejaRemitoParcial = dejaRemitoParcial;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Situación de la documentación que el usuario marcó con esta opción. */
    public SituacionDocumentacion getSituacionDocumentacionIndicada() {
        return situacionDocumentacionIndicada;
    }

    /**
     * Si el remito tiene documentación marcada con esta opción, ¿queda Recibido y
     * aceptado parcial? Sí cuando la documentación no llegó (regla de negocio
     * "Recepción de un Bolsín") o no corresponde a la CM destino (decisión del
     * grupo: la ERS no lo dice).
     */
    public boolean dejaRemitoParcial() {
        return dejaRemitoParcial;
    }
}
