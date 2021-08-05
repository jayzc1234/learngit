package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.GuGengOrderClientCategoryListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-30
 */
@Service
public class GuGengOrderClientCategoryDataStatisticsService implements BaseService<GuGengOrderClientCategoryListVO> {

    @Autowired
    private CommonUtil commonUtil;


    @Autowired
    private ProductManageOrderMapper orderMapper;


    @Override
    public IPage<GuGengOrderClientCategoryListVO> pageList(DaoSearch daoSearch) throws SuperCodeException {
        Page<GuGengOrderClientCategoryListVO> page = new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize());
        DateIntervalListDTO dateIntervalListDTO = (DateIntervalListDTO) daoSearch;
        @NotBlank(message = "查询日期不可为空") String startQueryDate = dateIntervalListDTO.getStartQueryDate();
        @NotBlank(message = "查询日期不可为空") String endQueryDate = dateIntervalListDTO.getEndQueryDate();
        QueryWrapper<ProductionManageOrder> orderQueryWrapper = commonUtil.queryTemplate(ProductionManageOrder.class);
        orderQueryWrapper.ge(StringUtils.isNotBlank(startQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", startQueryDate);
        orderQueryWrapper.le(StringUtils.isNotBlank(endQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", endQueryDate);
        orderQueryWrapper.groupBy(ProductionManageOrder.COL_CLIENT_CATEGORY_ID);
        IPage<GuGengOrderClientCategoryListVO> iPage = orderMapper.statisticsOrderClientCategoryData(page, orderQueryWrapper);
        List<GuGengOrderClientCategoryListVO> records = iPage.getRecords();

        String dateTime = startQueryDate + "~" + endQueryDate;
        if (CollectionUtils.isNotEmpty(records)) {
            QueryWrapper<ProductionManageOrder> orderQueryWrapper2 = commonUtil.queryTemplate(ProductionManageOrder.class);
            orderQueryWrapper2.ge(StringUtils.isNotBlank(startQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", startQueryDate);
            orderQueryWrapper2.le(StringUtils.isNotBlank(endQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", endQueryDate);
            BigDecimal totalOrderMoney = orderMapper.sumOrderMoneyDuringDataInterval(orderQueryWrapper2);
            for (GuGengOrderClientCategoryListVO record : records) {
                BigDecimal orderMoney = record.getOrderMoney();
                BigDecimal divide = orderMoney.divide(totalOrderMoney, 2, RoundingMode.HALF_UP);
                record.setOrderMoneyProportion(new BigDecimal(divide.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                record.setOrderDate(dateTime);
            }
            CommonUtil.setRank(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize(), records);
        }
        return iPage;
    }

    /**
     * 销售产品饼状图
     * @param dateIntervalListDTO
     * @return
     */
    public Map<String,List<NameValueVO>> pie(DateIntervalListDTO dateIntervalListDTO) {
        @NotBlank(message = "查询日期不可为空") String startQueryDate = dateIntervalListDTO.getStartQueryDate();
        @NotBlank(message = "查询日期不可为空") String endQueryDate = dateIntervalListDTO.getEndQueryDate();
        QueryWrapper<ProductionManageOrder> orderQueryWrapper = commonUtil.queryTemplate(ProductionManageOrder.class);
        orderQueryWrapper.ge(StringUtils.isNotBlank(startQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", startQueryDate);
        orderQueryWrapper.le(StringUtils.isNotBlank(endQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", endQueryDate);
        orderQueryWrapper.groupBy(ProductionManageOrder.COL_CLIENT_CATEGORY_ID);
        Map<String, List<NameValueVO>> data = new HashMap<>();
        List<GuGengOrderClientCategoryListVO> records = orderMapper.listOrderClientCategoryData(orderQueryWrapper);
        List<NameValueVO> nameValueVOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            records.remove(null);
            int counter = 0;
            NameValueVO<String, BigDecimal> otherNameAndValueVO = new NameValueVO();
            otherNameAndValueVO.setName("其它");
            for (GuGengOrderClientCategoryListVO record : records) {
                BigDecimal value = record.getOrderMoney();
                if (counter >= 7) {
                    otherNameAndValueVO.setValue(CommonUtil.bigDecimalAdd(value, otherNameAndValueVO.getValue()));
                } else {
                    NameValueVO<String, Object> nameAndValueVO = new NameValueVO();
                    nameAndValueVO.setValue(value);
                    nameAndValueVO.setName(record.getClientCategoryName());
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

}
