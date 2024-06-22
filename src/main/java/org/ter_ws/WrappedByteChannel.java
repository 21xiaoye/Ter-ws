package org.ter_ws;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface WrappedByteChannel extends ByteChannel {
    boolean isNeedWrite();
    void writeMore();
    boolean isNeedRead();
    int readMore(ByteBuffer dst) throws IOException;
    boolean isBlocking();
}
