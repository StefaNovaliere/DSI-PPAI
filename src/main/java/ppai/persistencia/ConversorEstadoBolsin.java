package ppai.persistencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import ppai.entidades.estadobolsin.Cerrado;
import ppai.entidades.estadobolsin.Creado;
import ppai.entidades.estadobolsin.DeBaja;
import ppai.entidades.estadobolsin.Enviado;
import ppai.entidades.estadobolsin.RecibidoEnCMDestino;
import ppai.entidades.Estado;

/**
 * Conversor de base de datos para los estados de Bolsín (patrón State). Igual
 * que {@link ConversorEstadoDocumentacion}: guarda el nombre del estado y, al
 * materializar, crea el objeto del estado concreto.
 */
@Converter(autoApply = true)
public class ConversorEstadoBolsin implements AttributeConverter<Estado, String> {

    @Override
    public String convertToDatabaseColumn(Estado estado) {
        validarAmbito(estado);
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public Estado convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        Estado estado = switch (nombre) {
            case "Creado" -> new Creado();
            case "Cerrado" -> new Cerrado();
            case "Enviado" -> new Enviado();
            case "RecibidoEnCMDestino" -> new RecibidoEnCMDestino();
            case "DeBaja" -> new DeBaja();
            default -> throw new IllegalArgumentException("Estado de bolsín desconocido: " + nombre);
        };
        validarAmbito(estado);
        return estado;
    }

    private void validarAmbito(Estado estado) {
        if (estado != null && !estado.esAmbitoBolsin()) throw new IllegalArgumentException("El estado no pertenece al ámbito Bolsin");
    }
}

