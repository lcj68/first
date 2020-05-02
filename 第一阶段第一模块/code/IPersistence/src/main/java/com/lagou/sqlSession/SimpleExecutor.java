package com.lagou.sqlSession;


import com.lagou.config.BoundSql;
import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import com.lagou.utils.GenericTokenParser;
import com.lagou.utils.ParameterMapping;
import com.lagou.utils.ParameterMappingTokenHandler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleExecutor implements  Executor {


    @Override                                                                                //user
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws Exception {
    
    
        // 获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = preparedStatement(configuration, mappedStatement, params);
        
        // 执行sql
        ResultSet resultSet = preparedStatement.executeQuery();
        String resultType = mappedStatement.getResultType();
        Class<?> resultTypeClass = getClassType(resultType);

        ArrayList<Object> objects = new ArrayList<>();

        // 封装返回结果集
        while (resultSet.next()){
            Object o =resultTypeClass.newInstance();
            //元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {

                // 字段名
                String columnName = metaData.getColumnName(i);
                // 字段的值
                Object value = resultSet.getObject(columnName);

                //使用反射或者内省，根据数据库表和实体的对应关系，完成封装
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultTypeClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(o,value);


            }
            objects.add(o);

        }
            return (List<E>) objects;

    }
    
    private PreparedStatement preparedStatement(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        
        // 注册驱动，获取连接
        Connection connection = configuration.getDataSource().getConnection();
    
        String sql = mappedStatement.getSql();
        BoundSql boundSql = getBoundSql(sql);
    
    
        String paramterType = mappedStatement.getParamterType();
        Class<?> paramtertypeClass = getClassType(paramterType);
    
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());
        
        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappingList();
        for (int i = 0; i < parameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = parameterMappingList.get(i);
            String content = parameterMapping.getContent();
        
            //反射
            Field declaredField = paramtertypeClass.getDeclaredField(content);
            //暴力访问
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);
        
            preparedStatement.setObject(i+1,o);
        
        }
        
        return preparedStatement;
        
    }
    
    @Override
    public boolean insert(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        
        boolean result = false;
    
        // 获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = preparedStatement(configuration, mappedStatement, params);
    
        // 5. 执行sql
        result = preparedStatement.execute();
        
        return result;
    }
    
    @Override
    public int update(Configuration configuration, MappedStatement mappedStatement, Object... params) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, SQLException {
    
        int result = 0;
    
        // 获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = preparedStatement(configuration, mappedStatement, params);
    
        // 5. 执行sql
        result = preparedStatement.executeUpdate();
    
        return result;
    }
    
    @Override
    public boolean delete(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        boolean result = false;
    
        // 获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = preparedStatement(configuration, mappedStatement, params);
    
        // 5. 执行sql
        result = preparedStatement.execute();
    
        return result;
    }
    
    private Class<?> getClassType(String paramterType) throws ClassNotFoundException {
        if(paramterType!=null){
            Class<?> aClass = Class.forName(paramterType);
            return aClass;
        }
         return null;

    }


    /**
     * 完成对#{}的解析工作：1.将#{}使用？进行代替，2.解析出#{}里面的值进行存储
     * @param sql
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        //标记处理类：配置标记解析器来完成对占位符的解析处理工作
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", parameterMappingTokenHandler);
        //解析出来的sql
        String parseSql = genericTokenParser.parse(sql);
        //#{}里面解析出来的参数名称
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();

        BoundSql boundSql = new BoundSql(parseSql,parameterMappings);
         return boundSql;

    }


}
