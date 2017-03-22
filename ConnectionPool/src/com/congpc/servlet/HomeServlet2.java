package com.congpc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//Demos: Async Servlet - Non Async Servlet - Thread - Thread Pool

//DemoType 1: http://qiita.com/opengl-8080/items/7d51351cb540269e1d05#%E9%9D%9E%E5%90%8C%E6%9C%9F%E5%87%A6%E7%90%86%E3%82%92%E5%AE%9F%E8%A3%85%E3%81%99%E3%82%8B
//DemoType 2: http://www.journaldev.com/2008/async-servlet-feature-of-servlet-3

//Async supported
@WebServlet(value="/async", initParams = {@WebInitParam(name="default", value="Demo AsyncServlet")}, asyncSupported = true)
//Async not supported
//@WebServlet(value="/", initParams = {@WebInitParam(name="default", value="Demo NonAsyncServlet")})
public class HomeServlet2 extends HttpServlet{
	public static ExecutorService pool = Executors.newFixedThreadPool(10);
	
	private int sleepSecs = 5000; // default: sleep 5s
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String defaultStr = "";
	@Override
    public void init(ServletConfig config) throws ServletException {
    		System.out.println("[HomeServlet] init() : hash=" + this.hashCode() + " | thread=" + Thread.currentThread().getName());
    		defaultStr = config.getInitParameter("default");
	}
	
	@Override
    public void destroy() {
        System.out.println("destroy() : hash=" + this.hashCode() + " | thread=" + Thread.currentThread().getName());
    }
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		AsyncContext ctx = req.startAsync();
		ctx.start(() -> {
			try {
		        	System.out.println("Async Supported? " + req.isAsyncSupported());
		    		String signature = "Signature";
		    		System.out.println("signature=" + signature);
		    		System.out.println("response=" + res.hashCode());
	        	    pool.execute(new HandelRequestProcessRunnable(signature , res) );
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        } finally {
	            ctx.complete();
	        }
		});
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		//But only if the POST data is encoded as key-value pairs of content type: "application/x-www-form-urlencoded" 
		//like when you use a standard HTML form.
		String time = req.getParameter("time");
		if (time != null) {
			sleepSecs = Integer.valueOf(time); 
			if (sleepSecs > 10000)
				sleepSecs = 10000; //max 10 seconds
		}
		
		System.out.println("[Post]time="+time);
		
		long startTime = System.currentTimeMillis();
		String startStr = "doPost - Start"
        		+ "::Hash=" + this.hashCode() 
        		+ "::Name=" + Thread.currentThread().getName() 
        		+ "::ID=" + Thread.currentThread().getId();
        System.out.println(startStr);
		try  {
			System.out.println("Async Supported? " + req.isAsyncSupported());
			String signature = "Signature";
			System.out.println("signature=" + signature);
			System.out.println("response=" + res.hashCode());
			pool.execute(new HandelRequestProcessRunnable(signature , res) );
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		String endStr = "doPost - End"
        		+ "::Hash=" + this.hashCode() 
        		+"::Name=" + Thread.currentThread().getName() + "::ID="
            + Thread.currentThread().getId() + "::Time Taken="
            + (endTime - startTime) + " ms.";
        System.out.println(endStr);
	}
	
	private static class AsyncListenerImpl implements AsyncListener {
        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            System.out.println("AsyncListenerImpl onStartAsync");
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            System.out.println("AsyncListenerImpl onComplete");
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            System.out.println("AsyncListenerImpl onTimeout");
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            System.out.println("AsyncListenerImpl onError");
        }
    }
	
	public class HandelRequestProcessRunnable implements Runnable {
		private String signature;
		private HttpServletResponse res;
		
		public HandelRequestProcessRunnable(String signature , HttpServletResponse res) {
			this.signature = signature;
			this.res = res;
		}
		
		@Override
	    public void run() {
	    		try {
	    			long startTime = System.currentTimeMillis();
	    			String startStr = "FixedPool Start"
	    	        		+ "::Hash=" + this.hashCode() 
	    	        		+ "::Name=" + Thread.currentThread().getName() 
	    	        		+ "::ID=" + Thread.currentThread().getId();
	    	        System.out.println(startStr);
	    	        
				if (this.res == null) {
	    				System.out.println("First response is null");
	    				return;
	    			}
				Thread.sleep(sleepSecs);
				if (this.res == null) {
    					System.out.println("Second response is null");
    					return;
    				}
				this.res.setStatus(HttpServletResponse.SC_OK);
//				try (PrintWriter pw = this.res.getWriter()) {
//					if (pw != null) {
//						//pw.println("<h1>FixedPool done</h1>"); // Can not print response
//						System.out.println("Writer is not null");
//					} else {
//						System.out.println("Writer is null");
//					}
//	            } catch (IOException e) {
//	            		e.printStackTrace();
//	            }
				
				System.out.println("signature after 5s=" + this.signature);
				System.out.println("response after 5s=" + this.res.hashCode());
				long endTime = System.currentTimeMillis();
		        String endStr = "FixedPool End"
		        		+ "::Hash=" + this.hashCode() 
		        		+"::Name=" + Thread.currentThread().getName() + "::ID="
		            + Thread.currentThread().getId() + "::Time Taken="
		            + (endTime - startTime) + " ms.";
		        System.out.println(endStr);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    		
	    }

	}
}
