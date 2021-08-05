package com.zxs.server.service.gugeng.common;

import com.alibaba.fastjson.JSONObject;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.OuterRequestUrlConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.AddOrgBroadcastMessageModel;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MessageInformService extends CommonUtil {

	/**
	 *
	 * @param recipientId
	 * @param recipientName
	 * @param msgContent
	 * @param senderType  发送人类型：0-个人(0)；1-管理员(1、2)；2-企业发送（1）；
	 * @param recipientType  接收者类型：0-个人；1-部门；2-企业；5-一组人（若干个用户
	 * @param msgType 消息类型：0-单播；1-组播；
	 * @return
	 * @throws SuperCodeException
	 */
    public String sendOrgMessageToAllPartmentUser(String recipientId,String recipientName,String msgContent,int senderType,int recipientType,int msgType)  {
    	AddOrgBroadcastMessageModel message=new AddOrgBroadcastMessageModel();
    	message.setMsgContent(msgContent);
    	message.setMsgType(msgType);//1
    	message.setRecipientId(recipientId);
    	message.setRecipientName(recipientName);
    	message.setMsgContent(msgContent);
    	message.setSenderType(senderType);//2
    	message.setRecipientType(recipientType);//1
    	message.setSenderId(getOrganizationId());
    	message.setSenderName(getOrganizationName());
    	String rquestData=JSONObject.toJSONString(message);
    	Map<String, String>headerJson=new HashMap<String, String>();
    	headerJson.put("super-token", getSuperToken());
    	log.info("发送企业信息数据："+rquestData);
    	String data=codeRequests.postAndGetResultBySpring(getUserDomain()+ OuterRequestUrlConstant.USER_SEND_ORG_MESSAGE_URL,rquestData , headerJson, String.class, getUserIsLoadBalanced());
        log.info("发送企业信息返回结果："+data);
    	return data;
    }
}
