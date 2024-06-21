package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;
import org.ter_ws.exceptions.InvalidDataException;
import org.ter_ws.utils.ByteBufferUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

public abstract class FrameDataImpl implements FrameData{
    private boolean fin;
    private OpCode opCode;
    private ByteBuffer unMaskedPayload;
    private boolean transferMasked;
    private boolean rsv1;
    private boolean rsv2;
    private boolean rsv3;

    public FrameDataImpl(OpCode opCode) {
        this.opCode = opCode;
        this.fin = true;
        this.unMaskedPayload = ByteBufferUtils.getEmptyBuffer();
        this.transferMasked = false;
        this.rsv1 = false;
        this.rsv2 = false;
        this.rsv3 = false;
    }

    @Override
    public boolean isFin() {
        return fin;
    }

    @Override
    public boolean isRSV1() {
        return rsv1;
    }

    @Override
    public boolean isRSV2() {
        return rsv2;
    }

    @Override
    public boolean isRSV3() {
        return rsv3;
    }

    @Override
    public boolean getTransferMasked() {
        return transferMasked;
    }

    @Override
    public OpCode getOpcode() {
        return opCode;
    }

    @Override
    public ByteBuffer getPayloadData() {
        return unMaskedPayload;
    }

    @Override
    public void append(FrameData nextFrame) {
        ByteBuffer payloadData = nextFrame.getPayloadData();
        // 为空直接拷贝创建
        if(Objects.isNull(unMaskedPayload)){
            unMaskedPayload = ByteBuffer.allocate(payloadData.remaining());
            payloadData.mark();
            unMaskedPayload.put(payloadData);
            payloadData.reset();
        }else{
            payloadData.mark();
            // 从已有数据末尾开始进行追加
            unMaskedPayload.position(unMaskedPayload.limit());
            // 保证容量能够容纳追加数据，不会因为容量不够导致追加失败
            unMaskedPayload.limit(unMaskedPayload.capacity());

            // 需要进行扩容
            if(payloadData.remaining() > unMaskedPayload.remaining()){
                ByteBuffer temp = ByteBuffer.allocate(payloadData.remaining() + unMaskedPayload.capacity());
                unMaskedPayload.flip();
                temp.put(unMaskedPayload);
                temp.put(payloadData);
                unMaskedPayload = temp;
            }else{
                unMaskedPayload.put(payloadData);
            }
            unMaskedPayload.rewind(); // 设置位置为0，从开始处读取数据
            payloadData.reset();
        }
        fin = nextFrame.isFin();
    }

    public void setFin(boolean fin) {
        this.fin = fin;
    }

    public void setUnMaskedPayload(ByteBuffer unMaskedPayload) {
        this.unMaskedPayload = unMaskedPayload;
    }

    public void setTransferMasked(boolean transferMasked) {
        this.transferMasked = transferMasked;
    }

    public void setRsv1(boolean rsv1) {
        this.rsv1 = rsv1;
    }

    public void setRsv2(boolean rsv2) {
        this.rsv2 = rsv2;
    }

    public void setRsv3(boolean rsv3) {
        this.rsv3 = rsv3;
    }
    /**
     * 对帧进行检查，具体实现由子类完成
     *
     * @throws InvalidDataException
     */
    public abstract void isValid() throws InvalidDataException;
    @Override
    public String toString() {
        return "Framedata{ opcode:" + getOpcode() + ", fin:" + isFin() + ", rsv1:" + isRSV1()
                + ", rsv2:" + isRSV2() + ", rsv3:" + isRSV3() + ", payload length:[pos:" + unMaskedPayload
                .position() + ", len:" + unMaskedPayload.remaining() + "], payload:" + (
                unMaskedPayload.remaining() > 1000 ? "(too big to display)"
                        : new String(unMaskedPayload.array())) + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        FrameDataImpl that = (FrameDataImpl) object;

        if (fin != that.fin) {
            return false;
        }
        if (transferMasked != that.transferMasked) {
            return false;
        }
        if (rsv1 != that.rsv1) {
            return false;
        }
        if (rsv2 != that.rsv2) {
            return false;
        }
        if (rsv3 != that.rsv3) {
            return false;
        }
        if (opCode != that.opCode) {
            return false;
        }
        return Objects.equals(unMaskedPayload, that.unMaskedPayload);
    }

    @Override
    public int hashCode() {
        int result = (fin ? 1 : 0);
        result = 31 * result + opCode.hashCode();
        result = 31 * result + (unMaskedPayload != null ? unMaskedPayload.hashCode() : 0);
        result = 31 * result + (transferMasked ? 1 : 0);
        result = 31 * result + (rsv1 ? 1 : 0);
        result = 31 * result + (rsv2 ? 1 : 0);
        result = 31 * result + (rsv3 ? 1 : 0);
        return result;
    }
}
