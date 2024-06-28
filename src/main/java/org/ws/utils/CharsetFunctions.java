package org.ws.utils;

import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class CharsetFunctions {
    public CharsetFunctions(){
    }

    private static final CodingErrorAction codingErrorAction = CodingErrorAction.REPLACE;
    public static byte[] asciiBytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }
    public static String stringAscii(byte[] bytes) {
        return stringAscii(bytes, 0, bytes.length);
    }

    public static String stringAscii(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.US_ASCII);
    }













}
