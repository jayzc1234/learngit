package com.zxs.mysql.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * shake message
 * @author zc
 */
public class MysqlShakeMessage {


    private ByteBuffer byteBuffer;

    public MysqlShakeMessage(ByteBuffer byteBuffer){
        this.byteBuffer = byteBuffer;
    }

    /**
     * 获取包长度，取接收到的数据前3个字节转成int
     * 要将小端模式转为大端模式
     * @return
     */
    public int getPackLength(){
        int b1 = byteBuffer.get() & 0xff ;
        int b2 = byteBuffer.get() & 0xff << 8;
        int b3 = byteBuffer.get() & 0xff << 16;
        return b1 | b2 | b3;
    }

    /**
     * 获取版本号读到null即00字节时结束
     * utf-8字符不需要考虑大小端问题
     * @return
     */
    public byte[] getServerVersion() {
        byte b ;
        List<Byte> bytes = new ArrayList<>();
        while ((b = byteBuffer.get()) != 0){
            bytes.add(b);
        }
        byte [] data = new byte[bytes.size()];

        for (int i = 0;i<bytes.size();i++){
            data[i] = bytes.get(i);
        }
        return data;
    }

    public byte[] getFixedLengthString(int length) {
        byte[]  data = new byte[length];
        byteBuffer.get(data);
        return data;
    }

    public int getInt(int length) {
        if (length > 4){
            throw new RuntimeException("length 不合法，不能超过4");
        }
        int len = length;
        byte[] bytes = new byte[length];
        int []int_data = new int[length];
        int index =0;
        byteBuffer.get(bytes);
        for (int i = bytes.length-1;i >= 0;i--){
            int offset = len-- * 8;
            int i1 = bytes[i] & 0xff << offset;
            int_data[index++] = i1;
        }

        int result =0;
        for (int int_datum : int_data) {
            result |=int_datum;
        }
        return result;
    }
}
