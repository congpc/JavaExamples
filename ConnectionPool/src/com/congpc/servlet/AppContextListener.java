package com.congpc.servlet;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.TimerTask;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		// Your code here
		System.out.println("Listener initialized.");
		// create the thread pool
        ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 200, 50000L,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
        servletContextEvent.getServletContext().setAttribute("executor",executor);
        
//		TimerTask vodTimer = new VodTimerTask();
//		Timer timer = new Timer();
//		timer.schedule(vodTimer, 4000, (2 * 1000));
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		// Your code here
		//BatchSingleton.getInstance().detroyBatchProcessing();
		System.out.println("Listener has been shutdown");
		ThreadPoolExecutor executor = (ThreadPoolExecutor)servletContextEvent.getServletContext().getAttribute("executor");
        executor.shutdown();
	}

	class VodTimerTask extends TimerTask {
		@Override
		public void run() {
			System.out.println("TimerTask " + new Date().toString());
			//BatchSingleton.getInstance().executeBatchProcessing();
		}
	}
}
