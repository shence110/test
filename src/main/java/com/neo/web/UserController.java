package com.neo.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neo.service.TbService;
import com.neo.util.JDBCUtil;
import com.neo.util.SpringContextUtil;
import com.neo.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/")
public class UserController {


    @Autowired
    TbService tbService;

    @Value("${spring.master.datasource}")
    public String masterDataSource;

    @Value("${spring.dbs}")
    public String dbArray;

    @Value("${groupSize}")
    public String groupSize;






    @RequestMapping("/")
    public String index(Model model, Long id) {
        //        return "/admin-help";
        return "/queryAllTable";
    }

    @RequestMapping("/getAllByDB")
    @ResponseBody
    public String getAllByDB() {

        Environment environment = SpringContextUtil.getApplicationContext().getEnvironment();
//        String masterDataSource = environment.getProperty("spring.master.datasource");
//        String dbArray = environment.getProperty("spring.dbs");
        List<Map<String, Object>> list = null ;
        Map<String,Object> result =new HashMap<>();
        try{
            list= tbService.getAllByDB(dbArray, masterDataSource);
            result.put("list",list);
            System.out.println("成功！");
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
                                 String rows, String sort, String order) {
        Map<String, Object> map = null;
        try {
            map = tbService.getTableByDB(dbName, page, rows, sort, order);

        } catch (Exception e) {
            map.put("err", true);
            map.put("content", e.getMessage());
        }
        return StringUtils.MapToString(map);
    }

    @RequestMapping("/mergeData")
    @ResponseBody
    public String mergeData(String dbName, String tbCollection) throws Exception {

        int groupSiz = Integer.valueOf(   groupSize  );

        List<Map<String, Object>> list = getParamList(tbCollection, "tbs");

        List<Map<String, String>> result = new ArrayList<>();
        Map<String,Object> resu =new HashMap<>();
        String tbName = null;
        try{
            for (Map<String, Object> map : list) {
                tbName = map.get("TABLE_NAME") + "";
                Map<String, String> resultMap = new HashMap();
                resultMap.put("TABLE_NAME", tbName);
                resultMap.put("INSERT_COUNT", tbService.mergeData(dbName, tbName, masterDataSource, list, groupSiz) + "");
                result.add(resultMap);
            }
            resu.put("list",result);

        }catch (Exception e){
            resu.put("err",true);
            resu.put("content",e.getMessage());
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