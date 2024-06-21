package org.ter_ws.utils;

import java.nio.ByteBuffer;

public class ByteBufferUtils {
    public ByteBufferUtils() {
    }

    public static ByteBuffer getEmptyBuffer(){
        return ByteBuffer.allocate(0);
    }
}
