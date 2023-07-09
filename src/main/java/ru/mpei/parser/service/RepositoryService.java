package ru.mpei.parser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.MeasList;
import ru.mpei.parser.repository.MeasurementsRepository;

import java.util.List;

@Service
public class RepositoryService {
    private final MeasurementsRepository measurementsRepository;

    @Autowired
    public RepositoryService(MeasurementsRepository measurementsRepository) {
        this.measurementsRepository = measurementsRepository;
    }

    public List<MeasList> getMeasByName(List<String> names, int start, int end) {
        if (start - end > 60000) end = start + 60000;
        return measurementsRepository.getMeasByNames(names, start, end);
    }

    public MetaInf getMetaInf() {
        return measurementsRepository.getMetaInf();
    }

    public List<String> getMeasName() {
        return measurementsRepository.getMeasNames();
    }
}
