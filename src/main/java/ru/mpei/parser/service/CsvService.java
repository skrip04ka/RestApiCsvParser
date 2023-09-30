package ru.mpei.parser.service;

import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

public interface CsvService {
    @SneakyThrows
    void parseFile(MultipartFile dat);
}
