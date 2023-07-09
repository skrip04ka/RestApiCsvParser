package ru.mpei.parser.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.controller.message.ResponseMessage;
import ru.mpei.parser.service.ComtradeService;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class ComtradeController {
    private final ComtradeService comtradeService;

    @Autowired
    public ComtradeController(ComtradeService comtradeService) {
        this.comtradeService = comtradeService;
    }


    @PostMapping("/data/upload/comtrade")
    public ResponseEntity<ResponseMessage<String>> uploadComtrade(
            @RequestParam MultipartFile cfg,
            @RequestParam MultipartFile dat) {
        log.info("get two file {} {}", cfg.getOriginalFilename(), dat.getOriginalFilename());
        try {
            comtradeService.parseFile(cfg, dat);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage<>(
                    "Uploaded file successfully: cfg: " + cfg.getOriginalFilename() + " dat: " + dat.getOriginalFilename())
            );
        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage<>(
                    "Could not upload the: cfg: " + cfg.getOriginalFilename() + " dat: " + dat.getOriginalFilename() + ". Error: " + e.getMessage())
            );
        }

    }

}
