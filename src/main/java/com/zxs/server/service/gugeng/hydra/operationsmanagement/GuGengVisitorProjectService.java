package com.zxs.server.service.gugeng.hydra.operationsmanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengVisitorProject;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.operationsmanagement.GuGengVisitorProjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-02-24
 */
@Service
public class GuGengVisitorProjectService extends ServiceImpl<GuGengVisitorProjectMapper, GuGengVisitorProject> {

    /**
        添加 
    */
    public String addGuGengVisitorProject(GuGengVisitorProject model){
        GuGengVisitorProject entity = new GuGengVisitorProject();
        BeanUtils.copyProperties(model,entity);
        save(entity);
        return null;
    }

    /**
          修改 
     */
     public void updateGuGengVisitorProject(GuGengVisitorProject model){
      GuGengVisitorProject entity = new GuGengVisitorProject();
      BeanUtils.copyProperties(model,entity);
      updateById(entity);
     }

     /**
           获取  详情
     */
    public GuGengVisitorProject getGuGengVisitorProject(String id){
         GuGengVisitorProject entity = getOne(new QueryWrapper<GuGengVisitorProject>().eq("id",id));
         if(entity!=null){
            GuGengVisitorProject result = new GuGengVisitorProject();
            BeanUtils.copyProperties(entity,result);
            return result;
         }
        return null;
     }


}
