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
import ru.mpei.parser.service.CsvService;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class CsvController {
    private final CsvService csvService;

    @Autowired
    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/file/upload/csv")
    public ResponseEntity<ResponseMessage<String>> uploadCsv(@RequestParam MultipartFile csv) {
        log.info("get one file {}", csv.getOriginalFilename());
        try {
            csvService.parseFile(csv);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage<>(
                    "Uploaded file successfully: csv:" + csv.getOriginalFilename())
            );
        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage<>(
                    "Could not upload file: csv:" + csv.getOriginalFilename() + ". Error: " + e.getMessage())
            );
        }
    }
}
