package ru.mpei.parser.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.measurement.ThreeMeasData;

import java.util.List;

@Data
@AllArgsConstructor
public class ThreeMeasDataDTO {
    private MetaInf meta;
    private List<ThreeMeasData> meas;
}
