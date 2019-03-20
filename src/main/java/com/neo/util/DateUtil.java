package com.neo.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    /**
     *
     * Description: 日期格式
     * @param date 日期
     * @param pattern 格式
     * @return 日期字符串
     * @see
     */
    public static String formatDate(Date date, String pattern) {
        if (date==null) {return null;}
        return new SimpleDateFormat(pattern).format(date);
    }

    public static void main(String[] args) {
//        List<String> names = Lists.newArrayList("John,a,a");
//
        String a =  " pvar_BIM_RANK=='0' or pvar_BIM_RANK=='1' or pvar_BIM_RANK=='2' ".replaceAll("'","\"") ;
        System.out.println(a);
        }
}
