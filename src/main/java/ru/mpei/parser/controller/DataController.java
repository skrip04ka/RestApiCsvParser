package ru.mpei.parser.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FileInfo;
import ru.mpei.parser.model.dto.MeasList;
import ru.mpei.parser.service.MeasurementsService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class DataController {

    private final MeasurementsService measurementsService;

    @Autowired
    public DataController(MeasurementsService measurementsService) {
        this.measurementsService = measurementsService;
    }


    @PostMapping("/data/get-data/{id}")
    public List<MeasList> getDataAll(@PathVariable Long id,
                                     @RequestParam List<String> names,
                                     @RequestParam Integer start,
                                     @RequestParam Integer end) {
        List<MeasList> list = measurementsService.getMeasByName(id, names, start, end);
        log.info("send all data len {}, analogMeas = {}, digital meas = {}", list.size(),
                list.get(0).getMeas().size(),
                list.get(0).getDmeas().size());
        return list;
    }

    @GetMapping("/data/names/{id}")
    public List<String> getMeasName(@PathVariable Long id) {
        return measurementsService.getMeasName(id);
    }

    @GetMapping("/data/meta-inf/{id}")
    public MetaInf getMetaInf(@PathVariable Long id) {
        return measurementsService.getMetaInf(id);
    }

    @GetMapping("/data/files")
    public List<FileInfo> getMeasName() {
        return measurementsService.getFilesInfo();
    }

}
