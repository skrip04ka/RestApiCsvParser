package ru.mpei.parser.model;

import lombok.*;
import ru.mpei.parser.model.measurement.ThreeMeasData;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasData {
    private String fault;
    private double time;
    private double set;
    private ThreeMeasData normalCurrent;
    private ThreeMeasData faultCurrent;
}

