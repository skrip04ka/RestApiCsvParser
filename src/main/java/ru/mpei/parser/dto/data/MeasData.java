package ru.mpei.parser.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasData {
    private double time;
    private List<AnalogMeasData> analogMeas = new ArrayList<>();
    private List<AnalogMeasData> rmsMeas = new ArrayList<>();
    private List<DigitalMeasData> digitalMeas = new ArrayList<>();
}
