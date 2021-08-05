package com.zxs.server.service.anjiwhitetea;

import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.LabourCompany;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-13
 */
public interface LabourCompanyService extends IService<LabourCompany> {
    /**
     * 新增劳务公司
     *
     * @param obj
     */
    void add(LabourCompany obj);

    /**
     * 更新劳务公司
     *
     * @param obj
     */
    void update(LabourCompany obj);

    /**
     * 删除劳务公司
     * @param id
     */
    void delete(Integer id);
}
