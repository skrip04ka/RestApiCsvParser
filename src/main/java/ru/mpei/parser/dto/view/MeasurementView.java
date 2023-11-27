package ru.mpei.parser.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mpei.parser.dto.Range;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementView {
    private KeyView key;
    private int number;
    private String name;
    private Range range;
    private List<Double> values;
}
