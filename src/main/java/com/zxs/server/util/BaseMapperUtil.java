package com.zxs.server.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import org.apache.commons.lang.StringUtils;

import java.util.Hashtable;
import java.util.Objects;

/**
 * 关于数据库操作的工具类
 * @author zc
 */
public class BaseMapperUtil {

    private static final String ORGANIZATION_ID_FIELD = "organization_id";

    private static final String SYS_ID_FIELD = "sys_id";
    /**
     * 单表编辑时唯一属性校验，根据id校验编辑值的唯一性
     * @param <T>
     * @param id ：当前编辑的记录主键id
     * @param uniqueField ：数据库字段，即需要唯一性校验字段
     * @param value
     * @param filterMap :过滤条件，使用hashTable保证key 和value不为空，由于该方法不存在并发请求，所以hashTable的锁不存在对性能影响
     * @param baseMapper ：
     * @return
     */
    public static <T> boolean singleTableUniqueFieldCheck(Long id, String uniqueField, Object value, String organizationId, String sysId, Hashtable<String,Object> filterMap, BaseMapper<T> baseMapper) {


        if (null == value){
            CommonUtil.throwSuperCodeExtException(500,"唯一值不能为空");
        }

        //如果是字符串类型，则不能为空字符
        if (value instanceof  String && ((String) value).isEmpty()){
            CommonUtil.throwSuperCodeExtException(500,"唯一值不能为空");
        }

        //查找要更新的值除当前记录外是否被其它记录使用
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(!Objects.isNull(id),"id",id);
        queryWrapper.eq(uniqueField,value);
        queryWrapper.eq(StringUtils.isNotBlank(organizationId),ORGANIZATION_ID_FIELD,organizationId);
        queryWrapper.eq(StringUtils.isNotBlank(sysId),SYS_ID_FIELD,sysId);
        if (null != filterMap && !filterMap.isEmpty()){
            for(String key:filterMap.keySet()){
                queryWrapper.eq(key,filterMap.get(key));
            }
        }
        Integer count = baseMapper.selectCount(queryWrapper);
        if (null != count && count > 0){
           return false;
        }
        return true;
    }

    /**
     * 通过主键校验记录是否存在校验，存在则返回
     * @param id
     * @param baseMapper
     * @param <T>
     * @return
     */
    public static <T> T recordExistCheck(Long id, BaseMapper<T> baseMapper) {
        if (null == id){
            return  null;
        }
        //校验对应id记录是否存在
        T t = baseMapper.selectById(id);
        boolean isNull = Objects.isNull(t);
        if (isNull){
            return  null;
        }
        return t;
    }


    /**
     * 通过主键校验记录是否存在校验，存在则返回
     * @param id
     * @param baseMapper
     * @param <T>
     * @return
     */
    public static <T> T recordExistCheckThrowError(Long id, BaseMapper<T> baseMapper) {
        T t = recordExistCheck(id, baseMapper);
        CustomAssert.isNull(t,"记录不存在");
        return t;
    }
}
