package com.zxs.server.service.gugeng.hydra.statistic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.hydra.statistic.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.hydra.VisitorClientTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.hydra.VisitorProjectTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengVisitorManage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengVisitorProject;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.statistic.GuGengVistorStatistic;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.operationsmanagement.GuGengVisitorManageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.operationsmanagement.GuGengVisitorProjectMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.statistic.GuGengVistorStatisticMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.line.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.statistic.VisitorClientDataCurveLineVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-12-09
 */
@Service
@Slf4j
public class GuGengVistorStatisticServiceImpl extends ServiceImpl<GuGengVistorStatisticMapper, GuGengVistorStatistic> implements BaseService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private GuGengVisitorManageMapper visitorManageMapper;

    @Autowired
    private GuGengVisitorProjectMapper visitorProjectMapper;

    private static String CLIENT_TYPE = "clientType";
    private static String PROJECT_TYPE = "projectType";
    private static String CASH_TYPE = "cashType";


    private static int CLIENT_TYPE_INT = 1;
    private static int PROJECT_TYPE_INT = 2;
    private static int CASH_TYPE_INT = 3;

    private static Map<Integer, Map<Integer, String>> typeMap=new HashMap<>();
    static {
        Map<Integer, String> clientTypeMap=new LinkedHashMap<>();
        Map<Integer, String> projectTypeMap=new LinkedHashMap<>();
        Map<Integer, String> cashTypeMap=new LinkedHashMap<>();
        //存入项目类型值和描述map
        for (VisitorProjectTypeEnum value : VisitorProjectTypeEnum.values()) {
            projectTypeMap.put(value.getStatus(),value.getDesc());
        }
        //存入客户类型的值与描述的map
        clientTypeMap.put(0,"来访客户总数");
        for (VisitorClientTypeEnum value : VisitorClientTypeEnum.values()) {
            clientTypeMap.put(value.getStatus(),value.getDesc());
        }

        //存入金额值和描述的map
        cashTypeMap.put(1,"实收金额");
        cashTypeMap.put(2,"应收金额");

        typeMap.put(1,clientTypeMap);
        typeMap.put(2,projectTypeMap);
        typeMap.put(3,cashTypeMap);
    }

    public VisitorClientDataCurveLineVO visitorClientStatistic(DateIntervalListDTO dateIntervalDTO) {
        return  visitorDataStatistic(dateIntervalDTO,1);
    }
    public VisitorClientDataCurveLineVO visitorProjectStatistic(DateIntervalListDTO dateIntervalDTO) {
        return  visitorDataStatistic(dateIntervalDTO,2);
    }

    public VisitorClientDataCurveLineVO visitorCashStatistic(DateIntervalListDTO dateIntervalDTO) {
        return  visitorDataStatistic(dateIntervalDTO,3);
    }

    /**
     *
     * @param dateIntervalDTO
     * @param type 1客户类目，2项目类型
     * @return
     */
    private VisitorClientDataCurveLineVO visitorDataStatistic(DateIntervalListDTO dateIntervalDTO,Integer type) {
        VisitorClientDataCurveLineVO curveLineVO = new VisitorClientDataCurveLineVO();
        List<LineChartVO> liberationists = new ArrayList<>(4);
        String startQueryDate = dateIntervalDTO.getStartQueryDate();
        String endQueryDate = dateIntervalDTO.getEndQueryDate();
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(startQueryDate, endQueryDate);
        curveLineVO.setVisitDate(startQueryDate+"~"+endQueryDate);

        //查询统计信息
        List<GuGengVistorStatistic> guGengVistorStatistics = queryGuGengVistorStatistics(type, startQueryDate, endQueryDate);
        if (CollectionUtils.isEmpty(guGengVistorStatistics)) {
            //查不到值则设置空值
            emptyInfoSet(type, curveLineVO, liberationists, dateInterval);
        } else {
            //根据请求类型获取到对应的类型与曲线数据map
            Map<Integer, LineChartVO> integerLineChartVOMap = getIntegerLineChartVO(liberationists,type);
            //把返回集合封装为以时间为key的map
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, List<GuGengVistorStatistic>> listMap = getStatisticListMap(guGengVistorStatistics, dateFormat);
            //设置标记map
            Map<Integer, List<Integer>> flagMap = new HashMap<>(4);

            //循环前端传过来的完整的时间周期
            for (String date : dateInterval) {
                //通过时间在数据库返回的map集合中查找是否有当前时间的数据
                List<GuGengVistorStatistic> guGengVistorStatistics1 = listMap.get(date);
                if (CollectionUtils.isNotEmpty(guGengVistorStatistics1)){
                    //定义dataNum的总和
                    Integer totalValue=0;
                    //如果有当前时间的数据则遍历集合，因为当前时间的数据可能有多条
                    for (GuGengVistorStatistic guGengVistorStatistic : guGengVistorStatistics1) {
                        totalValue=CommonUtil.integerAdd(totalValue,guGengVistorStatistic.getDataNum());
                        Integer clientType = guGengVistorStatistic.getClientType();
                        Integer projectType = guGengVistorStatistic.getProjectType();
                        switch (type){
                            case 1:
                                LineChartVO<Integer> lineChartVO1 = integerLineChartVOMap.get(clientType);
                                if (null==lineChartVO1){
                                    log.error("根据客户类型：{}无法获取曲线对象",clientType);
                                }
                                addClientOrProValues(date, lineChartVO1, guGengVistorStatistic.getDataNum(),type);
                                putFlagMap(CLIENT_TYPE_INT,clientType,flagMap);
                                break;
                            case 2:
                                LineChartVO<Integer> lineChartVO2 = integerLineChartVOMap.get(projectType);
                                log.error("根据项目类型：{}无法获取曲线对象",projectType);
                                log.info("当前时间="+date+",projectType="+projectType+",获取到lineChartVO2="+lineChartVO2);
                                addClientOrProValues(date, lineChartVO2, guGengVistorStatistic.getDataNum(),type);
                                putFlagMap(PROJECT_TYPE_INT,projectType,flagMap);
                                break;
                            case 3:
                                BigDecimal realMoney = guGengVistorStatistic.getRealMoney();
                                BigDecimal receivableMoney = guGengVistorStatistic.getReceivableMoney();

                                LineChartVO<BigDecimal> linechartvo1 = integerLineChartVOMap.get(1);
                                addCashValue(date,realMoney,linechartvo1);

                                LineChartVO<BigDecimal> linechartvo3 = integerLineChartVOMap.get(2);
                                addCashValue(date, receivableMoney, linechartvo3);
                                putFlagMap(CASH_TYPE_INT,projectType,flagMap);
                                break;
                            default:
                                break;
                        }
                    }
                    //如果是来访客户数据则设置总数
                    if (type==1){
                        LineChartVO<Integer> lineChartVO = integerLineChartVOMap.get(0);
                        addClientOrProValues(date, lineChartVO, totalValue,type);
                    }
                    //设置当前时间未查找到的客户类型或者项目类型的默认值
                    fillNoQueriedType(type, integerLineChartVOMap, flagMap, date);
                    //清空map中内容准备下次计算
                    flagMap.clear();
                }else {
                    if (type==3){
                        LineChartVO<BigDecimal> linechartvo1 = integerLineChartVOMap.get(1);
                        LineChartVO.NameAndValueVO nameandvaluevo1=linechartvo1.new NameAndValueVO();
                        nameandvaluevo1.setName(date);
                        nameandvaluevo1.setValue(0);
                        linechartvo1.getValues().add(nameandvaluevo1);

                        LineChartVO<BigDecimal> linechartvo2 = integerLineChartVOMap.get(2);
                        LineChartVO.NameAndValueVO nameandvaluevo2=linechartvo2.new NameAndValueVO();
                        nameandvaluevo2.setName(date);
                        nameandvaluevo2.setValue(0);
                        linechartvo2.getValues().add(nameandvaluevo2);
                    }else {
                        Collection<LineChartVO> values = integerLineChartVOMap.values();
                        for (LineChartVO value : values) {
                            LineChartVO.NameAndValueVO gNameandvaluevo=value.new NameAndValueVO();
                            gNameandvaluevo.setName(date);
                            gNameandvaluevo.setValue(0);
                            value.getValues().add(gNameandvaluevo);
                        }
                    }
                }
            }
            //设置curveLineVO的总的统计信息
            setTotalValue(type, curveLineVO, liberationists, integerLineChartVOMap);
        }
        return curveLineVO;
    }

    /**
     * 添加金额
     * @param date
     * @param realMoney
     * @param linechartvo1
     */
    private void addCashValue(String date, BigDecimal realMoney, LineChartVO<BigDecimal> linechartvo1) {
        List<LineChartVO<BigDecimal>.NameAndValueVO> values = linechartvo1.getValues();
        LineChartVO.NameAndValueVO nameandvaluevo1=null;
        boolean exist=false;
        if (CollectionUtils.isNotEmpty(values)){
            for (LineChartVO<BigDecimal>.NameAndValueVO value : values) {
                boolean equals = value.getName().equals(date);
                if (equals){
                    nameandvaluevo1=value;
                    exist=true;
                }
            }
        }
        if (exist){
            nameandvaluevo1.setValue(CommonUtil.bigDecimalAdd((BigDecimal)nameandvaluevo1.getValue(),realMoney));
        }else {
            nameandvaluevo1=linechartvo1.new NameAndValueVO();
            nameandvaluevo1.setName(date);
            nameandvaluevo1.setValue(realMoney);
            linechartvo1.getValues().add(nameandvaluevo1);
        }
        linechartvo1.setTotalValue(CommonUtil.bigDecimalAdd(linechartvo1.getTotalValue(), realMoney));
    }

    /**
     * 设置当前时间未查找到的客户类型或者项目类型的默认值
     * @param type
     * @param integerLineChartVOMap
     * @param flagMap
     * @param date
     */
    private void fillNoQueriedType(Integer type, Map<Integer, LineChartVO> integerLineChartVOMap, Map<Integer, List<Integer>> flagMap, String date) {
        //如果不是获取金额则计算剩余类型，因为如果是金额类型的话一定不会有未满足的类型 ，因为有数据则会手动把应收金额实收金额都设置一下
        if (type!=3){
            List<Integer> integers = flagMap.get(type);
            if (CollectionUtils.isNotEmpty(integers)){
                if (type==PROJECT_TYPE_INT){
                    //存入项目类型值和描述map
                    for (VisitorProjectTypeEnum value : VisitorProjectTypeEnum.values()) {
                        if (!integers.contains(value.getStatus())){
                            LineChartVO<Integer> lineChartVO = integerLineChartVOMap.get(value.getStatus());
                            LineChartVO.NameAndValueVO nameAndValueVO=lineChartVO.new NameAndValueVO();
                            nameAndValueVO.setName(date);
                            nameAndValueVO.setValue(0);
                            lineChartVO.getValues().add(nameAndValueVO);
                        }
                    }
                }else if (type==CLIENT_TYPE_INT){
                    for (VisitorClientTypeEnum value : VisitorClientTypeEnum.values()) {
                        if (!integers.contains(value.getStatus())){
                            LineChartVO<Integer> lineChartVO = integerLineChartVOMap.get(value.getStatus());
                            LineChartVO.NameAndValueVO nameAndValueVO=lineChartVO.new NameAndValueVO();
                            nameAndValueVO.setName(date);
                            nameAndValueVO.setValue(0);
                            lineChartVO.getValues().add(nameAndValueVO);
                        }
                    }
                }
            }
        }
    }

    private void putFlagMap(int clientTypeKey, Integer clientType, Map<Integer, List<Integer>> flagMap) {
        List<Integer> integers = flagMap.get(clientTypeKey);
        if (null==integers){
            integers=new ArrayList<>();
        }
        integers.add(clientType);
        flagMap.put(clientTypeKey,integers);
    }

    private List<GuGengVistorStatistic> queryGuGengVistorStatistics(Integer type, String startQueryDate, String endQueryDate) {
        QueryWrapper<GuGengVistorStatistic> queryWrapper = commonUtil.queryTemplate(GuGengVistorStatistic.class);
        queryWrapper.ge(StringUtils.isNotBlank(startQueryDate), GuGengVistorStatistic.COL_VISIT_DATE, startQueryDate);
        queryWrapper.le(StringUtils.isNotBlank(endQueryDate), GuGengVistorStatistic.COL_VISIT_DATE, endQueryDate);
        if (type==3){
            queryWrapper.eq(GuGengVistorStatistic.COL_TYPE,1);
        }else {
            queryWrapper.eq(GuGengVistorStatistic.COL_TYPE,type);
        }
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 未根据实际区间从数据库查到数据时设置默认值
     * @param type
     * @param curveLineVO
     * @param liberationists
     * @param dateInterval
     */
    private void emptyInfoSet(Integer type, VisitorClientDataCurveLineVO curveLineVO, List<LineChartVO> liberationists, List<String> dateInterval) {
        Map<Integer, String> integerStringMap = typeMap.get(type);
        for (String desc : integerStringMap.values()) {
            LineChartVO lineChartVO = new LineChartVO();
            List<LineChartVO.NameAndValueVO> values = new ArrayList<>();
            LineChartVO.Option option = lineChartVO.new Option();
            option.setName(desc);
            lineChartVO.setOption(option);
            for (String date : dateInterval) {
                LineChartVO.NameAndValueVO nameAndValueVO = lineChartVO.new NameAndValueVO();
                nameAndValueVO.setName(date);
                nameAndValueVO.setValue(0);
                values.add(nameAndValueVO);
            }
            lineChartVO.setValues(values);
            liberationists.add(lineChartVO);
        }
        curveLineVO.setValues(liberationists);
        curveLineVO.init();
    }

    /**
     * 设置返回前端的curveLineVO的每条曲线的统计信息
     * @param type
     * @param curveLineVO
     * @param liberationists
     * @param integerLineChartVOMap
     */
    private void setTotalValue(Integer type, VisitorClientDataCurveLineVO curveLineVO, List<LineChartVO> liberationists, Map<Integer, LineChartVO> integerLineChartVOMap) {
        switch (type){
            case 1:
                LineChartVO<Integer> glinechartvo = integerLineChartVOMap.get(VisitorClientTypeEnum.GROUP.getStatus());
                LineChartVO<Integer> iLinechartvo = integerLineChartVOMap.get(VisitorClientTypeEnum.INDIVIDUAL.getStatus());
                LineChartVO<Integer> mLinechartvo = integerLineChartVOMap.get(VisitorClientTypeEnum.MEMBER.getStatus());
                LineChartVO<Integer> rLinechartvo = integerLineChartVOMap.get(VisitorClientTypeEnum.RECEPTION.getStatus());

                curveLineVO.setGroupClientNum(Optional.ofNullable(glinechartvo.getTotalValue()).orElse(0));
                curveLineVO.setIndividualClientNum(Optional.ofNullable(iLinechartvo.getTotalValue()).orElse(0));
                curveLineVO.setMemberClientNum(Optional.ofNullable(mLinechartvo.getTotalValue()).orElse(0));
                curveLineVO.setReceptionClientNum(Optional.ofNullable(rLinechartvo.getTotalValue()).orElse(0));

                
                int i = CommonUtil.integerAdd(glinechartvo.getTotalValue(), iLinechartvo.getTotalValue());
                int i2 = CommonUtil.integerAdd(rLinechartvo.getTotalValue(), mLinechartvo.getTotalValue());
                curveLineVO.setClientTotalNum(CommonUtil.integerAdd(i,i2));
                curveLineVO.setValues(liberationists);
                break;
            case 2:
                LineChartVO<Integer> cLinechartvo = integerLineChartVOMap.get(VisitorProjectTypeEnum.CAN_YIN.getStatus());
                LineChartVO<Integer> xLinechartvo = integerLineChartVOMap.get(VisitorProjectTypeEnum.XIU_XIAN.getStatus());
                LineChartVO<Integer> zLinechartvo = integerLineChartVOMap.get(VisitorProjectTypeEnum.ZHU_SU.getStatus());
                LineChartVO<Integer> oLinechartvo = integerLineChartVOMap.get(VisitorProjectTypeEnum.OTHER.getStatus());
                curveLineVO.setFoodNum(Optional.ofNullable(cLinechartvo.getTotalValue()).orElse(0));
                curveLineVO.setStayNum(Optional.ofNullable(zLinechartvo.getTotalValue()).orElse(0));
                curveLineVO.setLeisureNum(Optional.ofNullable(xLinechartvo.getTotalValue()).orElse(0));
                curveLineVO.setOtherNum(Optional.ofNullable(oLinechartvo.getTotalValue()).orElse(0));

                int i3 = CommonUtil.integerAdd(cLinechartvo.getTotalValue(), xLinechartvo.getTotalValue());
                int i4 = CommonUtil.integerAdd(zLinechartvo.getTotalValue(), oLinechartvo.getTotalValue());
                curveLineVO.setClientTotalNum(CommonUtil.integerAdd(i3,i4));
                curveLineVO.setValues(liberationists);
                break;
            case 3:
                LineChartVO<BigDecimal> linechartvo1 = integerLineChartVOMap.get(1);
                LineChartVO<BigDecimal> linechartvo2 = integerLineChartVOMap.get(2);
                curveLineVO.setValues(liberationists);
                curveLineVO.setTotalRealMoney(Optional.ofNullable(linechartvo1.getTotalValue()).orElse(new BigDecimal(0)));
                curveLineVO.setTotalReceivableMoney(Optional.ofNullable(linechartvo2.getTotalValue()).orElse(new BigDecimal(0)));
                break;
            default:break;
        }
    }

    /**
     * 添加客户类型或项目类型的value
     * @param date
     * @param lineChartVO2
     * @param dataNum
     */
    private void addClientOrProValues(String date, LineChartVO<Integer> lineChartVO2, Integer dataNum,int type) {
        if (null==lineChartVO2){
           log.error("当前请求类型={}，时间为={}，无法获取到曲线对象",type,date);
        }else {
            LineChartVO.NameAndValueVO nameandvaluevo2 = lineChartVO2.new NameAndValueVO();
            nameandvaluevo2.setName(date);
            nameandvaluevo2.setValue(dataNum);

            lineChartVO2.setTotalValue(CommonUtil.integerAdd(lineChartVO2.getTotalValue(), dataNum));
            lineChartVO2.getValues().add(nameandvaluevo2);
        }
    }

    private Map<String, List<GuGengVistorStatistic>> getStatisticListMap(List<GuGengVistorStatistic> guGengVistorStatistics, SimpleDateFormat dateFormat) {
        Map<String, List<GuGengVistorStatistic>> listMap = new HashMap<>();
        for (GuGengVistorStatistic guGengVistorStatistic : guGengVistorStatistics) {
            Date visitDate = guGengVistorStatistic.getVisitDate();
            String format = dateFormat.format(visitDate);
            List<GuGengVistorStatistic> guGengVistorStatistics1 = listMap.get(format);
            if (null == guGengVistorStatistics1) {
                guGengVistorStatistics1 = new ArrayList<>();
            }
            guGengVistorStatistics1.add(guGengVistorStatistic);
            listMap.put(format, guGengVistorStatistics1);
        }
        return listMap;
    }

    /**
     * 根据请求类型生成对应要显示的曲线
     * @param lecherous 对应客户类型，项目类型。金额类型要生成的曲线数
     * @param type：1客户类型，2项目类型，3金额
     * @return
     */
    private Map<Integer, LineChartVO> getIntegerLineChartVO(List<LineChartVO> lecherous,int type) {
        Map<Integer,LineChartVO> map=new HashMap<>();
        Map<Integer, String> integerStringMap = typeMap.get(type);
        for (Integer key : integerStringMap.keySet()) {
            //接待
            LineChartVO receptionLineChartVO = new LineChartVO();
            List<LineChartVO.NameAndValueVO> receptionValues = new ArrayList<>();
            LineChartVO.Option receptionOption = receptionLineChartVO.new Option();
            receptionOption.setName(integerStringMap.get(key));
            receptionLineChartVO.setOption(receptionOption);
            receptionLineChartVO.setValues(receptionValues);

            lecherous.add(receptionLineChartVO);
            map.put(key,receptionLineChartVO);
        }
        return map;
    }


    public void dataSync() {
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        QueryWrapper<GuGengVistorStatistic> statisticQueryWrapper=new QueryWrapper<>();
        baseMapper.delete(statisticQueryWrapper);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)-1);
        Date time = calendar.getTime();
        String currentDate = dateFormat.format(time);

        QueryWrapper<GuGengVisitorManage> visitorManageQueryWrapper=new QueryWrapper<>();
        visitorManageQueryWrapper.le(GuGengVisitorManage.COL_VISIT_DATE,currentDate);
        List<GuGengVisitorManage> guGengVisitorManages = visitorManageMapper.selectList(visitorManageQueryWrapper);
        statisticData(guGengVisitorManages);
    }

    /**
     * 运营数据统计
     * 1.拿到要统计的数据，对其进行客户数和
     * @param guGengVisitorManages
     */
    public  void statisticData( List<GuGengVisitorManage> guGengVisitorManages) {
        if (CollectionUtils.isNotEmpty(guGengVisitorManages)){
            Map<String, GuGengVistorStatistic> clientTypeStatisticMap=new HashMap<>();
            Map<String, GuGengVistorStatistic> projectTypeStatisticMap=new HashMap<>();

            List<GuGengVistorStatistic> gengVistorStatisticList=new ArrayList<>();
            guGengVisitorManages.forEach(visitor->{
                Integer clientType = visitor.getClientType();
                String organizationId = visitor.getOrganizationId();
                String sysId = visitor.getSysId();
                Date visitDate = visitor.getVisitDate();
                long time = visitDate.getTime();
                String key=organizationId+sysId+1+clientType+time;
                GuGengVistorStatistic guGengVistorStatistic = clientTypeStatisticMap.get(key);

                BigDecimal leisureReceivableMoney = visitor.getLeisureReceivableMoney();
                BigDecimal otherReceivableMoney = visitor.getOtherReceivableMoney();
                BigDecimal repastReceivableMoney = visitor.getRepastReceivableMoney();
                BigDecimal stayReceivableMoney = visitor.getStayReceivableMoney();
                BigDecimal totalReceivableMoney= CommonUtil.bigDecimalAdd(leisureReceivableMoney,otherReceivableMoney).add(CommonUtil.bigDecimalAdd(repastReceivableMoney,stayReceivableMoney));

                BigDecimal leisureRealMoney = visitor.getLeisureRealMoney();
                BigDecimal otherRealMoney = visitor.getOtherRealMoney();
                BigDecimal repastRealMoney = visitor.getRepastRealMoney();
                BigDecimal stayRealMoney = visitor.getStayRealMoney();
                BigDecimal totalRealMoney=CommonUtil.bigDecimalAdd(leisureRealMoney,otherRealMoney).add(CommonUtil.bigDecimalAdd(repastRealMoney,stayRealMoney));

                GuGengVistorStatistic gengVistorStatistic = generateStatistic(1,visitor, organizationId, sysId, totalReceivableMoney, totalRealMoney, guGengVistorStatistic, null);
                gengVistorStatistic.setClientType(clientType);
                clientTypeStatisticMap.put(key,gengVistorStatistic);

                String visitProject = visitor.getVisitProject();
                if (null!=visitProject){
                    String[] split = visitProject.split(",");
                    for (String projectType : split) {
                        if (!StringUtils.isBlank(projectType)){
                            String projectKey=organizationId+sysId+2+projectType+time;
                            GuGengVistorStatistic projectVisitorStatistic = projectTypeStatisticMap.get(projectKey);
                            GuGengVistorStatistic gengVistorStatistic1 = generateStatistic(2,visitor, organizationId, sysId, totalReceivableMoney, totalRealMoney, projectVisitorStatistic,Integer.valueOf(projectType));
                            gengVistorStatistic1.setProjectType(Integer.valueOf(projectType));
                            projectTypeStatisticMap.put(projectKey,gengVistorStatistic1);
                        }
                    }
                }
            });

            Collection<GuGengVistorStatistic> values = projectTypeStatisticMap.values();
            Collection<GuGengVistorStatistic> values1 = clientTypeStatisticMap.values();
            if (CollectionUtils.isNotEmpty(values)){
                gengVistorStatisticList.addAll(values);
            }

            if (CollectionUtils.isNotEmpty(values1)){
                gengVistorStatisticList.addAll(values1);
            }
            saveBatch(gengVistorStatisticList);
        }
    }


    private GuGengVistorStatistic generateStatistic(int type,GuGengVisitorManage visitor, String organizationId, String sysId, BigDecimal totalReceivableMoney, BigDecimal totalRealMoney, GuGengVistorStatistic projectVisitorStatistic, Integer projectType) {
        Integer num=null;
        if (type==2){
            QueryWrapper<GuGengVisitorProject> projectQueryWrapper=new QueryWrapper<>();
            Long id = visitor.getId();
            projectQueryWrapper.eq(GuGengVisitorProject.COL_VISITOR_MANAGE_ID,id);
            projectQueryWrapper.eq(GuGengVisitorProject.COL_TYPE,projectType);
            num = visitorProjectMapper.selectCount(projectQueryWrapper);
            if (null ==num || 0==num){
                num=1;
            }
        }else {
            num = visitor.getVisitorNum();
        }

        if (null == projectVisitorStatistic) {
            projectVisitorStatistic = new GuGengVistorStatistic();
            projectVisitorStatistic.setType(type);
            projectVisitorStatistic.setVisitDate(visitor.getVisitDate());
            projectVisitorStatistic.setDataNum(num);
            projectVisitorStatistic.setReceivableMoney(totalReceivableMoney);
            projectVisitorStatistic.setRealMoney(totalRealMoney);
            projectVisitorStatistic.setSysId(sysId);
            projectVisitorStatistic.setOrganizationId(organizationId);
            projectVisitorStatistic.setClientType(visitor.getClientType());
            projectVisitorStatistic.setProjectType(projectType);
        } else {
            projectVisitorStatistic.setDataNum(CommonUtil.integerAdd(num, projectVisitorStatistic.getDataNum()));
            projectVisitorStatistic.setRealMoney(CommonUtil.bigDecimalAdd(projectVisitorStatistic.getRealMoney(), totalRealMoney));
            projectVisitorStatistic.setReceivableMoney(CommonUtil.bigDecimalAdd(projectVisitorStatistic.getReceivableMoney(), totalReceivableMoney));
        }
        return projectVisitorStatistic;
    }
}
