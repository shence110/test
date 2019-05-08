package com.neo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.neo.util.CollectionUtil;
import com.neo.util.DbUtil;
import org.apache.log4j.Logger;
import org.codehaus.groovy.tools.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Date: 2019/2/19/019 10:42
 * @Description:
 */
@Service
public class TbService {

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

    /**
     *
     * @param dbName
     * @param page
     * @param rows
     * @param sort
     * @param order
     * @return
     */
    public Map<String,Object> getTableByDB(String dbName,String tbName, String page,
                                           String rows, String sort, String order, Connection connection) throws SQLException {
        List<Map<String,Object>>  tbCollection = null ;
        Map<String,Object> map =new HashMap<>();
        Map<String,Object> param =new HashMap<>();
        param.put("sort",sort);
        param.put("order",order);

        int  end= Integer.valueOf(page)*Integer.valueOf(rows)+1;
        int start = ( Integer.valueOf(page) -1 ) * Integer.valueOf(rows);
        DbUtil db = new DbUtil(connection);
        String sql =" select t.table_name, count_rows(t.table_name)  num_rows,\n" +
                "            ( select count(*) from user_tab_columns where table_name= t.table_name ) num_columns from user_tables t\n where 1=1 " ;

        if (null!=tbName && !"".equals(tbName.trim()) ) sql+=" and t.TABLE_NAME = '"+tbName+"'";

        String totalSql = "select count(*)  total from ("+sql +") t";
                if(sort!=null && !"".equals(sort)){
                    sql+= "  ORDER BY "+sort+" "+order;
                }

        int total =  db.getCount(totalSql,new Object[][]{});
        String newSql =" select * from ( select a.*,rownum rn from (" +sql +" )a where rownum < "+end+") where rn> "+start;


        tbCollection = db.excuteQuery(newSql,new Object[][]{});
        map.put("rows",tbCollection);
        map.put("total",total);
        return map;
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
    public Integer mergeData(String dbName ,String tbName,String masterDataSource, List<Map<String,Object>> list,int groupSize,
    Connection masterConn,Connection slaverConn
    ) throws Exception {

        DbUtil masterDbUtil =new DbUtil(masterConn);
        DbUtil salverDbUtil =new DbUtil(slaverConn);

        int insertCount =0;
        int count =0;
        int threads = Integer.valueOf(threadNum) ;//多线程数量
        List<Map<String,Object>> tableStructure = null;
        Map<String,Object> param =new HashMap<>();
        //查询被导入数据库的表结构
        List<Map<String, Object>> tb = selectTableStructureByDbAndTb(dbName, tbName,salverDbUtil);
        //该主库是否存在此表
        count = checkTable(tbName,masterDbUtil,null);


        if (0 == count) {//若不存在则主数据源创建新表
           // tableStructure = selectTableStructureByDbAndTb(dbName, tbName,masterConn);
            getCreateTableSql(tbName, tb, param);
            int i =createNewTable(param,masterDbUtil);

        }

        // 查询所有数据
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("tbName", tbName);

        long queryStart =System.currentTimeMillis();
        //查询某个库下的某个表的所有数据
        List<Map<String, Object>> data = selectAllByDbAndTb(dbName, tbName, paramsMap,salverDbUtil);
        if (data.size()==0) return 0;
        if (data.size()<groupSize) threads =1; //如果同步的数据太少 小于切割的数据条数 则只用一个线程
        Map<String,Object> m =(Map<String,Object>)data.get(0);
        long queryEnd =System.currentTimeMillis();
        logger.info("查询"+dbName+"库 中表名为"+tbName+"的所有数据花费时间为"+(queryEnd-queryStart)/1000+"秒");
        //对数据进行切分
        int cutSize = data.size() /threads ;//每个线程处理的数据量

        List<List<Map<String, Object>>> newData = CollectionUtil.splitList(data, cutSize);

        //批量删除重复的数据
        int delCount = batchDelete(paramsMap, tbName, data, masterDbUtil,salverDbUtil);

        if (threads == 1 ){ //不开启多线程
            for (List<Map<String, Object>> dat : newData) {
                insertCount += masterDbUtil. batchInsertJsonArry(tbName,dat,tb);
            }
        }else{
            final BlockingQueue<Future<Integer>> queue = new LinkedBlockingQueue<>();
            final CountDownLatch  endLock = new CountDownLatch(threads); //结束门
            List<Future<Integer>> results = new ArrayList<Future<Integer>>();
            final ExecutorService exec = Executors.newFixedThreadPool(threads);
            for (List<Map<String, Object>> dat : newData ) {
                Future<Integer> future= //(Future<Integer>) threadPoolUtils
                        exec.submit(new Callable<Integer>(){
                    @Override
                    public Integer call() {
                        try {

                            return masterDbUtil. batchInsertJsonArry(tbName,dat,tb);
                        }catch(Exception e) {
                            logger.error("数据同步 exception!",e);
                            return 0;
                        }finally {
                            endLock.countDown(); //线程执行完毕，结束门计数器减1
                        }

                    }
                });
                queue.add(future);
            }
            endLock.await(); //主线程阻塞，直到所有线程执行完成
            for(Future<Integer> future : queue)  insertCount +=future.get();
            exec.shutdown(); //关闭线程池
        }


        return insertCount;
    }

    /**
     * 批量删除重复的数据
     * @param paramsMap
     * @param tbName
     * @param data
     * @param masterDbUtil
     * @return
     * @throws Exception
     */
    private int batchDelete(Map<String, Object> paramsMap, String tbName, List<Map<String, Object>> data, DbUtil masterDbUtil,DbUtil salverDbUtil) throws Exception {
        //获得列表中的唯一键
        List<Map<String, Object>> uniqueList = getUniqueConstriant(paramsMap, masterDbUtil,salverDbUtil);
        //批量删除重复数据
        return masterDbUtil.batchDelete(data, uniqueList, tbName);

    }

    /**
     * 获得参数值
     * @param tbName
     * @param dat
     * @return
     */
    private Object[][] getParams(String tbName, List<Map<String, Object>> dat) {
        Map<String,Object> m =(Map<String,Object>)dat.get(0);
        Object[][] params =new Object[dat.size()][m.size()];
        for (int i = 0; i <dat.size() ; i++) {
            Map<String,Object> ma =(Map<String,Object>)dat.get(i);
            int j=0;
            for (String k:ma.keySet()) {
                params[i][j]= String.valueOf(ma.get(k));
                j++;
            }

        }
        return  params;
    }




    /**
     * 获得唯一约束
     * @param paramsMap
     * @param masterDbUtil
     * @return
     */
    private List<Map<String, Object>> getUniqueConstriant(Map<String, Object> paramsMap, DbUtil masterDbUtil,DbUtil salverDbUtil) throws Exception {
        List<Map<String, Object>> list = null;

        JSONArray constraint = JSONArray.parseArray(uniqueConstraint);

        JSONObject jsonObject =null;
        String tbName = paramsMap.get("tbName")+"";
        for (int i = 0; i <constraint.size() ; i++) {
            jsonObject = (JSONObject) constraint.get(i);
            if (tbName.equals(jsonObject.get("table")+"")){
                list = new ArrayList<>();
                Map<String,Object> map =new HashMap<>();
                map.put("COLUMN_NAME",jsonObject.get("column")+"");
                map.put("IS_NEED_DEL",jsonObject.get("isNeedDel"));
                list.add(map);
                break;
            }
        }
        if (null!= list)return list;
        String sql ="select cu.*,au.* from user_cons_columns cu, user_constraints au where cu.constraint_name=au.constraint_name and\n" +
                " cu.table_name='"+tbName+"'"+" and constraint_type = 'P' " ;
        list = salverDbUtil.excuteQuery(sql, new Object[][]{});

        if (list.size()==0){
            sql = "select t.*,i.index_type from user_ind_columns t,user_indexes i where t.index_name = i.index_name and\n" +
                    "\n" +
                    "t.table_name='"+tbName+"' \n" +
                    "and i.index_name in (\n" +
                    "select index_name from user_indexes where uniqueness='UNIQUE' and table_name='"+tbName+"'\n" +
                    ")";
            list = salverDbUtil.excuteQuery(sql, new Object[][]{});
        }
        if (list.size()==0){//判断该表是否存在eaf_Id
          int i =  checkTable(  tbName, salverDbUtil,"EAF_ID");
          if (i==1) {
              Map<String,Object> map =new HashMap<>();
              map.put("COLUMN_NAME","EAF_ID");
              list.add(map);
          }
        }

        for (Map<String,Object> map:list) {
            map.put("IS_NEED_DEL",true);
        }
        return list;
    }

    /**
     *
     * @param param
     * @return
     */
    private int createNewTable(Map<String,Object> param,DbUtil dbUtil ) {
        String sql = param.get("sql")+"";
        return dbUtil.executeUpdate(sql,new Object[][]{});
    }

    /**
     * 判断表是否存在
     * @param tbName
     * @return
     */
    private int checkTable( String tbName,DbUtil dbUtil,String Column) throws Exception{
        String sql = null ;
        if(Column ==null )
         sql =" SELECT COUNT(*) TABLE_NUMS FROM User_Tables WHERE table_name = '"+tbName+"' " ;
        else sql="select count(0) as TABLE_NUMS  from user_tab_columns   \n" +
                "where UPPER(column_name)='"+Column+"' AND TABLE_NAME = '"+tbName+"'";

        List<Map<String,Object>> list =dbUtil.excuteQuery(sql,new Object[][]{});
        String count =list.get(0).get("TABLE_NUMS")+"";
        if ("1".equals(count) ) return 1;
        else return 0;
    }

    /**
     * 通过库名和表名查询所有数据
     * @param dbName
     * @param tbName
     * @param paramsMap
     * @return
     */
    private List<Map<String,Object>> selectAllByDbAndTb(String dbName, String tbName,Map<String,Object> paramsMap,  DbUtil dbUtil) {
        String sql =" select * from "+tbName;
        return dbUtil.excuteQuery(sql,new Object[][]{});
    }

    /**
     * 通过库名和表名查询数据库表结构
     * @param dbName
     * @param tbName
     * @return
     */
    private List<Map<String,Object>> selectTableStructureByDbAndTb(String dbName, String tbName, DbUtil dbUtil) {
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
        return dbUtil.excuteQuery(sql,new Object[][]{});
    }


    /**
     * 获取创建表的sql
     * @param tbName
     * @param tableStructure
     * @param param
     */
    private void getCreateTableSql(String tbName, List<Map<String, Object>> tableStructure, Map<String, Object> param) throws IOException, SQLException {
        String primarySql ="";
        String dataType = null; //数据类型
        String columnName =null ;//列名
        List<String> primaryList =new ArrayList<>();
        String sql =" CREATE TABLE "+tbName+" (\n";
        int k=0;
        for (Map<String,Object> map :tableStructure ) {
            dataType = map.get("DATA_TYPE")+"" ;
            columnName  = map.get("COLUMN_NAME")+"" ;
            k++;
            if ("P".equals(map.get("IS_PRIMARY")+"")){
                primaryList.add(columnName);
            }
            sql+= " "+map.get("COLUMN_NAME") +" "+dataType ;
            if (null!= map.get("DATA_LENGTH") ){
                if ("DATE".equals(dataType)||"CLOB".equals(dataType)
                        ||"BLOB".equals(dataType)  ||"LONG RAW".equals(dataType)
                        ) sql+= " " ;
                else sql+= " (" + map.get("DATA_LENGTH")+")";
            }
            if ("N".equals(map.get("NULLABLE"))){
                sql+=" NOT NULL "  ;
            }
            if ("U".equals(map.get("IS_UNIQUE")+"")){
                sql+=" UNIQUE "  ;
            }
            if (k==tableStructure.size() && primaryList.size() == 0 ){
                sql +=" \n";
            }else  sql +=" ,\n";
        }
        if (primaryList.size() > 0 ){
            primarySql = " PRIMARY KEY("+CollectionUtil.ListToString(primaryList)+")";
        }
        sql += primarySql +"   ) ";
        param.put("sql",sql);
    }

    /**
     * 获得所有数据库
     * @param dbArray
     * @param masterDataSource
     * @return
     */
    public List<Map<String,Object>> getAllByDB(String dbArray, String masterDataSource) {
        List<Map<String,Object>> list =new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(dbArray);
        JSONObject jsonObject =null;

        for (int i = 0; i <jsonArray.size() ; i++) {
            jsonObject = (JSONObject) jsonArray.get(i);
            Map<String,Object> map =new HashMap<>();
            map.put("id",jsonObject.get("value")+"");
            map.put("text",jsonObject.get("text")+"");

            if (masterDataSource.equals(jsonObject.get("value")+"")){
                map.put("IS_MASTER",1);
                map.put("selected",true);

            }else map.put("IS_MASTER",0);
            list.add(map);
        }

        return  list;
    }
}
