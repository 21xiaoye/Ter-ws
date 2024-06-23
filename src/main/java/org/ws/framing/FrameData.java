package org.ws.framing;

import org.ws.enums.OpCode;

import java.nio.ByteBuffer;

/**
 * ֡�Ľӿ�
 */
public interface FrameData {
    /**
     * �жϵ�ǰ֡�Ƿ�Ϊ��Ϣ�����һ֡
     *
     * @return
     */
    boolean isFin();

    /**
     * �жϴ�֡�Ƿ�ΪRSV1
     *
     * @return
     */
    boolean isRSV1();

    /**
     * �жϴ�֡�Ƿ�ΪRSV2
     *
     * @return
     */
    boolean isRSV2();

    /**
     * �жϴ�֡�Ƿ�ΪRSV3
     *
     * @return
     */
    boolean isRSV3();

    /**
     * �ж�֡�Ƿ�ʹ�����룬�ͻ��˷���һ֡��Ҫʹ�����룬����˷���֡����Ҫʹ������
     *
     * @return
     */
    boolean getTransferMasked();

    /**
     * ��ȡ��ǰ֡������
     *
     * @return
     */
    OpCode getOpcode();

    /**
     * ��ȡ��ǰ֡����Ч�غ�����
     *
     * @return
     */
    ByteBuffer getPayloadData();

    /**
     * ��ǰ֡׷��֡������¸���fin
     *
     * @param nextFrame
     */
    void append(FrameData nextFrame);
}