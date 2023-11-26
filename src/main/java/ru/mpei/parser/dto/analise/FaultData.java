package ru.mpei.parser.dto.analise;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultData {
    private String fault;
    private int time;
    private double set;
    private ThreePhaseData normalCurrent;
    private ThreePhaseData faultCurrent;
}

