package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.sql.SQLException;
import java.util.List;

public interface Executor {

    public <E> List<E> query(Configuration configuration,MappedStatement mappedStatement,Object... params) throws Exception;
    
    public boolean insert(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException;
    
    public int update(Configuration configuration, MappedStatement mappedStatement, Object... params) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, SQLException;
    
    public boolean delete(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException;
    

}
