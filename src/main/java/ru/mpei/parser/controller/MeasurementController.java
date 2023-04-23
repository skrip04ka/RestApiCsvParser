package ru.mpei.parser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.service.impl.MeasurementServiceImp;

@RestController
public class MeasurementController {

    private final MeasurementServiceImp measurementService;

    @Autowired
    public MeasurementController(MeasurementServiceImp measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping("/data/upload")
    public void uploadFile(@RequestParam MultipartFile file) {
        measurementService.parseFile(file);

    }

    @GetMapping("/data/findFault")
    public String findFault(@RequestParam int startIndex, @RequestParam int endIndex) {
        return measurementService.findFault(startIndex, endIndex);
    }

    @GetMapping("/saveSetPoint")
    public boolean saveSetPoint(@RequestParam double setPoint) {
        return measurementService.saveSetPoint(setPoint);
    }


}
