package ru.mpei.parser.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MeasurementRecord;

@Slf4j
@Service
public class TopicProducer {
    @Value("${topic.name.producer}")
    private String topicName;

    private final KafkaTemplate<String, MeasurementRecord> kafkaTemplate;

    @Autowired
    public TopicProducer(KafkaTemplate<String, MeasurementRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(MeasurementRecord measurement){
        kafkaTemplate.send(topicName, measurement);
    }
}
