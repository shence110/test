package com.neo.util;

import oracle.sql.BLOB;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: Administrator
 * @Date: 2019/3/21/021 16:01
 * @Description:
 */
public class DbUtil {

    /**
     * 日志对象
     */
    private Logger logger = Logger.getLogger(JDBCUtil.class);


    Connection conn ;

    private PreparedStatement pst = null;

    private ResultSet rst = null;
    /**
     * 构造方法
     *
     * @param connection
     * 			数据库连接
     */
    public DbUtil(Connection connection) {
        conn = connection ;
    }

    public Connection getConn() {
        return conn;
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
        }/* finally {
            // 关闭所有资源
            closeAll();
        }*/

        return list;
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
     * insert update delete SQL语句的执行的统一方法 执行完关闭连接
     * @param sql SQL语句
     * @param params 参数数组，若没有参数则为null
     * @return 受影响的行数
     */
    public int executeUpdate(String sql, Object[] params) {
        // 受影响的行数
        int affectedLine = 0;

        try {
            // 获得连接
            //conn = this.getConnection();
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

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(sql);
        }/* finally {
            // 释放资源
            if (isCloseConn){
                closeAll();
            }

        }*/
        return affectedLine;
    }

    public int batchInsertJsonArry(String tbName, List<Map<String, Object>> newData,List<Map<String, Object>> tbstruct){
        long start = System.currentTimeMillis();
        String  sql= null;
        int[] result= null;
        PreparedStatement pst ;
        try {
            sql =  getInsertSql( tbName,  newData);
            conn.setAutoCommit(false);
            pst = conn.prepareStatement(sql.toString());
            result =  insertBatch(tbName , newData, pst,tbstruct);
            long end = System.currentTimeMillis();
            logger.info("批量插入了:"+newData.size()+"条数据 需要时间:"+(end - start)/1000+"s"); //批量插入需要时间:
            return newData.size() ;
        } catch (Exception e) {
           logger.error(sql.toString(),e);
           e.printStackTrace();
        }
        return 0;

    }

    private String getInsertSql(String tbName, List<Map<String, Object>> newData) {
        StringBuilder sql = new StringBuilder();
        Map<String,Object> m= (Map<String,Object>)newData.get(0);
        sql.append("insert into "+tbName+" (");
        for (Map.Entry<String, Object> mm:  m.entrySet()) {
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

    private int[] insertBatch(String tbName, List<Map<String, Object>> dat,PreparedStatement pst,List<Map<String, Object>> tbstruct) throws SQLException, IOException {
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
                for (Map<String, Object> structure:tbstruct) {
                   cloumnName =structure.get("COLUMN_NAME")+"";
                   dataType =structure.get("DATA_TYPE")+"";
                    if ( k.equals(cloumnName) && ("DATE".equals(dataType)  && value !=null )){
                        value =   value.substring(0,value.indexOf("."));
                        dateValue = DateUtil.strToDate(value);
                        flag =true;
                        break;
                    }
                    if ( k.equals(cloumnName) && ("CLOB".equals(dataType) ||"BLOB".equals(dataType)) && value !=null){
                        value =getValueByType(ma,k,dataType);
                        break;
                    }

                }

                if (flag)pst.setObject(j+1,dateValue);
                else pst.setObject(j+1,value);
                j++;
            }
            pst.addBatch();
            ik = pst.executeBatch();
            conn.commit();
        }
        return  ik;
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

    public int batchDelete(List<Map<String, Object>> data, List<Map<String, Object>> uniqueList, String tbName) throws Exception {
        String sql = " DELETE  FROM   " + tbName + " where 1=1 ";

        int[] result = null;//批量插入返回的数组
        String columnName = null;//列名
        PreparedStatement pst = null;
        if (uniqueList.size() ==0) throw new Exception(tbName+"缺少唯一键,请在资源文件中配置");
        if (uniqueList.size() == 1) {
            columnName = uniqueList.get(0).get("COLUMN_NAME") + "";
            sql += " and " + columnName + " =  ? ";
            pst = conn.prepareStatement(sql);
            for (Map<String, Object> map : data) {
                pst.setObject(1, map.get(columnName) + "");
                pst.addBatch();
            }
            result = pst.executeBatch();
        } else if ((uniqueList.size() > 1)) {
            //sql 预编译
            int k = 0;
            String[] arr = new String[uniqueList.size()];
            for (Map<String, Object> uniqueMap : uniqueList) {
                columnName = uniqueMap.get("COLUMN_NAME") + "";
                sql += " and " + columnName + " =  ? ";
                arr[k] = columnName;
                k++;
            }
            pst = conn.prepareStatement(sql);
            //批量插入
            for (Map<String, Object> map : data) {
                for (int i = 0; i < arr.length; i++) {
                    pst.setObject(i + 1, map.get(arr[i]) + "");
                }
                pst.addBatch();
            }
            result = pst.executeBatch();
        }
        conn.commit();
        return result.length;
    }

}
