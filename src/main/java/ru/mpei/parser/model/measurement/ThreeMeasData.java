package ru.mpei.parser.model.measurement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreeMeasData {
    private double time;
    private double phA;
    private double phB;
    private double phC;

    public static ThreeMeasData create(double time, String[] names, double [] values,
                                       String phA, String phB, String phC) {
        ThreeMeasData ints = new ThreeMeasData();
        for (int i = 0; i < names.length; i++) {
            if (Objects.equals(phA, names[i])) ints.phA = values[i];
            else if (Objects.equals(phB, names[i])) ints.phB = values[i];
            else if (Objects.equals(phC, names[i])) ints.phC = values[i];
            else throw new RuntimeException("names[i] = " + names[i] + "not equals " + phA + " " + phB + " " + phC);
        }
        ints.time = time;
        return ints;
    }
}
