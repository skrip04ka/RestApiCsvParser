package ru.mpei.parser.dto.comtrade.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DigitalCfg {
    private int channelNumber;
    private String channelId;
    private int normalState;
}
