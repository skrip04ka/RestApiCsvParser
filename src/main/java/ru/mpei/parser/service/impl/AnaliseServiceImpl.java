package ru.mpei.parser.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FaultData;
import ru.mpei.parser.model.dto.NamedMeas;
import ru.mpei.parser.model.measurement.ThreeMeasData;
import ru.mpei.parser.repository.MeasurementsRepository;
import ru.mpei.parser.service.AnaliseService;

import java.util.List;

@Service
@Slf4j
public class AnaliseServiceImpl implements AnaliseService {

    private final MeasurementsRepository measurementsRepository;

    @Autowired
    public AnaliseServiceImpl(MeasurementsRepository measurementsRepository) {
        this.measurementsRepository = measurementsRepository;
    }

    @Override
    public FaultData analiseMeas(long id, String phA, String phB, String phC) {
        return analiseMeas(id, phA, phB, phC, 20);
    }

    @Override
    @Transactional(readOnly = true)
    public FaultData analiseMeas(long id, String phA, String phB, String phC, double stock) {
        if (stock < 1) stock = 1;

        MetaInf metaInf = measurementsRepository.getMetaInf(id).orElseThrow();
        List<NamedMeas> namedMeas = measurementsRepository.getMeasByNames(id, List.of(phA, phB, phC));

        Double[] time = null;
        Double[] pha = null;
        Double[] phb = null;
        Double[] phc = null;

        for (NamedMeas m : namedMeas) {
            if (m.getName().equals(phA)) pha = m.getValues();
            else if (m.getName().equals(phB)) phb = m.getValues();
            else if (m.getName().equals(phC)) phc = m.getValues();
            else if (m.getName().equals("time")) time = m.getValues();
        }

        if (time == null || pha == null || phb == null || phc == null)
            throw new RuntimeException("names not found");

        return analise(time, pha, phb, phc, metaInf.getN(), stock);
    }


    private FaultData analise(Double[] time, Double[] pha, Double[] phb, Double[] phc, int n, double stock) {

        int len = Math.min(Math.min(time.length, pha.length), Math.min(phb.length, phc.length));

        log.debug("start idx {}", n);

        FaultData faultData = new FaultData();

        double set = getMax(pha[n], phb[n], phc[n]) * (stock / 100 + 2);
        faultData.setSet(set);
        log.debug("setting {}", set);

        faultData.setNormalCurrent(new ThreeMeasData(
                time[n],
                pha[n],
                phb[n],
                phc[n]
        ));

        int faultMoment = -1;
        StringBuilder fault = new StringBuilder();

        for (int i = n; i < len; i++) {
            if (pha[i] >= set) {
                if (fault.indexOf("A") < 0) fault.append("A");
            }
            if (phb[i] >= set) {
                if (fault.indexOf("B") < 0) fault.append("B");
            }
            if (phc[i] >= set) {
                if (fault.indexOf("C") < 0) fault.append("C");
            }
            if (!fault.isEmpty()) {
                if (faultMoment < 0) {
                    faultMoment = i;
                }
                if (i > faultMoment + n) {
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

        faultData.setTime(time[faultMoment]);
        faultData.setFault(fault.toString());

        int steadyMoment = findStart(pha, phb, phc, len, faultMoment, 0.01);
        log.debug("fault type {}, steady state moment {}", fault, steadyMoment);

        if (steadyMoment >= len - 1) {
            log.debug("steady state not found");
            if (fault.indexOf("A") >= 0) {
                int maxi = 0;
                for (int i = 0; i < len; i++) {
                    if (pha[i] > pha[maxi]) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase A: i = {}", steadyMoment);
            } else if (fault.indexOf("B") >= 0) {
                int maxi = 0;
                for (int i = 0; i < len; i++) {
                    if (phb[i] > phb[maxi]) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase B: i = {}", steadyMoment);
            } else if (fault.indexOf("C") >= 0) {
                int maxi = 0;
                for (int i = 0; i < len; i++) {
                    if (phc[i] > phc[maxi]) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase C: i = {}", steadyMoment);
            }
        }

        faultData.setFaultCurrent(new ThreeMeasData(
                time[steadyMoment],
                pha[steadyMoment],
                phb[steadyMoment],
                phc[steadyMoment]
                ));


        log.info("Analise complete successfully. result: {}", faultData);


        return faultData;

    }

    private double getMax(double v1, double v2, double v3) {
        return Math.max(v1, Math.max(v2, v3));
    }

    private int findStart(Double[] pha, Double[] phb, Double[] phc, int len, int start, double vel) {
        int i = 10;
        if (start - 10 >= 0) {
            i = start;
        }
        while (i < len && (pha[i] - pha[i - 10] > vel ||
                phb[i] - phb[i - 10] > vel ||
                phc[i] - phc[i - 10] > vel)) {
            i++;
        }
        return i;
    }


}
