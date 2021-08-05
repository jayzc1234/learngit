package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.PlantingWorkRemindSearchDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PlantingBatchResponseDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProducePlantingWorkRemind;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceSchemeNode;
import net.app315.hydra.intelligent.planting.server.facade.MassifClient;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProducePlantingSchemeMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProducePlantingWorkRemindMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.BaseMassifbaseView;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.MassifPrincipalResponseVO;
import net.app315.nail.common.result.RichResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2021-07-05
 */
@Service
public class ProducePlantingWorkRemindServiceImpl extends ServiceImpl<ProducePlantingWorkRemindMapper, ProducePlantingWorkRemind>  {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private MassifClient massifClient;

    @Autowired
    private ProducePlantingSchemeMapper schemeMapper;

    @Autowired
    private ProducePlantingSchemeServiceImpl schemeService;

    public AbstractPageService.PageResults<List<ProducePlantingWorkRemind>> list(PlantingWorkRemindSearchDTO obj){
        QueryWrapper<ProducePlantingWorkRemind> queryWrapper = new QueryWrapper<>(); //commonUtil.queryTemplate(ProducePlantingWorkRemind.class);
        if(StringUtils.isNotEmpty(obj.getBaseName())){
            RichResult<PageResults<List<BaseMassifbaseView>>>  richResult = massifClient.list();
            List<BaseMassifbaseView> massifbaseViews = richResult.getResults().getList().stream().filter(e->e.getBaseName().equals(obj.getBaseName())).collect(Collectors.toList());
            List<String> massifIds = massifbaseViews.stream().map(e->e.getMassId()).collect(Collectors.toList());
            queryWrapper.in(ProducePlantingWorkRemind.COL_MASSIF_ID, massifIds);
        }
        if(StringUtils.isNotEmpty(obj.getPlantingBatchId())){
            queryWrapper.eq(ProducePlantingWorkRemind.COL_PLANTING_BATCH_ID, obj.getPlantingBatchId());
        }
        queryWrapper.eq(obj.getTaskStatus()!=null, ProducePlantingWorkRemind.COL_TASK_STATUS, obj.getTaskStatus());
        queryWrapper.orderByAsc(ProducePlantingWorkRemind.COL_ID);

        Page<ProducePlantingWorkRemind> page = new Page<>(obj.getCurrent(), obj.getPageSize());
        IPage<ProducePlantingWorkRemind> iPage = baseMapper.selectPage(page, queryWrapper);
        List<ProducePlantingWorkRemind> schemeList = iPage.getRecords();

        PageResults<List<ProducePlantingWorkRemind>> pageResults = new PageResults<List<ProducePlantingWorkRemind>>(schemeList,
                new com.jgw.supercodeplatform.common.pojo.common.Page(obj.getPageSize(), obj.getCurrent(), (int)iPage.getTotal()));

        return pageResults;

    }

    public void setStatus(Long id){
        baseMapper.update(new ProducePlantingWorkRemind().setTaskStatus(1),
                new LambdaQueryWrapper<ProducePlantingWorkRemind>().eq(ProducePlantingWorkRemind::getId, id));
    }

    public List<MassifPrincipalResponseVO> listMassifPrincipal(String baseName){
        RichResult<PageResults<List<BaseMassifbaseView>>>  richResult = massifClient.list();
        List<BaseMassifbaseView> massifbaseViews = richResult.getResults().getList().stream().filter(e->e.getBaseName().equals(baseName)).collect(Collectors.toList());
        List<MassifPrincipalResponseVO> list =new ArrayList<>();
        massifbaseViews.stream().map(e->e.getPrincipalName()).distinct().forEach(e->{
            MassifPrincipalResponseVO responseVO=new MassifPrincipalResponseVO();
            responseVO.setMassifbaseList(massifbaseViews.stream().filter(m->m.getPrincipalName().equals(e)).collect(Collectors.toList()));
            responseVO.setPrincipalName(e);
            list.add(responseVO);
        });
        return list;
    }

    public void add(PlantingBatchResponseDTO obj){
        List<ProductionManageSdProduceSchemeNode> schemeNodeList = schemeService.getNodeBySchemeId(obj.getSchemeId());
        List<ProducePlantingWorkRemind> remindList=new ArrayList<>();
        schemeNodeList.forEach(e->{
            ProducePlantingWorkRemind remind=new ProducePlantingWorkRemind();
            remind.setMassifId(obj.getMassId()).setMassifName(obj.getMassIfName()).setProductId(obj.getProductId()).setProductName(obj.getProductName())
                    .setPlantingBatchId(obj.getTraceBatchInfoId()).setPlantingBatchName(obj.getTraceBatchName()).setTimeInterval("")
                    .setTaskName(e.getName()).setTaskContent(e.getWorkDetail()).setTaskStatus(2);
            remindList.add(remind);
        });
        saveBatch(remindList);
    }

}
