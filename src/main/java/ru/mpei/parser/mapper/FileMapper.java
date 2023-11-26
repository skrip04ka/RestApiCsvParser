package ru.mpei.parser.mapper;

import ru.mpei.parser.dto.FileInfoDto;
import ru.mpei.parser.dto.data.AnalogMeasData;
import ru.mpei.parser.dto.data.MeasData;
import ru.mpei.parser.model.FileInfo;
import ru.mpei.parser.model.Measurement;
import ru.mpei.parser.model.SignalType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMapper {
    public static FileInfo mapToFileInfo(FileInfoDto fileInfoDto, List<MeasData> measData) {
        UUID fileInfoId = UUID.randomUUID();
        return FileInfo.builder()
                .id(fileInfoId)
                .name(fileInfoDto.getName())
                .type(fileInfoDto.getType())
                .analogNumber(fileInfoDto.getAnalogNumber())
                .digitalNumber(fileInfoDto.getDigitalNumber())
                .freq(fileInfoDto.getFreq())
                .n(fileInfoDto.getN())
                .timeStart(fileInfoDto.getTimeStart())
                .timeEnd(fileInfoDto.getTimeEnd())
                .measurements(mapToListMeasurement(measData, fileInfoId))
                .build();
    }

    public static FileInfoDto mapToFileInfoDto(FileInfo fileInfo) {
        return FileInfoDto.builder()
                .id(fileInfo.getId())
                .name(fileInfo.getName())
                .type(fileInfo.getType())
                .analogNumber(fileInfo.getAnalogNumber())
                .digitalNumber(fileInfo.getDigitalNumber())
                .freq(fileInfo.getFreq())
                .n(fileInfo.getN())
                .timeStart(fileInfo.getTimeStart())
                .timeEnd(fileInfo.getTimeEnd())
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
