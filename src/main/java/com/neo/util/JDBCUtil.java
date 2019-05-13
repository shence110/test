package com.neo.util;

import com.neo.service.TbService;
import oracle.sql.BLOB;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * 对jdbc的完整封装
 *
 */
public class JDBCUtil {

    /**
     * 日志对象
     */
    private Logger logger = Logger.getLogger(JDBCUtil.class);

    ApplicationContext context = SpringContextUtil.getApplicationContext();

    private static  String driver = null;
    private static  String url = null;
    private static  String username = null;
    private static  String password = null;

    private CallableStatement callableStatement = null;//创建CallableStatement对象
    private Connection conn = null;
    private PreparedStatement pst = null;
    private ResultSet rst = null;

/*	static {
        try {
            // 加载数据库驱动程序
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.out.println("加载驱动错误");
            System.out.println(e.getMessage());
        }
    } */

    public JDBCUtil(String dbName) {
        this.driver = context.getEnvironment().getProperty("spring.datasource."+dbName+".driverClassName");
        this.url = context.getEnvironment().getProperty("spring.datasource."+dbName+".url");;
        this.username = context.getEnvironment().getProperty("spring.datasource."+dbName+".username");;
        this.password = context.getEnvironment().getProperty("spring.datasource."+dbName+".password");;
    }

    public JDBCUtil(String driver,String url ,String username,String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 建立数据库连接
     * @return 数据库连接
     */
    public Connection getConnection() {
        try {
            // 加载数据库驱动程序
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                System.out.println("加载驱动错误");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            // 获取连接
            conn = DriverManager.getConnection(url, username,
                    password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
        return conn;
    }






    /**
     * insert update delete SQL语句的执行的统一方法 执行完关闭连接
     * @param sql SQL语句
     * @param params 参数数组，若没有参数则为null
     * @return 受影响的行数
     */
    public int executeUpdate(String sql, Object[] params)  {
        // 受影响的行数
        int affectedLine = 0;

        try {
            // 获得连接
            conn = this.getConnection();
            conn.setAutoCommit(false);
            // 调用SQL
            pst = conn.prepareStatement(sql);

            // 参数赋值
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pst.setObject(i + 1, params[i]);
                }
            }
            /*在此 PreparedStatement 对象中执行 SQL 语句，
                                          该语句必须是一个 SQL 数据操作语言（Data Manipulation Language，DML）语句，比如 INSERT、UPDATE 或 DELETE
                                          语句；或者是无返回内容的 SQL 语句，比如 DDL 语句。    */
            // 执行
            affectedLine = pst.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            logger.error(e.getMessage());
            logger.info(sql);
        } finally {
            // 释放资源
                closeAll();
        }
        return affectedLine;
    }

    /**
     * SQL 查询将查询结果直接放入ResultSet中
     * @param sql SQL语句
     * @param params 参数数组，若没有参数则为null
     * @return 结果集
     */
    private ResultSet executeQueryRS(String sql, Object[] params) {
        try {
            // 获得连接
            conn = this.getConnection();

            // 调用SQL
            pst = conn.prepareStatement(sql);

            // 参数赋值
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pst.setObject(i + 1, params[i]);
                }
            }

            // 执行
            rst = pst.executeQuery();

        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }

        return rst;
    }

    /**
     * SQL 查询将查询结果：一行一列
     * @param sql SQL语句
     * @param params 参数数组，若没有参数则为null
     * @return 结果集
     */
    public Object executeQuerySingle(String sql, Object[] params) {
        Object object = null;
        try {
            // 获得连接
            conn = this.getConnection();

            // 调用SQL
            pst = conn.prepareStatement(sql);

            // 参数赋值
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pst.setObject(i + 1, params[i]);
                }
            }

            // 执行
            rst = pst.executeQuery();

            if(rst.next()) {
                object = rst.getObject(1);
            }

        } catch (SQLException e) {
           logger.error(e.getMessage());
        } finally {
            closeAll();
        }

        return object;
    }

    /**
     * 获取结果集，并将结果放在List中
     *
     * @param sql  SQL语句
     *         params  参数，没有则为null
     * @return List
     *                       结果集
     */
    public List<Map<String, Object>> excuteQuery(String sql, Object[] params) {
        // 执行SQL获得结果集
        ResultSet rs = executeQueryRS(sql, params);

        // 创建ResultSetMetaData对象
        ResultSetMetaData rsmd = null;

        // 结果集列数
        int columnCount = 0;
        try {
            rsmd = rs.getMetaData();

            // 获得结果集列数
            columnCount = rsmd.getColumnCount();
        } catch (SQLException e1) {
           logger.error(e1.getMessage());
        }

        // 创建List
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            // 将ResultSet的结果保存到List中
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    map.put(rsmd.getColumnLabel(i), rs.getObject(i));
                }
                list.add(map);//每一个map代表一条记录，把所有记录存在list中
            }
        } catch (SQLException e) {
           logger.error(e.getMessage());
           logger.error(sql);
        } finally {
            // 关闭所有资源
            closeAll();
        }

        return list;
    }

    /**
     * 获取结果集，并将结果放在List中
     *
     * @param sql  SQL语句
     *         params  参数，没有则为null
     * @return List
     *                       结果集
     */
    public Map<String,Object> excuteQueryWithMuliResult(String sql, Object[] params) {
        Map<String,Object> resutltMap =new HashMap<>();
        Map<String,Object> clumn2Type =new HashMap<>();
        // 执行SQL获得结果集
        ResultSet rs = executeQueryRS(sql, params);

        // 创建ResultSetMetaData对象
        ResultSetMetaData rsmd = null;

        // 结果集列数
        int columnCount = 0;
        try {
            rsmd = rs.getMetaData();

            // 获得结果集列数
            columnCount = rsmd.getColumnCount();
        } catch (SQLException e1) {
            logger.error(e1.getMessage());
        }

        // 创建List
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            // 将ResultSet的结果保存到List中
            int k=0;
            while (rs.next()) {
                k++;
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {

                    if (k==1){
                       clumn2Type.put(rsmd.getColumnLabel(i),rsmd.getColumnTypeName(i));
                    }
                    if ("RN".equals(rsmd.getColumnLabel(i))) continue;
                    map.put(rsmd.getColumnLabel(i), rs.getObject(i));
                }
                list.add(map);//每一个map代表一条记录，把所有记录存在list中
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(sql);
        } finally {
            // 关闭所有资源
            closeAll();
        }
        resutltMap.put("list",list);
        resutltMap.put("columnType",clumn2Type);
        return resutltMap;
    }

    /**
     * 存储过程带有一个输出参数的方法
     * @param sql 存储过程语句
     * @param params 参数数组
     * @param outParamPos 输出参数位置
     * @param SqlType 输出参数类型
     * @return 输出参数的值
     */
    public Object excuteQuery(String sql, Object[] params,int outParamPos, int SqlType) {
        Object object = null;
        conn = this.getConnection();
        try {
            // 调用存储过程
            // prepareCall:创建一个 CallableStatement 对象来调用数据库存储过程。
            callableStatement = conn.prepareCall(sql);

            // 给参数赋值
            if(params != null) {
                for(int i = 0; i < params.length; i++) {
                    callableStatement.setObject(i + 1, params[i]);
                }
            }

            // 注册输出参数
            callableStatement.registerOutParameter(outParamPos, SqlType);

            // 执行
            callableStatement.execute();

            // 得到输出参数
            object = callableStatement.getObject(outParamPos);

        } catch (SQLException e) {
           logger.error(e.getMessage());
        } finally {
            // 释放资源
            closeAll();
        }

        return object;
    }

    /**
     * 关闭所有资源
     */
    public void closeAll() {
        // 关闭结果集对象
        if (rst != null) {
            try {
                rst.close();
            } catch (SQLException e) {
               logger.error(e.getMessage());
            }
        }

        // 关闭PreparedStatement对象
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
               logger.error(e.getMessage());
            }
        }

        // 关闭CallableStatement 对象
        if (callableStatement != null) {
            try {
                callableStatement.close();
            } catch (SQLException e) {
               logger.error(e.getMessage());
            }
        }

        // 关闭Connection 对象
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
               logger.error(e.getMessage());
            }
        }
    }

    public int getCount(String sql, Object[] params) {

        // 执行SQL获得结果集
        ResultSet rs = executeQueryRS(sql, params);

        int rowCount = 0;
        try {
            if(rs.next())
            {
                rowCount=rs.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        finally{
          closeAll();
        }

        return rowCount;
    }


    public int batchInsert(String tbName, Map<String, Object> resultMap) throws Exception{
        long start = System.currentTimeMillis();
        String  sql= null;
        int[] result= null;
        List<Map<String,Object>> newData = (List<Map<String,Object>> )resultMap.get("list");
        Map<String,Object> columnTypeMap = (Map<String,Object> )resultMap.get("columnType");
        PreparedStatement pst = null;
        int len =0;
        try {
            conn = this.getConnection();

            sql =  getInsertSql( tbName,  newData)+"";
            pst = conn.prepareStatement(sql);
            result =  insertBatch(tbName ,newData, pst,columnTypeMap);
            long end = System.currentTimeMillis();
            logger.info("批量插入了:"+newData.size()+"条数据 需要时间:"+(end - start)/1000+"s"); //批量插入需要时间:
             len= newData.size() ;
            return len;
        } catch (Exception e) {
           logger.error(sql.toString(),e);

            logger.info("批处理失败 开始一条一条插入.........");
            //e.printStackTrace();
           return odinaryInsert( tbName, newData, columnTypeMap);
        }finally {
            closeAll();
        }
       // return 0;

    }

    private String getInsertSql(String tbName, List<Map<String, Object>> newData) {
        StringBuilder sql = new StringBuilder();
        Map<String,Object> m= newData.get(0);
        sql.append("insert into "+tbName+" (");
        for (Map.Entry<String, Object> mm:  m.entrySet()) {
            if ("RN".equals(mm.getKey())){
                continue;
            }
            sql .append(mm.getKey()+",") ;
        }
        sql.deleteCharAt(sql.length()-1);
        sql .append(" ) values (");

        for (int i=0;i<m.size();i++) {
            sql .append(" ?,");
        }
        sql.deleteCharAt(sql.length()-1);
        sql .append(" ) ");
        return sql.toString();
    }

    private int[] insertBatch(String tbName, List<Map<String, Object>> dat,PreparedStatement pst,Map<String, Object> structureMap) throws SQLException, IOException {
        conn.setAutoCommit(false);
        Map<String,Object> m =(Map<String,Object>)dat.get(0);
        int[] ik =null;
        String value = null;
        Map<String,Object> ma = null;
        String cloumnName = null;
        String dataType = null;
        java.sql.Date dateValue =null;
        boolean flag ;
        for (int i = 0; i <dat.size() ; i++) {
            ma = (Map<String,Object>)dat.get(i);
            int j=0;
            for (String k:ma.keySet()) {
                value =ma.get(k)+"";

                if ("null".equals(value.trim())) value =null;
                flag =false;
                dataType = structureMap.get(k)+"";
                if ( ("DATE".equals(dataType)  && value !=null )){
                    value =   value.substring(0,value.indexOf("."));
                    dateValue = DateUtil.strToDate(value);
                    flag =true;
                }
                if ( ("CLOB".equals(dataType) ||"BLOB".equals(dataType)) && value !=null){
                    value =getValueByType(ma,k,dataType);
                }
                if (flag)pst.setObject(j+1,dateValue);
                else pst.setObject(j+1,value);
                j++;
            }
            pst.addBatch();

            if(i>0 && i%1000==0){
                ik = pst.executeBatch();
                //清除批处理命令
                pst.clearBatch();
                //如果不想出错后，完全没保留数据，则可以每执行一次提交一次，但得保证数据不会重复
                conn.commit();
            }
        }
        ik = pst.executeBatch();
        pst.clearBatch();
        conn.commit();
        return  ik;
    }

    private int odinaryInsert(String tbName, List<Map<String,Object>> dat, Map<String,Object> tbstruct) {
        long start = System.currentTimeMillis();
        String  sql= sql =  getInsertSql( tbName,  dat);;
        int[] result= null;
        PreparedStatement pst = null;
        Map<String,Object> ma;
        String value ;
        boolean flag;
        String cloumnName;
        String dataType;
        java.sql.Date dateValue = null;
        int rows=0;
        try {
            conn.setAutoCommit(false);
            pst = conn.prepareStatement(sql);

            for (int i = 0; i <dat.size() ; i++) {
                ma = (Map<String,Object>)dat.get(i);
                int j=0;
                for (String k:ma.keySet()) {
                    j++;
                    value =ma.get(k)+"";
                    if ("null".equals(value.trim())) value =null;
                    flag =false;

                        dataType =tbstruct.get(k)+"";
                        if (  ("DATE".equals(dataType)  && value !=null )){
                            value =   value.substring(0,value.indexOf("."));
                            dateValue = DateUtil.strToDate(value);
                            pst.setObject(j,dateValue);

                        }
                        if (  ("CLOB".equals(dataType) ||"BLOB".equals(dataType)) && value !=null){
                            value =getValueByType(ma,k,dataType);
                            pst.setObject(j,value);
                        }else{
                            pst.setObject(j,value);
                        }

                }
                try {
                    rows +=  pst.executeUpdate();
                    conn.commit();
                }catch (Exception e){
                    logger.error("插入出错 原因为: "+e.getMessage()); //批量插入需要时间:
                }

            }


            long end = System.currentTimeMillis();
            logger.info("插入了:"+rows+"条数据需要时间:"+(end - start)/1000+"s"); //批量插入需要时间:
            return rows;
        } catch (Exception e) {
            logger.error(sql.toString(),e);
            e.printStackTrace();
        }/*finally {
            closeAll();
        }*/
        return 0;

    }

    /**
     * 根据数据类型获得值
     * @param map
     * @param key
     * @param data_type
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private String getValueByType(Map<String, Object> map, String key, String data_type) throws IOException, SQLException {
        if ("CLOB".equals(data_type)){
            Clob columnClob = (Clob) map.get(key);
            return StringUtils.ClobToString(columnClob);
        }
        if ("BLOB".equals(data_type)){
            BLOB columnClob = (BLOB) map.get(key);
            return StringUtils.BlobToString(columnClob);
        }
        return null;
    }


}

