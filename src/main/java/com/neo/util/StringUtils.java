package com.neo.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Auther: Administrator
 * @Date: 2019/2/19/019 17:33
 * @Description:
 */
public class StringUtils {


    // Clob类型 转String
    public static String ClobToString(Clob clob) throws SQLException, IOException {
        String ret = "";
        Reader read= clob.getCharacterStream();
        BufferedReader br = new BufferedReader(read);
        String s = br.readLine();
        StringBuffer sb = new StringBuffer();
        while (s != null) {
            sb.append(s);
            s = br.readLine();
        }
        ret = sb.toString();
        if(br != null){
            br.close();
        }
        if(read != null){
            read.close();
        }
        return ret;
    }

    /**
     *
     * @param blob
     * @return
     * @throws SQLException
     */
    public static String BlobToString(Blob blob ) throws SQLException {
            return new String(blob.getBytes((long)1, (int)blob.length()));
     }

    /**
     *
     * @param map
     * @return
     * @throws SQLException
     */
    public static String MapToString(Map<String, Object> map )  {
        return  new Gson().toJson(map);
    }

    /**
     * 解析json
     *
     * @param tbCollection
     * @return
     */
    private List<Map<String, Object>> getParamList(String tbCollection, String key) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject) jsonParser.parse(tbCollection);
        JsonArray jsonArr = jo.getAsJsonArray(key);
        Gson googleJson = new Gson();
        return googleJson.fromJson(jsonArr, ArrayList.class);
    }


}
