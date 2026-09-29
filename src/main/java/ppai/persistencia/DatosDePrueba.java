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
import ppai.entidades.Estado;
import ppai.entidades.Remito;
import ppai.entidades.Sesion;
import ppai.entidades.TipoDocumento;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.EnBolsinSaliente;
import ppai.entidades.estadodocumentacion.EnRemito;
import ppai.entidades.estadodocumentacion.EstadoDocumentacion;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.Registrada;

/**
 * Arma el conjunto de objetos de prueba del CU 28. Lo usan el repositorio JPA
 * (para cargar la base de datos vacía) y el repositorio en memoria (pruebas).
 */
public class DatosDePrueba {

    private final List<ComisionMedica> comisiones = new ArrayList<>();
    private final List<TipoDocumento> tiposDocumento = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Bolsin> bolsines = new ArrayList<>();
    private final List<Estado> estados = new ArrayList<>();
    private final Sesion sesionActual;

    public DatosDePrueba() {
        // Comisiones Médicas
        ComisionMedica cmCordoba = new ComisionMedica(1, "CM Córdoba", "Av. Colón 1234", "cm.cordoba@cm.gob.ar", "351-4000000");
        ComisionMedica cmRosario = new ComisionMedica(2, "CM Rosario", "Bv. Oroño 567", "cm.rosario@cm.gob.ar", "341-4000000");
        ComisionMedica cmMendoza = new ComisionMedica(3, "CM Mendoza", "San Martín 890", "cm.mendoza@cm.gob.ar", "261-4000000");
        comisiones.add(cmCordoba);
        comisiones.add(cmRosario);
        comisiones.add(cmMendoza);

        // Empleados y sesión (el usuario logueado trabaja en CM Córdoba)
        Empleado empCordoba = new Empleado("Pérez", "Ana", "ana.perez@cm.gob.ar", "aperez", cmCordoba);
        Empleado empRosario = new Empleado("Gómez", "Juan", "juan.gomez@cm.gob.ar", "jgomez", cmRosario);
        Empleado empMendoza = new Empleado("López", "Carla", "carla.lopez@cm.gob.ar", "clopez", cmMendoza);
        empleados.add(empCordoba);
        empleados.add(empRosario);
        empleados.add(empMendoza);
        sesionActual = new Sesion(LocalDateTime.now(), "aperez");

        // Estados de Bolsín y Remito (Documentación usa el patrón State)
        Estado bolsinGenerado = new Estado(Estado.AMBITO_BOLSIN, "Generado", "Bolsín armado en la CM origen");
        Estado bolsinEnviado = new Estado(Estado.AMBITO_BOLSIN, "Enviado", "Bolsín retirado por el correo");
        Estado bolsinRecibido = new Estado(Estado.AMBITO_BOLSIN, "RecibidoEnCMDestino", "Bolsín recibido en la CM destino");
        Estado remitoEnviado = new Estado(Estado.AMBITO_REMITO, "Enviado", "Remito enviado en un bolsín");
        Estado remitoRecibido = new Estado(Estado.AMBITO_REMITO, "RecibidoYAceptado", "Remito recibido y aceptado");
        estados.add(bolsinGenerado);
        estados.add(bolsinEnviado);
        estados.add(bolsinRecibido);
        estados.add(remitoEnviado);
        estados.add(remitoRecibido);

        TipoDocumento expediente = new TipoDocumento("Expediente", "Expediente de trámite");
        TipoDocumento dictamen = new TipoDocumento("Dictamen Médico", "Dictamen emitido por la CM");
        TipoDocumento estudio = new TipoDocumento("Estudio Complementario", "Estudio médico adjunto");
        tiposDocumento.add(expediente);
        tiposDocumento.add(dictamen);
        tiposDocumento.add(estudio);

        LocalDateTime t0 = LocalDateTime.of(2026, 9, 21, 9, 0);

        // Bolsín 101: Rosario -> Córdoba, Enviado
        Documentacion doc1 = documentacionEnviada(1001, "Expediente de incapacidad laboral", expediente, t0, empRosario);
        Documentacion doc2 = documentacionEnviada(1002, "Dictamen de junta médica", dictamen, t0, empRosario);
        Documentacion doc3 = documentacionParaRedirigir(1003, "Estudio mal derivado a Mendoza", estudio, t0, empMendoza);
        Remito r5001 = remito(5001, t0, cmRosario, cmCordoba, remitoEnviado, doc1, doc2);
        Remito r5002 = remito(5002, t0, cmRosario, cmCordoba, remitoEnviado, doc3);
        bolsines.add(bolsin(101, "PR-0101", 1.2, cmRosario, cmCordoba, t0, empRosario,
                bolsinGenerado, bolsinEnviado, null, r5001, r5002));

        // Bolsín 102: Mendoza -> Córdoba, Enviado
        Documentacion doc4 = documentacionEnviada(1004, "Recurso de apelación", expediente, t0, empMendoza);
        Documentacion doc5 = documentacionEnviada(1005, "Estudio de audiometría", estudio, t0, empMendoza);
        Remito r6001 = remito(6001, t0, cmMendoza, cmCordoba, remitoEnviado, doc4, doc5);
        bolsines.add(bolsin(102, "PR-0102", 0.8, cmMendoza, cmCordoba, t0, empMendoza,
                bolsinGenerado, bolsinEnviado, null, r6001));

        // Bolsín 103: Córdoba -> Rosario (no es para la CM del usuario: no se lista)
        Documentacion doc6 = documentacionEnviada(1006, "Dictamen de reingreso", dictamen, t0, empCordoba);
        Remito r7001 = remito(7001, t0, cmCordoba, cmRosario, remitoEnviado, doc6);
        bolsines.add(bolsin(103, "PR-0103", 0.5, cmCordoba, cmRosario, t0, empCordoba,
                bolsinGenerado, bolsinEnviado, null, r7001));

        // Bolsín 104: Rosario -> Córdoba, ya recibido (no se lista)
        Documentacion doc7 = documentacionEnviada(1007, "Expediente cerrado", expediente, t0, empRosario);
        doc7.recibir(t0.plusDays(2), empCordoba);
        Remito r8001 = remito(8001, t0, cmRosario, cmCordoba, remitoRecibido, doc7);
        bolsines.add(bolsin(104, "PR-0104", 0.4, cmRosario, cmCordoba, t0, empRosario,
                bolsinGenerado, bolsinEnviado, bolsinRecibido, r8001));
    }

    /**
     * Crea la documentación y la lleva hasta EnBolsinEnviado disparando los
     * eventos de la máquina de estados (CU 7, 15, 19 y 27).
     */
    private Documentacion documentacionEnviada(int numero, String asunto, TipoDocumento tipo,
                                               LocalDateTime t0, Empleado responsable) {
        Documentacion documentacion = new Documentacion(numero, asunto, t0.toLocalDate(), tipo, t0, responsable);
        documentacion.remitar(t0.plusHours(1), responsable);
        documentacion.agregarAlBolsin(t0.plusHours(2), responsable);
        documentacion.enviar(t0.plusDays(1), responsable);
        return documentacion;
    }

    /**
     * Materializa una documentación que ya está en ParaRedirigir (resultado
     * de un CU 31 en otra CM), reconstruyendo su historial de estados.
     */
    private Documentacion documentacionParaRedirigir(int numero, String asunto, TipoDocumento tipo,
                                                     LocalDateTime t0, Empleado responsable) {
        List<CambioEstadoDocumentacion> historial = new ArrayList<>();
        historial.add(cambioEstado(new Registrada(), t0.minusDays(10), t0.minusDays(9), responsable));
        historial.add(cambioEstado(new EnRemito(), t0.minusDays(9), t0.minusDays(8), responsable));
        historial.add(cambioEstado(new EnBolsinSaliente(), t0.minusDays(8), t0.minusDays(7), responsable));
        historial.add(cambioEstado(new EnBolsinEnviado(), t0.minusDays(7), t0.minusDays(5), responsable));
        historial.add(cambioEstado(new ParaRedirigir(), t0.minusDays(5), null, responsable));
        return new Documentacion(numero, asunto, t0.minusDays(10).toLocalDate(), tipo, historial);
    }

    private CambioEstadoDocumentacion cambioEstado(EstadoDocumentacion estado,
                                                   LocalDateTime inicio, LocalDateTime fin, Empleado responsable) {
        CambioEstadoDocumentacion cambioEstado = new CambioEstadoDocumentacion(inicio, estado, responsable);
        if (fin != null) {
            cambioEstado.setFechaHoraFin(fin);
        }
        return cambioEstado;
    }

    private Remito remito(int numero, LocalDateTime t0, ComisionMedica origen, ComisionMedica destino,
                          Estado estado, Documentacion... documentacion) {
        Remito remito = new Remito(t0.toLocalDate(), numero, origen, destino, estado);
        for (Documentacion doc : documentacion) {
            remito.agregarDetalle(new DetalleRemito("Mesa de Entradas", doc));
        }
        return remito;
    }

    private Bolsin bolsin(int numero, String precinto, double peso, ComisionMedica origen, ComisionMedica destino,
                          LocalDateTime t0, Empleado responsable, Estado generado, Estado enviado,
                          Estado recibido, Remito... remitos) {
        Bolsin bolsin = new Bolsin(t0.toLocalDate(), numero, precinto, peso, origen, destino);
        for (Remito remito : remitos) {
            bolsin.asociarRemito(remito);
        }
        CambioEstadoBolsin ceGenerado = new CambioEstadoBolsin(t0.plusHours(2), generado, responsable);
        ceGenerado.setFechaHoraFin(t0.plusDays(1));
        bolsin.agregarCambioEstado(ceGenerado);
        CambioEstadoBolsin ceEnviado = new CambioEstadoBolsin(t0.plusDays(1), enviado, responsable);
        bolsin.agregarCambioEstado(ceEnviado);
        if (recibido != null) {
            ceEnviado.setFechaHoraFin(t0.plusDays(2));
            bolsin.agregarCambioEstado(new CambioEstadoBolsin(t0.plusDays(2), recibido, responsable));
        }
        return bolsin;
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

    public List<Estado> getEstados() {
        return Collections.unmodifiableList(estados);
    }
}
