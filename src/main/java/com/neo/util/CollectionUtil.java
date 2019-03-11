package com.neo.util;

/**
 * @Auther: Administrator
 * @Date: 2019/2/18/018 11:18
 * @Description:
 */

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public  class CollectionUtil {

    public static List<List<Map<String,Object>>> splitList(List<Map<String,Object>> list , int groupSize){
        return  Lists.partition(list, groupSize);
    }

    // list类型 转String
    public static String ListToString(List<?> list) throws SQLException, IOException {
       return  Joiner.on(",").join(list);
    }

    // String类型 转 list
    public static List <String>  stringToList(String input) {

        return  Splitter.on(",").trimResults().splitToList(input.replace("[","").replace("]",""));

    }

    public static void main(String[] args) {

    }

}
