package ppai.persistencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import ppai.entidades.estadodocumentacion.DeBaja;
import ppai.entidades.estadodocumentacion.EnBolsinEnviado;
import ppai.entidades.estadodocumentacion.EnBolsinSaliente;
import ppai.entidades.estadodocumentacion.EnRemito;
import ppai.entidades.estadodocumentacion.EstadoDocumentacion;
import ppai.entidades.estadodocumentacion.NoRecibida;
import ppai.entidades.estadodocumentacion.ParaRedirigir;
import ppai.entidades.estadodocumentacion.RecibidaYAceptada;
import ppai.entidades.estadodocumentacion.RecibidaYRechazada;
import ppai.entidades.estadodocumentacion.Registrada;

/**
 * Conversor de base de datos para los estados del patrón State.
 *
 * <p>Los estados concretos no tienen atributos propios, así que no hace falta
 * una tabla por estado: al desmaterializar se guarda el nombre del estado y al
 * materializar se crea el objeto del estado concreto correspondiente. Es el
 * único lugar del sistema que conoce la lista de estados; el dominio sigue sin
 * tener sentencias CASE sobre el estado.
 */
@Converter(autoApply = true)
public class ConversorEstadoDocumentacion implements AttributeConverter<EstadoDocumentacion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoDocumentacion estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public EstadoDocumentacion convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        return switch (nombre) {
            case "Registrada" -> new Registrada();
            case "EnRemito" -> new EnRemito();
            case "EnBolsinSaliente" -> new EnBolsinSaliente();
            case "EnBolsinEnviado" -> new EnBolsinEnviado();
            case "ParaRedirigir" -> new ParaRedirigir();
            case "NoRecibida" -> new NoRecibida();
            case "Recibida&Aceptada" -> new RecibidaYAceptada();
            case "Recibida&Rechazada" -> new RecibidaYRechazada();
            case "DeBaja" -> new DeBaja();
            default -> throw new IllegalArgumentException("Estado de documentación desconocido: " + nombre);
        };
    }
}
