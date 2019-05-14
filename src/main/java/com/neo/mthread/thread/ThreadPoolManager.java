package com.neo.mthread.thread;

import com.neo.mthread.ConsumerFactory;
import com.neo.mthread.IgnorePolicy;
import com.neo.mthread.ProducerFactory;

import java.util.concurrent.*;


/**
 * 数据线程池管理
 */
public class ThreadPoolManager
{
	private static final TimeUnit seconds = TimeUnit.SECONDS;
	private static final BlockingQueue<Runnable> workQueue;
//	private final BlockingQueue<Runnable> objectQueue = new LinkedBlockingQueue<>(50);


	//生产者线程池，负责根据 task取对象到队列
	private static final ThreadPoolExecutor producerPool;
	//消费者线程池，负责根据 task存对象到数据库
//	private final ThreadPoolExecutor consumerPool = new ThreadPoolExecutor(10, 20, 60,
//			seconds, objectQueue, new ConsumerFactory(), new IgnorePolicy());

	static
	{
		workQueue = new LinkedBlockingQueue<>(50);
		producerPool = new ThreadPoolExecutor(10, 20, 60,
				seconds, workQueue, new ProducerFactory(), new IgnorePolicy());
	}

	public static Future<Integer> submit(SQLTask task)
	{
		return producerPool.submit(task);
	}

	public static boolean isEmpty()
	{
		return workQueue.isEmpty();
	}
}
