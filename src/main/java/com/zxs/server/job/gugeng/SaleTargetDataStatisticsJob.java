package com.zxs.server.job.gugeng;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.EmployeeMsgDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.SaleTaskTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTask;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTaskComparison;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsSaleTargetData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageSaleTaskMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageOrderService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageSaleTaskComparisonService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageSaleTaskService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsSaleTargetDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataStatisticsConstants.SEPARATOR;


/**
 * 销售目标数据统计job
 *
 * @author shixiongfei
 * @date 2019-10-23
 * @since
 */
@Component
@Slf4j
public class SaleTargetDataStatisticsJob {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageSaleTaskService saleTaskService;

    @Autowired
    private ProductionManageSaleTaskMapper saleTaskMapper;

    @Autowired
    private ProductManageOrderService orderService;

    @Autowired
    private ProductionManageStatisticsSaleTargetDataService targetDataService;

    @Autowired
    private ProductionManageSaleTaskComparisonService comparisonService;



    /**
     * 销售目标数据统计
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public void statisticsData() {
        log.info("===========================销售目标数据统计开始==============================");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(superToken -> {
            String sysId = superToken.getSysId();
            String organizationId = superToken.getOrganizationId();
            // 获取开始结束时间
            List<LocalDate> startAndEndDate = LocalDateTimeUtil.getStartAndEndDate(0);
            LocalDate startDate = startAndEndDate.get(0);
            LocalDate endDate = startAndEndDate.get(1);
            List<ProductionManageStatisticsSaleTargetData> list = new ArrayList<>(1000);

            dataSynchronization(startDate.toString(), endDate.plusDays(1).toString(), superToken, list);

            // 移除报损统计数据
            QueryWrapper<ProductionManageStatisticsSaleTargetData> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(StringUtils.isNotBlank(sysId), "sys_id", sysId)
                    .eq(StringUtils.isNotBlank(organizationId), "organization_id", organizationId);
            targetDataService.remove(queryWrapper);

            // 处理结果集
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(data -> {
                    data.setSysId(sysId);
                    data.setOrganizationId(organizationId);
                });
                // 新增报损统计数据
                targetDataService.saveBatch(list);
            }
        });
        log.info("===========================销售目标数据统计执行结束=============================");
    }

    /**
     * 数据处理，以月份作为统计时间，获取目标销售额和实际销售额
     * 此统计过滤掉了部门id + 销售人员id为空的情况，
     * 如果存在此数据，则不会记入
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    private void dataSynchronization(String startDate, String endDate, ProductionManageSuperToken superToken, List<ProductionManageStatisticsSaleTargetData> list) {
        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();
        String token = superToken.getToken();

        // 1.获取所有的部门销售目标列表，因为采收时间可以为未来时间，
        // 所有不经过固定时间获取，直接获取所有, 这里需要防止出现null
        List<ProductionManageStatisticsSaleTargetData> depList = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils
                .elementIsNull(saleTaskService.listDepBySAEDate(sysId, organizationId));

        // 2.获取所有的的个人销售目标列表，理由同上
        List<ProductionManageStatisticsSaleTargetData> perList = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils
                .elementIsNull(saleTaskService.listPerBySAEDate(sysId, organizationId));

        // 3.获取指定时间的订单销售额列表
        List<ProductionManageStatisticsSaleTargetData> saleList = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils
                .elementIsNull(orderService.listByStartAndEndDate(startDate, endDate, sysId, organizationId));

        boolean isTrue = CollectionUtils.isEmpty(depList) && CollectionUtils.isEmpty(perList) && CollectionUtils.isEmpty(saleList);
        if (isTrue) {
            return;
        }

        // 处理个人信息
        List<ProductionManageStatisticsSaleTargetData> perDataList = handlePerData(perList, saleList, token);

        // 获取部门信息map集合
        Map<String, ProductionManageStatisticsSaleTargetData> depMap = parseDep2Map(depList);

        // 获取个人信息map集合
        Map<String, ProductionManageStatisticsSaleTargetData> neDepMap = parseNeDep2Map(perDataList);

        // 处理个人信息中等于部门 + 产品 + 销售时间的部门实际销售额
        List<ProductionManageStatisticsSaleTargetData> depDataList = handleDepData(depMap, neDepMap);

        // 处理个人信息中不完全等于部门 + 产品 + 销售时间的部门实际销售额（也就是说部门目标销售额为空的情况下的实际销售额）
        List<ProductionManageStatisticsSaleTargetData> depNEDataList = handleNEDepData(depMap, neDepMap);

        list.addAll(perDataList);
        list.addAll(depDataList);
        list.addAll(depNEDataList);

        // 过滤掉list中存在目标销售额和实际销售额为0的数据
        list.removeIf(next -> next.getTargetSaleAmount().signum() == 0 && next.getActualSaleAmount().signum() == 0);
    }

    /**
     * 处理不完全等于部门数据
     * 处理个人信息中不完全等于部门 + 产品 + 销售时间的部门实际销售额
     * （也就是说部门目标销售额为空的情况下的实际销售额）
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     */
    private List<ProductionManageStatisticsSaleTargetData> handleNEDepData(Map<String, ProductionManageStatisticsSaleTargetData> depMap, Map<String, ProductionManageStatisticsSaleTargetData> neDepMap) {

        List<ProductionManageStatisticsSaleTargetData> list = new ArrayList<>();
        // 处理不存在现有部门下的销售目标数据
        neDepMap.forEach((k, v) -> {
            if (depMap.containsKey(k)) {
                return;
            }
            list.add(v);
        });

        return list;
    }


    /**
     * 处理部门数据
     * 个人信息等于部门 + 产品 + 销售时间的部门实际销售额
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     */
    private List<ProductionManageStatisticsSaleTargetData> handleDepData(Map<String, ProductionManageStatisticsSaleTargetData> depMap, Map<String, ProductionManageStatisticsSaleTargetData> neDepMap) {
        depMap.forEach((k, v) -> {
            if (!neDepMap.containsKey(k)) {
                return;
            }
            ProductionManageStatisticsSaleTargetData data = neDepMap.get(k);
            v.setActualSaleAmount(data.getActualSaleAmount());
        });

        return new ArrayList<>(depMap.values());
    }

    /**
     * 获取个人信息中的部门map集合
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     */
    private Map<String, ProductionManageStatisticsSaleTargetData> parseNeDep2Map(List<ProductionManageStatisticsSaleTargetData> perDataList) {
        // 将个人信息以部门 + 产品 + 销售时间进行分组聚合, 期间过滤掉已存在的销售部门任务中的部门 + 产品 + 销售时间的信息
        Map<String, ProductionManageStatisticsSaleTargetData> map = perDataList.stream()
                .collect(Collectors.groupingBy(t -> t.getDepartmentId() + SEPARATOR + t.getProductId() + SEPARATOR +
                                DateFormatUtils.format(t.getSaleTargetDate(), LocalDateTimeUtil.YEAR_AND_MONTH),
                        Collectors.collectingAndThen(Collectors.toList(), t -> {
                            ProductionManageStatisticsSaleTargetData data = t.get(0);
                            ProductionManageStatisticsSaleTargetData newData = new ProductionManageStatisticsSaleTargetData();
                            BeanUtils.copyProperties(data, newData);
                            // 统计实际销售额
                            BigDecimal actualAmount = t.stream().map(actual -> Optional.ofNullable(actual.getActualSaleAmount())
                                    .orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
                            newData.setTargetSaleAmount(BigDecimal.ZERO);
                            newData.setActualSaleAmount(actualAmount);
                            newData.setTaskType((int) SaleTaskTypeEnum.DEPARTMENT.getKey());
                            newData.setSalesPersonnelId(StringUtils.EMPTY);
                            newData.setSalesPersonnelName(StringUtils.EMPTY);
                            return newData;
                        })));
        return map;
    }

    /**
     * 获取部门数据map集合
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     */
    private Map<String, ProductionManageStatisticsSaleTargetData> parseDep2Map(List<ProductionManageStatisticsSaleTargetData> depList) {
        // 对部门数据进行分组
        Map<String, ProductionManageStatisticsSaleTargetData> depMap = depList.stream()
                .collect(Collectors.toMap(t -> t.getDepartmentId() + SEPARATOR + t.getProductId() + SEPARATOR
                        + DateFormatUtils.format(t.getSaleTargetDate(), LocalDateTimeUtil.YEAR_AND_MONTH), t -> t));
        return depMap;
    }


    /**
     * 处理部门数据
     * 个人信息等于部门 + 产品 + 销售时间的部门实际销售额
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     */
    @Deprecated
    private List<ProductionManageStatisticsSaleTargetData> handleDepData(List<ProductionManageStatisticsSaleTargetData> depList, List<ProductionManageStatisticsSaleTargetData> perDataList) {
        // 1. 获取当天的部门实际销售额信息并设置进目标销售目标列表中, 统计个人中符合部门 + 产品 + 销售时间的实际销售额
        depList.forEach(dep -> {
            BigDecimal actualSaleAmount = perDataList.stream()
                    // 保证产品id + 部门id + 年月相同
                    .filter(t -> dep.getDepartmentId().equals(t.getDepartmentId())
                            && dep.getProductId().equals(t.getProductId())
                            && DateFormatUtils.format(dep.getSaleTargetDate(), LocalDateTimeUtil.YEAR_AND_MONTH)
                            .equals(DateFormatUtils.format(t.getSaleTargetDate(), LocalDateTimeUtil.YEAR_AND_MONTH)))
                    .map(data -> Optional.ofNullable(data.getActualSaleAmount()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // 设置部门的实际销售额
            dep.setActualSaleAmount(actualSaleAmount);
        });

        return depList;
    }

    /**
     * 获取个人的实际销售额
     * 这里的处理逻辑是：个人销售任务列表中和订单销售额列表如果存在销售人员id
     * 和产品id相同则汇聚为一条数据不同则分为2条不同的数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     */
    private List<ProductionManageStatisticsSaleTargetData> handlePerData(List<ProductionManageStatisticsSaleTargetData> perList, List<ProductionManageStatisticsSaleTargetData> saleList, String token) {
        // 校验集合不可为空
        Collection<ProductionManageStatisticsSaleTargetData> values = Stream.of(perList, saleList).flatMap(Collection::stream)
                // 按照销售人员 + 产品id + 年月进行分组
                .collect(Collectors.groupingBy(t -> t.getSalesPersonnelId() + SEPARATOR + t.getProductId() + SEPARATOR +
                                DateFormatUtils.format(t.getSaleTargetDate(), LocalDateTimeUtil.YEAR_AND_MONTH),
                        Collectors.collectingAndThen(Collectors.toList(), result -> {
                            ProductionManageStatisticsSaleTargetData msg = result.get(0);
                            // 统计每组下的目标销售额
                            BigDecimal targetSaleAmount = result.stream()
                                    .filter(data -> Objects.nonNull(data.getTaskType()))
                                    .map(data -> Optional.ofNullable(data.getTargetSaleAmount()).orElse(BigDecimal.ZERO))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            // 统计每组下的实际销售额
                            BigDecimal actualSaleAmount = result.stream()
                                    .filter(data -> Objects.isNull(data.getTaskType()))
                                    .map(data -> Optional.ofNullable(data.getActualSaleAmount()).orElse(BigDecimal.ZERO))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            msg.setTargetSaleAmount(targetSaleAmount);
                            msg.setActualSaleAmount(actualSaleAmount);

                            msg.setTaskType((int) SaleTaskTypeEnum.PERSONAL.getKey());
                            // 通过销售人员获取部门信息
                            EmployeeMsgDTO employeeMsg = commonUtil.getEmployeeMsg(token, msg.getSalesPersonnelId());
                            if (Objects.nonNull(employeeMsg)) {
                                msg.setDepartmentId(employeeMsg.getDepartmentId());
                                msg.setDepartmentName(employeeMsg.getDepartmentName());
                            }
                            return msg;
                        }))).values();

        return new ArrayList<>(values);
    }

    /**
     * 个人销售任务部门信息同步，
     * 仅调用一次即可
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void personalDataSynchronization() {
        log.info("个人销售任务部门信息同步执行开始");
        // 获取所有系统token
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();

        superTokenList.forEach(superToken -> {
            String sysId = superToken.getSysId();
            String organizationId = superToken.getOrganizationId();
            String token = superToken.getToken();

            // 获取所有的销售人员id集合
            List<String> ids = Optional.ofNullable(saleTaskMapper.listPersonnelIds())
                    .orElse(Collections.emptyList());

            ids.forEach(id -> {
                EmployeeMsgDTO msg = commonUtil.getEmployeeMsg(token, id);
                if (Objects.isNull(msg)) {
                    return;
                }

                String departmentId = msg.getDepartmentId();
                String departmentName = msg.getDepartmentName();

                // 数据同步
                UpdateWrapper<ProductionManageSaleTask> wrapper = new UpdateWrapper<>();
                wrapper.set("DepartmentId", departmentId)
                        .set("DepartmentName", departmentName)
                        .eq(org.apache.commons.lang.StringUtils.isNotBlank(sysId), "SysId", sysId)
                        .eq(org.apache.commons.lang.StringUtils.isNotBlank(organizationId), "OrganizationId", organizationId)
                        .eq("SalesPersonnelId", id);
                saleTaskMapper.update(null, wrapper);
            });
        });

        log.info("个人销售任务部门信息同步执行结束");
    }

    /**
     * 销售任务对比数据同步
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void saleTaskComparisonDataSynchronization() {
        log.info("===========================销售任务对比数据同步开始==============================");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();

        superTokenList.forEach(superToken -> {
            String sysId = superToken.getSysId();
            String organizationId = superToken.getOrganizationId();

            // 获取所有销售任务
            QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(StringUtils.isNotBlank(sysId), "SysId", sysId)
                    .eq(StringUtils.isNotBlank(organizationId), "OrganizationId", organizationId);
            List<ProductionManageSaleTask> saleTasks = saleTaskMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(saleTasks)) {
                return;
            }
            // 对销售任务进行分组，以部门 + 销售时间（年月）+ 产品作为唯一标识
            Collection<ProductionManageSaleTaskComparison> values = saleTasks.stream().collect(Collectors.groupingBy(t -> t.getDepartmentId() + t.getProductId() +
                            DateFormatUtils.format(t.getSaleDate(), LocalDateTimeUtil.YEAR_AND_MONTH),
                    Collectors.collectingAndThen(Collectors.toList(), list -> {

                        ProductionManageSaleTask saleTask = list.get(0);
                        ProductionManageSaleTaskComparison comparison = new ProductionManageSaleTaskComparison();
                        BeanUtils.copyProperties(saleTask, comparison);

                        // 获取部门集合
                        List<ProductionManageSaleTask> depList = list.stream().filter(t -> SaleTaskTypeEnum.DEPARTMENT.getKey() == t.getTaskType())
                                .collect(Collectors.toList());
                        // 获取部门目标销售额总和
                        double depTotalAmount = depList.stream().mapToDouble(ProductionManageSaleTask::getTargetSaleAmount).sum();
                        // 获取个人集合
                        List<ProductionManageSaleTask> perList = list.stream().filter(t -> SaleTaskTypeEnum.PERSONAL.getKey() == t.getTaskType())
                                .collect(Collectors.toList());
                        // 获取个人目标销售额总和
                        double perTotalAmount = perList.stream().mapToDouble(ProductionManageSaleTask::getTargetSaleAmount).sum();

                        comparison.setDepSaleTargetAmount(BigDecimal.valueOf(depTotalAmount));
                        comparison.setPerSaleTargetAmount(BigDecimal.valueOf(perTotalAmount));

                        return comparison;
                    }))
            ).values();

            List<ProductionManageSaleTaskComparison> results = new ArrayList<>(values);
            // 移除所有销售任务对比数据
            QueryWrapper<ProductionManageSaleTaskComparison> wrapper = new QueryWrapper<>();
            wrapper.eq(StringUtils.isNotBlank(sysId), "sys_id", sysId)
                    .eq(StringUtils.isNotBlank(organizationId), "organization_id", organizationId);
            comparisonService.remove(wrapper);
            // 批量新增销售任务对比
            comparisonService.saveBatch(results);
        });

        log.info("===========================销售任务对比数据同步结束==============================");
    }
}