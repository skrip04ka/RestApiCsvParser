package ru.mpei.parser.service.kafka.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import ru.mpei.parser.model.MeasurementRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MeasurementDeserializer implements Deserializer<MeasurementRecord> {
    @Override
    public MeasurementRecord deserialize(String s, byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, MeasurementRecord.class);
        } catch (IOException exception) {
            String message = new String(bytes, StandardCharsets.UTF_8);
            log.warn("Unable to deserialize measurement: m - {}, s - {}", message, s);
            return null;
        }
    }
}
