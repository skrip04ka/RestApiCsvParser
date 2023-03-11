package ru.mpei.comtrade.service;

import org.springframework.web.multipart.MultipartFile;

public interface MeasurementService {

    public void parseFile(MultipartFile file);

    public String findFault(int startIndex, int endIndex);

    public boolean saveSetPoint(double setPoint);
}
