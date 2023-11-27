package ru.mpei.parser.service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mpei.parser.dto.data.MeasData;
import ru.mpei.parser.dto.data.AnalogMeasData;

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
    public int calculate(List<MeasData> measurementList, double freq) {

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
                .map(AnalogMeasData::getName).toList();
        Map<String, RmsCalculator> rms = new HashMap<>();

        names.forEach(name -> rms.put(name, new RmsCalculator(N)));

        for (MeasData m : measurementList) {
            m.setRmsMeas(new ArrayList<>());
            for (AnalogMeasData am : m.getAnalogMeas()) {
                m.getRmsMeas().add(new AnalogMeasData(am.getName() + " " + rmsSuffix,
                        rms.get(am.getName()).calcNext(am.getVal())
                ));
            }

        }

        return N;
    }

    private static class RmsCalculator {
        private final double[] buffer;
        private int i = 0;

        public RmsCalculator(int N) {
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
