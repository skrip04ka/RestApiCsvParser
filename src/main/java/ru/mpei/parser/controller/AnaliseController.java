package ru.mpei.parser.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mpei.parser.model.dto.FaultData;
import ru.mpei.parser.service.AnaliseService;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class AnaliseController {

    private final AnaliseService analiseService;

    @Autowired
    public AnaliseController(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    @GetMapping("/data/analise/{id}")
    public FaultData analiseMeas(@PathVariable Long id,
                                 @RequestParam String phAName,
                                 @RequestParam String phBName,
                                 @RequestParam String phCName,
                                 @RequestParam(required = false) Double stock) {
        if (stock==null) {
            return analiseService.analiseMeas(id, phAName, phBName, phCName);
        } else {
            return analiseService.analiseMeas(id, phAName, phBName, phCName, stock);
        }
    }

}
