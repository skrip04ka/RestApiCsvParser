package ru.mpei.parser.dto.analise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreePhaseData {
    private double time;
    private double phA;
    private double phB;
    private double phC;
}
