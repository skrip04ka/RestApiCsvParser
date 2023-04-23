package ru.mpei.parser.configuration;

import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.SQLException;

import static java.lang.String.format;

@Data
@Slf4j
@Configuration
@ConfigurationProperties("clickhouse")
public class ClickHouseConfig {

    private String host;
    private String port;
    private String database;
    private String user;
    private String password;

    @Bean
    NamedParameterJdbcTemplate namedParameterJdbcTemplate() throws SQLException {
        return new NamedParameterJdbcTemplate(new ClickHouseDataSource(format("jdbc:ch:http://%s:%s", host, port)));
    }

}
