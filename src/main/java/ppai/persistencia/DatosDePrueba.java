package ppai.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ppai.entidades.Bolsin;
import ppai.entidades.CambioEstadoBolsin;
import ppai.entidades.CambioEstadoDocumentacion;
import ppai.entidades.ComisionMedica;
import ppai.entidades.DetalleRemito;
import ppai.entidades.Documentacion;
import ppai.entidades.Empleado;
import ppai.entidades.Remito;
import ppai.entidades.Sesion;
import ppai.entidades.SituacionDocumentacion;
import ppai.entidades.TipoDocumento;
import ppai.entidades.Usuario;
import ppai.entidades.estadobolsin.Cerrado;
import ppai.entidades.estadobolsin.Creado;
import ppai.entidades.estadobolsin.Enviado;
import ppai.entidades.estadobolsin.EstadoBolsin;
import ppai.entidades.estadobolsin.RecibidoEnCMDestino;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.EnBolsinSaliente;
import ppai.entidades.estadodocumentacion.EnRemito;
import ppai.entidades.estadodocumentacion.EstadoDocumentacion;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.Registrada;
import ppai.entidades.estadoremito.EstadoRemito;
import ppai.entidades.estadoremito.RecibidoYAceptado;

/**
 * Arma el conjunto de objetos de prueba del CU 28. Lo usan el repositorio JPA
 * (para cargar la base de datos vacía) y el repositorio en memoria (pruebas).
 *
 * <p>Los objetos quedan como los dejaron los casos de uso anteriores: cada
 * bolsín pasó por Creado (CU 19), Cerrado (CU 39) y Enviado (CU 27), y sus
 * remitos y su documentación están "En bolsín enviado".
 */
public class DatosDePrueba {

    private static final String MESA_DE_ENTRADAS = "Mesa de Entradas";
    private static final String AREA_DICTAMENES = "Área de Dictámenes";
    private static final String AREA_JURIDICA = "Área Jurídica";
    private static final String AREA_AUDITORIA = "Área de Auditoría Médica";

    private final List<ComisionMedica> comisiones = new ArrayList<>();
    private final List<TipoDocumento> tiposDocumento = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Bolsin> bolsines = new ArrayList<>();
    private final Sesion sesionActual;

    public DatosDePrueba() {
        // Comisiones Médicas: la Central (CMC) es la única que redirige documentación a sus áreas
        ComisionMedica cmc = new ComisionMedica(0, "Comisión Médica Central", "Florida 537, CABA",
                "mesa.entradas@cmc.example", "011-4000000", true);
        ComisionMedica cmCordoba = new ComisionMedica(1, "CM Córdoba", "Av. Colón 1234",
                "mesa.entradas@cmcordoba.example", "351-4000000", false);
        ComisionMedica cmRosario = new ComisionMedica(2, "CM Rosario", "Bv. Oroño 567",
                "mesa.entradas@cmrosario.example", "341-4000000", false);
        ComisionMedica cmMendoza = new ComisionMedica(3, "CM Mendoza", "San Martín 890",
                "mesa.entradas@cmmendoza.example", "261-4000000", false);
        comisiones.add(cmc);
        comisiones.add(cmCordoba);
        comisiones.add(cmRosario);
        comisiones.add(cmMendoza);

        // Usuarios, empleados y sesión (el usuario logueado trabaja en la CMC)
        Usuario aperez = new Usuario("aperez");
        Usuario jgomez = new Usuario("jgomez");
        Usuario clopez = new Usuario("clopez");
        Usuario mdiaz = new Usuario("mdiaz");
        usuarios.add(aperez);
        usuarios.add(jgomez);
        usuarios.add(clopez);
        usuarios.add(mdiaz);
        Empleado empCMC = new Empleado("Pérez", "Ana", "ana.perez@cmc.example", aperez, cmc);
        Empleado empRosario = new Empleado("Gómez", "Juan", "juan.gomez@cmrosario.example", jgomez, cmRosario);
        Empleado empMendoza = new Empleado("López", "Carla", "carla.lopez@cmmendoza.example", clopez, cmMendoza);
        Empleado empCordoba = new Empleado("Díaz", "Martín", "martin.diaz@cmcordoba.example", mdiaz, cmCordoba);
        empleados.add(empCMC);
        empleados.add(empRosario);
        empleados.add(empMendoza);
        empleados.add(empCordoba);
        sesionActual = new Sesion(LocalDateTime.now(), aperez);

        TipoDocumento expediente = new TipoDocumento("Expediente", "Expediente de trámite");
        TipoDocumento dictamen = new TipoDocumento("Dictamen Médico", "Dictamen emitido por la CM");
        TipoDocumento estudio = new TipoDocumento("Estudio Complementario", "Estudio médico adjunto");
        tiposDocumento.add(expediente);
        tiposDocumento.add(dictamen);
        tiposDocumento.add(estudio);

        LocalDateTime t0 = LocalDateTime.of(2026, 9, 21, 9, 0);

        // Bolsín 101: CM Rosario -> CMC, enviado
        Documentacion doc1 = documentacionEnviada(1001, "Expediente de incapacidad laboral", expediente, t0, empRosario);
        Documentacion doc2 = documentacionEnviada(1002, "Dictamen de junta médica", dictamen, t0, empRosario);
        Documentacion doc3 = documentacionParaRedirigir(1003, "Estudio reenviado por derivación errónea", estudio, t0, empMendoza);
        Remito r5001 = remito(5001, t0, cmRosario, cmc, remitoEnviado(),
                detalle(AREA_JURIDICA, doc1), detalle(AREA_DICTAMENES, doc2));
        Remito r5002 = remito(5002, t0, cmRosario, cmc, remitoEnviado(), detalle(AREA_AUDITORIA, doc3));
        bolsines.add(bolsinEnviado(101, "PR-0101", 1.2, cmRosario, cmc, t0, empRosario, r5001, r5002));

        // Bolsín 102: CM Mendoza -> CMC, enviado
        Documentacion doc4 = documentacionEnviada(1004, "Recurso de apelación", expediente, t0, empMendoza);
        Documentacion doc5 = documentacionEnviada(1005, "Estudio de audiometría", estudio, t0, empMendoza);
        Remito r6001 = remito(6001, t0, cmMendoza, cmc, remitoEnviado(),
                detalle(AREA_JURIDICA, doc4), detalle(AREA_AUDITORIA, doc5));
        bolsines.add(bolsinEnviado(102, "PR-0102", 0.8, cmMendoza, cmc, t0, empMendoza, r6001));

        // Bolsín 103: CMC -> CM Rosario (no es para la CM del usuario: no se lista)
        Documentacion doc6 = documentacionEnviada(1006, "Dictamen de reingreso", dictamen, t0, empCMC);
        Remito r7001 = remito(7001, t0, cmc, cmRosario, remitoEnviado(), detalle(MESA_DE_ENTRADAS, doc6));
        bolsines.add(bolsinEnviado(103, "PR-0103", 0.5, cmc, cmRosario, t0, empCMC, r7001));

        // Bolsín 104: CM Rosario -> CMC, ya recibido (no se lista)
        Documentacion doc7 = documentacionEnviada(1007, "Expediente cerrado", expediente, t0, empRosario);
        doc7.recibir(t0.plusDays(2), empCMC, SituacionDocumentacion.IGUAL_A_LA_REGISTRADA);
        Remito r8001 = remito(8001, t0, cmRosario, cmc, new RecibidoYAceptado(), detalle(AREA_JURIDICA, doc7));
        Bolsin bolsin104 = bolsinEnviado(104, "PR-0104", 0.4, cmRosario, cmc, t0, empRosario, r8001);
        agregarCambioEstado(bolsin104, new RecibidoEnCMDestino(), t0.plusDays(2), empCMC);
        bolsines.add(bolsin104);

        // Bolsín 105: CM Córdoba -> CMC, enviado, con dos remitos
        Documentacion doc8 = documentacionEnviada(1008, "Solicitud de revisión de incapacidad", expediente, t0, empCordoba);
        Documentacion doc9 = documentacionEnviada(1009, "Dictamen de comisión médica", dictamen, t0, empCordoba);
        Documentacion doc10 = documentacionEnviada(1010, "Estudio de resonancia magnética", estudio, t0, empCordoba);
        Remito r9001 = remito(9001, t0, cmCordoba, cmc, remitoEnviado(),
                detalle(AREA_JURIDICA, doc8), detalle(AREA_DICTAMENES, doc9));
        Remito r9002 = remito(9002, t0, cmCordoba, cmc, remitoEnviado(), detalle(AREA_AUDITORIA, doc10));
        bolsines.add(bolsinEnviado(105, "PR-0105", 1.5, cmCordoba, cmc, t0, empCordoba, r9001, r9002));
    }

    /** Documentación que viaja en un bolsín enviado: su estado actual es EnBolsinEnviado. */
    private Documentacion documentacionEnviada(int numero, String asunto, TipoDocumento tipo,
                                               LocalDateTime t0, Empleado responsable) {
        return documentacion(numero, asunto, tipo, t0.minusDays(2), responsable,
                new Registrada(), new EnRemito(), new EnBolsinSaliente(), new EnBolsinEnviado());
    }

    /**
     * Documentación que llegó por error a otra CM, se revisó allí y se reenvía a
     * la CM correcta: su estado actual es ParaRedirigir. Al recibirla, la máquina
     * de estados de la Entrega 1 la pasa a Recibida&amp;Aceptada.
     */
    private Documentacion documentacionParaRedirigir(int numero, String asunto, TipoDocumento tipo,
                                                     LocalDateTime t0, Empleado responsable) {
        return documentacion(numero, asunto, tipo, t0.minusDays(10), responsable,
                new Registrada(), new EnRemito(), new EnBolsinSaliente(), new EnBolsinEnviado(), new ParaRedirigir());
    }

    /**
     * Crea una documentación con su historial de estados, tal como la dejaron
     * los casos de uso anteriores: cada estado dura un día y el último es el actual.
     */
    private Documentacion documentacion(int numero, String asunto, TipoDocumento tipo, LocalDateTime inicio,
                                        Empleado responsable, EstadoDocumentacion... estados) {
        List<CambioEstadoDocumentacion> historial = new ArrayList<>();
        LocalDateTime desde = inicio;
        for (int i = 0; i < estados.length; i++) {
            CambioEstadoDocumentacion cambioEstado = new CambioEstadoDocumentacion(desde, estados[i], responsable);
            desde = desde.plusDays(1);
            if (i < estados.length - 1) {
                cambioEstado.setFechaHoraFin(desde);
            }
            historial.add(cambioEstado);
        }
        return new Documentacion(numero, asunto, inicio.toLocalDate(), tipo, historial);
    }

    /** Remito que viaja en un bolsín enviado (CU 27). */
    private static EstadoRemito remitoEnviado() {
        return new ppai.entidades.estadoremito.EnBolsinEnviado();
    }

    private static DetalleRemito detalle(String areaCMCDestino, Documentacion documentacion) {
        return new DetalleRemito(areaCMCDestino, documentacion);
    }

    private Remito remito(int numero, LocalDateTime t0, ComisionMedica origen, ComisionMedica destino,
                          EstadoRemito estado, DetalleRemito... detalles) {
        Remito remito = new Remito(t0.toLocalDate(), numero, origen, destino, estado);
        for (DetalleRemito detalle : detalles) {
            remito.agregarDetalle(detalle);
        }
        return remito;
    }

    /** Bolsín que pasó por Creado (CU 19), Cerrado (CU 39) y Enviado (CU 27). */
    private Bolsin bolsinEnviado(int numero, String precinto, double peso, ComisionMedica origen,
                                 ComisionMedica destino, LocalDateTime t0, Empleado responsable, Remito... remitos) {
        Bolsin bolsin = new Bolsin(t0.toLocalDate(), numero, precinto, peso, origen, destino);
        for (Remito remito : remitos) {
            bolsin.asociarRemito(remito);
        }
        agregarCambioEstado(bolsin, new Creado(), t0.plusHours(2), responsable);
        agregarCambioEstado(bolsin, new Cerrado(), t0.plusHours(20), responsable);
        agregarCambioEstado(bolsin, new Enviado(), t0.plusDays(1), responsable);
        return bolsin;
    }

    /** Cierra el cambio de estado vigente del bolsín y agrega el nuevo. */
    private void agregarCambioEstado(Bolsin bolsin, EstadoBolsin estado,
                                     LocalDateTime fechaHora, Empleado responsable) {
        for (CambioEstadoBolsin cambioEstado : bolsin.getCambiosEstado()) {
            if (cambioEstado.sosActual()) {
                cambioEstado.setFechaHoraFin(fechaHora);
            }
        }
        bolsin.agregarCambioEstado(new CambioEstadoBolsin(fechaHora, estado, responsable));
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public List<ComisionMedica> getComisiones() {
        return Collections.unmodifiableList(comisiones);
    }

    public List<TipoDocumento> getTiposDocumento() {
        return Collections.unmodifiableList(tiposDocumento);
    }

    public Sesion getSesionActual() {
        return sesionActual;
    }

    public List<Empleado> getEmpleados() {
        return Collections.unmodifiableList(empleados);
    }

    public List<Bolsin> getBolsines() {
        return Collections.unmodifiableList(bolsines);
    }
}
