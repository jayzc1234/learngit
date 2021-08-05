package com.zxs.server.service.fuding.impl.teagreen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.advancedsearch.common.AdvancedSearchUtil;
import com.jgw.supercodeplatform.advancedsearch.common.OrderByOperatorObject;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import com.jgw.supercodeplatform.advancedsearch.enums.OrderByOperatorEnum;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.songming.TraceBatchDTO;
import net.app315.hydra.intelligent.planting.dto.storehouse.AddSemiInStorageRequestDTO;
import net.app315.hydra.intelligent.planting.enums.fuding.AuthorizationCode;
import net.app315.hydra.intelligent.planting.pojo.base.ProductLevelMaintain;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenBatch;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenBatchMapper;
import net.app315.hydra.intelligent.planting.server.service.base.ProductLevelMaintainService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenBatchService;
import net.app315.hydra.intelligent.planting.server.util.ExcelUtils2;
import net.app315.hydra.intelligent.planting.utils.fuding.AuthorizationCodeUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaGreenBatchListModel;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author caihong
 * @since 2020-09-07
 */
@Service
public class ITeaGreenBatchServiceImpl extends ServiceImpl<TeaGreenBatchMapper, TeaGreenBatch> implements ITeaGreenBatchService {


    @Autowired
    private CommonUtil commonUtil;
    @Autowired
    private ExcelUtils2 excelUtils2;
    @Autowired
    private AuthorizationCodeUtil authorizationCodeUtil;
    @Autowired
    private ProductLevelMaintainService productLevelMaintainService;

    @Override
    public AbstractPageService.PageResults<List<TeaGreenBatch>> pageList(TeaGreenBatchListModel model) {
        model.setGeneralParam(new String[]{"trace_batch_name"});
        Set<OrderByOperatorObject> order=new HashSet<>();
        OrderByOperatorObject orderByOperatorObject=new OrderByOperatorObject();
        orderByOperatorObject.setField("traceBatchName");
        orderByOperatorObject.setOperator(OrderByOperatorEnum.DESC);
        order.add(orderByOperatorObject);
        model.setOrderBy(order);
        List<QuerySpecification> querySpecifications =
                authorizationCodeUtil.getQueryWrapper(AuthorizationCode.TEA_GREEN_BATCH_DATA);
        if (!CollectionUtils.isEmpty(querySpecifications)) {
            model.getAndConditions().addAll(querySpecifications);
        }
        return AdvancedSearchUtil.selectPage(model, this.getBaseMapper(), TeaGreenBatch.class);
    }

    @Override
    public void export(DaoSearch daoSearch, HttpServletResponse response) throws SuperCodeException {
        ArrayList<String> idList = daoSearch.getIdList();
        List<TeaGreenBatch> list ;
        // idList为空导出全部，不为空导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            TeaGreenBatchListModel advancedDaoSearch = new TeaGreenBatchListModel();
            BeanUtils.copyProperties(daoSearch,advancedDaoSearch);
            AbstractPageService.PageResults<List<TeaGreenBatch>> pageResults = this.pageList(advancedDaoSearch);
            list = pageResults.getList();
        } else {
            QueryWrapper<TeaGreenBatch> queryWrapper = commonUtil.queryTemplate(TeaGreenBatch.class);
            queryWrapper.and(query -> query.in(TeaGreenBatch.COL_ID, idList));
            list = baseMapper.selectList(queryWrapper);
        }
        String exportMetadata = daoSearch.getExportMetadata();
        Map<String, String>  exportMetadataMap ;
        if (StringUtils.isBlank(exportMetadata)){
            exportMetadataMap= excelUtils2.initExportMetadataMap(TeaGreenBatchExportVO.class);
        }else {
            exportMetadataMap=daoSearch.exportMetadataToMap();
        }
        ExcelUtils.listToExcel(list, exportMetadataMap, "茶青批次", response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTeaBatch(String organizationId,String token) {
        List<TeaGreenBatch> teaGreenBatches = baseMapper.selectList(new QueryWrapper<TeaGreenBatch>().lambda().eq(TeaGreenBatch::getOrganizationId, organizationId));
        List<TeaGreenBatch> teaList=new ArrayList<>();
        List<TeaGreenAcquisitionListVO> teaGreenAcquisitionDOList = baseMapper.getTeaBatchData(organizationId);
        List<TraceBatchDTO> batchNameList = new ArrayList<>();
        Map<String, TeaGreenAcquisitionListVO> batchMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(teaGreenAcquisitionDOList)) {
            teaGreenAcquisitionDOList.forEach(teaGreenAcquisitionListVO -> {
                String batchName = teaGreenAcquisitionListVO.getProductName() + "-" + DateUtils.dateFormat(teaGreenAcquisitionListVO.getAcquisitionTime(), "yyyyMMdd") + "-" + teaGreenAcquisitionListVO.getProductLevelName();
                TeaGreenBatch teaGreenBatch = teaGreenBatches.stream().filter(e -> e.getTraceBatchName().equals(batchName)).findFirst().orElse(null);
                teaGreenBatches.remove(teaGreenBatch);
                if (teaGreenBatch == null) {
                    TraceBatchDTO traceBatchDTO = new TraceBatchDTO();
                    traceBatchDTO.setProductId(teaGreenAcquisitionListVO.getProductId());
                    traceBatchDTO.setProductName(teaGreenAcquisitionListVO.getProductName());
                    traceBatchDTO.setTraceBatchName(batchName);
                    batchNameList.add(traceBatchDTO);
                }else {
                    teaList.add(teaGreenBatch);
                }
                batchMap.put(batchName, teaGreenAcquisitionListVO);

            });
        }
        List<String> traceBatchInfoIdList = teaGreenBatches.stream().map(TeaGreenBatch::getTraceBatchInfoId).collect(Collectors.toList());

        List<Long> idList = teaGreenBatches.stream().map(TeaGreenBatch::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idList)) {
            removeByIds(idList);
        }
        List<TeaGreenBatch> teaGreenBatchList = commonUtil.insertTraceBatch(batchNameList,traceBatchInfoIdList, token);
        if (CollectionUtils.isNotEmpty(teaGreenBatchList)) {
            teaGreenBatchList.forEach(teaGreenBatch -> {
                String traceBatchInfoName = teaGreenBatch.getTraceBatchName();
                TeaGreenAcquisitionListVO teaGreenAcquisitionListVO = batchMap.get(traceBatchInfoName);
                batchMap.remove(traceBatchInfoName);
                BeanUtils.copyProperties(teaGreenAcquisitionListVO, teaGreenBatch);
                teaGreenBatch.setQuantity(teaGreenAcquisitionListVO.getTeaGreenAcquisitionQuantity());
                teaGreenBatch.setUnitPrice(teaGreenAcquisitionListVO.getPrice());
                teaGreenBatch.setQuantity(teaGreenAcquisitionListVO.getTeaGreenAcquisitionQuantity());
                teaGreenBatch.setAcquisitionTime(teaGreenAcquisitionListVO.getAcquisitionTime());
                teaGreenBatch.setProductId(teaGreenAcquisitionListVO.getProductId());
                teaGreenBatch.setProductName(teaGreenAcquisitionListVO.getProductName());
                teaGreenBatch.setProductLevelName(teaGreenAcquisitionListVO.getProductLevelName());
            });
            saveBatch(teaGreenBatchList);
        }
        teaList.forEach(teaGreenBatch->{
            TeaGreenAcquisitionListVO teaGreenAcquisitionListVO =batchMap.get(teaGreenBatch.getTraceBatchName());
            teaGreenBatch.setQuantity(teaGreenAcquisitionListVO.getTeaGreenAcquisitionQuantity());
            teaGreenBatch.setUnitPrice(teaGreenAcquisitionListVO.getPrice());
            teaGreenBatch.setAmount(teaGreenAcquisitionListVO.getAmount());
            updateById(teaGreenBatch);
        });
    }

    @Override
    public AbstractPageService.PageResults<List<AddSemiInStorageRequestDTO>> listSemi(TeaGreenBatchListModel model) {
        AbstractPageService.PageResults<List<TeaGreenBatch>> listPageResults = pageList(model);
        List<String> traceBatchInfoIdList = listPageResults.getList().stream().map(TeaGreenBatch::getTraceBatchInfoId).collect(Collectors.toList());
        List<TeaGreenProductInfoVO> traceBatchList=commonUtil.selectByTraceBatchInfoIds(traceBatchInfoIdList);
        List<AddSemiInStorageRequestDTO> list=new ArrayList<>();
        listPageResults.getList().forEach(teaGreenBatch -> {
            AddSemiInStorageRequestDTO addSemiInStorageRequestDTO=new AddSemiInStorageRequestDTO();
            addSemiInStorageRequestDTO.setTeaGreenBatchName(teaGreenBatch.getTraceBatchName());
            addSemiInStorageRequestDTO.setTeaGreenBatchId(teaGreenBatch.getTraceBatchInfoId());
            BeanUtils.copyProperties(teaGreenBatch,addSemiInStorageRequestDTO);
            String levelName = teaGreenBatch.getTraceBatchName().split("-")[2];
            List<ProductLevelMaintain> productLevelMaintainList = productLevelMaintainService.selectByOrgAndSysId(commonUtil.getOrganizationId(), commonUtil.getSysId(), levelName);
            if (CollectionUtils.isNotEmpty(productLevelMaintainList)) {
                BeanUtils.copyProperties(productLevelMaintainList.get(0), addSemiInStorageRequestDTO);
                addSemiInStorageRequestDTO.setQualityLevelName(productLevelMaintainList.get(0).getQualityLevelName());
            }
            traceBatchList.stream().filter(batch ->
                    batch.getTeaGreenBatchId()!=null&&batch.getTeaGreenBatchId().equals(teaGreenBatch.getTraceBatchInfoId())
            ).findFirst().ifPresent(teaGreenProductInfoVO -> BeanUtils.copyProperties(teaGreenProductInfoVO, addSemiInStorageRequestDTO));
            list.add(addSemiInStorageRequestDTO);
        });
       return new AbstractPageService.PageResults<>(list,listPageResults.getPagination());
    }

    @Override
    public List<TeaGreenBatch> statisticsData(TeaGreenAndSemiProductStatisticsQuery model, String startTime, String endTime) {
        return list(new QueryWrapper<TeaGreenBatch>().lambda()
                .ge(StringUtils.isNotBlank(startTime), TeaGreenBatch::getAcquisitionTime,startTime)
                .le(StringUtils.isNotBlank(endTime), TeaGreenBatch::getAcquisitionTime,endTime)
                .eq(StringUtils.isNotBlank(model.getProductLevelName()), TeaGreenBatch::getProductLevelName,model.getProductLevelName())
                .eq(StringUtils.isNotBlank(model.getProductId()), TeaGreenBatch::getProductId,model.getProductId())
                .eq(StringUtils.isNotBlank(model.getTeaGreenBatchInfoId()), TeaGreenBatch::getTraceBatchInfoId,model.getTeaGreenBatchInfoId()));
    }

    @Override
    public AbstractPageService.PageResults<List<TeaGreenBatchProductLevelVO>> productPageList(TeaGreenBatchListModel model) {
        QueryWrapper<TeaGreenBatch> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(" distinct product_level_name ");
        List<TeaGreenBatch> teaGreenBatchList = baseMapper.selectList(queryWrapper.lambda()
                .eq(TeaGreenBatch::getProductId, model.getProductId()));
        List<TeaGreenBatchProductLevelVO> list = null;
        int total=0;
        if (CollectionUtils.isNotEmpty(teaGreenBatchList)) {
            list=teaGreenBatchList.stream().filter(e->StringUtils.isNotBlank(e.getProductLevelName())).map(e->{
                TeaGreenBatchProductLevelVO teaGreenBatchProductLevelVO=new TeaGreenBatchProductLevelVO();
                teaGreenBatchProductLevelVO.setProductLevelName(e.getProductLevelName());
                return teaGreenBatchProductLevelVO;
            }).collect(Collectors.toList());
            total=list.size();
        }
        return new AbstractPageService.PageResults<>(list, new Page(1, 10,total ));
    }

}
