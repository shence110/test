package com.neo.mapper.bimall;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Auther: Administrator
 * @Date: 2019/2/14/014 17:12
 * @Description:
 */
public interface BimallMapper {


    List<Map<String,Object>> getAll(Map<String,Object> map);


    int createNewTable(Map<String, Object> param);

    int checkTable(String tbName);

    List<Map<String,Object>>  selectAll(String tbName);

    int delete(Map<String, Object> param);

    int insert(Map<String, Object> param);

    List<Map<String,Object>> selectTableStructure(String tbName);
}
