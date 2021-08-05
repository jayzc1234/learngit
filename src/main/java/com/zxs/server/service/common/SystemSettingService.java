package com.zxs.server.service.common;

import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.pojo.common.SystemSetting;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-09-29
 */
public interface SystemSettingService extends IService<SystemSetting> {
    SystemSetting get();

    void update(SystemSetting setting);
}
