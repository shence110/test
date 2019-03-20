package com.neo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neo.mapper.bimall.BimallMapper;
import com.neo.mapper.eafbim.EafbimMapper;
import com.neo.mapper.gwsbim.GwsbimMapper;
import com.neo.mapper.wcjbim.WcjbimMapper;
import com.neo.util.*;
import com.sun.corba.se.spi.ior.ObjectKey;
import oracle.sql.BLOB;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.DateUtils;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    @Autowired
    private EafbimMapper eafbimMapper;

    @Autowired
    private BimallMapper bimallMapper;

    @Autowired
    private GwsbimMapper gwsbimMapper;

    @Autowired
    private WcjbimMapper wcjbimMapper;

    @Value("${spring.master.datasource}")
    public String masterDataSource;

    @Value("${spring.dbs}")
    public String dbArray;

    @Value("${groupSize}")
    public String groupSize;

    @Value("${uniqueConstraint}")
    public String uniqueConstraint;

    /**
     *
     * @param dbName
     * @param page
     * @param rows
     * @param sort
     * @param order
     * @return
     */
    public Map<String,Object> getTableByDB(String dbName, String page,
                                                 String rows, String sort, String order){
        List<Map<String,Object>>  tbCollection = null ;
        Map<String,Object> map =new HashMap<>();
        Map<String,Object> param =new HashMap<>();
        param.put("sort",sort);
        param.put("order",order);

        Environment environment = SpringContextUtil.getApplicationContext().getEnvironment();
        String masterDataSource = environment.getProperty("spring.master.datasource");

        if ("".equals(dbName)) dbName =masterDataSource;

        JDBCUtil jdbcUtil =new JDBCUtil(dbName);
        String sql =" select t.table_name, count_rows(t.table_name)  num_rows,\n" +
                "            ( select count(*) from user_tab_columns where table_name= t.table_name ) num_columns from user_tables t\n" ;
                if(sort!=null && !"".equals(sort)){
                    sql+= "  ORDER BY "+sort+" "+order;
                }

         tbCollection =  jdbcUtil.excuteQuery(sql,new Object[][]{});


        logger.info(new Gson().toJson(tbCollection));

        map.put("rows",tbCollection);
        map.put("total",tbCollection.size());
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
    public Integer mergeData(String dbName ,String tbName,String masterDataSource, List<Map<String,Object>> list,int groupSize) throws IOException, SQLException, ExecutionException, InterruptedException {
        int insertCount =0;
        int count =0;
        List<Map<String,Object>> tableStructure = null;
        Map<String,Object> param =new HashMap<>();
        //查询被导入数据库的表结构
        List<Map<String, Object>> tb = selectTableStructureByDbAndTb(dbName, tbName);
        //该主库是否存在此表
        count = checkTable(tbName);


        if (0 == count) {//若不存在则创建新表
            tableStructure = selectTableStructureByDbAndTb(dbName, tbName);
            getCreateTableSql(tbName, tableStructure, param);
            int i =createNewTable(param);

        }

        // 查询所有数据
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("tbName", tbName);

        //查询某个库下的某个表的所有数据
        List<Map<String, Object>> data = selectAllByDbAndTb(dbName, tbName, paramsMap);

        //对数据进行切分
        List<List<Map<String, Object>>> newData = CollectionUtil.splitList(data, groupSize);
        for (int i = 0; i < newData.size(); i++) {
            paramsMap.put("list", newData.get(i));
            //删除合并库多余的数据
            int delCount = deleteData(paramsMap,dbName);

        }
        //插入新数据
        for (List<Map<String, Object>> dat : newData) {
            String sqlInsert = getInsertSql(tbName, dat, newData, tb);


            sqlInsert += " SELECT 1 FROM DUAL ";

            paramsMap.put("sqlInsert", sqlInsert);


            insertCount += insert(paramsMap);


        }

        return insertCount;
    }

    /**
     * 主数据源 插入数据
     * @param paramsMap
     * @return
     */
    private int insert(final Map<String,Object> paramsMap) throws ExecutionException, InterruptedException {
        JDBCUtil jdbcUtil =new JDBCUtil(masterDataSource);
        String sql = paramsMap.get("sqlInsert")+"";
        return jdbcUtil.executeUpdate(sql,new Object[][]{});
//        Future<?> future =  ThreadPoolUtils.getInstance().submit(new Callable<Integer>(){
//            @Override
//            public Integer call() {
//                Date startDatetime,endDatetime;
//                try {
//                    startDatetime = new Date();
//                    return jdbcUtil.executeUpdate(sql,new Object[][]{});
//                }catch(Exception e) {
//                    logger.error("数据同步 exception!",e);
//                    return 0;
//                }
//
//            }
//        });
//        int count = (Integer) future.get();
//        System.out.println("成功执行"+count+"条数据");
//       return count ;

    }

    /**
     * 删除数据
     * @param paramsMap
     * @param dbName
     * @return
     */
    private int deleteData(Map<String,Object> paramsMap,String dbName) {
        String columnName = null;
        int k=0;
        int j=0;

        List<Map<String, Object>> uniqueList =  getUniqueConstriant(paramsMap,dbName);
        //需要删除重复数据
        List<Map<String,Object>> list = (List<Map<String, Object>>) paramsMap.get("list");

        String sql = " DELETE  FROM   " +paramsMap.get("tbName")+" where 1=1 ";
        //如果只有一个唯一键
        if (uniqueList.size()==1){
             columnName =uniqueList.get(0).get("COLUMN_NAME")+"";
            sql+= " and " +columnName+" IN (" ;
             k=0;
            for (Map<String,Object> map:  list) {
                k++;
                if (k==list.size())sql += "'"+map.get(columnName)+"'";
                else sql += "'"+map.get(columnName)+"',";

            }
            sql +=")";
        }else if((uniqueList.size()>1)){
            j= 0;
            for (Map<String,Object> map: list)  {
                sql += " or (" ;
                k=0;
                j++;
                for (Map<String, Object> uniqueMap: uniqueList) {
                    k++;
                    columnName = uniqueMap.get("COLUMN_NAME")+"";
                    if (k==1)
                    sql += columnName +" = " + "'"+map.get(columnName)+"'";
                    if (k>1 && k<uniqueMap.size())
                        sql += " and " +columnName+" = "+ "'"+map.get(columnName)+"'";
                    if (k==uniqueMap.size())
                         sql+=" and " +columnName+" = "+ "'"+map.get(columnName)+"')";
                }
                sql += " ) ";

            }
        }

        JDBCUtil jdbcUtil =new JDBCUtil(masterDataSource);
        return jdbcUtil.executeUpdate(sql,new Object[][]{});
    }

    /**
     *获得唯一约束
     * @param paramsMap
     * @param dbName
     * @return
     */
    private List<Map<String, Object>> getUniqueConstriant(Map<String,Object> paramsMap,String dbName) {
        List<Map<String, Object>> list = null;
        JDBCUtil jdbcUtil =new JDBCUtil(dbName);
        JSONArray constraint = JSONArray.parseArray(uniqueConstraint);

        JSONObject jsonObject =null;
        String tbName = paramsMap.get("tbName")+"";
        for (int i = 0; i <constraint.size() ; i++) {
            jsonObject = (JSONObject) constraint.get(i);
            if (tbName.equals(jsonObject.get("table")+"")){
                list = new ArrayList<>();
                Map<String,Object> map =new HashMap<>();
                map.put("COLUMN_NAME",jsonObject.get("column")+"");
                list.add(map);
                break;
            }
        }
        if (null!= list)return list;
        String sql ="select cu.*,au.* from user_cons_columns cu, user_constraints au where cu.constraint_name=au.constraint_name and\n" +
                " cu.table_name='"+tbName+"'"+" and constraint_type = 'P' " ;
        list= jdbcUtil.excuteQuery(sql,new Object[][]{});
        if (list.size()==0){
            sql = "select t.*,i.index_type from user_ind_columns t,user_indexes i where t.index_name = i.index_name and\n" +
                    "\n" +
                    "t.table_name='"+tbName+"' \n" +
                    "and i.index_name in (\n" +
                    "select index_name from user_indexes where uniqueness='UNIQUE' and table_name='"+tbName+"'\n" +
                    ")";
            list =  jdbcUtil.excuteQuery(sql,new Object[][]{});
        }
        return list;
    }

    /**
     *
     * @param param
     * @return
     */
    private int createNewTable(Map<String,Object> param) {
        JDBCUtil jdbcUtil =new JDBCUtil(masterDataSource);
        String sql = param.get("sql")+"";
        return jdbcUtil.executeUpdate(sql,new Object[][]{});
    }

    /**
     *
     * @param tbName
     * @return
     */
    private int checkTable( String tbName) {
        JDBCUtil jdbcUtil =new JDBCUtil(masterDataSource);
        String sql =" SELECT COUNT(*) TABLE_NUMS FROM User_Tables WHERE table_name = '"+tbName+"' " ;
        List<Map<String,Object>> list = jdbcUtil.excuteQuery(sql,new Object[][]{});
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
    private List<Map<String,Object>> selectAllByDbAndTb(String dbName, String tbName,Map<String,Object> paramsMap) {
        JDBCUtil jdbcUtil =new JDBCUtil(dbName);
        String sql =" select * from "+tbName;

        return jdbcUtil.excuteQuery(sql,new Object[][]{});
    }

    /**
     * 通过库名和表名查询数据库表结构
     * @param dbName
     * @param tbName
     * @return
     */
    private List<Map<String,Object>> selectTableStructureByDbAndTb(String dbName, String tbName) {
        JDBCUtil jdbcUtil =new JDBCUtil(dbName);
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
        return  jdbcUtil.excuteQuery(sql,new Object[][]{});
    }

    /**
     * 获取插入sql
     * @param tbName
     * @param newData
     * @param tb
     * @return
     */
    private String getInsertSql(String tbName,List<Map<String,Object>> dat, List<List<Map<String, Object>>> newData,List<Map<String,Object>> tb) throws IOException, SQLException {
       int test = 0;
        String sqlInsert = null;
        long k= 0;
        String value =null;

            sqlInsert =" insert ALL " ;
            for (int j = 0; j <dat.size() ; j++) {
                Map<String,Object> map =dat.get(j);


                k= 0 ;
                sqlInsert +="  into " + tbName +" ( ";
                for (String key:  map.keySet()) {
                    k++;

                    if ( k ==map.size() ){
                        sqlInsert += " "+ key+"" ;
                    }
                    else  sqlInsert += " "+ key+", " ;
                }
                sqlInsert +=") values  " ;

                sqlInsert +=" (" ;

                k= 0 ;
                for (String key:  map.keySet()) {

                    k++;
                    //如果当前列名是最后一个 则拼接sql时不跟逗号


                    if ( k ==map.size() ){

                        boolean flag =false;
                        for ( Map<String,Object> ma: tb) {
                            if ( key.equals(ma.get("COLUMN_NAME")+"")
                                    && "DATE".equals(ma.get("DATA_TYPE"))&& map.get(key)!=null  ){
                                sqlInsert += " TO_DATE('"+ DateUtil.splitDate(map.get(key)+"") +"',  'YYYY/MM/DD HH24:mi:ss' ) " ;
                                flag =true ;
                                break;

                            }
                            if ( key.equals(ma.get("COLUMN_NAME")+"")
                                    && ("CLOB".equals(ma.get("DATA_TYPE")) ||"BLOB".equals(ma.get("DATA_TYPE"))  )&& map.get(key)!=null  ){
                                sqlInsert += " '"+getValueByType(map,key,ma.get("DATA_TYPE")+"")+"' ";
                                flag =true ;
                                break;
                            }
                        }
                        if (!flag){
                            if (map.get(key)==null) sqlInsert += " null  ";
                            else sqlInsert += " '"+  (map.get(key)+"").replace("'","\"") +"'  " ;
                        }

                    }
                    else {

                        boolean flag =false;
                        int mmm =0;
                        for ( Map<String,Object> ma: tb) {
                            mmm ++;


                            if ( key.equals(ma.get("COLUMN_NAME")+"")
                                    && "DATE".equals(ma.get("DATA_TYPE")) && map.get(key)!=null  ){
                                value = map.get(key)+"";

                                sqlInsert += " TO_DATE('"+ DateUtil.splitDate(value)+"',  'YYYY/MM/DD HH24:mi:ss' ) , " ;
                                flag =true ;
                                break;
                            }
                            if ( key.equals(ma.get("COLUMN_NAME")+"")
                                    && ("CLOB".equals(ma.get("DATA_TYPE")) ||"BLOB".equals(ma.get("DATA_TYPE")) ) && map.get(key)!=null ){
                                sqlInsert += " '"+getValueByType(map,key,ma.get("DATA_TYPE")+"")+"', ";
                                flag =true ;
                                break;
                            }

                        }
                        if (!flag){
                            if (map.get(key)==null) sqlInsert += " null , ";
                            else sqlInsert += " '"+  (map.get(key)+"").replace("'","\"")+"', " ;
                        }

                    }

                }

                sqlInsert += " ) " ;


            }


        return sqlInsert;

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

    /**
     * 获取创建表的sql
     * @param tbName
     * @param tableStructure
     * @param param
     */
    private void getCreateTableSql(String tbName, List<Map<String, Object>> tableStructure, Map<String, Object> param) throws IOException, SQLException {
        String primarySql ="";
        List<String> primaryList =new ArrayList<>();
        String sql =" CREATE TABLE "+tbName+" (\n";
        int k=0;
        for (Map<String,Object> map :tableStructure ) {
            k++;
            if ("P".equals(map.get("IS_PRIMARY")+"")){
                primaryList.add(map.get("COLUMN_NAME")+"");
            }
            sql+= " "+map.get("COLUMN_NAME") +" "+map.get("DATA_TYPE") ;
            if (null!= map.get("DATA_LENGTH") ){
                if ("DATE".equals(map.get("DATA_TYPE"))||"CLOB".equals(map.get("DATA_TYPE"))
                        ||"BLOB".equals(map.get("DATA_TYPE"))
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
