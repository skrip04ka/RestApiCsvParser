package ru.mpei.parser.service.kafka;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.model.MeasurementRecord;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class KafkaService {

    private final TopicProducer topicProducer;

    @Autowired
    public KafkaService(TopicProducer topicProducer) {
        this.topicProducer = topicProducer;
    }

    @SneakyThrows
    public void parseFile(MultipartFile file) {
        InputStream inputStream = file.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();
        line = bufferedReader.readLine();
        while (line != null) {
            String[] stringParts = line.split(",");
            if (stringParts.length > 4) {
                topicProducer.send(new MeasurementRecord(
                        Double.parseDouble(stringParts[0]),
                        Double.parseDouble(stringParts[1]),
                        Double.parseDouble(stringParts[2]),
                        Double.parseDouble(stringParts[3])
                ));
            }

            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        log.info("File read successfully");
    }

}
