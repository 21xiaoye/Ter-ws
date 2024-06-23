package org.ws.handshake;

import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * 握手的具体实现
 */
public class HandshakeImpl implements HandshakeBuild{
    /**
     * 握手内容
     */
    private byte[] content;

    /**
     * http字段名和值
     */
    private TreeMap<String, String> map;

    public HandshakeImpl() {
        map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public void put(String name, String value) {
        map.put(name, value);
    }

    @Override
    public Iterator<String> iterateHttpFields() {
        return Collections.unmodifiableSet(map.keySet()).iterator();
    }

    @Override
    public String getFieldValue(String name) {
        String value = map.get(name);
        if(value  == null){
            return "";
        }
        return value;
    }

    @Override
    public boolean hasFieldValue(String name) {
        return map.containsKey(name);
    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
