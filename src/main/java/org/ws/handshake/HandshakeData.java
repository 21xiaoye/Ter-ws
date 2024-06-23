package org.ws.handshake;

import java.util.Iterator;

/**
 * 握手数据接口
 */
public interface HandshakeData {
    /**
     * http字段迭代器
     *
     * @return
     */
    Iterator<String> iterateHttpFields();

    /**
     * 获取http字段的值
     *
     * @param name
     * @return
     */
    String getFieldValue(String name);

    /**
     * http字段是否存在
     *
     * @param name
     * @return
     */
    boolean hasFieldValue(String name);

    /**
     * 获取握手内容
     *
     * @return
     */
    byte[] getContent();
}
