package com.neo.service;

import com.neo.util.CollectionUtil;
import com.neo.util.DbUtil;
import com.neo.util.JDBCUtil;
import com.neo.util.SyncTask;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Auther: Administrator
 * @Date: 2019/5/13/013 16:55
 * @Description:
 */
@Service
public class TableService {

    /**
     * 日志对象
     */
    private Logger logger = Logger.getLogger(TbService.class);

    @Value("${spring.master.datasource}")
    public String masterDataSource;

    @Value("${spring.dbs}")
    public String dbArray;

    @Value("${groupSize}")
    public String groupSize;

    @Value("${uniqueConstraint}")
    public String uniqueConstraint;

    @Value("${threadNum}")
    public String threadNum;


    int getThreads(int dataCount){
        if (dataCount>1000 && dataCount<=10000){
            return dataCount%1000==0? dataCount/1000:dataCount/1000+1;
        }
        if (dataCount>10000 && dataCount<=100000){
            return dataCount%2000==0? dataCount/2000:dataCount/2000+1;
        }
        if (dataCount>100000 && dataCount<=1000000){
            return dataCount%5000==0? dataCount/5000:dataCount/5000+1;
        }
        if (dataCount>1000000 && dataCount<=10000000){
            return dataCount%10000==0? dataCount/10000:dataCount/10000+1;
        }
        return 1;
    }

    int getGroupSize(int dataCount){
        if (dataCount>1000 && dataCount<=10000){
            return 1000;
        }
        if (dataCount>10000 && dataCount<=100000){
            return 2000;
        }
        if (dataCount>100000 && dataCount<=1000000){
            return 5000;
        }
        if (dataCount>1000000 && dataCount<=10000000){
            return 10000;
        }
        return 1000;
    }

    /**
     * 合并数据
     * @param dbName
     * @param tbName
     * @param masterDataSource
     * @param list
     * @param groupSize
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public Integer mergeData(String dbName , String tbName, String masterDataSource, List<Map<String,Object>> list, int groupSize,
                             Connection masterConn, Connection slaverConn
    ) throws Exception {
        //从库
        JDBCUtil salverDbUtil = new JDBCUtil(dbName);//从库
        JDBCUtil master  =new JDBCUtil(masterDataSource);//主库
        int count = getCount(dbName,tbName);//获得从库的数据条数
        int thrednum = getThreads(count) ;//线程数量
        int nums = getGroupSize(count);
        //查询被导入数据库的表结构
        List<Map<String, Object>> tb = selectTableStructureByDbAndTb(dbName, tbName,salverDbUtil);
        //该主库是否存在此表
        count = checkTable(tbName,null);
        if (0 == count) {//若不存在则主数据源创建新表
            int i = createNewTable(master,salverDbUtil ,tbName, tbName);
        }

        final CountDownLatch  endLock = new CountDownLatch(thrednum); //结束门
        ExecutorService service = Executors.newFixedThreadPool(thrednum);
        BlockingQueue<Future<Integer>> queue = new LinkedBlockingQueue<Future<Integer>>();

        for (int i = 0; i <thrednum ; i++) {
            int startIndex = i * nums;
            int maxIndex = startIndex + nums;
            Future<Integer> future = service.submit(new SyncTask(i, nums,dbName,tbName,endLock,startIndex,maxIndex));

        }



       return 0;
    }



    private int getCount(String  dbName,String tbName) {
        String sql =" select count(1) from "+tbName;
        JDBCUtil util =new JDBCUtil(dbName);
        return util.getCount(sql,new Object[][]{});

    }

    private List<Map<String,Object>> selectAllByDbAndTb(String dbName, String tbName, Integer startIndex, Integer maxIndex) {
        String sql = " select * from "+tbName;
        if (null !=startIndex && null !=maxIndex)   {
            sql =" SELECT * FROM  ( SELECT A.*, ROWNUM RN  FROM (SELECT * FROM "+tbName+") A  " +
                    "WHERE ROWNUM <= " + maxIndex+
                    ")  \n" +
                    "WHERE RN > "+startIndex;
        }


        logger.info(sql);
        JDBCUtil slaver = new JDBCUtil(dbName);
        return slaver.excuteQuery(sql,new Object[][]{});
    }

    private int createNewTable(JDBCUtil master, JDBCUtil salverDbUtil, String tbName, String dbName) {
    String sql = "select  to_char(dbms_metadata.get_ddl('TABLE','"+tbName.toUpperCase()+"')) TB_SQL from dual";
        List<Map<String, Object>> list = salverDbUtil.excuteQuery(sql,new Object[][]{});
         sql  =list.get(0).get("TB_SQL")+"";
         sql = sql.replace(dbName.toUpperCase(),masterDataSource.toUpperCase());
       return   master.executeUpdate(sql,new Object[][]{});
    }

    private void getCreateTableSql(String tbName,JDBCUtil until) throws IOException, SQLException {

    }

    private int checkTable(String tbName,String Column) {
        JDBCUtil master = new JDBCUtil(masterDataSource);
        String sql = null ;
        if(Column ==null )
            sql =" SELECT COUNT(*) TABLE_NUMS FROM User_Tables WHERE table_name = '"+tbName+"' " ;
        else sql="select count(0) as TABLE_NUMS  from user_tab_columns   \n" +
                "where UPPER(column_name)='"+Column+"' AND TABLE_NAME = '"+tbName+"'";

        List<Map<String,Object>> list =master.excuteQuery(sql,new Object[][]{});
        String count =list.get(0).get("TABLE_NUMS")+"";
        if ("1".equals(count) ) return 1;
        else return 0;
    }

    private List<Map<String,Object>> selectTableStructureByDbAndTb(String dbName, String tbName, JDBCUtil util) {
        String sql ="select t.COLUMN_NAME,  t.DATA_TYPE, t.DATA_LENGTH,\n" +
                "        t.DATA_PRECISION, t.NULLABLE, t.COLUMN_ID, c.COMMENTS,\n" +
                "                (\n" +
                "        select a.CONSTRAINT_TYPE\n" +
                "        from user_constraints  a,USER_CONS_COLUMNS b\n" +
                "        where   a.CONSTRAINT_TYPE ='P'\n" +
                "              and a. constraint_name=b.constraint_name\n" +
                "              and a.table_name =  '"+tbName +"' and b.column_name = t.COLUMN_NAME\n" +
                "        ) IS_PRIMARY,\n" +
                "           (\n" +
                "        select a.CONSTRAINT_TYPE\n" +
                "        from user_constraints  a,USER_CONS_COLUMNS b\n" +
                "        where   a.CONSTRAINT_TYPE ='P'\n" +
                "              and a. constraint_name=b.constraint_name\n" +
                "              and a.table_name =  '"+tbName+"'  and b.column_name = t.COLUMN_NAME\n" +
                "        ) IS_UNIQUE\n" +
                "\n" +
                "        from user_tab_columns t, user_col_comments c\n" +
                "\n" +
                "        where t.table_name = c.table_name  and t.column_name = c.column_name\n" +
                "\n" +
                "        and t.table_name =  '"+tbName+"'";
        return  util.excuteQuery(sql,new Object[][]{});
    }
}
