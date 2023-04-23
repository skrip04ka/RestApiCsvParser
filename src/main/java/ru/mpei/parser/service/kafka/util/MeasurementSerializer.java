package ru.mpei.parser.service.kafka.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import ru.mpei.parser.model.MeasurementRecord;

@Slf4j
public class MeasurementSerializer implements Serializer<MeasurementRecord> {

    @Override
    public byte[] serialize(String s, MeasurementRecord measurementRecord) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (measurementRecord != null) {
            try {
                return objectMapper.writeValueAsBytes(measurementRecord);
            } catch (JsonProcessingException e) {
                log.warn("Unable to serialize measurement cause : {}", e.getMessage());
                return new byte[0];
            }
        }
        return new byte[0];
    }
}
