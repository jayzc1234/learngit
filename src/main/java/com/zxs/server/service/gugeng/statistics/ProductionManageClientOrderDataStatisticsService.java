package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageClientOrderDataStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageClientOrderDataStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageClientOrderDataStatisticsListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  客户数据--订单客户数据
 * </p>
 * @author ZC
 * @since 2019-10-22
 */
@Service
public class ProductionManageClientOrderDataStatisticsService extends ServiceImpl<ProductionManageClientOrderDataStatisticsMapper, ProductionManageClientOrderDataStatistics> implements BaseService<ProductionManageClientOrderDataStatisticsListVO> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Override
    public IPage<ProductionManageClientOrderDataStatisticsListVO> pageList(DaoSearch daoSearch) {
        DateIntervalListDTO dateIntervalDTO = (DateIntervalListDTO) daoSearch;
        Page<ProductionManageClientOrderDataStatisticsListVO> page = CommonUtil.genPage(dateIntervalDTO);
        QueryWrapper<ProductionManageClientOrderDataStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageClientOrderDataStatistics.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), ProductionManageClientOrderDataStatistics.COL_ORDER_DATE, dateIntervalDTO.getStartQueryDate());
        queryWrapper.le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), ProductionManageClientOrderDataStatistics.COL_ORDER_DATE, dateIntervalDTO.getEndQueryDate());
        queryWrapper.groupBy(ProductionManageClientOrderDataStatistics.COL_CLIENT_ID);
        CommonUtil.sortList(dateIntervalDTO.getOrderField(), dateIntervalDTO.getOrderType(), queryWrapper, null);

        IPage<ProductionManageClientOrderDataStatisticsListVO> productionManageClientOrderDataStatisticsListVOIPage = baseMapper.pageList(page, queryWrapper);
        List<ProductionManageClientOrderDataStatisticsListVO> records = productionManageClientOrderDataStatisticsListVOIPage.getRecords();
        StringBuilder builder = new StringBuilder();
        builder.append(dateIntervalDTO.getStartQueryDate()).append("~").append(dateIntervalDTO.getEndQueryDate());
        String dateTime = builder.toString();
        if (CollectionUtils.isNotEmpty(records)) {
            for (ProductionManageClientOrderDataStatisticsListVO record : records) {
                record.setOrderDate(dateTime);
            }
        }
        CommonUtil.setRank(dateIntervalDTO.getDefaultCurrent(),dateIntervalDTO.getDefaultPageSize(),records);
        return productionManageClientOrderDataStatisticsListVOIPage;
    }

    /**
     *
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void timedTaskDataSync() {
        QueryWrapper<ProductionManageClientOrderDataStatistics> queryWrapper = new QueryWrapper<>();
        baseMapper.delete(queryWrapper);
        List<ProductionManageClientOrderDataStatistics> saleClientRankingVOS=baseMapper.clientOrderDataList();
        if (CollectionUtils.isNotEmpty(saleClientRankingVOS)){
            saveBatch(saleClientRankingVOS);
        }
    }
}
