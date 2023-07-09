package ru.mpei.parser.model.comtrade;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Cfg {
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
