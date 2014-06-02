package eu.ginere.jdbc.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eu.ginere.util.test.TestInterface;
import eu.ginere.util.test.TestResult;


/**
 * @version $Id: CompositeFormat.java 1436768 2013-01-22 07:07:42Z ggregory $
 */
public class DataBase implements TestInterface{

	protected static final Logger log = Logger.getLogger(DataBase.class);

	public static DataBase DEFAULT_DATABASE=null;

	private DataSource dataSource;

	private final String name;
	
	public DataBase(String jndiName) throws NamingException{
		this.name=jndiName;
		log.info("Obteniendo la datasource del recurso jndi:"+jndiName+"'");
		InitialContext initContext = new InitialContext();
		this.dataSource = (DataSource) initContext.lookup("java:comp/env/"+jndiName);		
		log.info("Se ha actualizado satisfactoriamente la DataSource del recurso jndi:"+jndiName+"'");		
	}
	
	public DataBase(String name,DataSource datasource) throws NamingException{
		this.name=name;
		this.dataSource = datasource;
		log.info("Se ha actualizado satisfactoriamente la DataSource i:"+name+"'");		
	}
	
	public String getName() {
		return name;
	}

	public DataSource getDataSource(){
		return dataSource;
	}
	
	/**
	 * Cambia la datasource.
	 * 
	 * @param jndiName
	 * @throws NamingException
	 */
	public static void initDatasource(String jndiName) throws NamingException {
		DEFAULT_DATABASE=new DataBase(jndiName);
//		log.info("Obteniendo la datasource del recurso jndi:"+jndiName+"'");
//		InitialContext initContext = new InitialContext();
//		this.dataSource = (DataSource) initContext.lookup("java:comp/env/"+jndiName);		
//		log.info("Se ha actualizado satisfactoriamente la DataSource del recurso jndi:"+jndiName+"'");
	}
	
	public static void initDatasource(String name,DataSource datasource) throws NamingException {
		DEFAULT_DATABASE=new DataBase(name,datasource);
	}

	public static void initDatasource(DataBase database) throws NamingException {
		DEFAULT_DATABASE=database;
	}
	
	public boolean testConnection() {
		return testConnection(dataSource);
	}

	/**
	 * Verifica que la conexion es correcta.
	 * 
	 * @param dataSource
	 * @return
	 */
	public boolean testConnection(DataSource dataSource) {
		try {
			Connection connection = getConnection();
			String testQuery="SELECT 1 from DUAL";
			try {
				PreparedStatement pstm = getPrepareStatement(connection,
															 testQuery);
				try {
//					executeQuery(pstm, testQuery);
					hasResult(pstm, testQuery);
					return true;				
				}finally{
					close(pstm);
				}
			} finally {
				closeConnection(connection);
			}	
		} catch (Exception e) {
			log.error("Connection test error", e);
			return false;
		}
	}

	
	/**
	 * Obtien un prepared statemend.
	 * 
	 * @param connection
	 * @param query
	 * @return
	 * @throws DaoManagerException
	 */
	public PreparedStatement getPrepareStatement(Connection connection,String query) throws DaoManagerException {
		return ThreadLocalConection.getPrepareStatement(this,connection, query);
//		try {
//			return connection.prepareStatement(query);
//		} catch (SQLException e) {
//			throw new DaoManagerException("Query:'" + query	+ "'", e);
//		}
	}

	public List<String> getStringList (String query) throws DaoManagerException{
		Connection connection = getConnection();
		try{
			PreparedStatement pstm = getPrepareStatement(connection,query);
			try {
				return getStringList(pstm, query);
			}finally{
				close(pstm);
			}
		} finally {
			closeConnection(connection);
		}
	}
	
	
	public static List<String> getStringList(PreparedStatement pstm,
											 String query) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}
		
		try {
			ResultSet rset = executeQuery(pstm,query);
			try {
				List<String> ret = new ArrayList<String>(rset.getFetchSize());
				
				while (rset.next()) {
					ret.add(rset.getString(1));
				}
				return ret;
			} catch (SQLException e) {
				throw new DaoManagerException("While executing query:'" + query+ "'", e);
			}finally{
				close(rset);
			}
		} finally {			
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}
	
	
	

	public static String getString(PreparedStatement pstm,
								   String query) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}
		
		try {
			ResultSet rset = executeQuery(pstm,query);
			try {
				if(rset.next()) {
					return rset.getString(1);
				} else {
					throw new DaoManagerException("Not result for query:'"+query+"'");
				}
			} catch (SQLException e) {
				throw new DaoManagerException("While executing query:'" + query+ "'", e);
			}finally{
				close(rset);
			}
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}
	
	
	

	public static String getString(PreparedStatement pstm,
								   String query,String defaultValue) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}
		
		try {
			ResultSet rset = executeQuery(pstm,query);
			try {
				if(rset.next()) {
					return rset.getString(1);
				} else {
					return defaultValue;
				}
			} catch (SQLException e) {
				throw new DaoManagerException("While executing query:'" + query+ "'", e);
			}finally {
				close(rset);
			}
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}
	


	public static boolean hasResult(PreparedStatement pstm,
								   String query) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}
		
		try {
			ResultSet rset = executeQuery(pstm,query);
			try {
				return rset.next();
			}finally{
				close(rset);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("While executing query:'" + query+ "'", e);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}
	
	
	public List<Object[]> getStringListArray (String query,int tamano) throws DaoManagerException{
		Connection connection = getConnection();
		try{
			PreparedStatement pstm = getPrepareStatement(connection,query);
			try {
				return getStringListArray(pstm, query,tamano);
			}finally{
				close(pstm);
			}			
		} finally {
			closeConnection(connection);
		}
	}
	
	public static List<Object[]> getStringListArray(PreparedStatement pstm,
													String query,
													int tamano) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}

		try {
			ResultSet rset = executeQuery(pstm, query);
			try {
				List<Object[]> ret = new ArrayList<Object[]>(rset.getFetchSize());
				
				while(rset.next()){
					Object[] objetoResultado =  new Object[tamano];				
					for (int i = 0; i < objetoResultado.length; i++) {
						objetoResultado[i] = rset.getString(i+1);
					}				
					ret.add(objetoResultado);			
				}
				
				return ret;
			} catch (SQLException e) {
				throw new DaoManagerException("While executing query:'" + query
											  + "'", e);
			}finally{
				close(rset);
			}
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}
	
	public long getLong(String query) throws DaoManagerException{
		Connection connection = getConnection();
		try{
			PreparedStatement pstm = getPrepareStatement(connection,query);
			try {
				return getLong(pstm, query);
			}finally{
				close(pstm);
			}
		} finally {
			closeConnection(connection);
		}
	}
	
	public long getLong(String query,long defaultValue) throws DaoManagerException{
		try {
			Connection connection = getConnection();
			try{
				PreparedStatement pstm = getPrepareStatement(connection,query);
				try {
					return getLong(pstm, query);
				}finally{
					close(pstm);
				}				
			} finally {
				closeConnection(connection);
			}
		}catch (DaoManagerException e) {
			log.debug("query:"+query+"' fails, return default value.",e);
			return defaultValue;
		}
	}
	
	public static long getLong(PreparedStatement pstm,
							   String query) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}
		
		try {
			ResultSet rset = executeQuery(pstm,query);
			try {
				if(rset.next()) {
					long ret=rset.getLong(1);
					if (!rset.wasNull()){
						return ret;
					}
				}
				
				throw new DaoManagerException("No result for query:'"+query+"'");
			} catch (SQLException e) {
				throw new DaoManagerException("While executing query:'" + query+ "'", e);
			}finally{
				close(rset);
			}
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}



	/**
	 * Ejecuta una query.
	 * 
	 * @param pstm
	 * @param query
	 * @return
	 * @throws DaoManagerException
	 */
	public long executeUpdate(String query) throws DaoManagerException {
		Connection connection = getConnection();
		try {
			PreparedStatement pstm = getPrepareStatement(connection,query);
			try {
				return executeUpdate(pstm, query);
			}finally{
				close(pstm);
			}
		} finally {
			closeConnection(connection);
		}	
	}

	/**
	 * Ejecuta una query.
	 * 
	 * @param pstm
	 * @param query
	 * @return
	 * @throws DaoManagerException
	 */
	public static long executeUpdate(PreparedStatement pstm, String query) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}

		try {
			return pstm.executeUpdate();
		} catch (SQLException e) {
			throw new DaoManagerException("While executing query:'" + query+ "'", e);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}

	/**
	 * Ejecuta una query.
	 * 
	 * @param pstm
	 * @param query
	 * @return
	 * @throws DaoManagerException
	 */
	public static ResultSet executeQuery(PreparedStatement pstm, String query) throws DaoManagerException {
		long time = 0;
		if (log.isDebugEnabled()) {
			time = System.currentTimeMillis();
		}

		try {
			return pstm.executeQuery();
		} catch (SQLException e) {
			throw new DaoManagerException("While executing query:'" + query
					+ "'", e);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}

	/**
	 * Cierra una conexion.
	 * @param connection
	 */
	public static void closeConnection(Connection connection) {
		ThreadLocalConection.close(connection);
//		try {
//			connection.close();
//		} catch (SQLException e) {
//			log.warn("Clossing conection", e);
//		}
	}

	static protected void close(PreparedStatement pstm) {
		ThreadLocalConection.close(pstm);
	}
	
	static protected void close(ResultSet rset) {
		try {
			rset.close();
		} catch (SQLException e) {
			log.warn("Clossing conection", e);
		}
	}

	public Connection getConnection() throws DaoManagerException {
//		return getConnection(dataSource);
		return ThreadLocalConection.getConnection(this); 
	}


//	/**
//	 * Obtiene una conexion.
//	 * 
//	 * @param dataSource
//	 * @return
//	 * @throws DaoManagerException
//	 */
//	public static Connection getConnection(DataSource dataSource) throws DaoManagerException {
//		if (dataSource == null) {
//			throw new DaoManagerException(
//					"La datasource es null, lo mas probable es que no se haya inicializado correctamente");
//		} else {
//			try {
//				long time = 0;
//
//				if (log.isDebugEnabled()) {
//					time = System.currentTimeMillis();
//				}
//				Connection ret = dataSource.getConnection();
//				ret.setAutoCommit(true);
//				if (log.isDebugEnabled()) {
//					log.debug("Connection obtained in:"
//							+ (System.currentTimeMillis() - time) + " mill");
//				}
//
//				return ret;
//			} catch (SQLException e) {
//				throw new DaoManagerException("Obteniendo la conexion", e);
//			}
//		}
//	}
	
	public long getSequenceNextVal(String sequence) throws DaoManagerException {
		String query="SELECT "+sequence+".nextval from dual";
		
		return getLong(query);
	}

	
	public static void setString(PreparedStatement pstm,int poss,String value,String query) throws DaoManagerException {
		try {
			pstm.setString(poss,value);
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}

	public static void setInt(PreparedStatement pstm,int poss,int value,String query) throws DaoManagerException {
		try {
			pstm.setInt(poss,value);
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}
	
	public static void setInt(PreparedStatement pstm,int poss,Integer value,String query) throws DaoManagerException {
		try {
			if (value==null){
				pstm.setNull(poss, Types.INTEGER);
			} else {
				pstm.setInt(poss,value);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}
	
	public static void setFloat(PreparedStatement pstm,int poss,Float value,String query) throws DaoManagerException {
		try {
			if (value!=null){
				pstm.setFloat(poss,value);
			} else {
				pstm.setNull(poss, Types.FLOAT);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}
	
	public static void setDate(PreparedStatement pstm,int poss,Date value,String query) throws DaoManagerException {
		try {
			if (value != null) {
				Timestamp sqlDate = new Timestamp(value.getTime());
				pstm.setTimestamp(poss, sqlDate);
			} else {
				pstm.setNull(poss, Types.TIMESTAMP);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}
	
	public static void setTimestamp(PreparedStatement pstm,int poss,Timestamp value,String query) throws DaoManagerException {
		try {
			if (value != null) {
				pstm.setTimestamp(poss, value);
			} else {
				pstm.setNull(poss, Types.TIMESTAMP);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}

	public static void setLong(PreparedStatement pstm, int poss, Long value,String query) throws DaoManagerException {
		try {
			if (value!=null){
				pstm.setLong(poss,value);
			} else {
				pstm.setNull(poss, Types.BIGINT);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}
	
	public static void setDouble(PreparedStatement pstm, int poss, Double value,String query) throws DaoManagerException {
		try {
			if (value!=null){
				pstm.setDouble(poss,value);
			} else {
				pstm.setNull(poss, Types.DOUBLE);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}
	
	public static void setBoolean(PreparedStatement pstm, int poss, Boolean value,String query) throws DaoManagerException {
		try {
			if (value!=null){
				pstm.setBoolean(poss,value);
			} else {
				pstm.setNull(poss, Types.BOOLEAN);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("Query:'"+query+"' Value:'" + value	+ "'", e);
		}
	}

	public TestResult test() {
		TestResult ret=new TestResult(DataBase.class);
		if (dataSource == null){
			ret.addError("The datasource is null");
			return ret;
		} else if (!testConnection()){
			ret.addError("There is not conection to the database:"+getName());
			return ret;
		} else {
			return ret;
		}
	}	
	
	protected long getNextValueFromSecuence(String sequenceName)throws DaoManagerException {
		Connection connection = getConnection();

		StringBuilder buffer = new StringBuilder();

		buffer.append("SELECT ");
		buffer.append(sequenceName);
		buffer.append(".nextval from dual");

		String query = buffer.toString();

		try {
			PreparedStatement pstm = getPrepareStatement(connection,query);
			try {
				long value = getLongFromQuery(pstm, query, -1);
				if (value < 0) {
					throw new DaoManagerException(
												  "No se pudo obtener un valor de la secuencia:'"
												  + sequenceName + "'");
				} else {
					return value;
				}
			}finally{
				close(pstm);
			}
		} finally {
			closeConnection(connection);
		}
	}

	protected static long getLongFromQuery(PreparedStatement pstm, 
										   String query,
										   int defaultValue) throws DaoManagerException {
		long time = 0;
		if (log.isInfoEnabled()) {
			time = System.currentTimeMillis();
		}
		try {
			ResultSet rset = executeQuery(pstm,query);
			try {
				if (!rset.next()) {
					return defaultValue;
				} else {
					long ret=rset.getLong(1);
					
					if (rset.wasNull()){
						return defaultValue;
					} else {
						return ret;
					}
				}
				
		
			}finally{
				close(rset);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("While executing query:'" + query
										  + "'", e);
		} finally {
			if (log.isInfoEnabled()) {
				log.info("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}
	

	protected static int getIntFromQuery(PreparedStatement pstm, 
										   String query,
										   int defaultValue) throws DaoManagerException {
		long time = 0;
		if (log.isInfoEnabled()) {
			time = System.currentTimeMillis();
		}
		try {
			ResultSet rset = executeQuery(pstm,query);
			try {
				if (!rset.next()) {
					return defaultValue;
				} else {
					int ret=rset.getInt(1);
					
					if (rset.wasNull()){
						return defaultValue;
					} else {
						return ret;
					}
				}				
			}finally{
				close(rset);
			}
		} catch (SQLException e) {
			throw new DaoManagerException("While executing query:'" + query
										  + "'", e);
		} finally {
			if (log.isInfoEnabled()) {
				log.info("query:'" + query + "' executed in:"
						+ (System.currentTimeMillis() - time) + " mill");
			}
		}
	}

	protected static void set(PreparedStatement pstm,int poss,Object value,String query) throws DaoManagerException {

		if (value instanceof String || value == null){
			setString(pstm,poss,(String)value,query);
		} else if (value instanceof Date){
			setDate(pstm,poss,(Date)value,query);
		} else if (value instanceof Boolean){
			setBoolean(pstm,poss,(Boolean)value,query);
		} else if (value instanceof Integer){
			setInt(pstm,poss,(Integer)value,query);
		} else if (value instanceof Long){
			setLong(pstm,poss,(Long)value,query);
		} else if (value instanceof Double){
			setDouble(pstm,poss,(Double)value,query);
		} else if (value instanceof Float){
			setFloat(pstm,poss,(Float)value,query);
		} else if (value instanceof Timestamp){
			setTimestamp(pstm,poss,(Timestamp)value,query);
		} else {
			throw new IllegalAccessError("Type :"+value.getClass().getName()+" is not suported");
		}
	}

	public void startThreadLocal(){
		ThreadLocalConection.startThreadLocal();
	}

	static public void endThreadLocal(boolean forzeClean) {
		ThreadLocalConection.endThreadLocal(forzeClean);
	}	
}
