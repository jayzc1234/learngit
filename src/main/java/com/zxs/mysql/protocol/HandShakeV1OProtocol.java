package com.zxs.mysql.protocol;


import lombok.Data;

/**
 * 握手协议--服务端发送到客户端
 * @author zc
 */
@Data
public class HandShakeV1OProtocol {
  private byte seq;
  private byte protocolVersion;
  private byte[] serverVersion;
  private int connectionId;
  private byte[] authPluginDataPart1;
  private byte filler;
  private int lowerCapabilityFlags;
  private byte characterSet;
  private int statusFlags;
  private byte[] upperCapabilityFlags;
  private byte lengthPluginData;
  private byte[] reserved;
  private byte[] authPluginDataPart2;
  private byte[] pluginName;

  @Override
  public String toString() {
    return "HandShakeV1OProtocol:" +
            "seq:="+seq+"\r"+
            "protocolVersion=" + protocolVersion +"\r"+
            ", serverVersion=" + new String(serverVersion) +"\r"+
            ", connectionId=" + connectionId +"\r"+
            ", authPluginDataPart1=" + new String(authPluginDataPart1) +"\r"+
            ", filler=" + filler +"\r"+
            ", lowerCapabilityFlags=" +lowerCapabilityFlags +"\r"+
            ", characterSet=" + characterSet +"\r"+
            ", statusFlags=" + statusFlags +"\r"+
            ", upperCapabilityFlags=" + new String(upperCapabilityFlags) +"\r"+
            ", lengthPluginData=" + lengthPluginData +"\r"+
            ", reserved=" + new String(reserved) +"\r"+
            ", authPluginDataPart2=" + new String(authPluginDataPart2) +"\r"+
            ", pluginName=" + new String(pluginName);
  }
}
