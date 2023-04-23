package ru.mpei.parser.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.mpei.parser.model.MeasurementRecord;
import ru.mpei.parser.service.ClickHouseService;

@Slf4j
@Component
@EnableKafka
public class TopicListener {

    private final ClickHouseService clickHouseService;

    @Autowired
    public TopicListener(ClickHouseService clickHouseService) {
        this.clickHouseService = clickHouseService;
    }

    @KafkaListener(topics = "${topic.name.consumer}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, MeasurementRecord> payload) {
        clickHouseService.save(payload.value());
    }
}
