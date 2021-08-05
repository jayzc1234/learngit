package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchSaleOutRecordRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSaleOutRecord;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSaleOutRecordMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSaleOutRecordResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-07-29
 */
@Service
public class ProductionManageSaleOutRecordService extends ServiceImpl<ProductionManageSaleOutRecordMapper, ProductionManageSaleOutRecord> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 获取销售客户信息列表
     * @param requestDTO
     * @return
     * @throws SuperCodeException
     */
    public PageResults<List<SearchSaleOutRecordResponseVO>> list(SearchSaleOutRecordRequestDTO requestDTO) throws SuperCodeException {
        Page<ProductionManageSaleOutRecord> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageSaleOutRecord> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleOutRecord.class);
        queryWrapper.eq(ProductionManageSaleOutRecord.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchId());
        IPage<ProductionManageSaleOutRecord> iPage = baseMapper.selectPage(page, queryWrapper);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int)iPage.getSize(), (int)iPage.getCurrent(), (int)iPage.getTotal());
        PageResults<List<SearchSaleOutRecordResponseVO>> pageResults = new PageResults<>();
        pageResults.setPagination(pagination);

        List<ProductionManageSaleOutRecord> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageResults;
        }

        List<SearchSaleOutRecordResponseVO> list = records.stream().map(record -> {
            SearchSaleOutRecordResponseVO responseVO = new SearchSaleOutRecordResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setOrderType(String.valueOf(record.getOrderType()));
            return responseVO;
        }).collect(Collectors.toList());

        pageResults.setList(list);

        return pageResults;
    }
}