package ru.mpei.parser.dto.comtrade.cfg;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalCfg {
    private int channelNumber;
    private String channelId;
    private int normalState;
}
