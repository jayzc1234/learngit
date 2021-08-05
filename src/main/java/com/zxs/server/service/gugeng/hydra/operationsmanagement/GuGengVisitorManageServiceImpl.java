package com.zxs.server.service.gugeng.hydra.operationsmanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.hydra.VisitorProjectTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengVisitorManage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengVisitorProject;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.statistic.GuGengVistorStatistic;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.operationsmanagement.GuGengVisitorManageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.operationsmanagement.GuGengVisitorProjectMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.statistic.GuGengVistorStatisticMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.hydra.statistic.GuGengVistorStatisticServiceImpl;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.operationsmanagement.GuGengVisitorManageListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.operationsmanagement.GuGengVisitorManageNewDetailVO;
import net.app315.nail.common.result.RichResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.VISITOR;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-12-03
 */
@Service
public class GuGengVisitorManageServiceImpl extends ServiceImpl<GuGengVisitorManageMapper, GuGengVisitorManage> implements BaseService {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private GuGengVistorStatisticMapper statisticMapper;

    @Autowired
    private GuGengVisitorProjectMapper visitorProjectMapper;

    @Autowired
    private GuGengVisitorProjectService visitorProjectService;

    @Autowired
    private GuGengVistorStatisticServiceImpl guGengVistorStatisticService;

    public RichResult<String> add(GuGengVisitorManageDTO visitorManageDTO) throws SuperCodeException {
        RichResult<String> richResult=new RichResult<>();
        GuGengVisitorManage entity = new GuGengVisitorManage();
        BeanUtils.copyProperties(visitorManageDTO,entity);
        entity.setVisitDate(visitorManageDTO.getVisitDate());
        entity.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        String visitNo = visitorManageDTO.getVisitNo();
        boolean flag=generateNo(visitNo,entity);
        if (!flag){
            richResult.setState(500);
            richResult.setMsg("编号已存在");
        }else {
            baseMapper.insert(entity);
        }
        return richResult;
    }

    public RichResult<String> update(GuGengVisitorManageDTO visitorManageDTO) throws SuperCodeException {
        if (null==visitorManageDTO.getId()){
            CommonUtil.throwSuperCodeExtException(500,"主键不能为空");
        }
        RichResult<String> richResult=new RichResult<>();
        GuGengVisitorManage entity = new GuGengVisitorManage();
        BeanUtils.copyProperties(visitorManageDTO,entity);
        entity.setVisitDate(visitorManageDTO.getVisitDate());
        String visitNo = visitorManageDTO.getVisitNo();
        boolean flag=generateNo(visitNo,entity);
        if (!flag){
            richResult.setState(500);
            richResult.setMsg("编号已存在");
        }else {
            baseMapper.updateById(entity);
        }
        reStatistic(visitorManageDTO.getVisitDate());
        return richResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public RichResult addV2(GuGengVisitorManageNewDTO visitorManageDTO) {
        RichResult<String> richResult=new RichResult<>();
        GuGengVisitorManage entity = new GuGengVisitorManage();
        BeanUtils.copyProperties(visitorManageDTO,entity);
        entity.setVisitDate(visitorManageDTO.getVisitDate());
        entity.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        String visitNo = visitorManageDTO.getVisitNo();
        boolean flag=generateNo(visitNo,entity);
        if (!flag){
            richResult.setState(500);
            richResult.setMsg("编号已存在");
        }else {
            baseMapper.insert(entity);
            List<ArrayVisitorProjectByTypeDTO> visitorProjects = visitorManageDTO.getVisitorProjects();
            List<GuGengVisitorProject> projectList = getGuGengVisitorProjects(entity, visitorProjects);
            visitorProjectService.saveBatch(projectList);
            baseMapper.updateById(entity);
        }
        return richResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public RichResult<String> updateV2(GuGengVisitorManageNewDTO visitorManageDTO) throws SuperCodeException {
        if (null==visitorManageDTO.getId()){
            CommonUtil.throwSuperCodeExtException(500,"主键不能为空");
        }
        RichResult<String> richResult=new RichResult<>();
        GuGengVisitorManage entity = new GuGengVisitorManage();
        BeanUtils.copyProperties(visitorManageDTO,entity);
        entity.setVisitDate(visitorManageDTO.getVisitDate());
        String visitNo = visitorManageDTO.getVisitNo();
        boolean flag=generateNo(visitNo,entity);
        if (!flag){
            richResult.setState(500);
            richResult.setMsg("编号已存在");
        }else {
            List<ArrayVisitorProjectByTypeDTO> visitorProjects = visitorManageDTO.getVisitorProjects();
            List<GuGengVisitorProject> projectList = getGuGengVisitorProjects(entity, visitorProjects);
            visitorProjectService.saveOrUpdateBatch(projectList);

            List<Long> deleteVisitorProjectIds = visitorManageDTO.getDeleteVisitorProjectIds();
            if (CollectionUtils.isNotEmpty(deleteVisitorProjectIds)){
                visitorProjectService.removeByIds(deleteVisitorProjectIds);
            }
            baseMapper.updateById(entity);
        }
        reStatistic(visitorManageDTO.getVisitDate());
        return richResult;
    }

    /**
     * 根据项目参数获取项目列表并设置来访记录相应的实收应收额
     * @param entity
     * @param visitorProjects
     * @return
     */
    private List<GuGengVisitorProject> getGuGengVisitorProjects(GuGengVisitorManage entity, List<ArrayVisitorProjectByTypeDTO> visitorProjects) {
        if (CollectionUtils.isEmpty(visitorProjects)){
            CommonUtil.throwSuperCodeExtException(500,"不存在项目信息");
        }
        List<GuGengVisitorProject> visitorProjectList=new ArrayList<>();
        visitorProjects.stream().forEach(pro ->
        {
            List<GuGengVisitorProjectDTO> visitorProjects1 = pro.getVisitorProjects();
            if (CollectionUtils.isNotEmpty(visitorProjects1)){
                Integer type = pro.getType();
                List<GuGengVisitorProject> typeVisitorProjects = visitorProjects1.stream().map(proDTO -> {
                    GuGengVisitorProject pp = new GuGengVisitorProject();
                    BeanUtils.copyProperties(proDTO, pp);
                    pp.setVisitorManageId(entity.getId());
                    if (null == type) {
                        CommonUtil.throwSuperCodeExtException(500, "项目类型不能为空");
                    }
                    VisitorProjectTypeEnum byStatus = VisitorProjectTypeEnum.getByStatus(type);
                    switch (byStatus) {
                        /**
                         * 住宿
                         */
                        case ZHU_SU:
                            entity.setStayRealMoney(CommonUtil.bigDecimalAdd(entity.getStayRealMoney(), proDTO.getRealMoney()));
                            entity.setStayReceivableMoney(CommonUtil.bigDecimalAdd(entity.getStayReceivableMoney(), proDTO.getReceivableMoney()));
                            break;
                        /**
                         * 休闲
                         */
                        case XIU_XIAN:
                            entity.setLeisureRealMoney(CommonUtil.bigDecimalAdd(entity.getLeisureRealMoney(), proDTO.getRealMoney()));
                            entity.setLeisureReceivableMoney(CommonUtil.bigDecimalAdd(entity.getLeisureReceivableMoney(), proDTO.getReceivableMoney()));
                            break;
                        /**
                         * 餐饮
                         */
                        case CAN_YIN:
                            entity.setRepastRealMoney(proDTO.getRealMoney());
                            entity.setRepastReceivableMoney(proDTO.getReceivableMoney());
                            break;
                        /**
                         * 其它
                         */
                        case OTHER:
                            entity.setOtherRealMoney(proDTO.getRealMoney());
                            entity.setOtherReceivableMoney(proDTO.getReceivableMoney());
                            break;
                        default:
                            break;
                    }
                    pp.setType(type);
                    pp.setVisitorManageId(entity.getId());
                    return pp;
                }).collect(Collectors.toList());
                visitorProjectList.addAll(typeVisitorProjects);
            }
      });
      return visitorProjectList;
    }

    /**
     * 详情信息
     * @param id
     * @return
     */
    public RichResult<GuGengVisitorManageNewDetailVO> detail(Long id) {
        RichResult<GuGengVisitorManageNewDetailVO> richResult=new RichResult();
        GuGengVisitorManage visitorManage = baseMapper.selectById(id);
        if (null==visitorManage){
            richResult.setState(500);
            richResult.setMsg("该人员信息不存在");
        }else {
            richResult.setState(200);
            GuGengVisitorManageNewDetailVO visitorManageVO=new GuGengVisitorManageNewDetailVO();
            BeanUtils.copyProperties(visitorManage,visitorManageVO);
            visitorManageVO.setVisitDate(visitorManage.getVisitDate());

            QueryWrapper<GuGengVisitorProject> projectQueryWrapper=new QueryWrapper<>();
            projectQueryWrapper.eq(GuGengVisitorProject.COL_VISITOR_MANAGE_ID,id);
            List<GuGengVisitorProject> list = visitorProjectService.list(projectQueryWrapper);
            if (CollectionUtils.isNotEmpty(list)){
                Map<Integer,ArrayVisitorProjectByTypeDTO> distinctMap=new HashMap<>();
                List<ArrayVisitorProjectByTypeDTO> collect = new ArrayList<>();

                for (GuGengVisitorProject p : list) {
                    //根据type分组
                    Integer type = p.getType();
                    ArrayVisitorProjectByTypeDTO arrayVisitorProjectByTypeDTO= distinctMap.get(type);
                    if (null== arrayVisitorProjectByTypeDTO){
                        arrayVisitorProjectByTypeDTO=new ArrayVisitorProjectByTypeDTO();
                        List<GuGengVisitorProjectDTO> visitorProjects=new ArrayList<>();
                        arrayVisitorProjectByTypeDTO.setVisitorProjects(visitorProjects);
                        collect.add(arrayVisitorProjectByTypeDTO);
                    }
                    arrayVisitorProjectByTypeDTO.setType(type);
                    //新增项目
                    GuGengVisitorProjectDTO guGengVisitorProjectDTO=new GuGengVisitorProjectDTO();
                    BeanUtils.copyProperties(p,guGengVisitorProjectDTO);
                    arrayVisitorProjectByTypeDTO.getVisitorProjects().add(guGengVisitorProjectDTO);
                    distinctMap.put(type,arrayVisitorProjectByTypeDTO);

                }
                //设置整个类型的数据
                visitorManageVO.setVisitorProjects(collect);
            }
            richResult.setResults(visitorManageVO);
        }
        return richResult;
    }

    private boolean generateNo( String visitNo,GuGengVisitorManage entity) {
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();
        Long id = entity.getId();
        QueryWrapper<GuGengVisitorManage> queryWrapper=commonUtil.queryTemplate(GuGengVisitorManage.class);
        queryWrapper.ne(null!=id,GuGengVisitorManage.COL_ID,id);
        if (StringUtils.isBlank(visitNo)){
            visitNo=numberGenerator.getSerialNumber(6, RedisKey.VISITOR_NO, organizationId+sysId, CommonUtil.getSecondsNextEarlyMorning());
            queryWrapper.eq(GuGengVisitorManage.COL_VISIT_NO,visitNo);
            Integer count = baseMapper.selectCount(queryWrapper);
            while (null!=count && count>0){
                visitNo=numberGenerator.getSerialNumber(3, RedisKey.VISITOR_NO, organizationId+sysId, CommonUtil.getSecondsNextEarlyMorning());
                queryWrapper.eq(GuGengVisitorManage.COL_VISIT_NO,visitNo);
                count = baseMapper.selectCount(queryWrapper);
            }
            entity.setVisitNo(visitNo);
        }else {
            queryWrapper.eq(GuGengVisitorManage.COL_VISIT_NO,visitNo);
            Integer count = baseMapper.selectCount(queryWrapper);
            if (null!=count && count>0){
               return false;
            }
            entity.setVisitNo(visitNo);
        }
        return true;
    }

    @Override
    public IPage<GuGengVisitorManageListVO> pageList(DaoSearch daoSearch) {
        GuGengVisitorManageListDTO visitorManageListDTO= (GuGengVisitorManageListDTO) daoSearch;
        Page<GuGengVisitorManageListVO> page = new Page<>(visitorManageListDTO.getDefaultCurrent(), visitorManageListDTO.getDefaultPageSize());
        QueryWrapper<GuGengVisitorManage> queryWrapper =commonUtil.advanceSearchQueryWrapperSet(visitorManageListDTO,"GuGengVisitorManageService.pageList",GuGengVisitorManage.class);
        queryWrapper.orderByDesc("Id");
        // 添加数据权限
        commonUtil.roleDataAuthFilter(VISITOR, queryWrapper, GuGengVisitorManage.COL_CREATE_USER_ID, StringUtils.EMPTY);
        return baseMapper.pageList(page,queryWrapper);
    }

    public void deleteById(Long id) {
        GuGengVisitorManage guGengVisitorManage = baseMapper.selectById(id);
        if (null==guGengVisitorManage){
            CommonUtil.throwSuperCodeExtException(500,"不存在该来访信息");
        }
        baseMapper.deleteById(id);
        reStatistic(guGengVisitorManage.getVisitDate());
    }

    /**
     *
     * @param visitDate
     */
    public  void reStatistic(Date visitDate){
        if (null==visitDate){
            return ;
        }
        //首先删除当前来访日期统计数据
        QueryWrapper<GuGengVistorStatistic> gengVistorStatisticQueryWrapper=new QueryWrapper<>();
        gengVistorStatisticQueryWrapper.eq(GuGengVistorStatistic.COL_VISIT_DATE,visitDate);
        statisticMapper.delete(gengVistorStatisticQueryWrapper);

        //重新统计来访日期数据
        QueryWrapper<GuGengVisitorManage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(GuGengVistorStatistic.COL_VISIT_DATE,visitDate);
        List<GuGengVisitorManage> guGengVisitorManages = baseMapper.selectList(queryWrapper);
        guGengVistorStatisticService.statisticData(guGengVisitorManages);
    }


}
