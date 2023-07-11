package ru.mpei.parser.service;

import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

public interface ComtradeService {
    @SneakyThrows
    void parseFile(MultipartFile cfg, MultipartFile dat);
}
