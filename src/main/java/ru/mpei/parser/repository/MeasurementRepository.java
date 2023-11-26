package ru.mpei.parser.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.mpei.parser.dto.view.MeasurementInfoView;
import ru.mpei.parser.mapper.MeasurementMapper;
import ru.mpei.parser.model.FileInfo;
import ru.mpei.parser.model.Measurement;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class MeasurementRepository {

    @PersistenceContext
    private EntityManager em;

    public void saveFile(FileInfo fileInfo) {
        em.persist(fileInfo);
    }


    public List<FileInfo> getFilesInfo() {
        return em.createQuery("select f from FileInfo f", FileInfo.class)
                .getResultList();
    }

    public Optional<FileInfo> getFileInfoById(UUID fileInfoId) {
        return Optional.ofNullable(em.find(FileInfo.class, fileInfoId));
    }

    public List<Measurement> getMeasurementsByFileInfoIdAndSignalNumbers(UUID fileInfoId, List<Integer> signalNumbers) {
        return em.createQuery("select m from Measurement m " +
                                "where m.key.fileInfoId = :fileInfoId " +
                                "and m.key.signalNumber in :signalNumbers ",
                        Measurement.class)
                .setParameter("fileInfoId", fileInfoId)
                .setParameter("signalNumbers", signalNumbers)
                .getResultList();
    }

    public List<MeasurementInfoView> getMeasurementsInfo(UUID fileInfoId) {
        return em.createQuery("select m.key, m.name, m.number from Measurement m " +
                        "where m.key.fileInfoId = :fileInfoId order by m.key.signalNumber", Tuple.class)
                .setParameter("fileInfoId", fileInfoId)
                .getResultList()
                .stream()
                .map(t -> MeasurementInfoView.builder()
                        .key(MeasurementMapper.mapToKeyView((Measurement.Key) t.get(0)))
                        .name((String) t.get(1))
                        .number((Integer) t.get(2))
                        .build())
                .toList();
    }

    public void deleteFileInfo(UUID fileInfoId) {
        em.createQuery("delete FileInfo f where f.id = :fileInfoId")
                .setParameter("fileInfoId", fileInfoId)
                .executeUpdate();
    }
}
