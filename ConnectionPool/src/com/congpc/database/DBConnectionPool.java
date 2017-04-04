/**
 * 
 */
package com.congpc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Cong Pham
 *
 */
public class DBConnectionPool {
	private String name = "";
	private String URL = "";
	private String user = "";
	private String password = "";
	private int maxConn = 0;
	private int checkedOut = 0;
	
	Vector<Connection> freeConnections = new Vector<Connection>();
	
	public DBConnectionPool(String name, String URL, String user,
			String password, int maxConn) {
		this.name = name;
		this.URL = URL;
		this.user = user;
		this.password = password;
		this.maxConn = maxConn;
	}
	
	
	public synchronized Connection getConnection() {
		Connection con = null;
		if (freeConnections.size() > 0) {
			// Pick the first Connection in the Vector
			// to get round-robin usage
			con = (Connection) freeConnections.firstElement();
			freeConnections.removeElementAt(0);
			try {
				if (con.isClosed()) {
					log("Removed bad connection from " + name);
					// Try again recursively
					con = getConnection();
				}
			}
			catch (SQLException e) {
				log("Removed bad connection from " + name);
				// Try again recursively
				con = getConnection();
			}
		}
		else if (maxConn == 0 || checkedOut < maxConn) {
			con = newConnection();
		}
		if (con != null) {
			checkedOut++;
		}
		return con;
	}
	
	private Connection newConnection() {
        Connection con = null;
        try {
            if (user == null) {
                con = DriverManager.getConnection(URL);
            }
            else {
                con = DriverManager.getConnection(URL, user, password);
            }
            log("Created a new connection in pool " + name);
        }
        catch (SQLException e) {
            log(e, "Can't create a new connection for " + URL);
            return null;
        }
        return con;
    }
	
	public synchronized Connection getConnection(long timeout) {
        long startTime = new Date().getTime();
        Connection con;
        while ((con = getConnection()) == null) {
            try {
                wait(timeout);
            }
            catch (InterruptedException e) {}
            if ((new Date().getTime() - startTime) >= timeout) {
                // Timeout has expired
                return null;
            }
        }
        return con;
    }
	
	public synchronized void freeConnection(Connection con) {
        // Put the connection at the end of the Vector
        freeConnections.addElement(con);
        checkedOut--;
        notifyAll();
    }
	
	public synchronized void release() {
        Enumeration allConnections = freeConnections.elements();
        while (allConnections.hasMoreElements()) {
            Connection con = (Connection) allConnections.nextElement();
            try {
                con.close();
                log("Closed connection for pool " + name);

            }
            catch (SQLException e) {
                log(e, "Can't close connection for pool " + name);
            }
        }
        freeConnections.removeAllElements();
    }
	
	private void log(String log) {
		System.out.println(log);
	}
	private void log(SQLException e, String log) {
		System.out.println(e.getMessage() + ":" + log);
	}
}
