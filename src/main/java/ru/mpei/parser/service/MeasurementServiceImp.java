package ru.mpei.parser.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.model.Measurement;
import ru.mpei.parser.repository.MeasurementsRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
@Slf4j
public class MeasurementServiceImp implements MeasurementService {

    private double setPoint = 0.0;

    private final MeasurementsRepository measurementsRepository;

    private final FilterService filterService;

    @Autowired
    public MeasurementServiceImp(MeasurementsRepository measurementsRepository, FilterService filterService) {
        this.measurementsRepository = measurementsRepository;
        this.filterService = filterService;
    }

    @Override
    @SneakyThrows
    public void parseFile(MultipartFile file) {
        InputStream inputStream = file.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();
        line = bufferedReader.readLine();
        while (line != null) {
            String[] stringParts = line.split(",");
            if (stringParts.length > 4) {
                measurementsRepository.save(new Measurement(
                        Double.parseDouble(stringParts[0]),
                        Double.parseDouble(stringParts[1]),
                        Double.parseDouble(stringParts[2]),
                        Double.parseDouble(stringParts[3])
                ));
            }

            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        measurementsRepository.flush();
        log.info("FIle read successfully");
    }

    @Override
    public String findFault(int startIndex, int endIndex) {
        if (startIndex >= endIndex) {
            log.warn("Error: startIndex >= endIndex");
            return "Error: startIndex >= endIndex";
        }

        Long lastIndex = measurementsRepository.getLastIndex();
        if (lastIndex == null) {
            log.warn("Error: db is empty");
            return "Error: db is empty";
        }

        if (startIndex > lastIndex) {
            log.warn("Error: startIndex > measurements size");
            return "Error: startIndex > measurements size";
        }

        Map<FilterService.phase, List<Double>> rmsByPhase =
                filterService.rmsByPhase(measurementsRepository.getMeasByIdDiapason(startIndex, endIndex), 50);

        if (rmsByPhase == null) {
            log.warn("Error: uncorrected diapason, use minimum 1 period");
            return "Error: uncorrected diapason, use minimum 1 period";
        }

//        StringJoiner logResult = new StringJoiner("", "fault type: ", "");
//        logResult.setEmptyValue("no fault");

        StringJoiner result = new StringJoiner("");
        result.setEmptyValue("");


        for (FilterService.phase phaseName : FilterService.phase.values()) {
            for (Double rms : rmsByPhase.get(phaseName)) {
                if (rms >= setPoint) {
                    result.add(phaseName.name());
                    break;
                }
            }
        }

        log.info("in diapason {} - {} : {}", startIndex, endIndex,
                result.toString().equals("") ? "no fault" : ("fault type " + result));

        return result.toString();

    }

    @Override
    public boolean saveSetPoint(double setPoint) {
        this.setPoint = setPoint;
        log.info("set point save: setPoint = {}", this.setPoint);
        return true;
    }
}
