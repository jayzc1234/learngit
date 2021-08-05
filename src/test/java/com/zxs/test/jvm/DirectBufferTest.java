package com.zxs.test.jvm;

import io.netty.buffer.UnpooledDirectByteBuf;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class DirectBufferTest {
    public static void main(String[] args) {
        ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(5 * 1024 * 1024);
        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(5 * 1024 * 1024);
        ByteBuffer byteBuffer3 = ByteBuffer.allocateDirect(5 * 1024 * 1024);
    }
}
