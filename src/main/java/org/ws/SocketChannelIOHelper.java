package org.ws;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Objects;

public class SocketChannelIOHelper {

    private SocketChannelIOHelper(){
        throw new IllegalStateException("Utility class");
    }

    public static boolean read(final ByteBuffer buffer, WebSocketImpl webSocketImpl, ByteChannel byteChannel)
        throws IOException {
        buffer.clear();
        int read = byteChannel.read(buffer);
        buffer.flip();
        if(read == -1){
            return false;
        }
        return read!=0;
    }

    public static boolean batch(WebSocketImpl webSocketImpl, ByteChannel byteChannel) throws IOException{
        if(Objects.isNull(webSocketImpl)){
            return false;
        }

        ByteBuffer buffer = webSocketImpl.outQueue.poll();
        WrappedByteChannel wrappedByteChannel = null;
        if(Objects.isNull(buffer)){
            if(byteChannel instanceof WrappedByteChannel){
                wrappedByteChannel = (WrappedByteChannel) byteChannel;
                if(wrappedByteChannel.isNeedRead()){
                    wrappedByteChannel.writeMore();
                }
            }
        }else{
            do {
                byteChannel.write(buffer);
                if (buffer.remaining() > 0) {
                    return false;
                } else {
                    webSocketImpl.outQueue.poll();
                    buffer = webSocketImpl.outQueue.peek();
                }
            }while (Objects.nonNull(buffer));
        }
        if(webSocketImpl.outQueue.isEmpty()){
            webSocketImpl.closeConnection();
        }
        return Objects.isNull(wrappedByteChannel) || ((WrappedByteChannel) byteChannel).isNeedRead();
    }
}
