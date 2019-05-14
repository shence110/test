package com.neo.mthread;


import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;


public class IgnorePolicy implements RejectedExecutionHandler
{
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
	{

	}
}
