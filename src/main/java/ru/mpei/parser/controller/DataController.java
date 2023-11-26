package ru.mpei.parser.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mpei.parser.dto.FileDto;
import ru.mpei.parser.dto.Range;
import ru.mpei.parser.dto.view.MeasurementInfoView;
import ru.mpei.parser.dto.view.MeasurementView;
import ru.mpei.parser.service.MeasurementsService;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class DataController {

    private final MeasurementsService measurementsService;

    @Autowired
    public DataController(MeasurementsService measurementsService) {
        this.measurementsService = measurementsService;
    }

    @GetMapping("/file/{id}/measurements")
    public List<MeasurementView> getMeasWithValuesByRange(
            @PathVariable UUID id,
            @RequestBody NumbersAndRange numbersAndRange
    ) {
        List<MeasurementView> list = measurementsService.getMeasWithValuesByRange(
                id,
                numbersAndRange.signalNumber,
                numbersAndRange.range);
        log.info("send all data len {}, values size = {}", list.size(),
                list.get(0).getValues().size());
        return list;
    }

    @GetMapping("/file/{id}/measurements/info")
    public List<MeasurementInfoView> getMeasurementsInfo(@PathVariable UUID id) {
        return measurementsService.getMeasurementsInfo(id);
    }

    @GetMapping("/file/{id}/info")
    public FileDto getFileInfo(@PathVariable UUID id) {
        return measurementsService.getFile(id);
    }

    @GetMapping("/files")
    public List<FileDto> getFilesInfo() {
        return measurementsService.getFiles();
    }

    @DeleteMapping("/file/{id}/delete")
    public void deleteFile(@PathVariable UUID id) {
        measurementsService.deleteFile(id);
    }

    @Data
    public static class NumbersAndRange{
        List<Integer> signalNumber;
        Range range;
    }
}
