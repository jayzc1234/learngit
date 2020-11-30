package zxs.mysql.protocol;


import lombok.Data;

/**
 * 握手协议--服务端发送到客户端
 * @author zc
 */
@Data
public class HandShakeV1OProtocol {
  private byte protocolVersion;
  private byte[] serverVersion;
  private int connectionId;
  private byte[] authPluginDataPart1;
  private byte filler;
  private byte[] lowerCapabilityFlags;
  private byte characterSet;
  private byte[] statusFlags;
  private byte[] upperCapabilityFlags;
  private byte lengthPluginData;
  private byte[] reserved;
  private byte[] authPluginDataPart2;
  private byte[] pluginName;

  @Override
  public String toString() {
    return "HandShakeV1OProtocol:" +
            "protocolVersion=" + protocolVersion +"\r"+
            ", serverVersion=" + new String(serverVersion) +"\r"+
            ", connectionId=" + connectionId +"\r"+
            ", authPluginDataPart1=" + new String(authPluginDataPart1) +"\r"+
            ", filler=" + filler +"\r"+
            ", lowerCapabilityFlags=" + new String(lowerCapabilityFlags) +"\r"+
            ", characterSet=" + characterSet +"\r"+
            ", statusFlags=" + new String(statusFlags) +"\r"+
            ", upperCapabilityFlags=" + new String(upperCapabilityFlags) +"\r"+
            ", lengthPluginData=" + lengthPluginData +"\r"+
            ", reserved=" + new String(reserved) +"\r"+
            ", authPluginDataPart2=" + new String(authPluginDataPart2) +"\r"+
            ", pluginName=" + new String(pluginName);
  }
}
