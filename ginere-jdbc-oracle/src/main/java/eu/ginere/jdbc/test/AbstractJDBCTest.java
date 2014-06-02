package eu.ginere.jdbc.test;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.TestCase;

public class AbstractJDBCTest extends TestCase{

	

	/**
	 * @param driverClassName
	 * @param url
	 *            jdbc:oracle:thin:@172.24.0.22:1521:ESTRADA1
	 * @param user
	 *            USER
	 * @param password
	 *            SECRET
	 * @return
	 * @throws SQLException
	 */
	public static DataSource createOracleDataSource(String url,
			String username, String password) throws SQLException {
		
		oracle.jdbc.pool.OracleDataSource oracleDataSource = new oracle.jdbc.pool.OracleDataSource();

		oracleDataSource.setURL(url);
		oracleDataSource.setUser(username);
		oracleDataSource.setPassword(password);
		oracleDataSource.setConnectionCachingEnabled(true);
		Properties prop=oracleDataSource.getConnectionCacheProperties();
		
		if (prop!=null){
			prop.list(System.err);
		}

		
		return oracleDataSource;
	}


}
