package com.mi.test.test1.helper;

import com.mi.test.test1.util.PropsUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.swing.BakedArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * 数据库操作助手类
 * Created by Administrator on 2017/6/6.
 */
public class DatabaseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final QueryRunner QUERY_RUNNER = new QueryRunner();

    private static final ThreadLocal<Connection> CONNECTION_HOLDER = new ThreadLocal<Connection>();

    private static final String DRIVER;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties conf = PropsUtil.loadProps("config.properties");
        DRIVER = conf.getProperty("jdbc.driver");
        URL = conf.getProperty("jdbc.url");
        USERNAME = conf.getProperty("jdbc.username");
        PASSWORD = conf.getProperty("jdbc.password");

        try{
            Class.forName(DRIVER);
        }catch (ClassNotFoundException e){
            LOGGER.error("can not load jdbc driver",e);
        }
    }

    public static Connection getConnection(){
        Connection conn = CONNECTION_HOLDER.get();
        if (conn == null){
            try{
                conn = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            }catch (SQLException e){
                LOGGER.error("get connection failure",e);
                throw new RuntimeException(e);
            }finally {
                CONNECTION_HOLDER.set(conn);
            }
        }
        return conn;
    }

    public static void closeConnection(){
        Connection conn = CONNECTION_HOLDER.get();
        if (conn != null){
            try{
                conn.close();
            }catch (SQLException e){
                LOGGER.error("close connection failure",e);
                throw new RuntimeException(e);
            }finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    /**
     * 查询实体列表
     * @param entityClass
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public static<T> List<T> queryEntityList(Class<T> entityClass, String sql, Object ... params){
        List<T> entityList;
        try{
            Connection conn = getConnection();
            entityList = QUERY_RUNNER.query(conn,sql,new BeanListHandler<T>(entityClass),params);
        }catch (SQLException e){
            LOGGER.error("query entity list failure",e);
            throw new RuntimeException(e);
        }finally {
            closeConnection();
        }
        return entityList;
    }

    /**
     * 查询实体
     * @param entityClass
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public static <T> T queryEnity(Class<T> entityClass,String sql,Object ... params){
        T entity;
        try{
            Connection conn = getConnection();
            entity = QUERY_RUNNER.query(conn,sql,new BeanHandler<T>(entityClass),params);
        }catch (SQLException e){
            LOGGER.error("query entity failure",e);
            throw new RuntimeException(e);
        }finally {
            closeConnection();
        }
        return entity;
    }

    /**
     * 执行查询语句
     * @param sql
     * @param params
     * @return
     */
    public static List<Map<String,Object>> executeQuery(String sql,Object ... params){
        List<Map<String,Object>> result;
        try{
            Connection conn = getConnection();
            result = QUERY_RUNNER.query(conn,sql,new MapListHandler(),params);
        }catch (SQLException e){
            LOGGER.error("execute query failure",e);
            throw new RuntimeException(e);
        }finally {
            closeConnection();
        }
        return result;
    }

    /**
     * 执行更新语句(包括 update,insert,delete)
     * @param sql
     * @param params
     * @return
     */
    public static int executeUpdate(String sql,Object ... params){
        int rows = 0;
        try {
            Connection conn = getConnection();
            rows  = QUERY_RUNNER.update(conn,sql,params);
        }catch (SQLException e){
            LOGGER.error("execute update failure",e);
            throw new RuntimeException(e);
        }finally {
            closeConnection();
        }
        return rows;
    }

    /**
     * 插入实体
     * @param entityClass
     * @param fieldMap
     * @param <T>
     * @return
     */
    public static <T> boolean insertEntity(Class<T> entityClass,Map<String,Object> fieldMap){
        if (MapUtils.isEmpty(fieldMap)){
            LOGGER.error("can not insert entity: fieldMap is empty");
            return false;
        }

        String sql = "INSERT INTO "+getTableName(entityClass);
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String fieldName : fieldMap.keySet()){
            columns.append(fieldName).append(",");
            values.append("?").append(",");
        }
        columns.replace(columns.lastIndexOf(","),columns.length(),")");
        values.replace(values.lastIndexOf(","),values.length(),")");
        sql += columns + " VALUES "+ values;
        Object[] params = fieldMap.values().toArray();
        return executeUpdate(sql,params) == 1;

    }

    /**
     * 更新实体
     * @param entityClass
     * @param id
     * @param fieldMap
     * @param <T>
     * @return
     */
    public static <T> boolean updateEntity(Class<T> entityClass,long id,Map<String,Object> fieldMap){
        if (MapUtils.isEmpty(fieldMap)){
            LOGGER.error("can not update entity: fieldMap is empty");
            return false;
        }
        String sql = "UPDATE "+getTableName(entityClass)+" SET ";
        StringBuilder columns = new StringBuilder();
        for (String fieldName : fieldMap.keySet()){
            columns.append(fieldName).append("=?").append(",");
        }
        columns.replace(columns.lastIndexOf(","),columns.length()," WHERE id=?");
        List<Object> paramList = new ArrayList<Object>();
        paramList.addAll(fieldMap.values());
        paramList.add(id);
        Object[] params = paramList.toArray();
        return executeUpdate(sql,params) == 1;
    }

    /**
     * 删除实体
     * @param entityClass
     * @param id
     * @param <T>
     * @return
     */
    public static <T> boolean deleteEntity(Class<T> entityClass,long id){
        String sql = "DELETE FROM "+getTableName(entityClass)+" WHERE id = ?";
        return executeUpdate(sql,id) == 1;
    }


    private static String getTableName(Class<?> entityClass){
        return entityClass.getSimpleName();
    }
}
