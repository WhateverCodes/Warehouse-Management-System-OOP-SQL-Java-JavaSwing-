import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles database connection using MySQL JDBC.
 * IMPORTANT: This class is modified to ensure connection reliability.
 * It now provides a NEW connection for every call to ensure reliability
 * and prevent "connection closed" errors when DAO methods implicitly close
 * connections via try-with-resources.
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/warehouse_db";
    private static final String USER = "root"; // your MySQL username
    private static final String PASSWORD = "sqlkapassword"; // your MySQL password

    // The static 'connection' field has been removed to enforce the connection-per-request model.

    public static Connection getConnection() throws SQLException {
        // Always return a new connection. DAO methods must ensure this connection is closed
        // using the try-with-resources block (try (Connection conn = ...) { ... }).
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * This method is retained for compatibility but is generally unused since
     * connections are closed automatically by DAO try-with-resources blocks.
     */
    public static void closeConnection() {
        // No implementation needed since no static connection is stored/reused.
    }
}