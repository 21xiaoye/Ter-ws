import org.ws.WebSocket;
import org.ws.handshake.ClientHandshake;
import org.ws.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ChatServer extends WebSocketServer{

    public ChatServer(int port) {
        super(new InetSocketAddress(port));
    }

    public ChatServer(InetSocketAddress address){
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        System.out.println(
                conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer byteBuffer) {

    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
    }

    @Override
    public void onError(WebSocket conn, Exception exception) {

    }

//    public ChatServer(int port, Draft_6455 draft6455){
//        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft6455));
//    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        ChatServer s = new ChatServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
//            s.broadcast(in);
            if (in.equals("exit")) {
                s.stop(1000);
                break;
            }
        }
    }
}
