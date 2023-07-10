package ru.mpei.parser.util;

import com.ibm.icu.text.Transliterator;

import java.util.ArrayList;
import java.util.List;

public class ParserUtil {

    private static final Transliterator toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN");

    private ParserUtil() {
    }

    public static String toCorrectStr(String str) {
//        return toLatinTrans.transliterate(str)
//                .replaceAll("\\.", "_")
//                .replaceAll("№", "N")
//                .replaceAll("[^a-zA-Z0-9_]", "");
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


}
