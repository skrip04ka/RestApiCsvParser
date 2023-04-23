package ru.mpei.parser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mpei.parser.model.MeasurementRecord;
import ru.mpei.parser.service.ClickHouseService;

import java.util.List;

@RestController
@RequestMapping("/click-house")
public class ClickHouseController {

    private final ClickHouseService clickHouseService;

    @Autowired
    public ClickHouseController(ClickHouseService clickHouseService) {
        this.clickHouseService = clickHouseService;
    }

    @GetMapping("/get-data")
    public List<MeasurementRecord> getData() {
        return clickHouseService.findAll();
    }
}
