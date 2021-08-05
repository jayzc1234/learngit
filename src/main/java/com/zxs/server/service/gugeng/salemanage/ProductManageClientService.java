package com.zxs.server.service.gugeng.salemanage;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.ProductionManageClientMO;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.HttpUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.QueryMapperUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.ExcelImportDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengContactManDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageClientDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageClientListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.GuGengContactMan;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClientCategory;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ClientCategoryMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.OrderImportBaseInfoCommonService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageSaleClientNumStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductManageClientListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageClientDetailVO;
import net.app315.hydra.user.data.auth.sdk.component.RoleDataAuthComponent;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorEmployee;
import net.app315.hydra.user.data.auth.sdk.utils.AreaUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.EofSensorInputStream;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.ORDER_CLIENT;
import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.POTENTIAL_CLIENT;

@Slf4j
@Service
public class ProductManageClientService extends ServiceImpl<ProductManageClientMapper,ProductionManageClient> implements BaseService<ProductManageClientListVO> {
    @Autowired
    private ProductManageClientMapper dao;

    @Autowired
    private ClientCategoryMapper clientCategoryDao;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private AreaUtil areaUtil;

    @Value("${qiniu.upload.domain}")
    private String OSS_DOMAIN;

    @Autowired
    private ProductManageClientImportService clientImportService;

    @Autowired
    private ProductionManageSaleClientNumStatisticsService saleClientNumStatisticsService;

    @Autowired
    private GuGengContactManService contactManService;

    @Autowired
    private OrderImportBaseInfoCommonService baseInfoCommonService;

    @Autowired
    private RoleDataAuthComponent authComponent;

    @Transactional(rollbackFor = Exception.class)
    public ProductionManageClient add(ProductManageClientDTO clientDTO) throws SuperCodeException, ParseException {
        ProductionManageClient existClient=checkExistClient(clientDTO,true);
        if (null!=existClient){
            CommonUtil.throwSupercodeException(500,"客户已存在");
        }
        ProductionManageClient pManageClient = addProductionManageClient(clientDTO);
        return pManageClient;
    }

    private ProductionManageClient addProductionManageClient(ProductManageClientDTO clientDTO) throws SuperCodeException, ParseException {
        ProductionManageClientCategory category=clientCategoryDao.selectById(clientDTO.getCategoryId());
        if (null==category) {
            throw new SuperCodeException("客户类目不存在", 500);
        }
        //保存古耕联系人
        List<GuGengContactManDTO> contactManDTOList = clientDTO.getContactManDTOList();
        if (CollectionUtils.isEmpty(contactManDTOList)){
            throw new SuperCodeException("客户联系人不能为空", 500);
        }
        Employee employee=commonUtil.getEmployee();
        ProductionManageClient pManageClient=new ProductionManageClient();
        pManageClient.setCategoryName(category.getCategoryName());
        pManageClient.setCategoryId(category.getId());
        pManageClient.setClientName(clientDTO.getClientName());
        pManageClient.setOperator(employee.getEmployeeId());
        pManageClient.setAuthDepartmentId(employee.getDepartmentId());
        pManageClient.setOrganizationId(commonUtil.getOrganizationId());
        pManageClient.setSysId(commonUtil.getSysId());
        Date currentDate = CommonUtil.getCurrentDate(null);
        pManageClient.setCreateDate(currentDate);
        pManageClient.setClientType(0);
        pManageClient.setEstimateSalesDate(clientDTO.getEstimateSalesDate());
        pManageClient.setSaleUserId(clientDTO.getSaleUserId());
        pManageClient.setSaleUserName(clientDTO.getSaleUserName());
        pManageClient.setFoEstimateSales(clientDTO.getFoEstimateSales());
        pManageClient.setClientProfile(clientDTO.getClientProfile());
        //古耕新增逻辑
        pManageClient.setGgBirthDay(clientDTO.getGgBirthDay());
        pManageClient.setGgCustomersFond(clientDTO.getGgCustomersFond());
        pManageClient.setGgDietTaboos(clientDTO.getGgDietTaboos());

        GuGengContactManDTO guGengContactManDTO = contactManDTOList.get(0);
        pManageClient.setContactMan(guGengContactManDTO.getContactMan());
        pManageClient.setContactPhone(guGengContactManDTO.getContactPhone());
        pManageClient.setDetailAddress(guGengContactManDTO.getDetailAddress());
        pManageClient.setAreaCode(guGengContactManDTO.getAreaCode());
        areaUtil.getAreaInfoByAreaCode(pManageClient,guGengContactManDTO.getAreaCode());
        dao.insert(pManageClient);

        List<GuGengContactMan> contactManList=new ArrayList<>();
        contactManDTOList.forEach(con->{
            GuGengContactMan contactMan=new GuGengContactMan();
            BeanUtils.copyProperties(con,contactMan);
            contactMan.setClientId(pManageClient.getId());
            areaUtil.getAreaInfoByAreaCode(contactMan,contactMan.getAreaCode());
            contactManList.add(contactMan);
        });
        contactManService.saveBatch(contactManList);
        //新增潜在及订单客户转化率统计
        saleClientNumStatisticsService.updatePotentialClientNum(clientDTO.getSaleUserId(),clientDTO.getSaleUserName(),1);
        return pManageClient;
    }

    /**
     * 订单新增客户
     * @param clientDTO
     * @return
     * @throws SuperCodeException
     * @throws ParseException
     */
    public ProductionManageClient orderAddOrReturn(ProductManageClientDTO clientDTO) throws SuperCodeException, ParseException {
        ProductionManageClient productionManageClient = existClient(clientDTO.getId(), clientDTO.getCategoryId(), clientDTO.getClientName(), clientDTO.getContactMan(), clientDTO.getContactPhone(), clientDTO.getAreaCode());
        if (null!=productionManageClient){
            return productionManageClient;
        }
        List<GuGengContactManDTO> contactManDTOList = new ArrayList<>();
        GuGengContactManDTO guGengContactManDTO=new GuGengContactManDTO();
        BeanUtils.copyProperties(clientDTO,guGengContactManDTO);
        contactManDTOList.add(guGengContactManDTO);
        clientDTO.setContactManDTOList(contactManDTOList);
        return  addProductionManageClient(clientDTO);
    }

    /**
     * 新增客户
     * @param clientDTO
     * @return
     * @throws SuperCodeException
     * @throws ParseException
     */
    public ProductionManageClient addOrReturn(ProductManageClientDTO clientDTO) throws SuperCodeException, ParseException {
        ProductionManageClient productionManageClient = existClient(clientDTO.getId(), clientDTO.getCategoryId(), clientDTO.getClientName(), clientDTO.getContactMan(), clientDTO.getContactPhone(), clientDTO.getAreaCode());
        if (null!=productionManageClient){
            CommonUtil.throwSuperCodeExtException(500,"客户："+clientDTO.getClientName()+"已存在");
        }
        return  addProductionManageClient(clientDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ProductManageClientDTO clientDTO) throws SuperCodeException, ParseException {
        if (null==clientDTO.getId()) {
            throw new SuperCodeException("更新时id不能为空", 500);
        }
        ProductionManageClient existClient=checkExistClient(clientDTO,false);
        if (null!=existClient){
            CommonUtil.throwSupercodeException(500,"客户已存在");
        }
        List<GuGengContactManDTO> contactManDTOList = clientDTO.getContactManDTOList();
        if (CollectionUtils.isEmpty(contactManDTOList)){
            throw new SuperCodeException("客户联系人不能为空", 500);
        }
        ProductionManageClient pManageClient=new ProductionManageClient();
        pManageClient.setId(clientDTO.getId());
        pManageClient.setCategoryId(clientDTO.getCategoryId());
        pManageClient.setCategoryName(clientDTO.getCategoryName());
        pManageClient.setClientName(clientDTO.getClientName());
        pManageClient.setOrganizationId(commonUtil.getOrganizationId());
        pManageClient.setSysId(commonUtil.getSysId());
        InterceptorEmployee employee = commonUtil.getEmployeeInfo();
        pManageClient.setUpdateUserId(employee.getEmployeeId());
        pManageClient.setUpdateDate(CommonUtil.getCurrentDate(null));
        pManageClient.setEstimateSalesDate(clientDTO.getEstimateSalesDate());
        pManageClient.setSaleUserId(clientDTO.getSaleUserId());
        pManageClient.setSaleUserName(clientDTO.getSaleUserName());
        pManageClient.setFoEstimateSales(clientDTO.getFoEstimateSales());
        pManageClient.setClientProfile(clientDTO.getClientProfile());

        //古耕新增逻辑
        pManageClient.setGgBirthDay(clientDTO.getGgBirthDay());
        pManageClient.setGgCustomersFond(clientDTO.getGgCustomersFond());
        pManageClient.setGgDietTaboos(clientDTO.getGgDietTaboos());
        dao.updateById(pManageClient);

        //保存古耕联系人
        GuGengContactManDTO guGengContactManDTO = contactManDTOList.get(0);
        pManageClient.setContactMan(guGengContactManDTO.getContactMan());
        pManageClient.setContactPhone(guGengContactManDTO.getContactPhone());
        pManageClient.setDetailAddress(guGengContactManDTO.getDetailAddress());
        pManageClient.setAreaCode(guGengContactManDTO.getAreaCode());
        areaUtil.getAreaInfoByAreaCode(pManageClient, guGengContactManDTO.getAreaCode());
        dao.updateById(pManageClient);

        List<Long> deleteContactManIds = clientDTO.getDeleteContactManIds();
        if (CollectionUtils.isNotEmpty(deleteContactManIds)){
            contactManService.removeByIds(deleteContactManIds);
        }

        List<GuGengContactMan> contactManList=new ArrayList<>();
        contactManDTOList.forEach(con->{
            GuGengContactMan contactMan=new GuGengContactMan();
            BeanUtils.copyProperties(con,contactMan);
            contactMan.setClientId(pManageClient.getId());
            areaUtil.getAreaInfoByAreaCode(contactMan,contactMan.getAreaCode());
            contactManList.add(contactMan);
        });
        contactManService.saveOrUpdateBatch(contactManList);
    }

    private ProductionManageClient checkExistClient(ProductManageClientDTO clientDTO, boolean isAdd) {
        Map<String, Object> columMap= QueryMapperUtil.singleQueryColumnMap(ProductionManageClient.COL_CLIENT_NAME,clientDTO.getClientName(), commonUtil.getOrganizationId(), commonUtil.getSysId());
        columMap.put(ProductionManageClient.COL_CATEGORY_ID,clientDTO.getCategoryId());
        columMap.put(ProductionManageClient.COL_CONTACT_PHONE,clientDTO.getContactPhone());
        columMap.put(ProductionManageClient.COL_CONTACT_MAN,clientDTO.getContactMan());
        columMap.put(ProductionManageClient.COL_AREA_CODE,clientDTO.getAreaCode());
        List<ProductionManageClient> list=dao.selectByMap(columMap);
        if (null!=list && !list.isEmpty()) {
            if (isAdd){
                return list.get(0);
            }else{
                for (ProductionManageClient client:list) {
                    boolean exist=client.getId().equals(clientDTO.getId());
                    if (!exist){
                        return client;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public PageResults<List<ProductManageClientListVO>> page(DaoSearch daosearch) {
        ProductManageClientListDTO clientListDTO=(ProductManageClientListDTO) daosearch;
        log.info("获取到高级搜索参数："+JSONObject.toJSONString(clientListDTO));
        if (null==clientListDTO.getClientType()){
            CommonUtil.throwSuperCodeExtException(500,"客户类型不能为空");
        }
        Page<ProductManageClientListVO> page = new Page<>(clientListDTO.getDefaultCurrent(), clientListDTO.getDefaultPageSize());
        String roleFunAuthSqlByAuthCode = pageParamSet(clientListDTO);
        IPage<ProductManageClientListVO> ipage=dao.list(page, clientListDTO,roleFunAuthSqlByAuthCode);
        PageResults<List<ProductManageClientListVO>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    /**
     * 小程序分页
     * @param clientListDTO
     * @return
     */
    public PageResults<List<ProductManageClientListVO>> mobilePage(ProductManageClientListDTO clientListDTO) {
        Page<ProductManageClientListVO> page = new Page<>(clientListDTO.getDefaultCurrent(), clientListDTO.getDefaultPageSize());
        String roleFunAuthSqlByAuthCode = pageParamSet(clientListDTO);
        IPage<ProductManageClientListVO> ipage=dao.mobilePage(page, clientListDTO,roleFunAuthSqlByAuthCode);
        PageResults<List<ProductManageClientListVO>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    public PageResults<List<ProductManageClientListVO>> dropPage(ProductManageClientListDTO clientListDTO) throws SuperCodeException {
        Page<ProductManageClientListVO> page = new Page<>(clientListDTO.getDefaultCurrent(), clientListDTO.getDefaultPageSize());
        clientListDTO.setOrganizationId(commonUtil.getOrganizationId());
        clientListDTO.setSysId(commonUtil.getSysId());

        String dateInteral[]= LocalDateTimeUtil.substringDate(clientListDTO.getOrderDate());
        clientListDTO.setStartOrderDate(dateInteral[0]);
        clientListDTO.setEndOrderDate(dateInteral[1]);

        String estimateSalesDatedateInteral[]=LocalDateTimeUtil.substringDate(clientListDTO.getEstimateSalesDate());
        clientListDTO.setStartEstimateSalesDate(estimateSalesDatedateInteral[0]);
        clientListDTO.setEndEstimateSalesDate(estimateSalesDatedateInteral[1]);
        IPage<ProductManageClientListVO> ipage=dao.dropPage(page, clientListDTO);
        PageResults<List<ProductManageClientListVO>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    public ProductionManageClientDetailVO selectDetailById(Long id) {
        ProductionManageClientDetailVO vo=new ProductionManageClientDetailVO();
        ProductionManageClient productionManageClient = dao.selectById(id);
        if (null==productionManageClient){
            CommonUtil.throwSuperCodeExtException(500,"不存在该客户");
        }
        BeanUtils.copyProperties(productionManageClient,vo);
        vo.setGgBirthDay(productionManageClient.getGgBirthDay());
        vo.setEstimateSalesDate(productionManageClient.getEstimateSalesDate());
        List<GuGengContactMan> gengContactManList=contactManService.selectByClientId(id);
        vo.setContactManDTOList(gengContactManList);
        return vo;
    }

    public ProductionManageClient selectById(Long id) {
        ProductionManageClient productionManageClient = dao.selectById(id);
        return productionManageClient;
    }

    public ProductionManageClient existClient(Long excludeClientId,Long categoryId, String clientName, String contactMan, String contactPhone, String areaCode) {
        QueryWrapper<ProductionManageClient> queryWrapper=commonUtil.queryTemplate(ProductionManageClient.class);
        queryWrapper.eq("c."+ProductionManageClient.COL_CATEGORY_ID,categoryId);
        queryWrapper.eq("c."+ProductionManageClient.COL_CLIENT_NAME,clientName);
        queryWrapper.eq("cm."+ProductionManageClient.COL_CONTACT_MAN,contactMan);
        queryWrapper.eq("cm."+ProductionManageClient.COL_CONTACT_PHONE,contactPhone);
        queryWrapper.eq(StringUtils.isNotBlank(areaCode),"cm."+ProductionManageClient.COL_AREA_CODE,areaCode);
        queryWrapper.ne(null!=excludeClientId,"c."+ProductionManageClient.COL_ID,excludeClientId);
        ProductionManageClient productionManageClient = baseMapper.selectExistClient(queryWrapper);
        return productionManageClient;
    }

    public void delete(Long id) throws SuperCodeException {
        Map<String, Object> columMap= QueryMapperUtil.singleQueryColumnMap("Id",id, commonUtil.getOrganizationId(), commonUtil.getSysId());
        List<ProductionManageClient> list=dao.selectByMap(columMap);
        if (null==list || list.isEmpty()) {
            throw new SuperCodeException("该客户不存在", 500);
        }
        ProductionManageClient client=list.get(0);
        client.setDelStatus(1);
        dao.updateById(client);
    }

    @Override
    public  List<ProductManageClientListVO> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        return dao.listByIds((List<String>)ids, commonUtil.getSysId(), commonUtil.getOrganizationId());
    }
    /**
     * 新增客户类目
     *
     * @param categoryName
     * @param rowIndex
     * @return
     * @throws SuperCodeExtException
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductionManageClientMO importClient(String clientName, String contactMan, String contactPhone, String categoryName, Long categoryId,
                                                 String address, String detailAddress, String saleUserName, String saleUserId, String employeeId, int rowIndex, Date estimateSalesDate, String foEstimateSales, Date currentDate) throws SuperCodeExtException {
        String copyAddress = address;
        ProductionManageClient productionManageClient1 = existClient(null, categoryId, clientName, contactMan, contactPhone, null);
        if (null == productionManageClient1) {
            GuGengContactMan guGengContactMan = new GuGengContactMan();
            guGengContactMan.setContactMan(contactMan);
            guGengContactMan.setContactPhone(contactPhone);
            guGengContactMan.setDetailAddress(detailAddress);
            ProductionManageClient client = new ProductionManageClient();
            client.setClientName(clientName);
            client.setCategoryId(categoryId);
            client.setCategoryName(categoryName);
            client.setContactMan(contactMan);
            client.setContactPhone(contactPhone);
            client.setClientType(0);
            client.setDelStatus(0);
            client.setOrganizationId(commonUtil.getOrganizationId());
            client.setSysId(commonUtil.getSysId());
            client.setSaleUserId(saleUserId);
            client.setSaleUserName(saleUserName);
            client.setDetailAddress(detailAddress);
            if (StringUtils.isNotBlank(foEstimateSales)){
                client.setFoEstimateSales(new BigDecimal(foEstimateSales));
            }
            client.setEstimateSalesDate(estimateSalesDate);
            detailAddress = baseInfoCommonService.areaSet(address, detailAddress, false, rowIndex, copyAddress, guGengContactMan, client);
            client.setOperator(employeeId);
            client.setDetailAddress(detailAddress);
            client.setCreateDate(currentDate);
            baseMapper.insert(client);
            guGengContactMan.setClientId(client.getId());
            contactManService.save(guGengContactMan);
            client.setGgContactManId(guGengContactMan.getId());
            baseMapper.updateById(client);
            //新增潜在及订单客户转化率统计
            saleClientNumStatisticsService.updatePotentialClientNum(client.getSaleUserId(), client.getSaleUserName(), 1);
        }
        return null;
    }
    /**
     * 客户信息导入
     * @param importDTO
     */
    public void clientImport(ExcelImportDTO importDTO) throws SuperCodeException {

        // 解析excel文件流
        // 从七牛云获取excel文件流
        EofSensorInputStream inputStream = (EofSensorInputStream) HttpUtil.doGet(OSS_DOMAIN + "/" + importDTO.getUniqueCode());
        if (Objects.isNull(inputStream)) {
            throw new SuperCodeException("获取excel文件失败");
        }
        clientImportService.getExcel2Database(inputStream, "t_production_manage_client", importDTO.getFileName(), 100);
    }

    public void updateClientToRealClient(Long clientId) {
        ProductionManageClient client=new ProductionManageClient();
        client.setId(clientId);
        client.setClientType(1);
        dao.updateById(client);
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public void test() {
        ProductionManageClient client=new ProductionManageClient();
        client.setId(18L);
        client.setClientName("大客户-zc");
        dao.updateById(client);
    }

    private String pageParamSet(ProductManageClientListDTO clientListDTO) {
        clientListDTO.setOrganizationId(commonUtil.getOrganizationId());
        clientListDTO.setSysId(commonUtil.getSysId());
        String[] dateIntegral = LocalDateTimeUtil.substringDate(clientListDTO.getOrderDate());
        clientListDTO.setStartOrderDate(dateIntegral[0]);
        clientListDTO.setEndOrderDate(dateIntegral[1]);

        String [] estimateSalesDanetteIntegral = LocalDateTimeUtil.substringDate(clientListDTO.getEstimateSalesDate());
        clientListDTO.setStartEstimateSalesDate(estimateSalesDanetteIntegral[0]);
        clientListDTO.setEndEstimateSalesDate(estimateSalesDanetteIntegral[1]);
        String authCode = clientListDTO.getClientType() == 0 ? POTENTIAL_CLIENT : ORDER_CLIENT;
        return authComponent.getRoleFunAuthSqlByAuthCode(authCode, "c."+ProductionManageClient.COL_CREATE_USER_ID, commonUtil.getEmployee().getEmployeeId(), "c."+ProductionManageClient.COL_AUTH_DEPARTMENT_ID);
    }


}
