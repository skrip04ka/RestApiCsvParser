package ru.mpei.parser.service;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.dto.FileInfoDto;
import ru.mpei.parser.dto.data.MeasData;
import ru.mpei.parser.dto.data.AnalogMeasData;
import ru.mpei.parser.dto.data.DigitalMeasData;
import ru.mpei.parser.mapper.FileMapper;
import ru.mpei.parser.model.FileType;
import ru.mpei.parser.repository.MeasurementRepository;
import ru.mpei.parser.service.filter.RmsFilterService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CsvService {

    @Value("${parser.csv.analogKeyword}")
    private String analogKeyword = "branch";
    private final MeasurementRepository measurementsRepository;
    private final RmsFilterService filterService;

    @Autowired
    public CsvService(MeasurementRepository measurementsRepository, RmsFilterService filterService) {
        this.measurementsRepository = measurementsRepository;
        this.filterService = filterService;
    }

    @SneakyThrows
    @Transactional
    public void parseFile(MultipartFile dat) {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(dat.getInputStream(), StandardCharsets.UTF_8.newDecoder()));

        List<MeasData> measurements = parseData(bufferedReader, parseHeader(bufferedReader.readLine()));

        bufferedReader.close();

        log.info("read data complete");
        int N = filterService.calculate(measurements, 50);
        log.info("rms calculate complete with N={}", N);

        FileInfoDto fileInfoDto = new FileInfoDto(N, 50);
        if (dat.getOriginalFilename() != null)
            fileInfoDto.setName(dat.getOriginalFilename().split("\\.csv")[0]);
        fileInfoDto.setAnalogNumber(measurements.get(0).getAnalogMeas().size());
        fileInfoDto.setDigitalNumber(measurements.get(0).getDigitalMeas().size());
        fileInfoDto.setType(FileType.CSV);

        measurementsRepository.saveFile(FileMapper.mapToFileInfo(fileInfoDto, measurements));

    }

    private HeaderData parseHeader(String header) {
        HeaderData headerData = new HeaderData();

        String[] s = header.split(",");

        for (int i = 1; i < s.length; i++) {
            CsvSignalType csvSignalType = getType(s[i]);
            headerData.csvSignalTypes.add(csvSignalType);
            headerData.names.add(getName(s[i]));
        }

        return headerData;
    }

    private String getName(String s) {
        String[] h = s.split("\\|");
        return h[h.length - 1];
    }

    private CsvSignalType getType(String s) {
        return s.toLowerCase().contains(analogKeyword) ? CsvSignalType.ANALOG : CsvSignalType.DIGITAL;
    }

    @SneakyThrows
    private List<MeasData> parseData(BufferedReader bufferedReader, HeaderData headerData) {
        String line;
        List<MeasData> measurements = new ArrayList<>();

        while ((line = bufferedReader.readLine()) != null) {
            MeasData m = new MeasData();
            String[] l = line.split(",");
            m.setTime(Double.parseDouble(l[0]));

            for (int i = 1; i < l.length; i++) {
                switch (headerData.csvSignalTypes.get(i - 1)) {
                    case DIGITAL -> m.getDigitalMeas().add(
                            new DigitalMeasData(headerData.names.get(i - 1), l[i].equals("1.0")));
                    case ANALOG -> m.getAnalogMeas().add(
                            new AnalogMeasData(headerData.names.get(i - 1), Double.parseDouble(l[i])));
                }
            }
            measurements.add(m);
        }
        return measurements;
    }

    @ToString
    private static class HeaderData {
        private final List<CsvSignalType> csvSignalTypes = new ArrayList<>();
        private final List<String> names = new ArrayList<>();
    }

    private enum CsvSignalType {
        ANALOG, DIGITAL
    }

}
