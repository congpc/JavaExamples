package com.congpc.servlet;

import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//Demo: https://blog.krecan.net/2010/05/02/cool-tomcat-is-able-to-handle-more-than-13000-concurrent-connections/
//Async supported
@WebServlet(value="/concurrency", initParams = {@WebInitParam(name="default", value="Demo concurrent connection")}, asyncSupported = true)
public class ConcurrentServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private String defaultStr = "";
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("[ConcurrentServlet] init()" + "::Hash=" + this.hashCode() 
		+ "::Name=" + Thread.currentThread().getName() 
		+ "::ID=" + Thread.currentThread().getId());
//		defaultStr = config.getInitParameter("default");
	}
	
	@Override
	public void destroy() {
		System.out.println("destroy() : hash=" + this.hashCode() + " | thread=" + Thread.currentThread().getName());
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String numberStr = req.getParameter("number");
		int number = 1;
		if (numberStr != null) {
			number = Integer.valueOf(numberStr);
		}
		try {
			System.out.println("Servlet no. "+number+" called." 
					+ "::Hash=" + this.hashCode() 
					+ "::Name=" + Thread.currentThread().getName() 
					+ "::ID=" + Thread.currentThread().getId());
			URL url = new URL(req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getRequestURI()+"?number="+(number+1));
			Object content = url.getContent();  
			res.setContentType("plain/text");  
			res.getWriter().write("OK: "+content);  
		} catch (Throwable e) {  
			String message = "Reached "+number+" of connections";  
			System.out.println(message);  
			System.out.println(e);  
			res.getWriter().write(message);  
		}
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
	}
}
