package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.utils.RedisLockUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionManageOrderProductDataStatisticsDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReceived;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderProductDataStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageOrderProductDataStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderProductDataStatisticsListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-30
 */
@Service
public class ProductionManageOrderProductDataStatisticsService extends ServiceImpl<ProductionManageOrderProductDataStatisticsMapper, ProductionManageOrderProductDataStatistics> implements BaseService<ProductionManageOrderProductDataStatisticsListVO> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManageOrderProductMapper orderProductMapper;

    private static final String KEY = "productmanage:OrderProductData";

    @Override
    public IPage<ProductionManageOrderProductDataStatisticsListVO> pageList(DaoSearch daoSearch) throws SuperCodeException {
        boolean exists = redisUtil.exists(KEY);
        if (!exists){
            try{
                boolean lock = redisLockUtil.lock(KEY, 1000, 3, 100);
                if (lock){
                    QueryWrapper<ProductionManageOrderProduct> orderProductQueryWrapper=new QueryWrapper<>();
                    List<String> productIds=orderProductMapper.selectProductIds(orderProductQueryWrapper);
                    if (CollectionUtils.isNotEmpty(productIds)){
                        StringBuilder builder=new StringBuilder();
                        for (String productId : productIds) {
                            builder.append(productId).append(",");
                        }
                        String productIdStr = builder.substring(0, builder.length() - 1).toString();
                        RestResult andGetResultBySpring = commonUtil.codeRequests.getAndGetResultBySpring(commonUtil.getUserDomain()+"/product/selectByProductIds?productIds="+productIdStr, null, commonUtil.getSuperCodeTokenMap(), RestResult.class, commonUtil.getUserIsLoadBalanced());
                        Integer state = andGetResultBySpring.getState();
                        if (state!=null && state==200){
                            List<Map<String,String>> results = (List<Map<String, String>>) andGetResultBySpring.getResults();
                            if (CollectionUtils.isNotEmpty(results)){
                                for (Map<String, String> map : results) {
                                    orderProductMapper.updateByMap(map);
                                }
                            }
                            //同步数据
                            baseMapper.syncData(null,null);
                            redisUtil.set(KEY,"exist");
                        }
                    }
                }
            }finally {
                redisLockUtil.releaseLock(KEY);
            }
        }
        QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderProductDataStatistics.class);
        ProductionManageOrderProductDataStatisticsDTO statisticsDTO = (ProductionManageOrderProductDataStatisticsDTO) daoSearch;
        String productSortId = statisticsDTO.getProductSortId();
        if (StringUtils.isNotBlank(productSortId)) {
            querySetProductSortId(queryWrapper, productSortId);
        }
        Page<ProductionManageOrderProductDataStatisticsListVO> page = CommonUtil.genPage(statisticsDTO);
        String startQueryDate = statisticsDTO.getStartQueryDate();
        String endQueryDate = statisticsDTO.getEndQueryDate();
        queryWrapper.ge(StringUtils.isNotBlank(startQueryDate),ProductionManageOrderProductDataStatistics.COL_ORDER_DATE,startQueryDate);
        queryWrapper.le(StringUtils.isNotBlank(endQueryDate),ProductionManageOrderProductDataStatistics.COL_ORDER_DATE,endQueryDate);
        ProductionManageOrderProductDataStatisticsListVO sumMoney=baseMapper.sumMoney(queryWrapper);
        queryWrapper.groupBy(ProductionManageOrderProductDataStatistics.COL_PRODUCT_ID);

        if (StringUtils.isNotBlank(statisticsDTO.getOrderField()) && StringUtils.isNotBlank(statisticsDTO.getOrderType())) {
            if ("ASC".equalsIgnoreCase(statisticsDTO.getOrderType())) {
                queryWrapper.orderByAsc(statisticsDTO.getOrderField());
            } else {
                queryWrapper.orderByDesc(statisticsDTO.getOrderField());
            }
        }else {
            queryWrapper.orderByDesc(ProductionManageOrderProductDataStatistics.COL_ORDER_MONEY);
        }
        String queryDateIn = startQueryDate + "~" + endQueryDate;
        IPage<ProductionManageOrderProductDataStatisticsListVO> iPage = baseMapper.pageList(page, queryWrapper);
        List<ProductionManageOrderProductDataStatisticsListVO> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            records.remove(null);
            CommonUtil.setRank(statisticsDTO.getDefaultCurrent(),statisticsDTO.getDefaultPageSize(),records);
            BigDecimal totalReceivedOrderMoney=sumMoney.getTotalReceivedOrderMoney();
            BigDecimal totalOrderMoney=sumMoney.getTotalOrderMoney();
            for (ProductionManageOrderProductDataStatisticsListVO record : records) {
                record.setOrderDate(queryDateIn);
                if (totalReceivedOrderMoney.doubleValue()==0 || null==record.getReceivedOrderMoney()){
                    record.setReceivedOMProportion(0d);
                }else {
                    BigDecimal divide = record.getReceivedOrderMoney().divide(totalReceivedOrderMoney, 10, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100)).setScale(4,BigDecimal.ROUND_DOWN);
                    record.setReceivedOMProportion(divide.doubleValue());
                }
                BigDecimal divide = record.getOrderMoney().divide(totalOrderMoney, 10, BigDecimal.ROUND_DOWN);
                BigDecimal multiply = divide.multiply(new BigDecimal(100));
                BigDecimal decimal = multiply.setScale(4,BigDecimal.ROUND_DOWN);
                String productName = record.getProductName();
                record.setOrderMoneyProportion(decimal.doubleValue());
            }
        }
        return iPage;
    }

    private void querySetProductSortId(QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper, String productSortId) {
        List<String> allProductSortIds=new ArrayList<>();
        allProductSortIds.add(productSortId);
        RestResult sortResult = commonUtil.codeRequests.getAndGetResultBySpring(commonUtil.getUserDomain()+"/product-sort/allChildes?previousSortId="+productSortId, null, commonUtil.getSuperCodeTokenMap(), RestResult.class, commonUtil.getUserIsLoadBalanced());
        Integer state = sortResult.getState();
        if (null!=state && state==200){
            List<String> productSortIds = (List<String>) sortResult.getResults();
            if (CollectionUtils.isNotEmpty(productSortIds)){
                allProductSortIds.addAll(productSortIds);
            }
        }
        queryWrapper.in(ProductionManageOrderProductDataStatistics.COL_PRODUCT_SORT_ID,allProductSortIds);
    }

    /**
     * 销售产品饼状图
     * @param statisticsDTO
     * @return
     */
    public Map<String,List<NameValueVO>> pie(ProductionManageOrderProductDataStatisticsDTO statisticsDTO) {
        QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderProductDataStatistics.class);
        String startQueryDate = statisticsDTO.getStartQueryDate();
        String endQueryDate = statisticsDTO.getEndQueryDate();
        queryWrapper.ge(StringUtils.isNotBlank(startQueryDate),ProductionManageOrderProductDataStatistics.COL_ORDER_DATE,startQueryDate);
        queryWrapper.le(StringUtils.isNotBlank(endQueryDate),ProductionManageOrderProductDataStatistics.COL_ORDER_DATE,endQueryDate);
        String productSortId = statisticsDTO.getProductSortId();

        Map<String, List<NameValueVO>> data = new HashMap<>();
        if (StringUtils.isNotBlank(productSortId)) {
            querySetProductSortId(queryWrapper, productSortId);
        }
        queryWrapper.groupBy(ProductionManageOrderProductDataStatistics.COL_PRODUCT_ID);
        boolean isOrderMoney=false;
        if (statisticsDTO.getType()==1){
            isOrderMoney=true;
            queryWrapper.orderByDesc(ProductionManageOrderProductDataStatistics.COL_ORDER_MONEY);
        }else if (statisticsDTO.getType()==2){
            queryWrapper.orderByDesc(ProductionManageOrderProductDataStatistics.COL_RECEIVED_ORDER_MONEY);
        }
        List<ProductionManageOrderProductDataStatisticsListVO> records = baseMapper.selectByWrapper(queryWrapper);
        List<NameValueVO> nameValueVOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            records.remove(null);
            int counter = 0;
            NameValueVO<String, BigDecimal> otherNameAndValueVO = new NameValueVO();
            otherNameAndValueVO.setName("其它");
            for (ProductionManageOrderProductDataStatisticsListVO record : records) {
                BigDecimal value = null;
                if (isOrderMoney) {
                    value = record.getOrderMoney();
                } else {
                    value = record.getReceivedOrderMoney();
                }
                if (counter>=7){
                    otherNameAndValueVO.setValue(CommonUtil.bigDecimalAdd(value,otherNameAndValueVO.getValue()));
                }else {
                    NameValueVO<String,Object> nameAndValueVO=new NameValueVO();
                    nameAndValueVO.setValue(value);
                    nameAndValueVO.setName(record.getProductName());
                    nameValueVOList.add(nameAndValueVO);
                }
                counter++;
            }
            if (null!=otherNameAndValueVO.getValue()){
                nameValueVOList.add(otherNameAndValueVO);
            }
        }
        data.put("values",nameValueVOList);
        return data;
    }

    public void reStatistics(List<ProductionManageOrderProduct> prOrderProducts, Date currentDate) {
        if (CollectionUtils.isNotEmpty(prOrderProducts)) {
            String date = CommonUtil.formatDateToStringWithFormat(currentDate, "yyyy-MM-dd");
            List<String> productIds = new ArrayList<>();
            QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper=commonUtil.queryTemplate(ProductionManageOrderProductDataStatistics.class);
            queryWrapper.eq(ProductionManageOrderProductDataStatistics.COL_ORDER_DATE,date);
            Map<String, ProductionManageOrderProduct> map = new HashMap<>();
            for (ProductionManageOrderProduct prOrderProduct : prOrderProducts) {
                String productId = prOrderProduct.getProductId();
                queryWrapper.in(ProductionManageOrderProductDataStatistics.COL_PRODUCT_ID,productId);
                baseMapper.delete(queryWrapper);
                baseMapper.syncData(productId,date);
            }
        }
    }

    public void updateOrderMoney(List<ProductionManageOrderProduct> prOrderProducts, Date currentDate) throws ParseException {
        if (CollectionUtils.isNotEmpty(prOrderProducts)){
            String date = CommonUtil.formatDateToStringWithFormat(currentDate, "yyyy-MM-dd");
            Date orderDate = CommonUtil.formatStringToDate(date, "yyyy-MM-dd");
            List<String> productIds=new ArrayList<>();
            Map<String,ProductionManageOrderProduct> map=new HashMap<>();
            for (ProductionManageOrderProduct prOrderProduct : prOrderProducts) {
                String totalPrice = prOrderProduct.getTotalPrice().toString();
                if (StringUtils.isNotBlank(totalPrice)) {
                    String productId = prOrderProduct.getProductId();
                    ProductionManageOrderProduct orderProduct = map.get(productId);
                    if (null == orderProduct) {
                        map.put(productId, prOrderProduct);
                    } else {
                        prOrderProduct.setTotalPrice(CommonUtil.bigDecimalAdd(new BigDecimal(totalPrice), prOrderProduct.getTotalPrice()));
                    }
                    productIds.add(productId);
                }
            }
            List<ProductionManageOrderProductDataStatistics> updateProductDataStatisticsList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(productIds)){
                QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper=commonUtil.queryTemplate(ProductionManageOrderProductDataStatistics.class);
                queryWrapper.in(ProductionManageOrderProductDataStatistics.COL_PRODUCT_ID,productIds);
                queryWrapper.eq(ProductionManageOrderProductDataStatistics.COL_ORDER_DATE,date);
                List<ProductionManageOrderProductDataStatistics> productDataStatisticsList = baseMapper.selectList(queryWrapper);

                if (CollectionUtils.isNotEmpty(productDataStatisticsList)) {
                    for (ProductionManageOrderProductDataStatistics productionManageOrderProductDataStatistics : productDataStatisticsList) {
                        String productId = productionManageOrderProductDataStatistics.getProductId();
                        ProductionManageOrderProduct orderProduct = map.get(productId);
                        if (null != orderProduct) {
                            productionManageOrderProductDataStatistics.setOrderMoney(CommonUtil.bigDecimalAdd(productionManageOrderProductDataStatistics.getOrderMoney(), orderProduct.getTotalPrice()));
                            updateProductDataStatisticsList.add(productionManageOrderProductDataStatistics);
                            map.remove(productId);
                        }
                    }
                }
            }

            String sysId = commonUtil.getSysId();
            String organizationId = commonUtil.getOrganizationId();
            for (ProductionManageOrderProduct orderProduct : map.values()) {
                ProductionManageOrderProductDataStatistics orderProductDataStatistics=new ProductionManageOrderProductDataStatistics();
                orderProductDataStatistics.setSysId(sysId);
                orderProductDataStatistics.setOrganizationId(organizationId);
                orderProductDataStatistics.setProductSortId(orderProduct.getProductSortId());
                orderProductDataStatistics.setProductSortName(orderProduct.getProductSortName());
                orderProductDataStatistics.setProductId(orderProduct.getProductId());
                orderProductDataStatistics.setProductName(orderProduct.getProductName());
                orderProductDataStatistics.setOrderDate(orderDate);
                orderProductDataStatistics.setOrderMoney(orderProduct.getTotalPrice());
                updateProductDataStatisticsList.add(orderProductDataStatistics);
            }
            if (CollectionUtils.isNotEmpty(updateProductDataStatisticsList)){
                saveOrUpdateBatch(updateProductDataStatisticsList);
            }
        }
    }

    /**
     * 同步实收额
     * @param productReceiveds
     * @param orderDate
     * @throws ParseException
     */
    public void updateReceivedOrderMoney(List<ProductionManageOrderProductReceived> productReceiveds, Date orderDate) throws ParseException {
        if (CollectionUtils.isNotEmpty(productReceiveds)){
            String date = CommonUtil.formatDateToStringWithFormat(orderDate, "yyyy-MM-dd");
            Date orderDateYYYYMMDD = CommonUtil.formatStringToDate(date, "yyyy-MM-dd");
            UpdateWrapper<ProductionManageOrderProductDataStatistics> updateWrapper=new UpdateWrapper<>();
            for (ProductionManageOrderProductReceived productReceived : productReceiveds) {
                if (StringUtils.isNotBlank(productReceived.getReceivedProMoney())){
                    updateWrapper.setSql(ProductionManageOrderProductDataStatistics.COL_RECEIVED_ORDER_MONEY+"=IFNULL("+ProductionManageOrderProductDataStatistics.COL_RECEIVED_ORDER_MONEY+",0)+"+productReceived.getReceivedProMoney());
                    updateWrapper.eq(ProductionManageOrderProductDataStatistics.COL_ORDER_DATE,orderDateYYYYMMDD);
                    updateWrapper.eq(ProductionManageOrderProductDataStatistics.COL_PRODUCT_ID,productReceived.getProductId());
                    baseMapper.update(null,updateWrapper);
                }
            }
        }
    }
}
