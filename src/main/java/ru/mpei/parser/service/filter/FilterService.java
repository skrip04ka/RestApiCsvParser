package ru.mpei.parser.service.filter;

import ru.mpei.parser.dto.data.MeasData;

import java.util.List;

public interface FilterService {
    int calculate(List<MeasData> measurementList, double freq);
}
