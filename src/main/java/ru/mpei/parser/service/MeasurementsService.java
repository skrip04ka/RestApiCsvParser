package ru.mpei.parser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mpei.parser.dto.FileInfoDto;
import ru.mpei.parser.dto.Range;
import ru.mpei.parser.dto.view.MeasurementInfoView;
import ru.mpei.parser.dto.view.MeasurementView;
import ru.mpei.parser.mapper.FileMapper;
import ru.mpei.parser.mapper.MeasurementMapper;
import ru.mpei.parser.repository.MeasurementRepository;

import java.util.*;

@Service
public class MeasurementsService {
    private final MeasurementRepository measurementsRepository;

    @Autowired
    public MeasurementsService(MeasurementRepository measurementsRepository) {
        this.measurementsRepository = measurementsRepository;
    }

    @Transactional(readOnly = true)
    public List<MeasurementView> getMeasWithValuesByRange(UUID fileInfoId, List<Integer> signalNumbers, Range range) {
        int MAX_SIZE = 50_000;
        range.setEnd(Math.min(range.getEnd() - range.getStart(), MAX_SIZE) + range.getStart());
        return measurementsRepository
                .getMeasurementsByFileInfoIdAndSignalNumbers(fileInfoId, signalNumbers).stream()
                .map(m -> MeasurementMapper.mapToMeasurementViewAndSplitValues(m, range))
                .toList();
    }

    @Transactional(readOnly = true)
    public FileInfoDto getFileInfo(UUID id) {
        return FileMapper.mapToFileInfoDto(measurementsRepository.getFileInfoById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<MeasurementInfoView> getMeasurementsInfo(UUID fileInfoId) {
        return measurementsRepository.getMeasurementsInfo(fileInfoId);
    }

    @Transactional(readOnly = true)
    public List<FileInfoDto> getFilesInfo() {
        return measurementsRepository.getFilesInfo().stream()
                .map(FileMapper::mapToFileInfoDto)
                .toList();
    }

    @Transactional
    public void deleteFileInfo(UUID fileInfoId) {
        measurementsRepository.deleteFileInfo(fileInfoId);
    }
}
