package com.congpc.servlet;

import java.io.IOException;
import java.io.PrintWriter; 
import javax.servlet.AsyncContext;

public class AsyncRequestProcessor implements Runnable {
	private AsyncContext asyncContext;
    private int secs;
 
    public AsyncRequestProcessor() {
    }
 
    public AsyncRequestProcessor(AsyncContext asyncCtx, int secs) {
        this.asyncContext = asyncCtx;
        this.secs = secs;
    }
 
    @Override
    public void run() {
    		long startTime2 = System.currentTimeMillis();
		String startStr2 = "AsyncThread by ThreadPoolExecutor - Start"
        		+ "::Hash=" + this.hashCode() 
        		+ "::Name=" + Thread.currentThread().getName() 
        		+ "::ID=" + Thread.currentThread().getId();
        System.out.println(startStr2);
        System.out.println("Async Supported? " + asyncContext.getRequest().isAsyncSupported());
        longProcessing(secs);
        try {
            PrintWriter out = asyncContext.getResponse().getWriter();
            out.write("<h3>Processing done for " + secs + " milliseconds!!</h3>");
            System.out.println("AsyncServlet process by ThreadPoolExecutor done.");
            long endTime2 = System.currentTimeMillis();
            String endStr2 = "AsyncThread by ThreadPoolExecutor - End"
            		+ "::Hash=" + this.hashCode() 
            		+"::Name=" + Thread.currentThread().getName() + "::ID="
                + Thread.currentThread().getId() + "::Time Taken="
                + (endTime2 - startTime2) + " ms.";
            System.out.println(endStr2);
            out.println("<h1>"+ endStr2 +"</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //complete the processing
        asyncContext.complete();
    }
 
    private void longProcessing(int secs) {
        // wait for given time before finishing
        try {
            Thread.sleep(secs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
