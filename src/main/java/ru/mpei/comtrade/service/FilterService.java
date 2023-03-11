package ru.mpei.comtrade.service;

import ru.mpei.comtrade.model.Measurement;

import java.util.List;
import java.util.Map;

public interface FilterService {

    enum phase {
        A, B, C
    }

    int getMinPoints(double freq, double dt);

    Map<phase, List<Double>> rmsByPhase(List<Measurement> measurementList, double freq);
}
