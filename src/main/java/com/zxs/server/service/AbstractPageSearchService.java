package com.zxs.server.service;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 高级搜索抽象类
 * @param <M>
 * @param <T>
 * @author zc
 */

public abstract class AbstractPageSearchService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements PageSearchService<M, T> {

    @Autowired
    public CommonUtil commonUtil;

    private static final int ADVANCE_SEARCH = 0;
    private static final int COMMON_SEARCH = 1;

    private static final Map<Class, WeakReference<Map<String,String>>> CLOUMN_MAP = new HashMap<>();

    @Override
    public  <P extends DaoSearch> IPage<T> listPage(P daoSearch){
        IPage<T> tiPage = customPage(daoSearch);
        if (null != tiPage){
            return tiPage;
        }
        QueryWrapper<T> queryWrapper = buildWrapper(daoSearch,true);
        additionalQuerySet( queryWrapper, daoSearch);
        Page<T> page = new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize());
        IPage iPage = baseMapper.selectPage(page, queryWrapper);
        return iPage;
    }

    protected <P extends DaoSearch> QueryWrapper<T> buildWrapper(P daoSearch,boolean useOrgParam){
        Integer flag = daoSearch.getFlag();
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (useOrgParam){
            queryWrapper.eq(CommonConstants.SYS_ID,commonUtil.getSysId());
            queryWrapper.eq(CommonConstants.ORGANIZATION_ID,commonUtil.getOrganizationId());
        }
        /**
         * 支持pda search不传时默认为普通搜索的情况
         */
        if (Objects.isNull(flag)){
            flag = 1;
        }
        if (flag == ADVANCE_SEARCH){
            queryWrapper =buildAdvanceSearchWrapper(daoSearch,queryWrapper);
        }else if (flag == COMMON_SEARCH){
            queryWrapper =buildCommonSearchWrapper(daoSearch,queryWrapper);
        }else {
            throw new SuperCodeExtException("搜索类型flag： "+flag+" 非法");
        }

        return queryWrapper;
    }

    /**
     * 构建普通搜索wrapper
     * @param <P>
     * @param daoSearch
     * @param queryWrapper
     * @return
     */
    protected  <P extends DaoSearch> QueryWrapper<T> buildCommonSearchWrapper(P daoSearch, QueryWrapper<T> queryWrapper) {
        if (StringUtils.isNotBlank(daoSearch.getSearch())) {
            String search = daoSearch.getSearch();
            Map<String, String> map = parseAdvanceSearchColumns(daoSearch);
            if (!map.isEmpty()) {
                Map<String, String> columnMap2 = filterCommonColumn(map);
                if (null != columnMap2 && !columnMap2.isEmpty()) {
                    int size = columnMap2.size();
                    queryWrapper.and(w -> {
                        int counter = 1;
                        for (String name : columnMap2.keySet()) {
                            String column = columnMap2.get(name);
                            if (!column.endsWith("time")){
                                w.like(column, search).or();
                                if (counter++ < size) {
                                    w.or();
                                }
                            }
                        }
                        return w;
                    });
                }
            }
        }
        return queryWrapper;
    }

    /**
     * 构建高级搜索queryWrapper
     * @param <P>
     * @param daoSearch
     * @param queryWrapper
     * @return
     */
    protected  <P extends DaoSearch> QueryWrapper<T> buildAdvanceSearchWrapper(P daoSearch, QueryWrapper<T> queryWrapper){
        Map<String, String> map = parseAdvanceSearchColumns(daoSearch);
        if (!map.isEmpty()){
            Map<String, String> columnMap2 = filterAdvanceColumn(map);
            if (null != columnMap2 && !columnMap2.isEmpty()){
                MetaObject metaObject = SystemMetaObject.forObject(daoSearch);
                for(String name:columnMap2.keySet()){
                    String column = columnMap2.get(name);
                    Object value = metaObject.getValue(name);
                    boolean nonNull = Objects.nonNull(value);
                    if (nonNull){
                        if (name.endsWith("Time")){
                            continue;
                        }
                        if (value instanceof String){
                            if (!value.toString().isEmpty()){
                                queryWrapper.eq(column,value);
                            }
                        }else {
                            queryWrapper.eq(column,value);
                        }
                    }
                }
            }
        }

        customAdvanceWrapper(daoSearch,queryWrapper);
        return queryWrapper;
    }

    /**
     * 对高级搜索的queryWrapper进行个性化设置
     * @param daoSearch
     * @param queryWrapper
     * @param <P>
     */
    protected  <P extends DaoSearch> void customAdvanceWrapper(P daoSearch, QueryWrapper<T> queryWrapper){

    }

    /**
     * 获取高级搜索对象中的列
     * @param daoSearch
     * @param <P>
     * @return
     */
    protected   <P extends DaoSearch> Map<String, String> parseAdvanceSearchColumns(P daoSearch){
        Map<String,String> cloumnMap = new HashMap<>();
        Class<? extends DaoSearch> aClass = daoSearch.getClass();

        WeakReference<Map<String, String>> weakReference = CLOUMN_MAP.get(aClass);
        if (null != weakReference && null != (cloumnMap=weakReference.get())){
            return cloumnMap;
        }
        //注意这里的锁不能使用daoSearch对象锁，不然锁不住，因为每次执行这个方法时daoSearch都是不同的对象
        synchronized (aClass){
            weakReference = CLOUMN_MAP.get(aClass);
            if (null != weakReference && null != (cloumnMap=weakReference.get())){
                return cloumnMap;
            }
            Field[] declaredFields = aClass.getDeclaredFields();
            if (null != declaredFields && declaredFields.length != 0){
                Map<String, String> fullColumnMap = parseEntityColumns();
                for (Field declaredField : declaredFields) {
                    String name = declaredField.getName();
                    String columnName = fullColumnMap.get(name);
                    if (null != columnName){
                        cloumnMap.put(name,columnName);
                    }
                }
            }
        }
        return cloumnMap;
    }

    /**
     * 思考CLOUMN_MAP需要使用volatile关键字修饰吗？ 答案是不需要
     * @return
     */
    private Map<String, String> parseEntityColumns(){
        Class<? extends AbstractPageSearchService> currentClass = this.getClass();
        WeakReference<Map<String, String>> weakReference = CLOUMN_MAP.get(currentClass.getSimpleName());
        Map<String,String> cloumnMap = null;
        if (null == weakReference || null == (cloumnMap=weakReference.get())){
            synchronized (this){
                //双从检查锁
                weakReference = CLOUMN_MAP.get(currentClass.getSimpleName());
                if (null != weakReference && null != (cloumnMap=weakReference.get())){
                    return cloumnMap;
                }
                //获取POJO的class类型
                ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Class pojoClass = (Class) actualTypeArguments[1];

                //获取表字段设置到columnMap中，反射日后使用mybatis的反射机制来改造
                cloumnMap = new HashMap<>();
                Field[] declaredFields = pojoClass.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    TableField annotation = declaredField.getAnnotation(TableField.class);
                    //如果字段有tableField注解且不是时间类型
                    if (null != annotation ){
                        String name = declaredField.getName();
                        String value = annotation.value();
                        cloumnMap.put(name,value);
                    }
                }

                weakReference = new WeakReference<Map<String,String>>(cloumnMap);
                CLOUMN_MAP.put(this.getClass(),weakReference);
            }
        }
        return cloumnMap;
    }


    /**
     * 不能直接对参数map进行操作，因为这个map是共享变量会导致线程安全问题，使用子类实现该方法
     * 都不允许修改map
     * @param map
     * @return
     */
    protected  Map<String, String> filterAdvanceColumn(Map<String, String> map){
        return map;
    }

    /**
     * 对普通搜索字段进行过滤
     * @param map
     * @return
     */
    protected  Map<String, String> filterCommonColumn(Map<String, String> map){
        return map;
    }

    /**
     * 自定义的列表方法
     * @param daoSearch
     * @param <P>
     * @return
     */
    protected   <P extends DaoSearch>IPage<T> customPage(P daoSearch){
        return null;
    }

    protected <P extends DaoSearch> void additionalQuerySet(QueryWrapper<T> queryWrapper, P daoSearch){};
}

