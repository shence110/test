package com.neo.util;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: Administrator
 * @Date: 2019/3/21/021 15:43
 * @Description:
 */
public class DataSourceHelper {
    /**
     * 日志对象
     */
    private Logger logger = Logger.getLogger(JDBCUtil.class);


    static ApplicationContext context = SpringContextUtil.getApplicationContext();


    public static Connection GetConnection(String dbName) {
        Connection connection = null;

        try {
            String driver = context.getEnvironment().getProperty("spring.datasource."+dbName+".driverClassName");
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                System.out.println("加载驱动错误");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            String url = context.getEnvironment().getProperty("spring.datasource."+dbName+".url");
            String username = context.getEnvironment().getProperty("spring.datasource."+dbName+".username");
            String password = context.getEnvironment().getProperty("spring.datasource."+dbName+".password");
            connection = DriverManager.getConnection(url, username, password);

            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }





}
