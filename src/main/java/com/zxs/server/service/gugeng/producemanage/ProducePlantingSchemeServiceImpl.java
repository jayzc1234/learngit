package com.zxs.server.service.gugeng.producemanage;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProducePlantingSchemeDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProducePlantingScheme;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceSchemeNode;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProducePlantingSchemeMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageSdProduceSchemeNodeMapper;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ObjectUniqueValueResponseVO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2021-07-05
 */
@Service
public class ProducePlantingSchemeServiceImpl extends ServiceImpl<ProducePlantingSchemeMapper, ProducePlantingScheme>  {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSdProduceSchemeNodeMapper schemeNodeMapper;

    @Autowired
    private ProductionManageSdProduceSchemeNodeService schemeNodeService;

    @Autowired
    private RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    public void add(ProducePlantingSchemeDTO obj){
        ProducePlantingScheme scheme=new ProducePlantingScheme();
        BeanUtils.copyProperties(obj, scheme);
        scheme.setSchemeId(commonUtil.getUUID());
        String today=DateFormatUtils.format(new Date(), "yyyyMMdd");
        long generate = redisUtil.generate(RedisKey.PRODUCE_PLANTING_SCHEME_NO_KEY + ":" + commonUtil.getOrganizationId()+ ":" +scheme.getProductId()+":"+today);
        String schemeNo = String.format("%s%s%s", scheme.getProductName(), today, StringUtils.leftPad(String.valueOf(generate), 3,"0"));
        scheme.setSchemeNo(schemeNo);
        save(scheme);

        saveSchemeNode(obj, scheme.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ProducePlantingSchemeDTO obj){
        ProducePlantingScheme scheme=new ProducePlantingScheme();
        BeanUtils.copyProperties(obj, scheme);
        baseMapper.updateById(scheme);

        saveSchemeNode(obj, scheme.getId());
    }

    public ProducePlantingSchemeDTO get(Integer id){
        ProducePlantingScheme scheme= baseMapper.selectById(id);
        ProducePlantingSchemeDTO schemeDTO=new ProducePlantingSchemeDTO();
        BeanUtils.copyProperties(scheme, schemeDTO);

        List<ProductionManageSdProduceSchemeNode> schemeNodeList = schemeNodeMapper.selectList(new LambdaQueryWrapper<ProductionManageSdProduceSchemeNode>().eq(ProductionManageSdProduceSchemeNode::getPlantingSchemeId, id));
        schemeDTO.setNodeList(schemeNodeList);
        return schemeDTO;
    }

    private void saveSchemeNode(ProducePlantingSchemeDTO obj, Long plantingSchemeId){
        schemeNodeMapper.delete(new LambdaQueryWrapper<ProductionManageSdProduceSchemeNode>().eq(ProductionManageSdProduceSchemeNode::getPlantingSchemeId, obj.getId()));
        List<ProductionManageSdProduceSchemeNode> schemeNodeList=new ArrayList<>();
        obj.getNodeList().forEach(e->{
            ProductionManageSdProduceSchemeNode node=new ProductionManageSdProduceSchemeNode();
            BeanUtils.copyProperties(e, node);
            node.setPlantingSchemeId(plantingSchemeId);
            schemeNodeList.add(node);

        });
        schemeNodeService.saveBatch(schemeNodeList);
    }

    public PageResults<List<ProducePlantingSchemeDTO>> selectList(DaoSearch obj){
        Page<ProducePlantingScheme> page = new Page<>(obj.getCurrent(), obj.getPageSize());
        QueryWrapper<ProducePlantingScheme> queryWrapper = commonUtil.queryTemplate(ProducePlantingScheme.class);
        if(StringUtils.isNotEmpty(obj.getSearch())){
            queryWrapper.and(q-> q.or().like(ProducePlantingScheme.COL_PRODUCT_NAME, obj.getSearch())
                    .or().like(ProducePlantingScheme.COL_SCHEME_NO, obj.getSearch()));
        }
        queryWrapper.orderByDesc(ProducePlantingScheme.COL_ID);
        IPage<ProducePlantingScheme> iPage = baseMapper.selectPage(page, queryWrapper);
        List<ProducePlantingScheme> schemeList = iPage.getRecords();
        List<ProducePlantingSchemeDTO> schemeDTOS=new ArrayList<>();
        schemeList.forEach(s->{
            ProducePlantingSchemeDTO dto=new ProducePlantingSchemeDTO();
            BeanUtils.copyProperties(s, dto);
            dto.setSchemeTypeText(s.getSchemeType()==1?"按每年固定时间":"按间隔天数");
            schemeDTOS.add(dto);
        });

        PageResults<List<ProducePlantingSchemeDTO>> pageResults = new PageResults<List<ProducePlantingSchemeDTO>>(schemeDTOS,
                new com.jgw.supercodeplatform.common.pojo.common.Page(obj.getPageSize(), obj.getCurrent(), (int)iPage.getTotal()));

        return pageResults;
    }

    public AbstractPageService.PageResults<List<ObjectUniqueValueResponseVO>> listfield(DaoSearch requestDTO){
        PageResults<List<ProducePlantingSchemeDTO>> pageResults = selectList(requestDTO);
        List<ProducePlantingSchemeDTO> list = pageResults.getList();
        PageResults<List<ObjectUniqueValueResponseVO>> pageResult = new PageResults<>();
        pageResult.setPagination(pageResults.getPagination());
        pageResult.setList(list.stream().map(e->new ObjectUniqueValueResponseVO(e.getSchemeId(), e.getSchemeNo(), JSONObject.parseObject(JSONObject.toJSONString(e), Map.class))).collect(Collectors.toList()));
        return pageResult;
    }

    public List<ProductionManageSdProduceSchemeNode> getNodeBySchemeId(String schemeId){
        ProducePlantingScheme scheme= baseMapper.selectOne(new LambdaQueryWrapper<ProducePlantingScheme>().eq(ProducePlantingScheme::getSchemeId, schemeId));
        List<ProductionManageSdProduceSchemeNode> schemeNodeList = schemeNodeMapper.selectList(new LambdaQueryWrapper<ProductionManageSdProduceSchemeNode>().eq(ProductionManageSdProduceSchemeNode::getPlantingSchemeId, scheme.getId()));
        return schemeNodeList;
    }
}
