package com.congpc.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BatchSingleton {
	private Connection batchConnection;
	private PreparedStatement batchStatement;
	
	private static BatchSingleton instance = null;
	protected BatchSingleton() {
		// Exists only to defeat instantiation.
	}
	public static BatchSingleton getInstance() {
		if(instance == null) {
			instance = new BatchSingleton();
		}
		return instance;
	}
	
	public Connection getBatchConnection() {
		return batchConnection;
	}
	public void setBatchConnection(Connection batchConnection) {
		this.batchConnection = batchConnection;
	}
	public PreparedStatement getBatchStatement() {
		return batchStatement;
	}
	public void setBatchStatement(PreparedStatement batchStatement) {
		this.batchStatement = batchStatement;
	}
	
	public void executeBatchProcessing () {
		System.out.println("Start batch processing by timer");
		try {
			if (this.batchStatement != null) {
				System.out.println("Execute batch processing by timer");
				this.batchStatement.executeBatch();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void detroyBatchProcessing () {
		try {
			
			if (this.batchStatement != null) {
				System.out.println("Close statement in batch processing");
				this.batchStatement.close();
			}
			if (this.batchConnection != null) {
				System.out.println("Close connection in batch processing");
				this.batchConnection.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
