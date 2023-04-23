package ru.mpei.parser.service.impl;

import org.springframework.stereotype.Service;
import ru.mpei.parser.model.Measurement;
import ru.mpei.parser.service.FilterService;

import java.util.*;

@Service
public class FourierFilterService implements FilterService {

    @Override
    public int getMinPoints(double freq, double dt) {
        return (int) (1 / dt / freq);
    }

    @Override
    public Map<phase, List<Double>> rmsByPhase(List<Measurement> measurementList, double freq) {

        int N = getMinPoints(freq, measurementList.get(1).getTime() - measurementList.get(0).getTime());

        if (measurementList.size() < N) {
            return null;
        }


        Map<phase, List<Double>> rmsByPhase = new HashMap<>();

        rmsByPhase.put(phase.A, calculateRms(
                measurementList.stream().map(Measurement::getIa).toList(), N
        ));

        rmsByPhase.put(phase.B, calculateRms(
                measurementList.stream().map(Measurement::getIb).toList(), N
        ));

        rmsByPhase.put(phase.C, calculateRms(
                measurementList.stream().map(Measurement::getIc).toList(), N
        ));


        return rmsByPhase;
    }

    private List<Double> calculateRms(List<Double> values, int N) {
        List<Double> rmsList = new ArrayList<>();

        if (values.size() < N) {
            return rmsList;
        }

        int left = 0;
        int right = N;

        while (right < values.size()) {
            rmsList.add(rms(values.subList(left, right), N));
            left++;
            right++;
        }
        return rmsList;
    }

    private double rms(List<Double> values, int N) {
        double x = 0;
        for (double value : values) {
            x = x + Math.pow(value, 2);
        }
        return Math.sqrt(x / N);
    }

}
