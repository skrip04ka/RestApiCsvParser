package ru.mpei.parser.service;

import ru.mpei.parser.model.Measurement;

import java.util.List;
import java.util.Map;

public interface FilterService {

    enum phase {
        A, B, C
    }

    int getMinPoints(double freq, double dt);

    Map<phase, List<Double>> rmsByPhase(List<Measurement> measurementList, double freq);
}
