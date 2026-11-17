package ppai.persistencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import ppai.entidades.Estado;
import ppai.entidades.estadoremito.Cancelado;
import ppai.entidades.estadoremito.CreadoRemito;
import ppai.entidades.estadoremito.EnBolsinEnviado;
import ppai.entidades.estadoremito.EnBolsinSaliente;
import ppai.entidades.estadoremito.RecibidoYAceptadoParcial;
import ppai.entidades.estadoremito.RemitoRecibidoYAceptado;

/**
 * Conversor de base de datos para los estados de Remito (patrón State). Igual
 * que {@link ConversorEstadoDocumentacion}: guarda el nombre del estado y, al
 * materializar, crea el objeto del estado concreto.
 */
@Converter
public class ConversorEstadoRemito implements AttributeConverter<Estado, String> {

    @Override
    public String convertToDatabaseColumn(Estado estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public Estado convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        return switch (nombre) {
            case "Creado" -> new CreadoRemito();
            case "Cancelado" -> new Cancelado();
            case "EnBolsinSaliente" -> new EnBolsinSaliente();
            case "EnBolsinEnviado" -> new EnBolsinEnviado();
            case "RecibidoYAceptado" -> new RemitoRecibidoYAceptado();
            case "RecibidoYAceptadoParcial" -> new RecibidoYAceptadoParcial();
            default -> throw new IllegalArgumentException("Estado de remito desconocido: " + nombre);
        };
    }
}
