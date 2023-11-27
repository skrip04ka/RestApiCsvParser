package ru.mpei.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mpei.parser.dto.FileDto;
import ru.mpei.parser.dto.analise.FaultData;
import ru.mpei.parser.dto.analise.FaultPhasesNumber;
import ru.mpei.parser.dto.analise.ThreePhaseData;
import ru.mpei.parser.dto.view.MeasurementView;
import ru.mpei.parser.mapper.FileMapper;
import ru.mpei.parser.mapper.MeasurementMapper;
import ru.mpei.parser.repository.MeasurementRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AnaliseService {

    private final MeasurementRepository measurementsRepository;

    @Autowired
    public AnaliseService(MeasurementRepository measurementsRepository) {
        this.measurementsRepository = measurementsRepository;
    }

    @Transactional(readOnly = true)
    public FaultData analiseMeas(UUID fileInfoId, FaultPhasesNumber faultPhasesNumber) {
        return analiseMeas(fileInfoId, faultPhasesNumber, 20);
    }

    @Transactional(readOnly = true)
    public FaultData analiseMeas(UUID fileInfoId, FaultPhasesNumber faultPhasesNumber, double stock) {
        if (stock < 1) stock = 1;

        FileDto fileDto = FileMapper.mapToFileInfoDto(measurementsRepository
                .getFileById(fileInfoId).orElseThrow());
        List<MeasurementView> measurement = measurementsRepository
                .getMeasurementsByFileIdAndSignalNumbers(fileInfoId, faultPhasesNumber.getPhasesNumber())
                .stream()
                .map(MeasurementMapper::mapToMeasurementView)
                .toList();

        List<Double> pha = measurement.get(0).getValues();
        List<Double> phb = measurement.get(1).getValues();
        List<Double> phc = measurement.get(2).getValues();
        List<Integer> time = new ArrayList<>();
        for (int i = 0; i < pha.size(); i++) {
            time.add(i);
        }

        return analise(time, pha, phb, phc, fileDto.getN(), stock);
    }


    private FaultData analise(List<Integer> time, List<Double> pha, List<Double> phb, List<Double> phc, int n, double stock) {

        int len = time.size();

        log.debug("start idx {}", n);

        FaultData faultData = new FaultData();

        double set = getMax(pha.get(n), phb.get(n), phc.get(n)) * (stock / 100 + 2);
        faultData.setSet(set);
        log.debug("setting {}", set);

        faultData.setNormalCurrent(new ThreePhaseData(
                time.get(n),
                pha.get(n),
                phb.get(n),
                phc.get(n)
        ));

        int faultMoment = -1;
        StringBuilder fault = new StringBuilder();

        for (int i = n; i < len; i++) {
            if (pha.get(i) >= set) {
                if (fault.indexOf("A") < 0) fault.append("A");
            }
            if (phb.get(i) >= set) {
                if (fault.indexOf("B") < 0) fault.append("B");
            }
            if (phc.get(i) >= set) {
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
            faultData.setFaultCurrent(new ThreePhaseData());
            log.info("fault not found. result: {}", faultData);
            return faultData;
        }

        faultData.setTime(time.get(faultMoment));
        faultData.setFault(fault.toString());

        int steadyMoment = findStart(pha, phb, phc, len, faultMoment, 0.01);
        log.debug("fault type {}, steady state moment {}", fault, steadyMoment);

        if (steadyMoment >= len - 1) {
            log.debug("steady state not found");
            if (fault.indexOf("A") >= 0) {
                int maxi = 0;
                for (int i = 0; i < len; i++) {
                    if (pha.get(i) > pha.get(maxi)) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase A: i = {}", steadyMoment);
            } else if (fault.indexOf("B") >= 0) {
                int maxi = 0;
                for (int i = 0; i < len; i++) {
                    if (phb.get(i) > phb.get(maxi)) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase B: i = {}", steadyMoment);
            } else if (fault.indexOf("C") >= 0) {
                int maxi = 0;
                for (int i = 0; i < len; i++) {
                    if (phc.get(i) > phc.get(maxi)) {
                        maxi = i;
                    }
                }
                steadyMoment = maxi;
                log.debug("find max with phase C: i = {}", steadyMoment);
            }
        }

        faultData.setFaultCurrent(new ThreePhaseData(
                time.get(steadyMoment),
                pha.get(steadyMoment),
                phb.get(steadyMoment),
                phc.get(steadyMoment)
        ));


        log.info("Analise complete successfully. result: {}", faultData);


        return faultData;

    }

    private double getMax(double v1, double v2, double v3) {
        return Math.max(v1, Math.max(v2, v3));
    }

    private int findStart(List<Double> pha, List<Double> phb, List<Double> phc, int len, int start, double velosity) {
        int i = 10;
        if (start - 10 >= 0) {
            i = start;
        }
        while (i < len && (pha.get(i) - pha.get(i - 10) > velosity ||
                phb.get(i) - phb.get(i - 10) > velosity ||
                phc.get(i) - phc.get(i - 10) > velosity)) {
            i++;
        }
        return i;
    }
}
