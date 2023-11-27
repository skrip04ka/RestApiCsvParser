package ru.mpei.parser.dto.analise;

import lombok.Data;

import java.util.List;

@Data
public class FaultPhasesNumber {
    private int phA;
    private int phB;
    private int phC;
    private Double stock;

    public List<Integer> getPhasesNumber() {
        return List.of(phA, phB, phC);
    }
}
