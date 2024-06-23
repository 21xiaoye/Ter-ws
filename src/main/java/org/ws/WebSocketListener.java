package org.ws;


import org.ws.draft.Draft;
import org.ws.exceptions.InvalidDataException;
import org.ws.framing.FrameData;
import org.ws.framing.PingFrame;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeData;
import org.ws.handshake.ServerHandshake;
import org.ws.handshake.ServerHandshakeBuilder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface WebSocketListener {
    /**
     * �״���WebSocket��������������ʱ��������ֲ������ɷ���˵���
     * ����˿�������������
     *
     * @param conn
     * @param draft
     * @param request
     * @return
     * @throws com.sun.media.sound.InvalidDataException
     */
    ServerHandshakeBuilder onWebSocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException;

    /**
     * �״ν���WebSocket�������յ�WebSocket�������Ӧ���ɿͻ��˵���
     *
     * @param conn
     * @param request
     * @param response
     * @throws com.sun.media.sound.InvalidDataException
     */
    void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException;

    /**
     * �״ν���WebSocket�����ҷ������������ɿͻ��˵���
     *
     * @param conn
     * @param request
     * @throws com.sun.media.sound.InvalidDataException
     */
    void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException;

    /**
     * �յ��ı�����ʱ����
     *
     * @param conn
     * @param message
     */
    void onWebsocketMessage(WebSocket conn, String message);

    /**
     * �յ�����������ʱ����
     *
     * @param conn
     * @param byteBuffer
     */
    void onWebSocketMessage(WebSocket conn, ByteBuffer byteBuffer);

    /**
     * ������������ʱ����
     *
     * @param conn
     * @param handshakeData
     */
    void onWebSocketOpen(WebSocket conn, HandshakeData handshakeData);

    /**
     * ��ʾ���û��߿ͻ��������ж�ʱ����
     *
     * @param conn
     * @param handshakeData
     */
    void onWebSocketClose(WebSocket conn, HandshakeData handshakeData);

    /**
     *
     *
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    void onWebSocketClosing(WebSocket conn, int code, String reason, boolean remote);

    /**
     * WebSocket����˹ر�����
     *
     * @param ws
     * @param code  �ر�״̬��
     * @param reason    �ر�ԭ��
     */
    void onWebSocketCloseInitiated(WebSocket ws, int code, String reason);
    /**
     * ���ӷ�������ʱ���ã������Ϊ����������ʧ�ܻ����жϽ�����onWebSocketClose();
     *
     * @param conn
     * @param ex
     */
    void onWebsocketError(WebSocket conn, Exception ex);

    /**
     * �յ�ping frameʱ���ã�����pong frame��Ϊ��Ӧ
     * @param conn
     * @param frameData
     */
    void onWebSocketPing(WebSocket conn, FrameData frameData);

    /**
     * ����ping frame֮ǰ���Զ���ping frame
     * @param conn
     * @return
     */
    PingFrame onPreparePing(WebSocket conn);

    /**
     * ���յ� Pong ֡ʱ���ã�ȷ�����ӵĻ�Ծ�Ժ��ӳ١�
     *
     * @param conn
     * @param frameData
     */
    void onWebSocketPong(WebSocket conn, FrameData frameData);

    /**
     * ����Ҫ�� WebSocket����д������ʱ���ô˷����������ݷ����̷߳���ġ�
     * �����������֪ͨѡ�����̣߳�selector thread����������Ҫд�뵽�׽�����
     *
     * @param conn
     */
    void onWriteDemand(WebSocket conn);

    /**
     * ����WebSocket�����ʵ����ַ��Ϣ
     *
     * @param conn
     * @return
     */
    InetSocketAddress getLocalSocketAddress(WebSocket conn);

    /**
     * ����WebSocketʵ�����ӵĿͻ��˵�ַ��Ϣ
     *
     * @param conn
     * @return
     */
    InetSocketAddress getRemoteSocketAddress(WebSocket conn);
}
