package ru.mpei.parser.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.Measurements;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.service.FilterService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RmsFilterService implements FilterService {
    @Value("${filter.rmsSuffix}")
    private String rmsSuffix = "RMS";

    private int getMinPoints(double freq, double dt) {
        log.debug("dt = {}", dt);
        return (int) (1 / dt / freq);
    }


    @Override
    public int calculate(List<Measurements> measurementList, double freq) {

        if (measurementList.size() < 2) {
            log.warn("measurementList.size() < 2");
            return -1;
        }

        int N = getMinPoints(freq, measurementList.get(1).getTime() - measurementList.get(0).getTime());

        if (measurementList.size() < N) {
            log.warn("measurementList.size() < N");
            return -1;
        }

        List<String> names = measurementList.get(0).getAnalogMeas().stream()
                .map(AnalogMeas::getName).toList();
        Map<String, RmsCalc> rms = new HashMap<>();

        names.forEach(name -> rms.put(name, new RmsCalc(N)));

        for (Measurements m : measurementList) {
            m.setRmsMeas(new ArrayList<>());
            for (AnalogMeas am : m.getAnalogMeas()) {
                m.getRmsMeas().add(new AnalogMeas(am.getName() + "_" + rmsSuffix,
                        rms.get(am.getName()).calcNext(am.getVal())
                ));
            }

        }

        return N;
    }

    private static class RmsCalc {
        private final double[] buffer;
        private int i = 0;

        public RmsCalc(int N) {
            this.buffer = new double[N];
        }

        public double calcNext(double val) {
            if (i >= buffer.length) i = 0;
            buffer[i] = val;
            i++;
            return rms();
        }

        private double rms() {
            double x = 0;
            for (double value : buffer) {
                x = x + Math.pow(value, 2);
            }
            return Math.sqrt(x / buffer.length);
        }

    }

}
