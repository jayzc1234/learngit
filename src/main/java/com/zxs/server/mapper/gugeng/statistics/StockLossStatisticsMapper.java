package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DepartmentRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ExportStockLossStatisticsPageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossPageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.StockLossOperateTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStockLossDayStatistics;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockLoss;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 库存报损数据统计mapper
 * @author shixiongfei
 * @date 2019-09-18
 * @since
 */
public interface StockLossStatisticsMapper extends BaseMapper<ProductionManageStockLoss> {


    /**
     * 获取库存报损分析分页数据信息列表
     *
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param requestDTO 库存报损分页数据请求dto
     * @param sysId 系统id
     * @param organizationId 部门id
     * @return 库存报损pojo实体类集合
     */
    @Deprecated
    default IPage<ProductionManageStockLoss> list(SearchStockLossPageRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStockLoss> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        // 获取部门id集合（这里是为了防止出现npe错误）
        QueryWrapper<ProductionManageStockLoss> wrapper = new QueryWrapper<>();
        // 通过批次类型来获取库存报损的分拣类型
        List<Integer> types = getType(requestDTO.getType());
        wrapper.select(ProductionManageStockLoss.COL_TYPE + " AS type", ProductionManageStockLoss.COL_PLANT_BATCH_ID + " AS plantBatchId", ProductionManageStockLoss.COL_PLANT_BATCH_NAME + " AS plantBatchName",
                ProductionManageStockLoss.COL_DEPARTMENT_ID + " AS departmentId", ProductionManageStockLoss.COL_DEPARTMENT_NAME + " AS departmentName", "SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + ") AS damageWeight")
                .eq(StringUtils.isNotBlank(sysId), ProductionManageStockLoss.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStockLoss.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageStockLoss.COL_OPERATE_TYPE, StockLossOperateTypeEnum.STOCK_LOSS.getKey())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStockLoss.COL_DAMAGE_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStockLoss.COL_DAMAGE_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1))
                .in(Objects.nonNull(types), ProductionManageStockLoss.COL_TYPE, types)
                .in(CollectionUtils.isNotEmpty(requestDTO.getPlantBatchIds()), ProductionManageStockLoss.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchIds())
                .groupBy(ProductionManageStockLoss.COL_AUTH_DEPARTMENT_ID, ProductionManageStockLoss.COL_PLANT_BATCH_ID);

        return selectPage(page, wrapper);
    }

    /**
     * 获取库存报损数据统计集合
     *
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param requestDTO 库存报损统计请求dto
     * @param sysId 系统id
     * @param organizationId 部门id
     * @return 库存报损pojo实体类集合
     */
    @Deprecated
    default List<ProductionManageStockLoss> listStatistics(SearchStockLossStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        // 通过批次类型来获取库存报损的分拣类型
        List<Integer> types = getType(requestDTO.getType());

        QueryWrapper<ProductionManageStockLoss> wrapper = new QueryWrapper<>();
        wrapper.select("DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ",'%Y-%m-%d') AS damageDate", ProductionManageStockLoss.COL_DEPARTMENT_ID + " AS departmentId",
                ProductionManageStockLoss.COL_DEPARTMENT_NAME + " AS departmentName", "SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + ") AS damageWeight")
                .eq(StringUtils.isNotBlank(sysId), ProductionManageStockLoss.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStockLoss.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageStockLoss.COL_OPERATE_TYPE, StockLossOperateTypeEnum.STOCK_LOSS.getKey())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStockLoss.COL_DAMAGE_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStockLoss.COL_DAMAGE_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1))
                .in(Objects.nonNull(types), ProductionManageStockLoss.COL_TYPE, types)
                .in(CollectionUtils.isNotEmpty(requestDTO.getPlantBatchIds()), ProductionManageStockLoss.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchIds())
                .groupBy(ProductionManageStockLoss.COL_AUTH_DEPARTMENT_ID, "DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ",'%Y-%m-%d')")
                // 按照时间升序，加快图表响应体的生成速度
                .orderByAsc("DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ", '%Y-%m-%d')");

        return selectList(wrapper);
    }

    /**
     * 通过批次类型获取分拣类型
     *
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param type 批次类型
     * @return
     */
    @Deprecated
    default List<Integer> getType(Byte type) {
        // 通过批次类型来获取库存报损的分拣类型
        List<Integer> types = null;
        if (Objects.nonNull(type)) {
            // 批次类型为1 => 种植批次 2 => 外采批次
            // 分拣类型 0，2 => 外采分拣 1，3 =>采收分拣（具体可查看枚举类 StockFlowDetailTypeEnum）
            // 这里的类型采用了魔法值，不建议如此使用，可在后期优化
            types = type == 1 ? Stream.of(1, 3).collect(Collectors.toList()) : Stream.of(0, 2).collect(Collectors.toList());
        }

        return types;
    }

    /**
     * 获取责任方id集合
     *
     * @author shixiongfei
     * @date 2019-09-23
     * @updateDate 2019-09-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    default List<String> getDepartmentIds(List<DepartmentRequestDTO> list) {
        // 获取部门id集合（这里是为了防止出现npe错误）
        return Optional.ofNullable(list).map(departments ->
                departments.stream().map(department -> department.getDepartmentId()).collect(Collectors.toList())).orElse(null);
    }

    /**
     * 通过唯一标识id集合来获取库存报损列表
     *
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param ids 唯一标识id集合

     * @param sysId 系统id
     * @param organizationId 组织id
     * @return
     */
    @Deprecated
    default List<ProductionManageStockLoss> excelByIds(List<String> ids, ExportStockLossStatisticsPageRequestDTO requestDTO, String sysId, String organizationId) {
        List<Integer> types = getType(requestDTO.getType());
        QueryWrapper<ProductionManageStockLoss> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(ProductionManageStockLoss.COL_TYPE + " AS type", ProductionManageStockLoss.COL_PLANT_BATCH_ID + " AS plantBatchId", ProductionManageStockLoss.COL_PLANT_BATCH_NAME + " AS plantBatchName",
                ProductionManageStockLoss.COL_DEPARTMENT_ID + " AS departmentId", ProductionManageStockLoss.COL_DEPARTMENT_NAME + " AS departmentName", "SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + ") AS damageWeight")
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStockLoss.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageStockLoss.COL_SYS_ID, sysId)
                .eq(ProductionManageStockLoss.COL_OPERATE_TYPE, StockLossOperateTypeEnum.STOCK_LOSS.getKey())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStockLoss.COL_DAMAGE_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStockLoss.COL_DAMAGE_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1))
                .in(Objects.nonNull(types), ProductionManageStockLoss.COL_TYPE, types)
                .and(wrapper -> {
                    ids.forEach(id -> {
                        String[] split = id.split(":::");
                        wrapper.or(wrap -> wrap.eq(ProductionManageStockLoss.COL_PLANT_BATCH_ID, split[0]).eq(ProductionManageStockLoss.COL_DEPARTMENT_ID, split[1]));
                    });
                    return wrapper;
                })
                .groupBy(ProductionManageStockLoss.COL_DEPARTMENT_ID, ProductionManageStockLoss.COL_PLANT_BATCH_ID)
                // 按库存报损重量降序
                .orderByDesc(ProductionManageStockLoss.COL_DAMAGE_WEIGHT);
        return selectList(queryWrapper);
    }

    @Select("SELECT DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ",'%Y-%m-%d') AS damageDate,SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + ") AS damageWeight \n" +
            "FROM t_production_manage_stock_loss\n" +
            "${ew.customSqlSegment} \n" +
            "GROUP BY DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ",'%Y-%m-%d') ")
    List<ProductionManageStockLossDayStatistics> listStatisticsStatistics(@Param(Constants.WRAPPER) Wrapper queryWrapper);

}