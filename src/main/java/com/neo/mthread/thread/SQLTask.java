package com.neo.mthread.thread;

import com.neo.mthread.database.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.Callable;


public class SQLTask
		implements Callable<Integer>
{
	private static final Logger logger = LoggerFactory.getLogger(SQLTask.class);
	private String dbName1;
	private String dbName2;
	private String tbName;
	private Integer fromRow;
	private Integer toRow;


	public SQLTask(String dbName1, String dbName2, String tbName, Integer fromRow, Integer toRow)
	{
		this.dbName1 = dbName1;
		this.dbName2 = dbName2;
		this.tbName = tbName;
		this.fromRow = fromRow;
		this.toRow = toRow;
	}

	@Override
	public Integer call() throws Exception
	{
		Connection conn = getConnection(dbName1);
		Statement stmt = conn.createStatement();

		String querySql;
		if (fromRow != null && toRow != null)
			querySql =
					"select * from " +
					"   (select A.*, ROWNUM RN from " +
					"       (select * from " + tbName + ") A " +
					"   WHERE ROWNUM <= 100) " +
					"WHERE RN > 10 ";
		else
			querySql = "select * from " + tbName;

		ResultSet rs = stmt.executeQuery(querySql);
		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();

		System.out.println("11111111111");


		StringBuilder updateSql = new StringBuilder(" insert into " + tbName + " (");
		StringBuilder partSql = new StringBuilder(" (");
		for (int i = 0; i < columnCount; i++)
		{
			String label = meta.getColumnLabel(i);
			if ("RN".equals(label))
			{
				continue;
			}
			updateSql.append(label).append(",");
			partSql.append("?,");
		}
		updateSql.deleteCharAt(updateSql.length() - 1);
		partSql.deleteCharAt(updateSql.length() - 1);
		updateSql.append(") values (");
		partSql.append(") ");
		updateSql.append(partSql);

		Connection conn2 = getConnection(dbName2);
		PreparedStatement pstmt = conn2.prepareStatement(updateSql.toString());


		long start = System.currentTimeMillis();

		int success = 0;
		while (rs.next())
		{
			for (int i = 1; i <= columnCount; i++)
			{
				pstmt.setObject(i, rs.getObject(i));
			}
			pstmt.addBatch();
//			success += pstmt.executeUpdate();
		}
		pstmt.executeBatch();
		conn2.commit();
		pstmt.clearBatch();
		
		long end = System.currentTimeMillis();

		logger.info("插入了" + success + "条数据, 耗时" + (end - start) + "ms");
		System.out.println("插入了" + success + "条数据, 耗时" + (end - start) + "ms");

		//todo:暂时这样关闭
		rs.close();
		stmt.close();
		pstmt.close();
		ConnectionManager.release(conn);
		ConnectionManager.release(conn2);
		return success;
	}

	private Connection getConnection(String dbName)
			throws InterruptedException
	{
		Connection conn;
		while (true)
		{
			conn = ConnectionManager.getConnection(dbName);
			if (conn != null)
			{
				break;
			}
			logger.trace("Thread " + Thread.currentThread().getName() + " wait connection for " + dbName);
			System.out.println("Thread " + Thread.currentThread().getName() + " wait connection for " + dbName);
			Thread.sleep(10);
		}
		return conn;
	}
}
