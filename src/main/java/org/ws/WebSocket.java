package org.ws;

import org.ws.enums.ReadyState;
import org.ws.framing.FrameData;
import org.ws.protocols.IProtocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface WebSocket {
    /**
     *  �������ֽ����¼�
     *
     * @param code      �رձ�ʶ
     * @param message   �ر���Ϣ
     */
    void close(int code, String message);

    /**
     * �������ֽ����¼�
     *
     * @param code  �رձ�ʶ
     */
    void close(int code);

    /**
     * �������ֽ����¼�
     */
    void close();

    /**
     * �����ر�����
     *
     * @param code
     * @param message
     */

    void closeConnection(int code, String message);

    /**
     * �����ı�����
     *
     * @param text
     */
    void send(String text);

    /**
     * ���Ͷ���������
     *
     * @param byteBuffer
     */
    void send(ByteBuffer byteBuffer);

    /**
     * ���Ͷ���������
     *
     * @param bytes
     */
    void send(byte[] bytes);

    /**
     * ����֡����
     *
     * @param frameData
     */
    void sendFrame(FrameData frameData);

    /**
     * ����һ��ping
     */
    void sendPing();

    /**
     * ����Ƿ���л�������
     * @return
     */
    boolean hasBufferedData();

    /**
     * ��ȡԶ�����ӵ�ַ��Ϣ
     *
     * @return
     */
    InetSocketAddress getRemoteSocketAddress();

    /**
     *��ȡԶ�����ӵ�ַ��Ϣ
     *
     * @return
     */
    InetSocketAddress getLocalSocketAddress();

    /**
     * �������״̬�Ƿ�ΪOPEN
     * @see org.ws.enums.ReadyState
     *
     * @return
     */
    boolean isOpen();

    /**
     * �������״̬�Ƿ�Ϊ CLOSING
     * @see org.ws.enums.ReadyState
     * @return
     */
    boolean isClosing();

    /**
     * �������״̬�Ƿ�ΪCLOSED
     * @see org.ws.enums.ReadyState
     *
     * @return
     */
    boolean isClosed();

    /**
     * ��ȡWebSocket����״̬
     *
     * @return
     */
    ReadyState getReadyState();

    /**
     * ����WebSocket��Ҫ��Sec-WebSocket-Protocol
     *
     * @return
     */
    IProtocol getProtocol();
}
