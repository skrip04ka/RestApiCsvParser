package ru.mpei.parser.mapper;

import ru.mpei.parser.dto.Range;
import ru.mpei.parser.dto.view.KeyView;
import ru.mpei.parser.dto.view.MeasurementView;
import ru.mpei.parser.model.Measurement;

public class MeasurementMapper {
    public static MeasurementView mapToMeasurementView(Measurement measurement) {
        return MeasurementView.builder()
                .key(mapToKeyView(measurement.getKey()))
                .number(measurement.getNumber())
                .name(measurement.getName())
                .values(measurement.getValues())
                .build();
    }

    public static MeasurementView mapToMeasurementViewAndSplitValues(Measurement measurement, Range range) {
        return MeasurementView.builder()
                .key(mapToKeyView(measurement.getKey()))
                .number(measurement.getNumber())
                .name(measurement.getName())
                .values(measurement.getValues().stream()
                        .skip(range.getStart())
                        .limit(range.getEnd() - range.getStart())
                        .toList())
                .range(range)
                .build();
    }

    public static KeyView mapToKeyView(Measurement.Key key) {
        return new KeyView(key.getFileInfoId(), key.getSignalNumber(), key.getType());
    }
}
