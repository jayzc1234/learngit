package com.zxs.mapper;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ces
 * @author zc
 */
public abstract class BaseMapper {
    @Autowired
    private SqlSession sqlSession;

    public SqlSession getSqlSession(){
        return sqlSession;
    }
}
