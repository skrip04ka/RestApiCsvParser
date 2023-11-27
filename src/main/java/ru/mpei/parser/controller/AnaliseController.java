package ru.mpei.parser.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mpei.parser.dto.analise.FaultData;
import ru.mpei.parser.dto.analise.FaultPhasesNumber;
import ru.mpei.parser.service.AnaliseService;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class AnaliseController {

    private final AnaliseService analiseService;

    @Autowired
    public AnaliseController(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    @PostMapping("/file/{id}/analise")
    public FaultData analiseMeas(@PathVariable UUID id,
                                 @RequestBody FaultPhasesNumber faultPhasesNumber) {
        if (faultPhasesNumber.getStock() == null) {
            return analiseService.analiseMeas(id, faultPhasesNumber);
        } else {
            return analiseService.analiseMeas(id, faultPhasesNumber, faultPhasesNumber.getStock());
        }
    }
}
