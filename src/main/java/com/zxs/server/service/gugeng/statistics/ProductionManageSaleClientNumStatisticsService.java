package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleClientNumStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSaleClientNumStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleClientNumStatisticsVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-21
 */
@Service
public class ProductionManageSaleClientNumStatisticsService extends ServiceImpl<ProductionManageSaleClientNumStatisticsMapper, ProductionManageSaleClientNumStatistics> implements BaseService<ProductionManageSaleClientNumStatisticsVO> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductManageClientMapper clientMapper;

    /**
     * 销售人员客户信息统计
     * @param daoSearch
     * @return
     */
    @Override
    public IPage<ProductionManageSaleClientNumStatisticsVO> pageList(DaoSearch daoSearch)  {
        DateIntervalListDTO dateIntervalDTO= (DateIntervalListDTO) daoSearch;
        dateIntervalDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalDTO.setSysId(commonUtil.getSysId());
        Page<ProductionManageSaleClientNumStatisticsVO> page = CommonUtil.genPage(dateIntervalDTO);
        IPage<ProductionManageSaleClientNumStatisticsVO> iPage = baseMapper.pageList(page, dateIntervalDTO);
        List<ProductionManageSaleClientNumStatisticsVO> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            for (ProductionManageSaleClientNumStatisticsVO record : records) {
                Integer orderClientNum = record.getOrderClientNum();
                if (null==orderClientNum){
                    record.setOrderClientNum(0);
                }
                Double conversionRates=baseMapper.getConversionRates(record.getSaleUserId(),commonUtil.getOrganizationId(),commonUtil.getSysId());
                if (null!=conversionRates){
                    record.setConversionRates(new BigDecimal(conversionRates*100).setScale(2,BigDecimal.ROUND_DOWN).doubleValue());
                }else {
                    record.setConversionRates(0D);
                }
            }
        }
        CommonUtil.setRank(dateIntervalDTO.getDefaultCurrent(),dateIntervalDTO.getDefaultPageSize(),records);
        return iPage;
    }

    public void updatePotentialClientNum(String saleUserId, String saleUserName,Integer potentialClientNum)  {
        if (StringUtils.isNotBlank(saleUserId) && StringUtils.isNotBlank(saleUserName)){
            if (null==potentialClientNum){
                potentialClientNum=0;
            }
            String organizationId = commonUtil.getOrganizationId();
            String sysId = commonUtil.getSysId();

            QueryWrapper<ProductionManageSaleClientNumStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleClientNumStatistics.class);
            queryWrapper.eq(ProductionManageSaleClientNumStatistics.COL_SALE_EMPLOYEE_ID,saleUserId);
            Date currentDate = null;
            currentDate = CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD);
            queryWrapper.eq(ProductionManageSaleClientNumStatistics.COL_CREATE_DATE,currentDate);
            ProductionManageSaleClientNumStatistics productionManageSaleClientNumStatistics = baseMapper.selectOne(queryWrapper);
            if (null==productionManageSaleClientNumStatistics){
                productionManageSaleClientNumStatistics =new ProductionManageSaleClientNumStatistics();
                productionManageSaleClientNumStatistics.setOrganizationId(organizationId);
                productionManageSaleClientNumStatistics.setSysId(sysId);
                productionManageSaleClientNumStatistics.setSaleEmployeeName(saleUserName);
                productionManageSaleClientNumStatistics.setPotentialClientNum(potentialClientNum);
                productionManageSaleClientNumStatistics.setCreateDate(currentDate);
                baseMapper.insert(productionManageSaleClientNumStatistics);
            }else {
                productionManageSaleClientNumStatistics.setPotentialClientNum(CommonUtil.integerAdd(productionManageSaleClientNumStatistics.getPotentialClientNum(),potentialClientNum));
                baseMapper.updateById(productionManageSaleClientNumStatistics);
            }
        }
    }

    public void updateOrderClientNum(String saleUserId,Integer orderClientNum) {
        if (StringUtils.isNotBlank(saleUserId)) {
            if (null == orderClientNum) {
                orderClientNum = 0;
            }
            String organizationId = commonUtil.getOrganizationId();
            String sysId = commonUtil.getSysId();

            Date currentDate = null;
            currentDate = CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD);
            QueryWrapper<ProductionManageSaleClientNumStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleClientNumStatistics.class);
            queryWrapper.eq(ProductionManageSaleClientNumStatistics.COL_SALE_EMPLOYEE_ID,saleUserId);
            queryWrapper.eq(ProductionManageSaleClientNumStatistics.COL_CREATE_DATE,currentDate);
            ProductionManageSaleClientNumStatistics productionManageSaleClientNumStatistics = baseMapper.selectOne(queryWrapper);
            if (null != productionManageSaleClientNumStatistics) {
                productionManageSaleClientNumStatistics.setOrderClientNum(CommonUtil.integerAdd(productionManageSaleClientNumStatistics.getOrderClientNum(), orderClientNum));
                baseMapper.updateById(productionManageSaleClientNumStatistics);
            }
        }
    }

}
