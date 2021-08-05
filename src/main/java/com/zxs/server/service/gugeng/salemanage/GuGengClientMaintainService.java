package com.zxs.server.service.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengClientMaintainDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengClientMaintainListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.GuGengClientMaintain;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.GuGengContactMan;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.GuGengClientMaintainMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.GuGengClientMaintainDetailVO;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorEmployee;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.CLIENT_MAINTAIN;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-28
 */
@Service
public class GuGengClientMaintainService extends ServiceImpl<GuGengClientMaintainMapper, GuGengClientMaintain> implements BaseService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;


    public void add(GuGengClientMaintainDTO gengClientMaintainDTO) throws SuperCodeException {
        GuGengClientMaintain entity = new GuGengClientMaintain();
        BeanUtils.copyProperties(gengClientMaintainDTO, entity);
        InterceptorEmployee employeeInfo = commonUtil.getEmployeeInfo();
        entity.setCreateUserName(employeeInfo.getEmployeeName());
        entity.setCreateUserId(employeeInfo.getEmployeeId());
        entity.setAuthDepartmentId(employeeInfo.getDepartmentId());
        entity.setCreateDate(CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS));
        baseMapper.insert(entity);
    }

    public void update(GuGengClientMaintainDTO gengClientMaintainDTO) throws SuperCodeException {
        if (null == gengClientMaintainDTO.getId()) {
            CommonUtil.throwSuperCodeExtException(500, "主键不能为空");
        }
        GuGengClientMaintain entity = new GuGengClientMaintain();
        BeanUtils.copyProperties(gengClientMaintainDTO, entity);
        baseMapper.updateById(entity);
    }

    @Override
    public IPage<GuGengClientMaintainDetailVO> pageList(DaoSearch daoSearch) {
        GuGengClientMaintainListDTO clientMaintainListDTO = (GuGengClientMaintainListDTO) daoSearch;
        Page<GuGengClientMaintain> page = new Page<>(clientMaintainListDTO.getDefaultCurrent(), clientMaintainListDTO.getDefaultPageSize());
        QueryWrapper<GuGengClientMaintain> queryWrapper = commonUtil.queryTemplate(GuGengClientMaintain.class);
        queryWrapper.eq(!Objects.isNull(clientMaintainListDTO.getClientId()), "cm."+GuGengClientMaintain.COL_CLIENT_ID, clientMaintainListDTO.getClientId());
        queryWrapper.orderByDesc("cm.Id");
        String search = clientMaintainListDTO.getSearch();
        if (StringUtils.isNotBlank(search)){
            queryWrapper.and(outorder -> outorder.like(GuGengClientMaintain.COL_CLIENT_NAME, search)
                    .or().like(GuGengContactMan.COL_DETAIL_ADDRESS, search)
                    .or().like("gcm."+GuGengContactMan.COL_CONTACT_MAN, search)
                    .or().like("gcm."+GuGengContactMan.COL_CONTACT_PHONE, search)
                    .or().like("cm."+GuGengClientMaintain.COL_MAINTAIN_CONTENT, search)
                    .or().like("cm."+GuGengClientMaintain.COL_MAINTAIN_PROJECT, search)
            );
        }else {

            String maintainDate = clientMaintainListDTO.getMaintainDate();
            String[] strings = LocalDateTimeUtil.substringDate(maintainDate);

            queryWrapper.eq(StringUtils.isNotBlank(clientMaintainListDTO.getProvinceName()),GuGengContactMan.COL_PROVINCE_NAME,clientMaintainListDTO.getProvinceName());
            queryWrapper.eq(StringUtils.isNotBlank(clientMaintainListDTO.getCityName()),GuGengContactMan.COL_CITY_NAME,clientMaintainListDTO.getCityName());
            queryWrapper.eq(StringUtils.isNotBlank(clientMaintainListDTO.getCountyName()),GuGengContactMan.COL_COUNTY_NAME,clientMaintainListDTO.getCountyName());
            queryWrapper.eq(StringUtils.isNotBlank(clientMaintainListDTO.getClientName()),GuGengClientMaintain.COL_CLIENT_NAME,clientMaintainListDTO.getClientName());
            queryWrapper.eq(StringUtils.isNotBlank(clientMaintainListDTO.getContactMan()),"gcm."+GuGengContactMan.COL_CONTACT_MAN,clientMaintainListDTO.getContactMan());
            queryWrapper.eq(StringUtils.isNotBlank(clientMaintainListDTO.getContactPhone()),"gcm."+GuGengContactMan.COL_CONTACT_PHONE,clientMaintainListDTO.getContactPhone());

            queryWrapper.eq(StringUtils.isNotBlank(clientMaintainListDTO.getMaintainEmployeeName()),"cm."+GuGengClientMaintain.COL_MAINTAIN_EMPLOYEE_NAME,clientMaintainListDTO.getMaintainEmployeeName());

            queryWrapper.ge(StringUtils.isNotBlank(strings[0]),"cm."+GuGengClientMaintain.COL_MAINTAIN_DATE,strings[0]);
            queryWrapper.le(StringUtils.isNotBlank(strings[1]),"cm."+GuGengClientMaintain.COL_MAINTAIN_DATE,strings[1]);
        }
        // 添加数据权限
        commonUtil.roleDataAuthFilter(CLIENT_MAINTAIN, queryWrapper, "cm."+GuGengClientMaintain.COL_CREATE_USER_ID, StringUtils.EMPTY);

        IPage<GuGengClientMaintainDetailVO> iPage = baseMapper.pageList(page, queryWrapper);
        return iPage;
    }

    public GuGengClientMaintainDetailVO getDetailById(Long id) {
        return baseMapper.getDetailById(id);
    }

    public void deleteById(Long id) {
        baseMapper.deleteById(id);
    }

}
