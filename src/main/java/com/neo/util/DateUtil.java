package com.neo.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * @Auther: Administrator
 * @Date: 2019/2/18/018 16:52
 * @Description:
 */
public class DateUtil {
    public static String splitDate(String orcDate ){

        return  orcDate.substring(0,orcDate.indexOf("."));
    }

    public static void main(String[] args) {
//        List<String> names = Lists.newArrayList("John,a,a");
//
        String a =  " pvar_BIM_RANK=='0' or pvar_BIM_RANK=='1' or pvar_BIM_RANK=='2' ".replaceAll("'","\"") ;
        System.out.println(a);
        }
}
