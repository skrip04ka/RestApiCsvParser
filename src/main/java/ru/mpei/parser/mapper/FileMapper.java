package ru.mpei.parser.mapper;

import ru.mpei.parser.dto.FileDto;
import ru.mpei.parser.dto.data.AnalogMeasData;
import ru.mpei.parser.dto.data.MeasData;
import ru.mpei.parser.model.File;
import ru.mpei.parser.model.Measurement;
import ru.mpei.parser.model.enums.SignalType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMapper {
    public static File mapToFileInfo(FileDto fileDto, List<MeasData> measData) {
        UUID fileInfoId = UUID.randomUUID();
        return File.builder()
                .id(fileInfoId)
                .name(fileDto.getName())
                .type(fileDto.getType())
                .analogNumber(fileDto.getAnalogNumber())
                .digitalNumber(fileDto.getDigitalNumber())
                .freq(fileDto.getFreq())
                .n(fileDto.getN())
                .timeStart(fileDto.getTimeStart())
                .timeEnd(fileDto.getTimeEnd())
                .measurements(mapToListMeasurement(measData, fileInfoId))
                .build();
    }

    public static FileDto mapToFileInfoDto(File file) {
        return FileDto.builder()
                .id(file.getId())
                .name(file.getName())
                .type(file.getType())
                .analogNumber(file.getAnalogNumber())
                .digitalNumber(file.getDigitalNumber())
                .freq(file.getFreq())
                .n(file.getN())
                .timeStart(file.getTimeStart())
                .timeEnd(file.getTimeEnd())
                .build();
    }

    private static List<Measurement> mapToListMeasurement(List<MeasData> measData, UUID fileIndoId) {

        List<Measurement> measurements = new ArrayList<>();
        int analogSize = measData.get(0).getAnalogMeas().size();
        int rmsSize = measData.get(0).getRmsMeas().size();
        int digitalSize = measData.get(0).getDigitalMeas().size();

        int number = 0;

        for (int i = 0; i < analogSize; i++) {
            int finalI = i;
            measurements.add(Measurement.builder()
                    .key(new Measurement.Key(fileIndoId, number++, SignalType.ANALOG))
                    .number(i)
                    .name(measData.get(0).getAnalogMeas().get(i).getName())
                    .values(measData.stream()
                            .map(m -> m.getAnalogMeas().get(finalI))
                            .map(AnalogMeasData::getVal)
                            .toList())
                    .build());
        }

        for (int i = 0; i < rmsSize; i++) {
            int finalI = i;
            measurements.add(Measurement.builder()
                    .key(new Measurement.Key(fileIndoId, number++, SignalType.RMS))
                    .number(i)
                    .name(measData.get(0).getRmsMeas().get(i).getName())
                    .values(measData.stream()
                            .map(m -> m.getRmsMeas().get(finalI))
                            .map(AnalogMeasData::getVal)
                            .toList())
                    .build());
        }

        for (int i = 0; i < digitalSize; i++) {
            int finalI = i;
            measurements.add(Measurement.builder()
                    .key(new Measurement.Key(fileIndoId, number++, SignalType.DIGITAL))
                    .number(i)
                    .name(measData.get(0).getDigitalMeas().get(i).getName())
                    .values(measData.stream()
                            .map(m -> m.getDigitalMeas().get(finalI))
                            .map(m -> m.isVal() ? 1.0 : 0.0)
                            .toList())
                    .build());
        }

        return measurements;
    }

}
