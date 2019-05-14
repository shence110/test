package com.neo.mthread.database;

import com.neo.util.SpringContextUtil;
import org.springframework.context.ApplicationContext;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * 数据库连接管理类
 *
 * 该类中使用到{@code poolMap}和{@code dbNameMap}的方法
 * 必须添加{@code synchronized}关键字
 */
public class ConnectionManager
{
	private static final ApplicationContext context = SpringContextUtil.getApplicationContext();

	private static final Map<String, Queue<Connection>> poolMap;
	private static final Map<Connection, String> dbNameMap;

	static {
		poolMap = new HashMap<>(20);
		dbNameMap = new HashMap<>(300);
	}


	//根据数据库名称(dbName)创建连接池，并将连接池放入{@code poolMap}中。
	private static synchronized void initConnectionPool(String dbName)
	{
		int poolSize = Integer.valueOf(context.getEnvironment().getProperty("thit.db.connection.pool.size"));

		Queue<Connection> pool = new LinkedList<>();
		for (int i = 0; i < poolSize; i++)
		{
			pool.add(ConnectionUtil.getConnection(dbName));
		}
		poolMap.put(dbName, pool);
	}

	/**
	 * 根据数据库名称(dbName)从连接池中获取一个{@code Connection}实例。
	 *
	 * @param dbName 数据库名
	 * @return java.Sql.Connection
	 */
	public static synchronized Connection getConnection(String dbName)
	{
		if (!poolMap.containsKey(dbName))
		{
			initConnectionPool(dbName);
		}

		Queue<Connection> pool = poolMap.get(dbName);
		if (!pool.isEmpty())
		{
			Connection conn = pool.poll();
			if (!dbNameMap.containsKey(conn))
			{
				dbNameMap.put(conn, dbName);
			}
			return conn;
		}
		return null;
	}

	/**
	 * 返回{@code Connection}实例到连接池中。
	 */
	public static synchronized void release(Connection conn)
	{
		String dbName = dbNameMap.get(conn);
		Queue<Connection> pool = poolMap.get(dbName);
		pool.add(conn);
	}

}
