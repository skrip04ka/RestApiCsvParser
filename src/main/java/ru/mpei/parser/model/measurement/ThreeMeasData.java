package ru.mpei.parser.model.measurement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreeMeasData {
    private double time;
    private double phA;
    private double phB;
    private double phC;

}
