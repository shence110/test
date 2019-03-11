package com.neo.mapper.gwsbim;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Auther: Administrator
 * @Date: 2019/2/14/014 17:12
 * @Description:
 */
public  interface GwsbimMapper {


    List<Map<String,Object>> getAll(Map<String,Object> map);

    List<Map<String,Object>> selectAll(Map<String,Object> map);



    List<Map<String,Object>> selectTableStructure(String tbName);
}
