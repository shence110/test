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

    /**
     * @param 返回java.sql.Date格式的
     * */
    public static java.sql.Date strToDate(String strDate) {
        String str = strDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = null;
        try {
            d = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        java.sql.Date date = new java.sql.Date(d.getTime());
        return date;
    }


}
