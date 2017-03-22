package com.congpc.servlet;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Your code here
		//BatchSingleton.getInstance().detroyBatchProcessing();
		System.out.println("Listener has been shutdown");
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		// Your code here
		System.out.println("Listener initialized.");
//		TimerTask vodTimer = new VodTimerTask();
//		Timer timer = new Timer();
//		timer.schedule(vodTimer, 4000, (2 * 1000));
	}

	class VodTimerTask extends TimerTask {
		@Override
		public void run() {
			System.out.println("TimerTask " + new Date().toString());
			//BatchSingleton.getInstance().executeBatchProcessing();
		}
	}
}
