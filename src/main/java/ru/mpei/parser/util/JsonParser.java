package ru.mpei.parser.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;

public class JsonParser {
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @SneakyThrows
    public static <T> String dataToString(T dataClass) {
        return mapper.writeValueAsString(dataClass);
    }

    @SneakyThrows
    public static <T> T parseData(String dataString, Class<T> clazz) {
        return mapper.readValue(dataString, clazz);
    }

}
