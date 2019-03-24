package com.test;

/**
 * @Auther: Administrator
 * @Date: 2019/3/22/022 14:59
 * @Description:
 */
import com.neo.util.CollectionUtil;
import com.neo.util.DateUtil;


import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;


public class InsertTest {

    private String url="jdbc:oracle:thin:@39.105.109.148:1521:orcl";
    private String user="bimall";
    private String password="bimall";
    public Connection getMasterConnect(){
        Connection con = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    public Connection getSlaverConnect(){
        Connection con = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String url ="jdbc:oracle:thin:@61.144.226.194:7047:orcl";
            String user ="eafbim";
            String password ="eafbim";
            con=DriverManager.getConnection("jdbc:oracle:thin:@61.144.226.194:7047:orcl", user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    private String getInsertSql( List<Map<String, Object>> newData) {
        StringBuilder sql = new StringBuilder();
        Map<String,Object> m= (Map<String,Object>)newData.get(0);
        sql.append("insert into BIM_DMM_NAV (");
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

    public void multiThreadImport( final int ThreadNum,List<Map<String, Object>> list){
        final CountDownLatch cdl= new CountDownLatch(ThreadNum);//定义线程数量
        long starttime=System.currentTimeMillis();
        for(int k=1;k<=ThreadNum;k++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection con=getMasterConnect();
                    try {
                        con.setAutoCommit(false);
                        String sql = getInsertSql(list);
                        PreparedStatement pst=con.prepareStatement(sql);
                        Map<String, Object> map =null;
                        Object value = null;
                        int k ;//计数器
                        String cloumnName = null;
                        String dataType = null;
                        java.sql.Date dateValue =null;
                        for(int i=1;i<=list.size()/ThreadNum;i++){
                            map=list.get(i);
                            /***
                             * 设置参数值 *****************start*************************************************
                             */
                            k=1;
                            for (String key : map.keySet()) {
                              value = map.get(key);
                               if ( null == value ) {
                                   pst.setObject(k,null);
                                   k++;
                                   continue;
                               }

                              if (value.getClass()== java.sql.Timestamp.class) {
                                   pst.setDate(k,DateUtil.strToDate(   (value+"").substring(0,(value+"").indexOf("."))  ));
                              }
                              k++;
                            }
                            /***
                             * ******************************************end*********************************************
                             */
                            pst.addBatch();
                            if(i%500 == 0){
                               pst.executeBatch();
                                con.commit();
                            }
                        }
                        cdl.countDown();    //执行完一个线程，递减1
                    } catch (Exception e) {
                    }finally{
                        try {
                            con.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        try {
            cdl.await();    //前面线程没执行完，其他线程等待，不往下执行
            long spendtime=System.currentTimeMillis()-starttime;
            System.out.println( ThreadNum+"个线程花费时间:"+spendtime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

/*    public static void main(String[] args) throws Exception {

        List<Map<String,Object>> data =new  ArrayList<>();
        Map<String,Object> map ;
        for (int i = 0; i <100 ; i++) {

            map =new HashMap<>();
            data.add(map);
        }
 int k =100/3;
        System.out.println(k);
        List<List<Map<String,Object>>> data1= CollectionUtil.splitList(data, 34);
        System.out.println(data1.size());

      *//*  InsertTest ti=new InsertTest();
        List<Map<String, Object>> list =  ti.query(ti);
       //ti.multiThreadImport(5,list);
        ti.Import(list);*//*
//        Map<String, Object> map = list .get(0);
//        for (String key :map.keySet()) {
//            if (null != map.get(key) )
//            System.out.println(map.get(key).getClass()  );
//        }
//        System.out.println(list.size());

        //
    }*/

    private void Import( List<Map<String,Object>> list) throws SQLException {
        long starttime=System.currentTimeMillis();

                    Connection con=getMasterConnect();

                        con.setAutoCommit(false);
                        String sql = getInsertSql(list);
                        PreparedStatement pst=con.prepareStatement(sql);
                        Map<String, Object> map =null;
                        Object value = null;
                        int k ;//计数器
                        String cloumnName = null;
                        String dataType = null;
                        java.sql.Date dateValue =null;
                        for(int i=1;i<=list.size();i++){
                            map=list.get(i);
                            /***
                             * 设置参数值 *****************start*************************************************
                             */
                            k=1;
                            for (String key : map.keySet()) {
                                value = map.get(key);
                                if ( null == value ) {
                                    pst.setObject(k,null);
                                    k++;
                                    continue;
                                }

                                if (value.getClass()== java.sql.Timestamp.class) {
                                    pst.setDate(k,DateUtil.strToDate(   (value+"").substring(0,(value+"").indexOf("."))  ));
                                    k++;
                                    continue;
                                }
                                pst.setObject(k,value);
                                k++;
                            }
                            /***
                             * ******************************************end*********************************************
                             */
                            pst.addBatch();
                            if(i%500 == 0){
                                pst.executeBatch();
                                con.commit();
                            }
                        }




            long spendtime=System.currentTimeMillis()-starttime;
            System.out.println( "******花费时间:"+spendtime);

    }

    private List<Map<String,Object>> query(InsertTest ti) throws SQLException {
        Connection connection = ti.getSlaverConnect();
        String sql ="select * from BIM_DMM_NAV";
        ResultSet rs = executeQueryRS(sql,connection);
        ResultSetMetaData rsmd = null;
        // 结果集列数
        int columnCount = 0;
        rsmd = rs.getMetaData();

        // 获得结果集列数
        columnCount = rsmd.getColumnCount();

        // 创建List
        List<Map<String, Object>> list = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                map.put(rsmd.getColumnLabel(i), rs.getObject(i));
            }
            list.add(map);//每一个map代表一条记录，把所有记录存在list中
        }
        rs.close();
        return list ;
    }

    private ResultSet executeQueryRS(String sql,Connection connection) throws SQLException {
       PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet resultSet=      pst  .executeQuery();
       // connection.close();
        //pst.close();
        return resultSet;
    }
}
