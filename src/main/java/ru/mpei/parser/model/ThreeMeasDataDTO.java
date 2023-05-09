package ru.mpei.parser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mpei.parser.model.measurement.ThreeMeasData;

import java.util.List;

@Data
@AllArgsConstructor
public class ThreeMeasDataDTO {
    private MetaInf meta;
    private List<ThreeMeasData> meas;
}
