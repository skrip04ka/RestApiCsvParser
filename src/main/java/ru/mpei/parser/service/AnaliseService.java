package ru.mpei.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FaultData;
import ru.mpei.parser.model.measurement.ThreeMeasData;
import ru.mpei.parser.repository.MeasurementsRepository;

import java.util.List;

@Service
@Slf4j
public class AnaliseService {

    private final MeasurementsRepository measurementsRepository;

    @Autowired
    public AnaliseService(MeasurementsRepository measurementsRepository) {
        this.measurementsRepository = measurementsRepository;
    }

    public FaultData analiseMeas(long id, String phA, String phB, String phC) {
        return analiseMeas(id, phA, phB, phC, 20);
    }

    public FaultData analiseMeas(long id, String phA, String phB, String phC, double stock) {
        if (stock < 1) stock = 1;

        return analise(measurementsRepository.getThreeMeas(id, phA, phB, phC),
                measurementsRepository.getMetaInf(id).orElseThrow(),
                stock);
    }

    private FaultData analise(List<ThreeMeasData> measurements, MetaInf metaInf, double stock) {

        int start;
        if (metaInf.getN() > 0) {
            start = metaInf.getN();
        } else {
            start = findStart(measurements, 1, 0.01);
        }
        log.debug("start idx {}", start);

        FaultData faultData = new FaultData();

        double set = getMax(measurements.get(start)) * (stock / 100 + 1);
        faultData.setSet(set);
        log.debug("setting {}", set);

        faultData.setNormalCurrent(new ThreeMeasData(
                measurements.get(start).getTime(),
                measurements.get(start).getPhA(),
                measurements.get(start).getPhB(),
                measurements.get(start).getPhC()
        ));

        int faultMoment = -1;
        StringBuilder fault = new StringBuilder();

        for (int i = start; i < measurements.size(); i++) {
            if (measurements.get(i).getPhA() >= set) {
                if (fault.indexOf("A") < 0) fault.append("A");
            }
            if (measurements.get(i).getPhB() >= set) {
                if (fault.indexOf("B") < 0) fault.append("B");
            }
            if (measurements.get(i).getPhC() >= set) {
                if (fault.indexOf("C") < 0) fault.append("C");
            }
            if (!fault.isEmpty()) {
                if (faultMoment < 0) {
                    faultMoment = i;
                }
                if (i > faultMoment + start) {
                    break;
                }
            }
        }

        fault = fault.chars().sorted().collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append);

        if (faultMoment == -1) {
            faultData.setTime(-1);
            faultData.setFaultCurrent(new ThreeMeasData());
            log.info("fault not found. result: {}", faultData);
            return faultData;
        }

        faultData.setTime(measurements.get(faultMoment).getTime());
        faultData.setFault(fault.toString());

        int steadyMoment = findStart(measurements, faultMoment, 0.01);
        log.debug("fault type {}, steady state moment {}", fault, steadyMoment);

        if (steadyMoment >= measurements.size() - 1) {
            log.debug("steady state not found");
            if (fault.indexOf("A") >= 0) {
                int maxi = 0;
                for (int i = 0; i < measurements.size(); i++) {
                    if (measurements.get(i).getPhA() > measurements.get(maxi).getPhA()) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase A: i = {}", steadyMoment);
            } else if (fault.indexOf("B") >= 0) {
                int maxi = 0;
                for (int i = 0; i < measurements.size(); i++) {
                    if (measurements.get(i).getPhB() > measurements.get(maxi).getPhB()) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase B: i = {}", steadyMoment);
            } else if (fault.indexOf("C") >= 0) {
                int maxi = 0;
                for (int i = 0; i < measurements.size(); i++) {
                    if (measurements.get(i).getPhC() > measurements.get(maxi).getPhC()) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase C: i = {}", steadyMoment);
            }
        }

        faultData.setFaultCurrent(new ThreeMeasData(
                measurements.get(steadyMoment).getTime(),
                measurements.get(steadyMoment).getPhA(),
                measurements.get(steadyMoment).getPhB(),
                measurements.get(steadyMoment).getPhC()
        ));


        log.info("Analise complete successfully. result: {}", faultData);


        return faultData;

    }

    private double getMax(ThreeMeasData m) {
        return Math.max(m.getPhA(), Math.max(m.getPhB(), m.getPhC()));
    }

    private int findStart(List<ThreeMeasData> measurements, int start, double vel) {
        int i = 10;
        if (start - 10 >= 0) {
            i = start;
        }
        while (i < measurements.size() && (measurements.get(i).getPhA() - measurements.get(i - 10).getPhA() > vel ||
                measurements.get(i).getPhB() - measurements.get(i - 10).getPhB() > vel ||
                measurements.get(i).getPhC() - measurements.get(i - 10).getPhC() > vel)) {
            i++;
        }
        return i;
    }


}
