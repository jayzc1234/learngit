package com.zxs.server.service.anjiwhitetea;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketManage;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 竹筐管理 服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
public interface BambooBasketManageService extends IService<BambooBasketManage> {

    /**
     * 新增竹筐
     * @param serialNumber
     */
    void add(String serialNumber);

    /**
     * 编辑竹筐
     * @param id
     * @param serialNumber
     */
    void update(Long id, String serialNumber);

    /**
     * 列表接口
     * @param basketListDTO
     * @return
     */
    AbstractPageService.PageResults<List<BambooBasketManage>> pageList(BambooBasketListDTO basketListDTO);

    /**
     * 导出excel
     *
     * @param listDTO
     * @param maxExportNum
     * @param laborManageListVOClass
     * @param response
     * @throws Exception
     */
    void exportExcelList(BambooBasketListDTO listDTO, Integer maxExportNum, Class<BambooBasketManage> laborManageListVOClass, HttpServletResponse response) throws Exception;

    /**
     * 更改竹筐状态
     *
     * @param id
     * @param useStatus
     */
    void changeUseStatus(Long id, Integer useStatus);
}
