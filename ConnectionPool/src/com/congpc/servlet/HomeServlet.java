package com.congpc.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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

//DemoType 1: http://qiita.com/opengl-8080/items/7d51351cb540269e1d05#%E9%9D%9E%E5%90%8C%E6%9C%9F%E5%87%A6%E7%90%86%E3%82%92%E5%AE%9F%E8%A3%85%E3%81%99%E3%82%8B
//DemoType 2: http://www.journaldev.com/2008/async-servlet-feature-of-servlet-3

//Async supported
@WebServlet(value="/", initParams = {@WebInitParam(name="default", value="Demo AsyncServlet")}, asyncSupported = true)
//Async not supported
//@WebServlet(value="/", initParams = {@WebInitParam(name="default", value="Demo NonAsyncServlet")})
public class HomeServlet extends HttpServlet{
	public static ExecutorService pool = Executors.newFixedThreadPool(10);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String defaultStr = "";
	@Override
    public void init(ServletConfig config) throws ServletException {
    		System.out.println("init() : hash=" + this.hashCode() + " | thread=" + Thread.currentThread().getName());
    		defaultStr = config.getInitParameter("default");
    		//if (defaultStr != null) System.out.println("default=" + defaultStr);
	}
	
	@Override
    public void destroy() {
        System.out.println("destroy() : hash=" + this.hashCode() + " | thread=" + Thread.currentThread().getName());
    }
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		int demoType = 0;//0: Japanese site, 1: English site, 2:TTV site
//		String time = req.getParameter("time");
//      int secs = Integer.valueOf(time);
      // max 10 seconds
//      if (secs > 10000)
//          secs = 10000;
		
		long startTime = System.currentTimeMillis();
		String startStr = "AsyncServlet Start"
        		+ "::Hash=" + this.hashCode() 
        		+ "::Name=" + Thread.currentThread().getName() 
        		+ "::ID=" + Thread.currentThread().getId();
        System.out.println(startStr);
        
        PrintWriter	out = res.getWriter();
        res.setContentType("text/html");
        out.println("<html><head><title>Thread Pool</title></head><body>");
        out.println(startStr + "<br>");
        
	    if (defaultStr.length() > 0) {
	    		out.println("Hello "+ defaultStr + "<br>");
	    } else {
	    	    out.println("Welcome to home page<br>");
	    }
        // Enable async
        if (demoType == 0 || demoType == 1) {
        		req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        }
        int secs = 5000; // sleep 5s
        if (demoType == 0) {
        		AsyncContext ctx = req.startAsync();
            ctx.addListener(new AsyncListenerImpl());
            ctx.start(() -> {
                try (PrintWriter pw = ctx.getResponse().getWriter()) {
	                	long startTime2 = System.currentTimeMillis();
		    			String startStr2 = "AsyncThread - Start"
		    	        		+ "::Hash=" + this.hashCode() 
		    	        		+ "::Name=" + Thread.currentThread().getName() 
		    	        		+ "::ID=" + Thread.currentThread().getId();
		    	        System.out.println(startStr2);
	    	        
                		System.out.println("Async Supported? " + ctx.getRequest().isAsyncSupported());
                    Thread.sleep(secs);
                    System.out.println("AsyncServlet process done.");
                    pw.println("<h1>Async Process Done</h1>");
                    long endTime2 = System.currentTimeMillis();
                    String endStr2 = "AsyncThread - End"
                    		+ "::Hash=" + this.hashCode() 
                    		+"::Name=" + Thread.currentThread().getName() + "::ID="
                        + Thread.currentThread().getId() + "::Time Taken="
                        + (endTime2 - startTime2) + " ms.";
                    System.out.println(endStr2);
                    pw.println("<h1>"+endStr2+"</h1>");
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    ctx.complete();
                }
            });
            res.getWriter().println("<h1>AsyncServlet by Japanese site</h1>");
        }else if (demoType == 1) {
        		AsyncContext asyncCtx = req.startAsync();
            asyncCtx.addListener(new AppAsyncListener());
            asyncCtx.setTimeout(9000);
            
            ThreadPoolExecutor executor = (ThreadPoolExecutor) req.getServletContext().getAttribute("executor");
            executor.execute(new AsyncRequestProcessor(asyncCtx, secs));
            res.getWriter().println("<h1>AsyncServlet by ThreadPoolExecutor site</h1>");
        }else {
        		try  {
        			System.out.println("Async Supported? " + req.isAsyncSupported());
	    			String signature = "Signature";
	    			System.out.println("signature=" + signature);
	    			System.out.println("response=" + res.hashCode());
	    			pool.execute(new HandelRequestProcessRunnable(signature , res) );
	    			res.getWriter().println("<h1>Servlet using Executors.newFixedThreadPool with size = 10</h1>");
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
        }
        
        long endTime = System.currentTimeMillis();
        String endStr = "AsyncServlet End"
        		+ "::Hash=" + this.hashCode() 
        		+"::Name=" + Thread.currentThread().getName() + "::ID="
            + Thread.currentThread().getId() + "::Time Taken="
            + (endTime - startTime) + " ms.";
        System.out.println(endStr);
        out.println(endStr + "<br/>");
        out.println("</body></html>");
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		System.out.println("doPost() : hash=" + this.hashCode() + " | thread=" + Thread.currentThread().getName());
		try (BufferedReader br = req.getReader()) {
            br.lines().forEach(System.out::println);
        }
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
				Thread.sleep(5000);
				if (this.res == null) {
    					System.out.println("Second response is null");
    					return;
    				}
//				try (PrintWriter pw = this.res.getWriter()) {
//	                pw.println("<h1>Async Process using pool</h1>");
//	            } catch (IOException e) {
//	                e.printStackTrace();
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
