package ru.mpei.parser.service;

import org.springframework.web.multipart.MultipartFile;

public interface MeasurementService {

    void parseFile(MultipartFile file);

    String findFault(int startIndex, int endIndex);

    boolean saveSetPoint(double setPoint);
}
