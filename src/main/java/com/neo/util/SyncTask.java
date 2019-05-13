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

    @Value("${spring.master.datasource}")
    public String masterDataSource;

    int i; //线程序号
    int nums ;//一次同步数据量

    String dbName ;//从库名
    String tbName;//表名
    CountDownLatch endLock;
    int startIndex;
    int maxIndex;

    public SyncTask(int i, int nums, String dbName, String tbName, CountDownLatch endLock, int startIndex, int maxIndex){
        this.i =i;
        this.nums=nums;
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
        String   sql =" SELECT * FROM  ( SELECT A.*, ROWNUM RN  FROM (SELECT * FROM "+tbName+") A  " +
                    "WHERE ROWNUM <= " + maxIndex+
                    ")  \n" +
                    "WHERE RN > "+startIndex;

        logger.info("当前线程 : "+Thread.currentThread().getName() +sql );
        List<Map<String,Object>> list = salver.excuteQuery(sql,new Object[][]{});
        salver.excuteQuery(sql,new Object[][]{});
        return null;
    }
}
