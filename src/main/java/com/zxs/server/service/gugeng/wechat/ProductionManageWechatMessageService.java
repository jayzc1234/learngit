package com.zxs.server.service.gugeng.wechat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.utils.http.SuperCodeRequests;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.enums.gugeng.MessageTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.wechat.ProductionManageWechatMessage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.wechat.ProductionManageWechatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-11-18
 */
@Service
public class ProductionManageWechatMessageService extends ServiceImpl<ProductionManageWechatMessageMapper, ProductionManageWechatMessage> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    public SuperCodeRequests codeRequests;


    @Transactional
    public void add(Object obj) throws SuperCodeException {
        ProductionManageWechatMessage entity = new ProductionManageWechatMessage();
        // TODO 添加相应的业务逻辑
        baseMapper.insert(entity);
    }

    /**
     * 创建推送消息
     * @param message
     * @param toUserIdList 用户id数组
     * @param messageType 消息类型：订单消息为0，库存消息为1
     */
    public void createMessage(String message, List<String> toUserIdList, MessageTypeEnum messageType){
        for(String userId: toUserIdList){
            ProductionManageWechatMessage wechatMessage=new ProductionManageWechatMessage();
            wechatMessage.setMessage(message);
            wechatMessage.setToUserId(userId);
            wechatMessage.setMessageType(messageType.getKey());
            wechatMessage.setCreateDate(new Date());
            wechatMessage.setSendStatus(0);
            wechatMessage.setCreateUserId(commonUtil.getUserId());
            wechatMessage.setOrganizationId(commonUtil.getOrganizationId());
            wechatMessage.setSysId(commonUtil.getSysId());
            baseMapper.insert(wechatMessage);
        }
    }

    public void createOrderMessage(){
        try{
           List<String> userIds= getUserIdListByServiceId();
           createMessage("您有一条待审核的订单，请登录系统及时审核。", userIds, MessageTypeEnum.Order);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<String> getUserIdListByServiceId() {
        String url = commonUtil.getHydraUserDomain();
//        url+= "/sysuser/getUserIdListByServiceId";
        url+= "/user/list/service-url";
        Map<String, Object> params = new HashMap<>(1);
        params.put("serviceUrl", "/productmanage/sale/order/verify");

        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("super-token", commonUtil.getSuperToken());

        List<String> userIds=null;
        try {
            String result = codeRequests.getAndGetResultBySpring(url, params, headerMap, String.class, true);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getIntValue("state") == 200) {
                JSONArray results = jsonObject.getJSONArray("results");
                userIds = results.toJavaList(String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userIds;
    }

}
