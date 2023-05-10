package ru.mpei.parser.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mpei.parser.model.MeasList;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.service.RepositoryService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class ClickHouseController {

    private final RepositoryService repositoryService;

    @Autowired
    public ClickHouseController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }


    @PostMapping("/data/get-data")
    public List<MeasList> getDataAll(@RequestParam List<String> names,
                                     @RequestParam Integer start,
                                     @RequestParam Integer end) {
        List<MeasList> list = repositoryService.getMeasByName(names, start, end);
        log.info("send all data len {}, analogMeas = {}, digital meas = {}", list.size(),
                list.get(0).getMeas().size(),
                list.get(0).getDmeas().size());
        return list;
    }

    @GetMapping("/data/names")
    public List<String> getMeasName() {
        return repositoryService.getMeasName();
    }

    @GetMapping("/data/meta-inf")
    public MetaInf getMetaInf() {
        return repositoryService.getMetaInf();
    }


}
