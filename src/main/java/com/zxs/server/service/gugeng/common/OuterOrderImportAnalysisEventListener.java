package com.zxs.server.service.gugeng.common;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.ProductionManageClientMO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.OuterOrderImportExcelHead;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundDeliveryWay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * 模板的读取类
 *
 * @author Jiaju Zhuang
 */
public class OuterOrderImportAnalysisEventListener extends AnalysisEventListener<OuterOrderImportExcelHead> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OuterOrderImportAnalysisEventListener.class);
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */

    private ApplicationContext applicationContext;

    private OrderImportService orderImportService;

    private Map<Integer, ProductionManageOrder> orderMap=new HashMap<>();

    private Map<Integer, ProductionManageOrderProduct> orderProductHashMap=new HashMap<>();

    private Map<Integer, ProductionManageOutboundDeliveryWay> deliveryWayHashMap=new HashMap<>();

    private List<ProductionManageClientMO> clientMOList=new ArrayList<>();

    private Integer importType;

    private boolean isHistoryImport=true;
    private Date orderDate;
    public OuterOrderImportAnalysisEventListener(OrderImportService orderImportService, Date orderDate, Integer importType) {
        this.orderImportService = orderImportService;
        this.orderDate=orderDate;
        this.importType=importType;
    }

    private static final int BATCH_COUNT = 5;
    List<OuterOrderImportExcelHead> list = new ArrayList<OuterOrderImportExcelHead>();

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {

        System.out.println(headMap);
    }

    @Override
    public void invoke(OuterOrderImportExcelHead data, AnalysisContext context) {
        Map<String,Object> dataMap=orderImportService.invoke(data,orderDate,context,importType);
        if (null!=dataMap && !dataMap.isEmpty()){
            Map<Integer, ProductionManageOrder>orderMap2= (Map<Integer, ProductionManageOrder>) dataMap.get("order");
            Map<Integer, ProductionManageOrderProduct>orderProductHashMap2= (Map<Integer, ProductionManageOrderProduct>) dataMap.get("product");
            Map<Integer, ProductionManageOutboundDeliveryWay>outboundDeliveryWayMap= (Map<Integer, ProductionManageOutboundDeliveryWay>) dataMap.get("deliveryWay");
            ProductionManageClientMO clientMO= (ProductionManageClientMO) dataMap.get("client");
            if (null!=orderMap2){
                orderProductHashMap.putAll(orderProductHashMap2);
                orderMap.putAll(orderMap2);
            }
            if (null!=orderProductHashMap2){
                orderProductHashMap.putAll(orderProductHashMap2);
            }
            if (null!=outboundDeliveryWayMap){
                deliveryWayHashMap.putAll(outboundDeliveryWayMap);
            }
            if (null!=clientMO){
                clientMOList.add(clientMO);
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!orderMap.isEmpty()){
            try {
                orderImportService.batchImportOrder(orderMap,orderProductHashMap,deliveryWayHashMap, clientMOList, importType);
            } catch (SuperCodeException e) {
                e.printStackTrace();
            }finally {
                orderMap.clear();
                orderProductHashMap.clear();
                orderImportService.close();
            }
        }
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        LOGGER.info("{}条数据，开始存储数据库！", list.size());
        LOGGER.info("存储数据库成功！");
    }
}
