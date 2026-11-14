package ppai.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.text.Normalizer;

/**
 * Comisión Médica: origen o destino de los bolsines y remitos.
 */
@Entity
@Table(name = "comision_medica")
public class ComisionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int codigo;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;
    /** Es la Comisión Médica Central (CMC), la única que redirige documentación a sus áreas. */
    private boolean central;

    protected ComisionMedica() {
        // Requerido por JPA
    }

    public ComisionMedica(int codigo, String nombre, String direccion, String email, String telefono,
                          boolean central) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
        this.central = central;
    }

    public boolean esCentral() {
        return central;
    }

    /**
     * ¿El nombre contiene el texto ingresado? No distingue mayúsculas ni tildes:
     * "rosario" encuentra "CM Rosario" y "cordoba" encuentra "CM Córdoba".
     */
    public boolean coincideNombre(String texto) {
        return normalizar(nombre).contains(normalizar(texto));
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }

    public String getNombre() {
        return nombre;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getEmail() {
        return email;
    }
}
