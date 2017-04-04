/**
 * 
 */
package com.congpc.database;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Cong Pham
 * Source : http://www.developer.com/java/improved-performance-with-a-connection-pool.html
 */
public class DBConnectionManager {
	private static int clients = 0;
	private PrintWriter log = null;
	Vector<Driver> drivers = new Vector<Driver>();
	Hashtable pools = new Hashtable();
	
	private DBConnectionManager() {
        init();
    }
	private static DBConnectionManager instance = null;
	static synchronized public DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        clients++;
        return instance;
    }
	
	private void init() {
        InputStream is = getClass().getResourceAsStream("/db.properties");
        Properties dbProps = new Properties();
        try {
            dbProps.load(is);
        }
        catch (Exception e) {
            System.err.println("Can't read the properties file. " +
                "Make sure db.properties is in the CLASSPATH");
            return;
        }
        String logFile = dbProps.getProperty("logfile", "DBConnectionManager.log");
        try {
            log = new PrintWriter(new FileWriter(logFile, true), true);
        }
        catch (IOException e) {
            System.err.println("Can't open the log file: " + logFile);
            log = new PrintWriter(System.err);
        }
        loadDrivers(dbProps);
        createPools(dbProps);
    }
	
	private void loadDrivers(Properties props) {
        String driverClasses = props.getProperty("drivers");
        StringTokenizer st = new StringTokenizer(driverClasses);
        while (st.hasMoreElements()) {
            String driverClassName = st.nextToken().trim();
            try {
                Driver driver = (Driver)
                    Class.forName(driverClassName).newInstance();
                DriverManager.registerDriver(driver);
                drivers.addElement(driver);
                log("Registered JDBC driver " + driverClassName);
            }
            catch (Exception e) {
                log("Can't register JDBC driver: " +
                    driverClassName + ", Exception: " + e);
            }
        }
    }
	
	private void createPools(Properties props) {
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".url")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                String url = props.getProperty(poolName + ".url");
                if (url == null) {
                    log("No URL specified for " + poolName);
                    continue;
                }
                String user = props.getProperty(poolName + ".user");
                String password = props.getProperty(poolName + ".password");
                String maxconn = props.getProperty(poolName + ".maxconn", "0");
                int max;
                try {
                    max = Integer.valueOf(maxconn).intValue();
                }
                catch (NumberFormatException e) {
                    log("Invalid maxconn value " + maxconn + " for " +
                        poolName);
                    max = 0;
                }
                DBConnectionPool pool =
                    new DBConnectionPool(poolName, url, user, password, max);
                pools.put(poolName, pool);
                log("Initialized pool " + poolName);
            }
        }
    }
	
	public Connection getConnection(String name) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            return pool.getConnection();
        }
        return null;
    }

    public Connection getConnection(String name, long time) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            return pool.getConnection(time);
        }
        return null;
    }

    public void freeConnection(String name, Connection con) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            pool.freeConnection(con);
        }
    }
    
    public synchronized void release() {
        // Wait until called by the last client
        if (--clients != 0) {
            return;
        }

        Enumeration allPools = pools.elements();
        while (allPools.hasMoreElements()) {
            DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
            pool.release();
        }
        Enumeration allDrivers = drivers.elements();
        while (allDrivers.hasMoreElements()) {
            Driver driver = (Driver) allDrivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log("Deregistered JDBC driver " + driver.getClass().getName());
            }
            catch (SQLException e) {
                log(e, "Can't deregister JDBC driver: " +
                    driver.getClass().getName());
            }
        }
    }
    
	private void log(String log) {
		System.out.println(log);
	}
	private void log(SQLException e, String log) {
		System.out.println(e.getMessage() + ":" + log);
	}
}
