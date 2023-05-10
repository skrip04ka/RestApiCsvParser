package ru.mpei.parser.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mpei.parser.model.comtrade.AnalogCfg;
import ru.mpei.parser.model.comtrade.DigitalCfg;
import ru.mpei.parser.model.comtrade.SamplingCfg;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class CfgData {
    private String name;
    private List<AnalogCfg> analogChannels;
    private List<DigitalCfg> digitalChannels;
    private Map<Integer, DataType> dataTypes;
    private double freq;
    private List<SamplingCfg> samplingsFreq;
    private String dateStart;
    private String dateEnd;
    private FileType fileType;

    public enum FileType {
        ASCII, BINARY
    }
    public enum DataType {
        A, D
    }
}
