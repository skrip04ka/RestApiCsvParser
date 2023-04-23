package ru.mpei.parser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.service.kafka.KafkaService;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaService kafkaService;

    @Autowired
    public KafkaController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping("/upload")
    public void uploadFile(@RequestParam MultipartFile file) {
        kafkaService.parseFile(file);
    }


}
