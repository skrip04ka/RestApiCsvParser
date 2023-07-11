package ru.mpei.parser.service;

import ru.mpei.parser.model.Measurements;

import java.util.List;

public interface FilterService {
    int calculate(List<Measurements> measurementList, double freq);
}
