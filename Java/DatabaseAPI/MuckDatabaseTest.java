package muck.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.apache.commons.dbutils.DbUtils.closeQuietly;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MuckDatabaseTest {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:test_db;INIT=runscript from 'src/test/resources/test_db.sql'";
    //  Database credentials
    static final String USER = "";
    static final String PASS = "";

    private static final Logger logger = LogManager.getLogger(MuckDatabaseTest.class);

    private static Connection h2Conn;

    @BeforeEach
    public  void beforeEach() throws SQLException{
        h2Conn = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    @AfterEach
    public void afterEach(){
        closeQuietly(h2Conn);
    }


    /**
     * Tests that H2 driver is discoverable in the crosspath
     * @throws ClassNotFoundException
     */
    @Test
    @Order(1)
    public void testH2DriverConnection() throws ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
        logger.info("Found database driver");
    }

    /**
     * Tests that H2 connection can be established
     * @throws SQLException
     */
    @Test
    @Order(2)
    public void testDatabaseConnection(){

        assertNotNull(h2Conn);
        logger.info("Database connection verified");
    }

    /**
     * Tests that a table can be added
     * @throws SQLException
     */

    @Test
    @Order(3)
    public void testDataBaseAddTable() throws SQLException{

        Statement stmt = h2Conn.createStatement();
        String dbName = "Test";
        String query = "id INTEGER PRIMARY KEY, " +
                " name VARCHAR(100) NOT NULL";
        String queryConcat = "CREATE TABLE IF NOT EXISTS " + dbName + " ( " + query + " );";
        stmt.execute(queryConcat);
        logger.info("Table correctly created");
    }

    /**
     * Test row updates
     * @throws SQLException
     */

    @Test
    @Order(4)
    public void testDataBaseUpdateRow() throws SQLException{

        Statement stmt = h2Conn.createStatement();
        String dbName = "Test";
        Date now = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("MMddhhmmss");
        String currTime = ft.format(now);
        String query = "INSERT INTO " + dbName +"("
                + "id, name) VALUES "
                + "("+currTime+", 'Test5') ";
        stmt.execute(query);
        logger.info("Table correctly updated");
    }

    /**
     * Tests database queries
     * @throws SQLException
     */

    @Test
    @Order(5)
    public void testDataBaseQueryTable() throws SQLException{

        Statement stmt = h2Conn.createStatement();
        String dbName = "Test";
        String query = "SELECT * FROM " + dbName;
        stmt.execute(query);
        logger.info("Database query completed");
    }

    /**
     * Test dropping table
     * @throws SQLException
     */

    @Test
    @Order(6)
    public void testDataDropTable() throws SQLException{

        Statement stmt = h2Conn.createStatement();
        String dbName = "Test";
        String query = "DROP TABLE ";
        String queryConcat = query + dbName + ";";
        stmt.execute(queryConcat);
        logger.info("Table successfully dropped");
    }

    /*
    * From MuckServer.java - removed on 05 Sept 2020
    * Do these need to be made into tests?
    */

    /*
     * H2 Database connection demo
     */
//        GeneralDAO<DatabaseEntityTest> databaseEntityTestDAO = new GeneralDAO<>("users_test", DatabaseEntityTest.class);
//        GeneralDAO<DatabaseEntityTestTwo> databaseEntityTestTwoDAO = new GeneralDAO<>("some_table", DatabaseEntityTestTwo.class);


//         Drop tables to start fresh demo or if table definition in script changes
//        use individual table name or * to drop all
//        try{
//            MuckDatabase.dropTable("*");
//        }catch(SQLException e){
//            logger.warn("Error dropping table");
//        }

    /*
     * Testing adding a row to the Users_test table using the GeneralDAO
     */

//        Data object created in and sent from client DatabaseEntityTest is stored in muck.protocol.connection and extends parcel
//        DatabaseEntityTest user = new DatabaseEntityTest(Id.zero(), 22, "Testing22");

//saving an entity to the database through DAO in array
//        if (databaseEntityTestDAO.saveRow(user)) {
//            logger.info("Database Updated for DatabaseEntityTest");
//        } else {
//            logger.info("Database Not Updated for DatabaseEntityTest");
//        }

    /*
     * Testing adding a row to a different table also using the GeneralDAO
     */

//Another Data object created in and sent from client can use the same GeneralDAO class
//        DatabaseEntityTestTwo someEntity = new DatabaseEntityTestTwo(Id.zero(), 12, "Testvalue", 3.14, true);

//        if (databaseEntityTestTwoDAO.saveRow(someEntity)) {
//            logger.info("Database Updated for DatabaseEntityTestTwo");
//        } else {
//            logger.info("Database Not Updated for DatabaseEntityTestTwo");
//        }

    /*
     * Testing Querying database:
     * 1. select all from User_test
     * 2. select all from some_table
     */
// 1.

//        try {
//            List<HashMap<String, String>> results = MuckDatabase.queryTable("SELECT * FROM users_test");
//            System.out.println("Results from user_test table");
//            if (results == null) {
//                System.out.println("EMPTY");
//            } else {
//                results.forEach((row) -> {
//                    row.entrySet().forEach(entry -> {
//                        System.out.println(entry.getKey() + " " + entry.getValue());
//                    });
//                });
//            }
//        } catch (SQLException e) {
//            logger.warn("Error Testing database");
//        }

//         2.

//        try {
//            List<HashMap<String, String>> results2 = MuckDatabase.queryTable("SELECT * FROM some_table");
//            System.out.println("Results from some_table table");
//            if (results2 == null) {
//                System.out.println("EMPTY");
//            } else {
//                results2.forEach((row) -> {
//                    row.entrySet().forEach(entry -> {
//                        System.out.println(entry.getKey() + " " + entry.getValue());
//                    });
//                });
//            }
//        } catch (SQLException e) {
//            logger.warn("Error Testing database");
//        }

    /*
     * Testing getting a database entity with GeneralDAO
     */

//         AccessSomeDataTableDAO object defined above linked to "some_table"
//        DatabaseAccessEvent getSomeEntity = new DatabaseAccessEvent(Id.zero(), 0, 9, "DatabaseEntityTest");
//
//
//      try{
//          GeneralDAO<DatabaseEntityTestTwo> testDAO = new GeneralDAO<>("some_table", DatabaseEntityTestTwo.class);
//          DatabaseEntityTestTwo result = testDAO.getRow(9, Id.zero());
//          logger.info("DatabaseEntityTestTwo {}", result);
//      }catch(SQLException e){
//
//      }

}
