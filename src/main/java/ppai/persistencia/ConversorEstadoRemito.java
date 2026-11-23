package ppai.persistencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import ppai.entidades.estadoremito.Cancelado;
import ppai.entidades.estadoremito.Creado;
import ppai.entidades.estadoremito.EnBolsinEnviado;
import ppai.entidades.estadoremito.EnBolsinSaliente;
import ppai.entidades.estadoremito.EstadoRemito;
import ppai.entidades.estadoremito.RemitoRecibidoYAceptado;
import ppai.entidades.estadoremito.RemitoRecibidoYAceptadoParcial;

/**
 * Conversor de base de datos para los estados de Remito (patrón State). Igual
 * que {@link ConversorEstadoDocumentacion}: guarda el nombre del estado y, al
 * materializar, crea el objeto del estado concreto.
 */
@Converter(autoApply = true)
public class ConversorEstadoRemito implements AttributeConverter<EstadoRemito, String> {

    @Override
    public String convertToDatabaseColumn(EstadoRemito estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public EstadoRemito convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        return switch (nombre) {
            case "Creado" -> new Creado();
            case "Cancelado" -> new Cancelado();
            case "EnBolsinSaliente" -> new EnBolsinSaliente();
            case "EnBolsinEnviado" -> new EnBolsinEnviado();
            case "RecibidoYAceptado" -> new RemitoRecibidoYAceptado();
            case "RecibidoYAceptadoParcial" -> new RemitoRecibidoYAceptadoParcial();
            default -> throw new IllegalArgumentException("Estado de remito desconocido: " + nombre);
        };
    }
}
