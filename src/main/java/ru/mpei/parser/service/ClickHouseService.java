package ru.mpei.parser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MeasurementRecord;
import ru.mpei.parser.repository.ClickHouseNative;

import java.util.List;

@Service
public class ClickHouseService {
    private final ClickHouseNative repository;

    @Autowired
    public ClickHouseService(ClickHouseNative repository) {
        this.repository = repository;
    }

    public void save(MeasurementRecord measurement) {
        repository.save(measurement);
    }

    public List<MeasurementRecord> findAll() {
        return repository.findAll();
    }

}
