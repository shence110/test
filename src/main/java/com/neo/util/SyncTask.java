package com.neo.util;

import com.neo.service.TbService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @Auther: Administrator
 * @Date: 2019/5/13/013 18:46
 * @Description:
 */
public class SyncTask implements Callable<Integer>{

    /**
     * 日志对象
     */
    private Logger logger = Logger.getLogger(SyncTask.class);



    int i; //线程序号
    int nums ;//一次同步数据量

    String dbName ;//从库名
    String tbName;//表名
    CountDownLatch endLock;
    int startIndex;
    int maxIndex;
    String masterDataSource;

    public SyncTask(int i, int nums,String masterDataSource, String dbName, String tbName, CountDownLatch endLock, int startIndex, int maxIndex){
        this.i =i;
        this.nums=nums;
        this.masterDataSource=masterDataSource;
        this.dbName=dbName;
        this.tbName =tbName;
        this.endLock =endLock;
        this.startIndex =startIndex;
        this.maxIndex = maxIndex;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        JDBCUtil salver  = new JDBCUtil(dbName);
        String sql = "select A.COLUMN_NAME,A.DATA_TYPE  from user_tab_columns A\n" +
                "where TABLE_NAME='"+tbName.toUpperCase()+"'";
        List<Map<String,Object>> list = salver.excuteQuery(sql,new Object[][]{});
        sql =" select ";
        String dataType;
        String column;
        int size = list.size();
        int k=0;

        for (Map<String,Object> column2Type:  list ) {
            k++;
            dataType = column2Type.get("DATA_TYPE")+"";
            column = column2Type.get("COLUMN_NAME")+"";
            if ("DATE".equals(dataType)){
                column= " to_char("+column+",'yyyy-mm-dd hh24:mi:ss') "+column;
            }
            else if ("CLOB".equals(dataType) || "BLOB".equals(dataType) || "LONG RAW".equals(dataType)){
                column= " to_char("+column+") "+column;
            }
            sql+= column;
            if (k!=size) sql+=",";
        }
        sql += " from "+tbName;

        String   querySql =" SELECT * FROM  ( SELECT A.*, ROWNUM RN  FROM (SELECT * FROM ("+sql+")t ) A  " +
                    "WHERE ROWNUM <= " + maxIndex+
                    ")  \n" +
                    "WHERE RN > "+startIndex;

        logger.info("当前线程 : "+Thread.currentThread().getName() +querySql );
        Map<String,Object> result = salver.excuteQueryWithMuliResult(querySql,new Object[][]{});
       JDBCUtil master =new JDBCUtil(masterDataSource);

     int len = 0;
        master.batchInsert(tbName,result);
        endLock.countDown();//计时器减1
        return  len ;

    }
}
