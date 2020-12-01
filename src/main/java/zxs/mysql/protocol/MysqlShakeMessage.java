package zxs.mysql.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * shake message
 * @author zc
 */
public class MysqlShakeMessage {


    private ByteBuffer byteBuffer;

    public MysqlShakeMessage(ByteBuffer byteBuffer){
        this.byteBuffer = byteBuffer;
    }

    public int getPackLength(){
        byte b1 = byteBuffer.get();
        byte b2 = byteBuffer.get();
        byte b3 = byteBuffer.get();
        return 1;
    }

    public static void main(String[] args) {
        byte a =-2;
        int i = (a & 0xff);
        System.out.println(i);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        ByteBuffer put = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).put("dd".getBytes());
        System.out.println(put);
    }

}
