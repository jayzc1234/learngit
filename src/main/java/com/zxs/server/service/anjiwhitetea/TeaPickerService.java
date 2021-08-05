package com.zxs.server.service.anjiwhitetea;

import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.TeaPicker;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-13
 */
public interface TeaPickerService extends IService<TeaPicker> {

    /**
     * 新增采茶工
     *
     * @param obj
     */
    void add(TeaPicker obj);

    /**
     * 更新采茶工
     *
     * @param obj
     */
    void update(TeaPicker obj);

    /**
     * 删除采茶工
     *
     * @param id
     */
    void delete(Integer id);
}
