package com.congpc.servlet;

import java.io.*;
import java.net.HttpURLConnection;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
//import java.io.*;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;

/**
 * Servlet implementation class ConnectionPoolServlet
 */
@WebServlet("/ConnectionPool")
public class ConnectionPoolServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static long pooledDuration, nonPooledDuration;
	private static long pooledCount, nonPooledCount;
	private DataSource datasource = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConnectionPoolServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init(ServletConfig config) throws ServletException {
        try {
          //Create a datasource for pooled connections.
          //Register the driver for non pooled connections.
          Context initCtx = new InitialContext();
          Context envCtx = (Context) initCtx.lookup("java:comp/env");
          datasource = (DataSource) envCtx.lookup("jdbc/ConnectionPool");
          Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch (Exception e) {
          //throw new ServletException(e.getMessage());
          e.printStackTrace();
        }
      }

      private Connection getConnection(boolean pooledConnection) throws SQLException {
        if (pooledConnection) {
          pooledCount++;
          return datasource.getConnection();
        }
        else {
          nonPooledCount++;
          Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/exampleDb", "congpc", "Demo_1987");
          return con;
        }
      }
      private Connection getPostConnection(boolean pooledConnection) throws SQLException {
          if (pooledConnection) {
            return datasource.getConnection();
          }
          else {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/exampleDb", "congpc", "Demo_1987");
            return con;
          }
        }
      
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String queryStr = req.getQueryString();
	    boolean poolingEnabled = queryStr == null || !queryStr.equals("disablePooling");
		//boolean poolingEnabled = false;
	    PrintWriter	out = res.getWriter();
	    res.setContentType("text/html");
	    out.println("<html><head><title>Orders</title></head><body>");
	    out.println("<br>PooledConnectionCount:"+pooledCount+", nonPooledConnectionCount:"+nonPooledCount+"<br>");
	    if (pooledDuration > 0) {
	      out.println("<br>"+ "Average pooled response:"+pooledDuration/pooledCount);
	    }
	    if (nonPooledDuration > 0) {
	    		out.println("<br>"+ "Average non pooled response:"+nonPooledDuration/nonPooledCount);
	    }

	    Connection connection = null;
	    Statement stmt = null;
	    ResultSet rs = null;
	    
	    long startTime = System.currentTimeMillis();

	    try {
	      connection = getConnection(poolingEnabled);
	      stmt = connection.createStatement();
	      
	      String sqlSelectCount = "SELECT COUNT(*) AS total FROM exampleDb.orders";
	      rs = stmt.executeQuery(sqlSelectCount);
	      int total = 0;
	      while(rs.next()){
	    	      total = rs.getInt("total");
	    	  }
	      out.println("<br>"+ "Item total:" + total);
	      rs.close();
	      
	      String sqlSelect = "SELECT exampleDb.orders.orderID, exampleDb.orders.customerID, exampleDb.lines.lineID, exampleDb.lines.lineName, exampleDb.lines.productName FROM exampleDb.orders INNER JOIN exampleDb.lines ON exampleDb.lines.orderID = exampleDb.orders.orderID ORDER BY exampleDb.orders.orderID DESC LIMIT 100";
	      rs = stmt.executeQuery(sqlSelect);
	      ResultSetMetaData dbMeta = rs.getMetaData();
	      out.println("<br><table border='1'>");

	      //Create the table headers

	      out.println("<tr>");
	      for (int col=0; col<dbMeta.getColumnCount(); col++) {
	        out.println("<th>" + dbMeta.getColumnName(col+1) + "</th>");
	      }
	      out.println("</tr>");

	      //Create the table data

	      while (rs.next()) {
	        out.println("<tr>");
	        for (int col=0; col < dbMeta.getColumnCount(); col++) {
	          out.println("<td>" + rs.getString(col+1) + "</td>");
	        }
	        out.println("</tr>");
	      }
	      out.println("</table>");
	      out.println("</body></html>");
	      connection.close();
	    }
	    catch (SQLException e) {
	      //throw new ServletException(e.getMessage());
	      e.printStackTrace();
	    }
	    finally {
	      try {if (rs != null) rs.close();} catch (SQLException e) {e.printStackTrace();}
	      try {if (stmt != null) stmt.close();} catch (SQLException e) {e.printStackTrace();}
	      try {if (connection != null) connection.close();} catch (SQLException e) {e.printStackTrace();}
	      long elapsed = System.currentTimeMillis() - startTime;
	      //Collect the times
	      if (poolingEnabled)
	        this.pooledDuration += elapsed;
	      else
	    		this.nonPooledDuration += elapsed;
	    }
	}
	
//	private void executeSqlCommands(boolean batchEnabled, boolean prepareEnabled, int loopCount) throws SQLException {
//		
//	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//		String queryStr = req.getQueryString();
//		boolean poolingEnabled = queryStr == null || !queryStr.equals("disablePooling");
//		boolean batchEnabled = queryStr == null || !queryStr.equals("disableBatch");
//		boolean prepareEnabled = queryStr == null || !queryStr.equals("disablePrepare");
		boolean poolingEnabled = true;
		boolean batchEnabled = true;
		boolean prepareEnabled = true;
		int loopCount = 1;
		Connection connection = null;
		PreparedStatement stmtp1 = null;
		Statement stmt2 = null;
		PreparedStatement stmtp2 = null;
		boolean resultFlag = false;
		String errorMessage = "";
		long startTime = System.currentTimeMillis();
		try {
			for (int i = 1; i <= loopCount; i++) {
				connection = getPostConnection(poolingEnabled);
				String sqlOrderInsert = "insert into exampleDb.orders (customerID) values (?)";
				long orderID = 0;
				stmtp1 = connection.prepareStatement(sqlOrderInsert, Statement.RETURN_GENERATED_KEYS);
				stmtp1.setLong(1, i);
				int affectedRows = stmtp1.executeUpdate();
				if (affectedRows == 0) {
					throw new SQLException("Creating order failed, no rows affected.");
				}
				try (ResultSet generatedKeys = stmtp1.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						orderID = generatedKeys.getLong(1);
					}else {
						throw new SQLException("Creating user failed, no ID obtained.");
					}
				}
				String name = "name-"+ orderID;
				String product = "product-"+ orderID;
				String sqlLineInsert = "insert into exampleDb.lines (lineName,productName,orderID) values (?,?,?)";
				
				if (batchEnabled) {
					final int batchSize = 100;
					int count = 0;
					for (int j = 1; j <= 1000; j++) {
						stmtp2 = connection.prepareStatement(sqlLineInsert);
						name = "batch-name-" + j;
						product = "batch-product-" + j;
						stmtp2.setString(1, name);
						stmtp2.setString(2, product);
						stmtp2.setLong(3, orderID);
						stmtp2.addBatch();
						if(++count % batchSize == 0) {
							stmtp2.executeBatch();
						}
					}
					stmtp2.executeBatch();
				} else {
					if (prepareEnabled) {
						stmtp2 = connection.prepareStatement(sqlLineInsert);
						stmtp2.setString(1, name);
						stmtp2.setString(2, product);
						stmtp2.setLong(3, orderID);
						stmtp2.executeUpdate();
					} else {
						sqlLineInsert = "insert into exampleDb.lines (lineName,productName,orderID) values ('"+name+"','" + product + "','" + orderID +"')";
						stmt2 = connection.createStatement();
						stmt2.executeUpdate(sqlLineInsert);
					}
				}
				resultFlag = true;
				if (stmtp1 != null) stmtp1.close();
				if (stmt2 != null) stmt2.close();
				if (stmtp2 != null) stmtp2.close();
				connection.close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
		}
		finally {
			try {if (stmtp1 != null) stmtp1.close();} catch (SQLException e) {e.printStackTrace();}
			try {if (stmt2 != null) stmt2.close();} catch (SQLException e) {e.printStackTrace();}
			try {if (stmtp2 != null) stmtp2.close();} catch (SQLException e) {e.printStackTrace();}
			try {if (connection != null) connection.close();} catch (SQLException e) {e.printStackTrace();}
			long elapsed = System.currentTimeMillis() - startTime;
			
			res.setContentType("text/html");
			res.setCharacterEncoding("UTF-8");
			PrintWriter out = res.getWriter();
			if (resultFlag) {
				out.append("{code:200,elapsed:"+elapsed+"}");
			} else {
				out.append("{code:300,errorMessage:"+ errorMessage + "}");
			}
			out.close();
		}
	}

}
