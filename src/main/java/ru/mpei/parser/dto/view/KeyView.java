package ru.mpei.parser.dto.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mpei.parser.model.SignalType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyView {
    private UUID fileInfoId;
    private int signalNumber;
    private SignalType type;
}
