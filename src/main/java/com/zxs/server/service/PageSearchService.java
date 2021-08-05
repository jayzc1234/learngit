package com.zxs.server.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.server.util.ExcelUtils2;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页抽象接口
 * @author zc
 */
public interface PageSearchService<M extends BaseMapper<T>, T> {


    /**
     * 分页方法
     * @param daoSearch
     * @param <P>
     * @return
     */
    default <P extends DaoSearch> IPage<T> listPage(P daoSearch){
        return null;
    }



    /**
     *
     * @param daoSearch
     * @param exportNum
     * @param text
     * @param claz:部分导出json字符串序列化类型
     * @param response
     * @throws Exception
     */
    public  default void exportExcelList(DaoSearch daoSearch, int exportNum, String text, Class claz, HttpServletResponse response) throws Exception {
        ArrayList<String> idList = daoSearch.getIdList();
        List list;
        // idList为空导出全部，不为空导出指定数据
        if (StringUtils.isNotBlank(daoSearch.getDataList())){
            list = JSONObject.parseArray(daoSearch.getDataList(),claz);
        }else {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(exportNum);
            IPage eiPage = this.listPage(daoSearch);
            list=eiPage.getRecords();
        }
        try {
            dataTransfer(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExcelUtils2.listToExcel(list, daoSearch.exportMetadataToMap(), text, response, claz);
    }

    public default void  dataTransfer(List<T> list) throws SuperCodeException {
    }
}
