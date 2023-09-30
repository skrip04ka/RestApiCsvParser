package ru.mpei.parser.model.dto;

import lombok.*;
import ru.mpei.parser.model.measurement.ThreeMeasData;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultData {
    private String fault;
    private double time;
    private double set;
    private ThreeMeasData normalCurrent;
    private ThreeMeasData faultCurrent;
}

