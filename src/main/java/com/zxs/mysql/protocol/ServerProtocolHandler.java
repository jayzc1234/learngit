package com.zxs.mysql.protocol;

import java.nio.ByteBuffer;

/**
 * 协议处理器
 * @author zc
 *
 * 1              [0a] protocol version
 * string[NUL]    server version
 * 4              connection id
 * string[8]      auth-plugin-data-part-1
 * 1              [00] filler
 * 2              capability flags (lower 2 bytes)
 *   if more data in the packet:
 * 1              character set
 * 2              status flags
 * 2              capability flags (upper 2 bytes)
 *   if capabilities & CLIENT_PLUGIN_AUTH {
 * 1              length of auth-plugin-data
 *   } else {
 * 1              [00]
 *   }
 * string[10]     reserved (all [00])
 *   if capabilities & CLIENT_SECURE_CONNECTION {
 * string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
 *   if capabilities & CLIENT_PLUGIN_AUTH {
 * string[NUL]    auth-plugin name
 *   }
 */
public class ServerProtocolHandler {
    public static void handleServerPacket(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        int remaining = byteBuffer.limit();
        MysqlShakeMessage mysqlShakeMessage = new MysqlShakeMessage(byteBuffer);
        int packLength = mysqlShakeMessage.getPackLength();
        System.out.println("接收到服务器字节数："+remaining);
        if (remaining < packLength){
            return;
        }
        byte seq = byteBuffer.get();
        HandShakeV1OProtocol v1OProtocol = new HandShakeV1OProtocol();
        v1OProtocol.setSeq(seq);
        v1OProtocol.setProtocolVersion(byteBuffer.get());
        v1OProtocol.setServerVersion(mysqlShakeMessage.getServerVersion());
        v1OProtocol.setAuthPluginDataPart1(mysqlShakeMessage.getFixedLengthString(8));
        v1OProtocol.setFiller(byteBuffer.get());
        v1OProtocol.setLowerCapabilityFlags(mysqlShakeMessage.getInt(2));
        if (0 == byteBuffer.remaining()){
          return;
        }

        v1OProtocol.setCharacterSet(byteBuffer.get());
        v1OProtocol.setStatusFlags(mysqlShakeMessage.getInt(2));

        byte[] upperCapacityFlags = readLengthData(byteBuffer, 2);
        v1OProtocol.setUpperCapabilityFlags(upperCapacityFlags);
        v1OProtocol.setLengthPluginData(byteBuffer.get());
        v1OProtocol.setReserved(readLengthData(byteBuffer,10));
        v1OProtocol.setAuthPluginDataPart2(readLengthData(byteBuffer,Math.max(13,v1OProtocol.getLengthPluginData()-8)));
        v1OProtocol.setPluginName(readNullEndByte(byteBuffer));
        System.out.println(v1OProtocol);
    }

    private static byte[] readLengthData(ByteBuffer byteBuffer, int length) {
        byte data [] =new byte[length];
        byteBuffer.get(data);
        return data;
    }

    private static byte[] readNullEndByte(ByteBuffer byteBuffer) {
        int index = 0;
        int position = byteBuffer.position();
        byte b = 0;
        while ((b=byteBuffer.get(position + 1)) != 0){
          if (b == 0){
              break;
          }
          index++;
        }
        byte data[] = new byte[index];
        byteBuffer.get(data);
        return data;
    }
}
