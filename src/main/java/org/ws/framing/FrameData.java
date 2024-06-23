package org.ws.framing;

import org.ws.enums.OpCode;

import java.nio.ByteBuffer;

/**
 * 帧的接口
 */
public interface FrameData {
    /**
     * 判断当前帧是否为消息的最后一帧
     *
     * @return
     */
    boolean isFin();

    /**
     * 判断此帧是否为RSV1
     *
     * @return
     */
    boolean isRSV1();

    /**
     * 判断此帧是否为RSV2
     *
     * @return
     */
    boolean isRSV2();

    /**
     * 判断此帧是否为RSV3
     *
     * @return
     */
    boolean isRSV3();

    /**
     * 判断帧是否使用掩码，客户端发送一帧需要使用掩码，服务端发送帧不需要使用掩码
     *
     * @return
     */
    boolean getTransferMasked();

    /**
     * 获取当前帧的类型
     *
     * @return
     */
    OpCode getOpcode();

    /**
     * 获取当前帧的有效载荷数据
     *
     * @return
     */
    ByteBuffer getPayloadData();

    /**
     * 当前帧追加帧，会更新覆盖fin
     *
     * @param nextFrame
     */
    void append(FrameData nextFrame);
}