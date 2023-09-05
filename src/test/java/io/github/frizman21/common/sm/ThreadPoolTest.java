package io.github.frizman21.common.sm;

import static org.junit.Assert.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ThreadPoolTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		runner = new RunCounter();
	}
	
	class RunCounter implements Runnable {
		int count = 0;
		int pause = 0;
		
		public void run() {
			System.out.println(Thread.currentThread().getName()+" is Running Delayed Task");
			
			if(pause != 0) {
				try {
					Thread.sleep(pause);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			count++;
		}
	}

	RunCounter runner;
	
	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void RunOneTask() throws InterruptedException {

		int threadCount = 1;
		ScheduledThreadPoolExecutor scheduledPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(threadCount);
		
		scheduledPool.schedule(runner, 100, TimeUnit.MILLISECONDS);

		Thread.sleep(200);
		
		assertEquals(1, runner.count);
		
		scheduledPool.shutdown();
		
	}

	@Test
	public void RunFixedRateTask() throws InterruptedException {
		
		int threadCount = 1;
		ScheduledThreadPoolExecutor scheduledPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(threadCount);
		
		scheduledPool.scheduleAtFixedRate(runner, 0, 100, TimeUnit.MILLISECONDS);
		
		Thread.sleep(305);
		
		assertEquals(4, runner.count);
		
		scheduledPool.shutdown();
	}
	
	@Test
	public void RunFixedDelayTask() throws InterruptedException {
		
		int threadCount = 1;
		ScheduledThreadPoolExecutor scheduledPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(threadCount);
		
		runner.pause = 50;
		
		scheduledPool.scheduleWithFixedDelay(runner, 0, 100, TimeUnit.MILLISECONDS);
		
		Thread.sleep(605);
		
		assertEquals(4, runner.count);
		
		scheduledPool.shutdown();
	}
	
	@Test
	public void RunFixedRateCancelle() throws InterruptedException {
		
		int threadCount = 1;
		ScheduledThreadPoolExecutor scheduledPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(threadCount);
		
		ScheduledFuture<?> t = 
				scheduledPool.scheduleAtFixedRate(runner, 0, 100, TimeUnit.MILLISECONDS);
		
		Thread.sleep(50);
		
		t.cancel(false);
		
		Thread.sleep(200);
		
		assertEquals(1, runner.count);
		
		scheduledPool.shutdown();
	}

}
