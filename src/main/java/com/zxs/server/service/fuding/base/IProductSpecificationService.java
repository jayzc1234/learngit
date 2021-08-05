package com.zxs.server.service.fuding.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.ProductSpecificationBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.ProductSpecificationDO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationVO;
import net.app315.nail.common.page.service.BasePageService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 产品规格 服务类
 * </p>
 *
 * @author 
 * @since 2019-12-13
 */
public interface IProductSpecificationService extends IService<ProductSpecificationDO> , BasePageService{

    /**
        添加 产品规格
    */
    public String addProductSpecification(ProductSpecificationBO model);

    /**
            修改 产品规格
      */
    public void updateProductSpecification(ProductSpecificationBO model);

      /**
           获取 产品规格 详情
        */
    public ProductSpecificationBO getProductSpecification(String specificationId);

    /**
      * 获取产品规格分页列表
      * @return
      */
    public AbstractPageService.PageResults<List<ProductSpecificationVO>> getProductSpecificationList(ProductSpecificationSearchModel model);

    /**
     * 禁用启用
     * @param specificationId
     */
    void updateTeaFarmerStatus(String specificationId);

    abstract AbstractPageService.PageResults<List<ProductSpecificationDO>> getProductProductSpecificationPage(String productId);


    List<ProductSpecificationBO> getProductProductSpecification(String productId);

    /**
     * 导出产品规格列表
     * @param model
     * @param response
     */
    void export(ProductSpecificationSearchModel model, HttpServletResponse response);

    /**
     * 分页根据产品
     * @param productId
     * @param daoSearch
     * @return
     */
    RestResult<AbstractPageService.PageResults<List<ProductSpecificationDO>>> specificationByProduct(String productId, DaoSearch daoSearch);
}
