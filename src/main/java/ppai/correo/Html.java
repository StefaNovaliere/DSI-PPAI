package ppai.correo;

/** Utilidad para armar el cuerpo HTML de los correos. */
public final class Html {

    private Html() {
    }

    /** Escapa los caracteres especiales de HTML de un texto. */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
