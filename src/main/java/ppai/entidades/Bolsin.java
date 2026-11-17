package ppai.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ppai.dto.DatosRemito;

/**
 * Bolsín: envío físico precintado de remitos entre Comisiones Médicas.
 *
 * <p>Patrón State - rol CONTEXTO: conoce su estado actual y le DELEGA la
 * recepción. El estado concreto decide si el bolsín puede recibirse y cuál es
 * su estado siguiente.
 */
@Entity
@Table(name = "bolsin")
public class Bolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private int numeroBolsin;
    private String numeroPrecinto;
    private double peso;
    @ManyToOne
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica origen;
    @ManyToOne
    @JoinColumn(name = "cm_destino_id")
    private ComisionMedica destino;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bolsin_id")
    @OrderBy("id")
    private List<Remito> remitos = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bolsin_id")
    @OrderBy("id")
    private List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();
    /** No se guarda como columna: se obtiene del cambio de estado actual (ver restaurarEstadoActual). */
    @Transient
    private Estado estadoActual;

    protected Bolsin() {
        // Requerido por JPA
    }

    public Bolsin(LocalDate fecha, int numeroBolsin, String numeroPrecinto, double peso,
                  ComisionMedica origen, ComisionMedica destino) {
        this.fecha = fecha;
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.peso = peso;
        this.origen = origen;
        this.destino = destino;
    }

    public void asociarRemito(Remito remito) {
        remitos.add(remito);
    }

    /**
     * El estado actual (objeto del patrón State) es el del cambio de estado
     * vigente. Se ejecuta al materializar el bolsín desde la base.
     */
    @PostLoad
    void restaurarEstadoActual() {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        estadoActual = actual == null ? null : actual.getEstado();
    }

    /** Lo usa el estado concreto al hacer la transición. */
    public void agregarCambioEstado(CambioEstadoBolsin cambioEstado) {
        cambiosEstado.add(cambioEstado);
    }

    /** Lo usa el estado concreto al hacer la transición. */
    public void setEstado(Estado estado) {
        if (!estado.esAmbitoBolsin()) {
            throw new IllegalArgumentException("El estado «" + estado + "» no es un estado de bolsín");
        }
        this.estadoActual = estado;
    }

    /** Indica si el estado actual del bolsín es Enviado. */
    public boolean sosEnviado() {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        return actual != null && actual.sosEnviado();
    }

    /** Nombre de la Comisión Médica destino del bolsín. */
    public String obtenerCMDestino() {
        return destino.getNombre();
    }

    /** Nombre de la Comisión Médica origen del bolsín. */
    public String obtenerCMOrigen() {
        return origen.getNombre();
    }

    public int getNumeroBolsin() {
        return numeroBolsin;
    }

    public String getNumeroPrecinto() {
        return numeroPrecinto;
    }

    /** CU 28, paso 4: ¿el número de precinto del bolsín es el ingresado? (sin distinguir mayúsculas). */
    public boolean tenesPrecinto(String precinto) {
        return numeroPrecinto.trim().equalsIgnoreCase(precinto.trim());
    }

    /** CU 28, paso 4: ¿la CM origen del bolsín coincide con el texto ingresado? */
    public boolean sosDeCMOrigen(String textoCMOrigen) {
        return origen.coincideNombre(textoCMOrigen);
    }

    /** CU 28, flujo A5: la opción de redirigir sólo corresponde si la CM destino es la CMC. */
    public boolean esDestinoCMC() {
        return destino.esCentral();
    }

    /** CU 28, flujos A3 a A5: ¿la documentación con ese número está en algún remito del bolsín? */
    public boolean contenesDocumentacion(int numeroDocumentacion) {
        for (Remito remito : remitos) {
            if (remito.contenesDocumentacion(numeroDocumentacion)) {
                return true;
            }
        }
        return false;
    }

    /** Número, estado y documentación de cada remito del bolsín. */
    public List<DatosRemito> obtenerInformacionRemito() {
        List<DatosRemito> informacion = new ArrayList<>();
        for (Remito remito : remitos) {
            informacion.add(new DatosRemito(remito.obtenerNumero(), remito.obtenerDescripcionEstado(),
                    remito.obtenerDatosRemito()));
        }
        return informacion;
    }

    /** CU 28, paso 11: delega en el estado actual, que lo pasa a Recibido en CM destino. */
    public void recibir(LocalDateTime fechaHora, Empleado responsable) {
        estadoActual.recibir(this, fechaHora, responsable);
    }

    /**
     * CU 28, paso 11 y flujos A3 a A5: recibe cada remito del bolsín según la
     * opción elegida y la documentación que indicó el usuario.
     */
    public void recibirYAceptarRemito(LocalDateTime fechaHora, Empleado responsable, OpcionRecepcion opcion,
                                      List<Integer> documentacionIndicada) {
        for (Remito remito : remitos) {
            remito.recibirYAceptar(fechaHora, responsable, opcion, documentacionIndicada);
        }
    }

    /** CU 29: la notificación se envía a la CM origen del bolsín. */
    public String obtenerCorreoCMOrigen() {
        return origen.getEmail();
    }

    public Estado getEstadoActual() {
        return estadoActual;
    }

    private CambioEstadoBolsin buscarCambioEstadoActual() {
        for (CambioEstadoBolsin cambioEstado : cambiosEstado) {
            if (cambioEstado.sosActual()) {
                return cambioEstado;
            }
        }
        return null;
    }

    public List<Remito> getRemitos() {
        return Collections.unmodifiableList(remitos);
    }

    public List<CambioEstadoBolsin> getCambiosEstado() {
        return Collections.unmodifiableList(cambiosEstado);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getPeso() {
        return peso;
    }
}
