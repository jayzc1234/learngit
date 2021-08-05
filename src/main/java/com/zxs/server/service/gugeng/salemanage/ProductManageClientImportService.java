package com.zxs.server.service.gugeng.salemanage;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import jxl.Cell;
import jxl.Sheet;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.template.BaseExcelImportTemplate;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClientCategory;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ClientCategoryMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.CommonService;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorEmployee;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zc
 */
@Slf4j
@Service
public class ProductManageClientImportService extends BaseExcelImportTemplate {

    @Autowired
    private ProductManageClientService clientService;

    @Autowired
    private ClientCategoryMapper categoryMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CommonUtil commonUtil;
    @Override
    public void parseExcel2SqlAndExecute(String tableName, Sheet sheet, List<String> columns, String newCols, int limitSize) throws SuperCodeException {
        // 获取sheet总长度, 过滤掉定义的表头和描述信息
        int count = sheet.getRows();
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();
        InterceptorEmployee employeeInfo = commonUtil.getEmployeeInfo();
        SimpleDateFormat dateFormat=new SimpleDateFormat(DateTimePatternConstant.YYYY_MM_DD);
        Date currentDate = CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS);
        Map<String,String> saleMap=new HashMap<>();
        // 获取多次执行的次数
        int time = count % limitSize == 0 ? count / limitSize : count / limitSize + 1;
        for (int i = 0; i < time; i++) {

            // 设置每次执行的次数
            int interval = (time - i == 1) ? count : limitSize * (i + 1);
            // 初始化行数据集合
            List<Map<String, String>> params = new ArrayList<>();
            for (int j = i * limitSize; j < interval; j++) {
                // 过滤sheet中前3行数据
                if (j == 0 || j == 1 || j == 2) {
                    continue;
                }

                Cell[] cells = sheet.getRow(j);
                if (null == cells[0] || StringUtils.isBlank(cells[0].getContents())) {
                    continue;
                }
                String categoryName = cells[0].getContents();
                String clientName = cells[1].getContents();
                String contactMan = cells[2].getContents();
                String contactPhone = cells[3].getContents();
                String address = cells[4].getContents();
                String detailAddress = cells[5].getContents();
                String estimateSalesDate = cells[6].getContents();
                String foEstimateSales = cells[7].getContents();
                String saleUserName = cells[8].getContents();

                ProductionManageClientCategory productionManageClientCategory = categoryMapper.selectByNameInOtherSysId(categoryName, null, organizationId, sysId);
                if (null==productionManageClientCategory){
                    productionManageClientCategory=new ProductionManageClientCategory();
                    productionManageClientCategory.setCategoryName(categoryName);
                    productionManageClientCategory.setAuthDepartmentId(employeeInfo.getEmployeeId());
                    productionManageClientCategory.setCreateUserId(employeeInfo.getEmployeeId());
                    productionManageClientCategory.setSysId(sysId);
                    productionManageClientCategory.setOrganizationId(organizationId);
                    productionManageClientCategory.setSortWeight(1);
                    categoryMapper.insert(productionManageClientCategory);
                }

                String saleUserId=null;
                if (StringUtils.isNotBlank(saleUserName)) {
                    try {
                        saleUserId = saleMap.get(saleUserName);
                        if (null==saleUserId){
                            saleUserId=commonService.getEmployName(saleUserName);
                            if (StringUtils.isBlank(saleUserId)){
                                CommonUtil.throwSupercodeException(500, "根据销售人员名称："+saleUserName+"获取销售人员信息失败");
                            }
                            saleMap.put(saleUserName,saleUserId);
                        }
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage());
                        CommonUtil.throwSupercodeException(500, "根据销售人员名称："+saleUserName+"获取销售人员信息失败");
                    }
                }
                Date estimateSalesDate_d=null;
                if (StringUtils.isNotBlank(estimateSalesDate)){
                    try {
                        String[] split = estimateSalesDate.split("-");
                        if (split[0].length()==2){
                            estimateSalesDate="20"+estimateSalesDate;
                        }
                        estimateSalesDate_d= dateFormat.parse(estimateSalesDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                clientService.importClient(clientName,contactMan,contactPhone,categoryName,productionManageClientCategory.getId(),address,detailAddress,saleUserName,
                        saleUserId,employeeInfo.getEmployeeId(),j,estimateSalesDate_d,foEstimateSales,currentDate);
            }
        }
    }

    @Override
    public List<Map<String, String>> customRow(List<Map<String, String>> params) throws SuperCodeException {
        return null;
    }

    @Override
    public void executeSql(String sql) throws SuperCodeException {

    }
}
