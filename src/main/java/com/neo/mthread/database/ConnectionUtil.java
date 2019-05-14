package com.neo.mthread.database;

import com.neo.util.SpringContextUtil;
import org.springframework.context.ApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectionUtil
{
	private static final ApplicationContext context = SpringContextUtil.getApplicationContext();


	/**
	 * 根据数据库名称(dbName)获取一个{@code Connection}实例。
	 *
	 * @param dbName 数据库名
	 * @return java.Sql.Connection
	 * @exception RuntimeException 如果数据库连接异常
	 */
	public static Connection getConnection(String dbName)
	{
		try
		{
			Connection conn = DriverManager.getConnection(driver(dbName), username(dbName), password(dbName));
			conn.setAutoCommit(false);
			return conn;
		}
		catch (SQLException e)
		{
			throw new RuntimeException("Can not Access to database "
					+ dbName + ", please check url, username and password!", e);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Can not Access to database " + dbName + "!", e);
		}
	}

	private static String driver(String dbName)
	{
		return context.getEnvironment().getProperty("thit.db." + dbName + ".driver");
	}

	private static String username(String dbName)
	{
		return context.getEnvironment().getProperty("thit.db." + dbName + ".username");
	}

	private static String password(String dbName)
	{
		return context.getEnvironment().getProperty("thit.db." + dbName + ".password");
	}
}
