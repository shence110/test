package com.neo.mthread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


public class ConsumerFactory implements ThreadFactory
{
	private final AtomicLong num = new AtomicLong();

	@Override
	public Thread newThread(Runnable r)
	{
		return new Thread(r, "T-consumer-" + num.getAndIncrement());
	}
}
