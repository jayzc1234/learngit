package com.zxs.server.service.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengContactManDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengContactManListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.GuGengContactMan;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.GuGengContactManMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.user.data.auth.sdk.utils.AreaUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-11-28
 */
@Service
public class GuGengContactManService extends ServiceImpl<GuGengContactManMapper, GuGengContactMan> implements BaseService {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private AreaUtil<GuGengContactMan> areaUtil;

    public void add(GuGengContactManDTO gengContactManDTO) throws SuperCodeException {
        GuGengContactMan entity = new GuGengContactMan();
        BeanUtils.copyProperties(gengContactManDTO,entity);
        areaUtil.getAreaInfoByAreaCode(entity, entity.getAreaCode());
        baseMapper.insert(entity);
    }

    public void update(GuGengContactManDTO gengContactManDTO) throws SuperCodeException {
        Long id = gengContactManDTO.getId();
        if (null==id){
            CommonUtil.throwSuperCodeExtException(500,"主键不能为空");
        }
        GuGengContactMan entity = new GuGengContactMan();
        BeanUtils.copyProperties(gengContactManDTO,entity);
        baseMapper.updateById(entity);
    }

    public IPage<GuGengContactMan> dropDown(GuGengContactManListDTO manListDTO) throws SuperCodeException {
        Page<GuGengContactMan> page = new Page<>(manListDTO.getDefaultCurrent(), manListDTO.getDefaultPageSize());
        QueryWrapper<GuGengContactMan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(null!=manListDTO.getClientId(),GuGengContactMan.COL_CLIENT_ID,manListDTO.getClientId());
        return baseMapper.selectPage(page,queryWrapper);
    }

    public List<GuGengContactMan> selectByClientId(Long clientId) {
        QueryWrapper<GuGengContactMan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(GuGengContactMan.COL_CLIENT_ID,clientId);
        return baseMapper.selectList(queryWrapper);
    }

    public GuGengContactMan addContactManFormOrder(Long clientId, String areaCode, String detailAddress, String contactMan, String contactPhone) {
      if (null==clientId){
          CommonUtil.throwSuperCodeExtException(500,"客户id不能为空");
      }
        QueryWrapper<GuGengContactMan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(GuGengContactMan.COL_CLIENT_ID,clientId);
        queryWrapper.eq(GuGengContactMan.COL_CONTACT_MAN,contactMan);
        queryWrapper.eq(GuGengContactMan.COL_CONTACT_PHONE,contactPhone);
        queryWrapper.eq(GuGengContactMan.COL_AREA_CODE,areaCode);
        queryWrapper.eq(GuGengContactMan.COL_DETAIL_ADDRESS,detailAddress);
        GuGengContactMan guGengContactMan = baseMapper.selectOne(queryWrapper);
        if (null==guGengContactMan){
            guGengContactMan=new GuGengContactMan();
            guGengContactMan.setClientId(clientId);
            guGengContactMan.setDetailAddress(detailAddress);
            guGengContactMan.setContactPhone(contactPhone);
            guGengContactMan.setContactMan(contactMan);
            guGengContactMan.setAreaCode(areaCode);
            areaUtil.getAreaInfoByAreaCode(guGengContactMan, areaCode);
            baseMapper.insert(guGengContactMan);
        }
       return guGengContactMan;
    }
}
