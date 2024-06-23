package org.ws.handshake;

import java.util.Iterator;

/**
 * �������ݽӿ�
 */
public interface HandshakeData {
    /**
     * http�ֶε�����
     *
     * @return
     */
    Iterator<String> iterateHttpFields();

    /**
     * ��ȡhttp�ֶε�ֵ
     *
     * @param name
     * @return
     */
    String getFieldValue(String name);

    /**
     * http�ֶ��Ƿ����
     *
     * @param name
     * @return
     */
    boolean hasFieldValue(String name);

    /**
     * ��ȡ��������
     *
     * @return
     */
    byte[] getContent();
}
