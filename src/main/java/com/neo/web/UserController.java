package com.neo.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neo.service.TbService;
import com.neo.util.DataSourceHelper;
import com.neo.util.JDBCUtil;
import com.neo.util.SpringContextUtil;
import com.neo.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/")
public class UserController  {


    @Autowired
    TbService tbService;

    @Value("${spring.master.datasource}")
    public String masterDataSource;

    @Value("${spring.dbs}")
    public String dbArray;

    @Value("${groupSize}")
    public String groupSize;


    private Logger logger = Logger.getLogger(UserController.class);



    @RequestMapping("/")
    public String index() {
//     return "queryAllTable";
//return  "admin-help";
        return "user_list";
    }

    @RequestMapping("/getAllByDB")
    @ResponseBody
    public String getAllByDB() {

        List<Map<String, Object>> list = null ;
        Map<String,Object> result =new HashMap<>();
        try{
            list= tbService.getAllByDB(dbArray, masterDataSource);
            result.put("list",list);
        }catch (Exception e){
            result.put("err",true);
            result.put("content",e.getMessage());
            e.printStackTrace();
        }
        return StringUtils.MapToString(result);
    }


    @RequestMapping("/getTableByDB")
    @ResponseBody
    public String getTableByDB(String dbName, String page,
                                 String rows, String sort, String order) throws SQLException {
        Map<String, Object> map = null;
        Connection conn = null ;
        try {
            if (null== dbName || dbName.trim().equals("")) dbName =masterDataSource;
            conn = DataSourceHelper.GetConnection(dbName);
            map = tbService.getTableByDB(dbName, page, rows, sort, order,conn);

        } catch (Exception e) {
            map.put("err", true);
            map.put("content", e.getMessage());
        }finally {
            if (conn != null) {
                conn.close();
            }
        }
        return StringUtils.MapToString(map);
    }

    @RequestMapping("/mergeData")
    @ResponseBody
    public String mergeData(String dbName, String tbCollection) throws Exception {
        Connection masterConn = null ;
        Connection slaverConn = null;

        int groupSiz = 0; //每张表数据插入多次 一次插入的数据条数
        List<Map<String, Object>> list = null;
        List<Map<String, String>> result = new ArrayList<>();
        Map<String,Object> resu =new HashMap<>();
        String tbName = null;
        try{
            masterConn = DataSourceHelper.GetConnection(masterDataSource);
            slaverConn = DataSourceHelper.GetConnection(dbName);
          Long start =   System.currentTimeMillis();
            groupSiz= Integer.valueOf(   groupSize  );
            list = getParamList(tbCollection, "tbs");
            for (Map<String, Object> map : list) {
                tbName = map.get("TABLE_NAME") + "";
                Map<String, String> resultMap = new HashMap();
                resultMap.put("TABLE_NAME", tbName);
                resultMap.put("INSERT_COUNT", tbService.mergeData(dbName, tbName, masterDataSource, list, Integer.valueOf(groupSiz),masterConn,slaverConn)+"");
                result.add(resultMap);
            }
            resu.put("list",result);
            Long end =   System.currentTimeMillis();
            masterConn.commit();
            logger.info("插入数据所花费的时间为"+ (end-start) /1000 +"s");
        }catch (Exception e){
            resu.put("err",true);
            resu.put("content",e.getMessage());
        }finally {
            if (null!=masterConn){
                masterConn.close();
            }
            if (null!=slaverConn){
                slaverConn.close();
            }
        }

        return StringUtils.MapToString(resu);


    }

    /**
     * 解析json
     *
     * @param tbCollection
     * @return
     */
    private List<Map<String, Object>> getParamList(String tbCollection, String key)throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject) jsonParser.parse(tbCollection);
        JsonArray jsonArr = jo.getAsJsonArray(key);
        Gson googleJson = new Gson();
        return googleJson.fromJson(jsonArr, ArrayList.class);
    }


}