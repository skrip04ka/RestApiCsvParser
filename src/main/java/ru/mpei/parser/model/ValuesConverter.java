package ru.mpei.parser.model;

import jakarta.persistence.AttributeConverter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValuesConverter implements AttributeConverter<List<Double>, Double[]> {

    @Override
    public Double[] convertToDatabaseColumn(List<Double> doubles) {
        if (doubles == null) return null;
        Double[] values = new Double[doubles.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = doubles.get(i);
        }
        return values;
    }

    @Override
    @SneakyThrows
    public List<Double> convertToEntityAttribute(Double[] pgArray) {
        return new ArrayList<>(Arrays.asList(pgArray));
    }
}
