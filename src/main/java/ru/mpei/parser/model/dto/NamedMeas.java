package ru.mpei.parser.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NamedMeas {
    private String name;
    private Double[] values;
}
