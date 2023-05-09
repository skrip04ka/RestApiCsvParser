package ru.mpei.parser.model;

import lombok.Data;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.model.measurement.DigitalMeas;

import java.util.ArrayList;
import java.util.List;

@Data
public class MeasList {
    private double time;
    private List<AnalogMeas> meas = new ArrayList<>();
    private List<DigitalMeas> dmeas = new ArrayList<>();
}
