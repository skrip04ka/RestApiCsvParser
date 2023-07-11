package ru.mpei.parser.util;

import ru.mpei.parser.model.Measurements;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.model.measurement.DigitalMeas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserUtil {

    private ParserUtil() {
    }

    public static String toCorrectStr(String str) {
        return str.replaceAll(" ", "");
    }

    public static int bArrTo16Int(byte[] b, int offset) {
        return (short) ((b[offset] & 0xFF) | (b[offset + 1] & 0xFF) << 8);
    }

    public static int bArrTo32UInt(byte[] b, int offset) {
        return ((b[offset] & 0xFF) | (b[offset + 1] & 0xFF) << 8 | (b[offset + 2] & 0xFF) << 16 | (b[offset + 3] & 0xFF) << 24);
    }

    public static List<Boolean> bArrTo8Bit(byte[] b, int offset) {
        int val = b[offset] & 0xFF;
        List<Boolean> arr = new ArrayList<>(8);
        for (int i = 0; i < 8; i++) {
            arr.add((val & 0b1) == 1);
            val = val >> 1;
        }
        return arr;
    }

    public static List<Boolean> bArrTo16Bit(byte[] b, int offset) {
        List<Boolean> arr = new ArrayList<>(16);
        arr.addAll(bArrTo8Bit(b, offset));
        arr.addAll(bArrTo8Bit(b, offset+1));
        return arr;
    }

    public static Map<String, List<Double>> convert(List<Measurements> measurements) {
        Map<String, List<Double>> valuesByName = new HashMap<>();
        for(Measurements m: measurements) {
            valuesByName.computeIfAbsent("time", v -> new ArrayList<>()).add(m.getTime());
            for (AnalogMeas am:m.getAnalogMeas()) {
                valuesByName.computeIfAbsent(am.getName(), v -> new ArrayList<>()).add(am.getVal());
            }
            for (AnalogMeas am:m.getRmsMeas()) {
                valuesByName.computeIfAbsent(am.getName(), v -> new ArrayList<>()).add(am.getVal());
            }
            for (DigitalMeas am:m.getDigitalMeas()) {
                valuesByName.computeIfAbsent(am.getName(), v -> new ArrayList<>()).add(am.isVal() ? 1.0 : 0.0);
            }
        }
        return valuesByName;
    }


}
