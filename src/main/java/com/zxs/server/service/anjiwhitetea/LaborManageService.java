package com.zxs.server.service.anjiwhitetea;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageListDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageUpdateDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.LaborManage;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.LaborManageListVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 用工管理 服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
public interface LaborManageService extends IService<LaborManage> {

    /**
     * 用工新增
     * @param laborManageAddDTO
     */
    void add(LaborManageAddDTO laborManageAddDTO);

    /**
     * 用工编辑
     * @param updateDTO
     */
    void update(LaborManageUpdateDTO updateDTO);

    /**
     * 获取用工列表
     * @param laborManageListDTO
     * @return
     */
    AbstractPageService.PageResults<List<LaborManageListVO>> pageList(LaborManageListDTO laborManageListDTO);

    /**
     *导出excel
     * @param listDTO
     * @param maxExportNum
     * @param laborManageListVOClass
     * @param response
     */
    void exportExcelList(LaborManageListDTO listDTO, Integer maxExportNum, Class<LaborManageListVO> laborManageListVOClass, HttpServletResponse response) throws Exception;
}
