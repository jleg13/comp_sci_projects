package muck.server.database;

import muck.core.Id;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;
import java.sql.*;
import java.util.*;

import static org.apache.commons.dbutils.DbUtils.closeQuietly;


/**
 * Connection point to the H2 database.
 */
public class MuckDatabase {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_SQL_FILE = "/muck_db.sql";
    static final String DB_URL = "jdbc:h2:muck_db;INIT=runscript from '%s';DATABASE_TO_UPPER=false";
    //  Database credentials
    static final String USER = "";
    static final String PASS = "";

    /**
     * A logger for logging output
     */
    private static final Logger logger = LogManager.getLogger(MuckDatabase.class);

    private static final String DATABASE_NAME = "muck_DB";

    /**
     * Private method that provides a connection to db to methods called from this class
     * @return Connection object to the database
     */
    public static Connection muckDatabaseInit() {

        Connection conn = null;
        while (conn == null) {
            try {
                logger.info("Connecting.....");
                Class.forName(JDBC_DRIVER);
                logger.info("Looking for Database.....");
                conn = DriverManager.getConnection(buildDatabaseInitString(), USER, PASS);
                logger.info("Connected to database: " + DATABASE_NAME);
            } catch (SQLException e) {
                logger.error("Failed to connect to Database, {}", e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                logger.error("Failed to initialise driver, {}", e);
                System.exit(0);
            }
        }
        return conn;
    }

    /**
     * Adds a table in real time simulation. Useful for local development
     * @param tableName The name of the table to create
     * @param query The body of the create statement with row details
     * @throws SQLException
     */
    public static void addTable(String tableName, String query) throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = muckDatabaseInit();
            stmt = conn.createStatement();
            String queryConcat = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + query + ");";
            stmt.execute(queryConcat);
            logger.info("Table created");
        } catch (SQLException e) {
            logger.warn("Unable to add table: " + e);
            throw e;
        }finally {
            closeQuietly(stmt);
            closeQuietly(conn);
            logger.info("Connection closed");
        }
    }

    /**
     * function to query db: Used to update the status of a table,
     * row or individual data entry from String input.
     * @param query An SQL query string that is required to be performed
     * @throws SQLException
     */
    public static void updateTable(String query) throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = muckDatabaseInit();
            stmt = conn.createStatement();
            //Executing the query
            stmt.execute(query);
            logger.info("Table updated");
        } catch (SQLException e) {
            logger.warn("Unable to add row: " + e);
            throw e;
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
            logger.info("Connection closed");
        }
    }

    /**
     * Method for the GeneralDOA to update a table
     * @param table
     * @param updates
     * @param conditions
     * @throws SQLException
     */
    public static void updateRow(String table, JSONObject updates, JSONObject conditions, ArrayList<String> separators,
                                 int transactionId) throws SQLException {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = muckDatabaseInit();
            Iterator<String> keys = updates.keys();
            String updateCols = "";

            /* set column/value pairs placeholders*/
            while(keys.hasNext()) {
                String key = keys.next();
                //IDColumn = IDColumn + 1
                if (transactionId == 0){
                    updateCols += key + " = " + key + " + 1, ";
                }else if (transactionId == 1){
                    updateCols += key + " = " + key + " - 1, ";
                }else if (transactionId == 2){
                    updateCols += key + " = NOT " + key + ", ";
                }else {
                    updateCols += key + " = ?, ";
                }
            }

            /* set condition/value pairs placeholders*/
            String conditionCols = getConditionString(conditions, separators);

            // clean-up strings
            updateCols = updateCols.substring(0, updateCols.length()-2);

            String sql = "UPDATE "+ table + " SET " + updateCols + " WHERE " + conditionCols + ";";
            System.out.println("SQL: " + sql);
            ps = conn.prepareStatement(sql);
            //And values to the prepared statement and execute the query
            if(transactionId == 0 || transactionId == 1 || transactionId == 2){
                JsonObjectConverter.convert(conditions, ps).executeUpdate();
            }else{
                JsonObjectConverter.convert(updates, conditions, ps).executeUpdate();
            }

            logger.info("Table updated");

        } catch (SQLException e) {
            logger.warn("Unable to update row: " + e);
            throw e;
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
            logger.info("Connection closed");
        }
    }

    /**
     * Method for the GeneralDOA to add a row to a table
     * @param row A JSONObject representing the class
     * @param table The database table that will be used
     * @throws SQLException
     */
    public static void addRow(JSONObject row, String table) throws SQLException {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = muckDatabaseInit();
            Iterator<String> keys = row.keys();

            String columns = "";
            int numPlaceholders = row.length() - 1;
            /* set column names */
            while(keys.hasNext()) {
                String key = keys.next();
                if(key.equals("userId")) {
                    numPlaceholders--;
                    continue;
                }else if(key.startsWith("_")){
                    numPlaceholders--;
                }else{
                    columns += key + ", ";
                }
            }
            /* set place holders */
            String placeholder = "?, ";
            String placeholders =  placeholder.repeat(numPlaceholders);

            columns = columns.substring(0, columns.length()-2);
            String sql = "INSERT INTO "+ table + " ("+ columns +") VALUES (" + placeholders + "?)";
            System.out.println("SQL: " + sql);
            ps = conn.prepareStatement(sql);
            //And values to the prepared statement and execute the query
            JsonObjectConverter.convert(row, ps).executeUpdate();
            logger.info("Table updated");

        } catch (SQLException e) {
            logger.warn("Unable to add row: " + e);
            throw e;
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
            logger.info("Connection closed");
        }
    }


    /**
     * Method for the GeneralDOA to get a row based on PrimaryKey
     * @param pkValues An String array holding the primary key values
     * @param table The database table that will be used
     * @throws SQLException
     */
    public static JSONArray getRow(String[] pkValues, String table, Id userId) throws SQLException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs1;
        ResultSet rs2;
        JSONArray js;

        try {
            conn = muckDatabaseInit();

            ArrayList<String> pk = getPrimaryKey(conn, table);
            String pkStr = parsePrimaryKeys(pk, pkValues);

            String sql = "SELECT * FROM "+ table + " WHERE " + pkStr + ";";
            System.out.println("SQL: " + sql);
            ps = conn.prepareStatement(sql);

            //Executing the query
            rs2 = ps.executeQuery();

            js = ResultSetConverter.convert(rs2, userId);
            if(js.isEmpty()){
                throw new SQLException();
            }
            logger.info("Query Successful, returning data");

        } catch (SQLException e) {
            logger.warn("Unable to get row: " + e);
            throw e;
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
            logger.info("Connection closed");
        }
        return js;
    }

    /**
     * Method for GeneralDao to get a row/rows based on the provided conditions
     * @param table The database table that will be used
     * @param userId The userId of the request
     * @param conditions A string array with the conditions
     * @return JSONArray holding the converted ResultSet data
     * @throws SQLException
     */
    public static JSONArray getRows(String table, Id userId, String[] conditions) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs;
        JSONArray js;

        try {
            conn = muckDatabaseInit();

            String conditionsStr = "";
            for(int i = 0; i < conditions.length; i++){
                conditionsStr += conditions[i] + " AND ";
            }
            //clean up condition string
            conditionsStr = conditionsStr.substring(0, conditionsStr.length()-5);

            String sql = "SELECT * FROM "+ table + " WHERE " + conditionsStr + ";";
            System.out.println("SQL: " + sql);
            stmt = conn.createStatement();

            //Executing the query
            rs = stmt.executeQuery(sql);

            js = ResultSetConverter.convert(rs, userId);
            if(js.isEmpty()){
                logger.warn("No matches were found");
                //throw new SQLException();     no matches should be a valid result
            }
            logger.info("Query Successful, returning data");

        } catch (SQLException e) {
            logger.warn("Unable to get row: " + e);
            throw e;
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
            logger.info("Connection closed");
        }
        return js;
    }

    /**
     * Utility function to build a string of condition values
     * @param conditions
     * @param separators
     * @return
     */
    private static String getConditionString(JSONObject conditions, ArrayList<String> separators){
        Iterator<String> keys = conditions.keys();
        String conditionStr = "";
        /* set condition/value pairs placeholders*/
        int counter = 0;
        while(keys.hasNext()) {
            String key = keys.next();
            conditionStr += key + " " + separators.get(counter) + " ? AND ";
            counter ++;
        }
        // clean-up string
        conditionStr = conditionStr.substring(0, conditionStr.length()-5);

        return conditionStr;
    }

    /**
     * Utility function to get primary keys from ResultSetMetaData
     * @param conn
     * @param table
     * @return
     * @throws SQLException
     */
    private static ArrayList<String> getPrimaryKey(Connection conn, String table) throws SQLException{
        ArrayList<String> pk = new ArrayList<>();
        try{
            DatabaseMetaData meta=conn.getMetaData();
            ResultSet rs1=meta.getPrimaryKeys(null, null, table);
            while(rs1.next())
                pk.add(rs1.getString(4));
        } catch(SQLException e){
            throw e;
        }
        return pk;
    }

    /**
     * Utility function to build a string with primary key/values
     * @param pk
     * @param values
     * @return
     */
    private static String  parsePrimaryKeys( ArrayList<String> pk, String[] values){
        Iterator<String> iter = pk.iterator();
        String statement = "";
        int counter = 0;
        while(iter.hasNext()){
            String col = iter.next();
            if(isNumeric(values[counter])) {
                statement = statement + col + " = " + values[counter];
            }else{
                statement = statement + col + " = \'"+ values[counter] + "\'";
            }
            statement += " AND ";
            counter++;

        }
        statement = statement.substring(0, statement.length()-5);
        return statement;
    }

    /**
     * Utility function to check if string value is numeric
     * @param s
     * @return
     */
    private static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }



    /**
     * Overloaded function to query db: Queries the database with a given SQL query
     * @param query An SQL query string that is required to be performed
     * @return A List of HashMaps, with key/value pairs where key is the column name, value is the row value
     * @throws SQLException
     */
    public static List<HashMap<String, String>> queryTable(String query) throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd;
        List<HashMap<String, String>> results = new ArrayList<>();
        try {
            conn = muckDatabaseInit();
            stmt = conn.createStatement();

            //Executing the query
            rs = stmt.executeQuery(query);
            rsmd = rs.getMetaData();

            while(rs.next()) {
                HashMap<String , String> row = new HashMap<>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String label = rsmd.getColumnName(i);
                    String value = rs.getString(i);
                    row.put(label, value);
                }
                results.add(row);
            }


        } catch (SQLException e) {
            logger.warn("Unable to complete enquiry: " + e);
            throw e;
        } finally {
            closeQuietly(conn, stmt, rs);
            logger.info("Connection closed");
            return results;
        }
    }

    /**
     * Utility function to drop tables when changes are made to the sql file
     * @param tableName
     * @throws SQLException
     */
    public static void dropTable(String tableName) throws SQLException{
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = muckDatabaseInit();
            stmt = conn.createStatement();
            String queryConcat = "DROP TABLE " + tableName;
            stmt.execute(queryConcat);
            logger.info("Table dropped");
        } catch (SQLException e) {
            logger.warn("Unable to drop table");
            throw e;
        } finally {
            closeQuietly(stmt);
            closeQuietly(conn);
            logger.info("Connection closed");
        }
    }

    /**
     * An OS-agnostic way of building the init string for connecting to the DB.
     * Requires obtaining the absolute path to the SQL file.
     * @return The init string the DB connection
     */
    private static String buildDatabaseInitString() {
        // File object with absolute path.
        File f = new File(MuckDatabase.class.getResource(DB_SQL_FILE).toString());
        // Replace '\' with '/' for Windows, replace '%20' with ' ' for Mac.
        String path = f.getPath().replace("\\", "/")
            .replace("%20", " ");
        return String.format(DB_URL, path);
    }
}