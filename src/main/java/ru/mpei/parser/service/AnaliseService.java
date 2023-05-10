package ru.mpei.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MeasData;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.measurement.ThreeMeasData;
import ru.mpei.parser.repository.ClickHouseRepository;

import java.util.List;

@Service
@Slf4j
public class AnaliseService {

    private final ClickHouseRepository clickHouseRepository;

    @Autowired
    public AnaliseService(ClickHouseRepository clickHouseRepository) {
        this.clickHouseRepository = clickHouseRepository;
    }

    public MeasData analiseMeas(String phA, String phB, String phC) {
        return analiseMeas(phA, phB, phC, 20);
    }

    public MeasData analiseMeas(String phA, String phB, String phC, double stock) {
        if (stock < 1) stock = 1;
        return analise2(clickHouseRepository.getThreeMeas(phA, phB, phC),
                clickHouseRepository.getMetaInf(),
                stock);
    }

    private MeasData analise2(List<ThreeMeasData> measurements, MetaInf metaInf, double stock) {

        int start = 0;
        if (metaInf.getN() > 0) {
            start = metaInf.getN();
        } else {
            start = findStart(measurements, 1, 0.01);
        }
        log.debug("start idx {}", start);

        MeasData measData = new MeasData();

        double set = getMax(measurements.get(start)) * (stock / 100 + 1);
        measData.setSet(set);
        log.debug("setting {}", set);

        measData.setNormalCurrent(new ThreeMeasData(
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
            measData.setTime(-1);
            measData.setFaultCurrent(new ThreeMeasData());
            log.info("fault not found. result: {}", measData);
            return measData;
        }

        measData.setTime(measurements.get(faultMoment).getTime());
        measData.setFault(fault.toString());

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

        measData.setFaultCurrent(new ThreeMeasData(
                measurements.get(steadyMoment).getTime(),
                measurements.get(steadyMoment).getPhA(),
                measurements.get(steadyMoment).getPhB(),
                measurements.get(steadyMoment).getPhC()
        ));


        log.info("Analise complete successfully. result: {}", measData);


        return measData;

    }

    private double getMax(ThreeMeasData m) {
        return Math.max(m.getPhA(), Math.max(m.getPhB(), m.getPhC()));
    }

    private int findStart(List<ThreeMeasData> measurements, int start, double vel) {
        int i = 10;
        if (start - 10 >= 0) {
            i = start;
        }
        while ((measurements.get(i).getPhA() - measurements.get(i - 10).getPhA() > vel ||
                measurements.get(i).getPhB() - measurements.get(i - 10).getPhB() > vel ||
                measurements.get(i).getPhC() - measurements.get(i - 10).getPhC() > vel) &&
                i < measurements.size()) {
            i++;
        }
        return i;
    }


}
