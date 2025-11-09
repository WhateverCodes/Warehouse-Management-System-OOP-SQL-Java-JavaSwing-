import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * FutureTradeDAO
 * -------------------------------------------
 * Handles CRUD operations for global future-trade records.
 * Each record belongs to a specific user (username field).
 */
public class FutureTradeDAO {

    // --------------------------------------------------------
    // Helper: Find the next sequential ID for the current user (Point 3 Fix)
    // --------------------------------------------------------
    private static int getNextUserId() {
        if (!SessionManager.isLoggedIn()) return 1;

        String username = SessionManager.getCurrentUser();
        // Finds the maximum existing ID for the current user only
        String sql = "SELECT MAX(id) AS max_id FROM future_trades WHERE username=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Returns 1 if no records exist, or max_id + 1 if records are found.
                    return rs.getInt("max_id") + 1;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 1;
    }

    // --------------------------------------------------------
    // Get all future trades for current user
    // --------------------------------------------------------
    public static ArrayList<FutureTrade> getAllFutureTrades() {
        ArrayList<FutureTrade> list = new ArrayList<>();
        if (!SessionManager.isLoggedIn()) return list;

        String username = SessionManager.getCurrentUser();
        // FIX: Order by ID to respect the intended sequence
        String sql = "SELECT * FROM future_trades WHERE username=? ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FutureTrade(
                            rs.getInt("id"),
                            rs.getString("warehouse_name"),
                            rs.getString("product"),
                            rs.getString("supplier"),
                            rs.getString("customer"),
                            rs.getInt("import_quantity"),
                            rs.getDouble("import_price"),
                            rs.getInt("export_quantity"),
                            rs.getDouble("export_price"),
                            rs.getDate("date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    // --------------------------------------------------------
    // Get single future trade record by ID (Point 5 support)
    // --------------------------------------------------------
    public static FutureTrade getFutureTradeById(int id) throws SQLException {
        if (!SessionManager.isLoggedIn()) return null;
        String username = SessionManager.getCurrentUser();
        String sql = "SELECT * FROM future_trades WHERE id=? AND username=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FutureTrade(
                            rs.getInt("id"),
                            rs.getString("warehouse_name"),
                            rs.getString("product"),
                            rs.getString("supplier"),
                            rs.getString("customer"),
                            rs.getInt("import_quantity"),
                            rs.getDouble("import_price"),
                            rs.getInt("export_quantity"),
                            rs.getDouble("export_price"),
                            rs.getDate("date").toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    // --------------------------------------------------------
    // Add a new future trade
    // --------------------------------------------------------
    public static void addFutureTrade(FutureTrade f) throws SQLException {
        if (!SessionManager.isLoggedIn())
            throw new IllegalStateException("User not logged in.");

        String username = SessionManager.getCurrentUser();
        int nextId = getNextUserId(); // Point 3: Get the user's next sequential ID

        String sql = "INSERT INTO future_trades (id, username, warehouse_name, product, supplier, customer, "
                + "import_quantity, import_price, export_quantity, export_price, date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextId); // Use the manually calculated ID
            ps.setString(2, username);
            ps.setString(3, f.getWarehouse());
            ps.setString(4, f.getName());
            ps.setString(5, f.getSupplier());
            ps.setString(6, f.getCustomer());
            ps.setInt(7, f.getimpQuantity());
            ps.setDouble(8, f.getimpPrice());
            ps.setInt(9, f.getexpQuantity());
            ps.setDouble(10, f.getexpPrice());
            ps.setDate(11, Date.valueOf(f.getDateAdded()));
            ps.executeUpdate();
        }
    }

    // --------------------------------------------------------
    // Update existing future trade by ID
    // --------------------------------------------------------
    public static void updateFutureTrade(FutureTrade f) throws SQLException {
        if (!SessionManager.isLoggedIn())
            throw new IllegalStateException("User not logged in.");

        String username = SessionManager.getCurrentUser();
        String sql = "UPDATE future_trades SET warehouse_name=?, product=?, supplier=?, customer=?, "
                + "import_quantity=?, import_price=?, export_quantity=?, export_price=?, date=? "
                + "WHERE id=? AND username=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getWarehouse());
            ps.setString(2, f.getName());
            ps.setString(3, f.getSupplier());
            ps.setString(4, f.getCustomer());
            ps.setInt(5, f.getimpQuantity());
            ps.setDouble(6, f.getimpPrice());
            ps.setInt(7, f.getexpQuantity());
            ps.setDouble(8, f.getexpPrice());
            ps.setDate(9, Date.valueOf(f.getDateAdded()));
            ps.setInt(10, f.getId());
            ps.setString(11, username);
            ps.executeUpdate();
        }
    }

    // --------------------------------------------------------
    // Delete future trade by ID (Point 4)
    // --------------------------------------------------------
    public static void deleteFutureTrade(int id) throws SQLException {
        if (!SessionManager.isLoggedIn())
            throw new IllegalStateException("User not logged in.");

        String username = SessionManager.getCurrentUser();
        String sql = "DELETE FROM future_trades WHERE id=? AND username=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    public static void addFutureImport(FutureTrade f) throws SQLException {
        if (!SessionManager.isLoggedIn()) throw new IllegalStateException("User not logged in.");
        String username = SessionManager.getCurrentUser();
        int nextId = getNextUserId(); // Point 3: Get the user's next sequential ID

        String sql = "INSERT INTO future_trades (id, username, warehouse_name, product, supplier, customer, "
                + "import_quantity, import_price, export_quantity, export_price, date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextId); // Use the manually calculated ID
            ps.setString(2, username);
            ps.setString(3, f.getWarehouse());
            ps.setString(4, f.getName());
            ps.setString(5, f.getSupplier());
            ps.setString(6, f.getCustomer());
            ps.setInt(7, f.getimpQuantity());
            ps.setDouble(8, f.getimpPrice());
            ps.setDate(9, Date.valueOf(f.getDateAdded()));
            ps.executeUpdate();
        }
    }

    /**
     * Add a future export record for the current logged-in user.
     */
    public static void addFutureExport(FutureTrade f) throws SQLException {
        if (!SessionManager.isLoggedIn()) throw new IllegalStateException("User not logged in.");
        String username = SessionManager.getCurrentUser();
        int nextId = getNextUserId(); // Point 3: Get the user's next sequential ID

        String sql = "INSERT INTO future_trades (id, username, warehouse_name, product, supplier, customer, "
                + "import_quantity, import_price, export_quantity, export_price, date) "
                + "VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextId); // Use the manually calculated ID
            ps.setString(2, username);
            ps.setString(3, f.getWarehouse());
            ps.setString(4, f.getName());
            ps.setString(5, f.getSupplier());
            ps.setString(6, f.getCustomer());
            ps.setInt(7, f.getexpQuantity());
            ps.setDouble(8, f.getexpPrice());
            ps.setDate(9, Date.valueOf(f.getDateAdded()));
            ps.executeUpdate();
        }
    }

    /**
     * Shift a future trade (id) into the specified warehouse's records table.
     * This performs the import/export action(s) in the target warehouse and then deletes the future_trades row.
     */
    public static void shiftToWarehouse(int id) throws SQLException {
        if (!SessionManager.isLoggedIn()) throw new IllegalStateException("User not logged in.");
        String username = SessionManager.getCurrentUser();
        Connection conn = null;

        // 1) fetch the future trade for this user
        String selectSql = "SELECT * FROM future_trades WHERE id=? AND username=?";
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction for atomicity

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {

                ps.setInt(1, id);
                ps.setString(2, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Future trade not found or not owned by current user.");

                    String warehouseName = rs.getString("warehouse_name");
                    String product = rs.getString("product");
                    String supplier = rs.getString("supplier");
                    String customer = rs.getString("customer");
                    int impQty = rs.getInt("import_quantity");
                    double impPrice = rs.getDouble("import_price");
                    int expQty = rs.getInt("export_quantity");
                    double expPrice = rs.getDouble("export_price");
                    Date date = rs.getDate("date");
                    LocalDate localDate = date == null ? LocalDate.now() : date.toLocalDate();

                    // 2) perform actions on the warehouse records via ProductDAO
                    ProductDAO.setCurrentWarehouse(warehouseName);

                    // Flag to check if export succeeded
                    boolean exportOk = true;

                    // If there's an import quantity, add it as an import record
                    if (impQty > 0) {
                        Product importProduct = new Product(
                                0,
                                product,
                                supplier,
                                customer,
                                0,
                                impQty,
                                impPrice,
                                0,
                                0.0,
                                localDate
                        );
                        // Temporarily bypass transaction for ProductDAO calls
                        conn.setAutoCommit(true);
                        ProductDAO.addProduct(importProduct);
                        conn.setAutoCommit(false);
                    }

                    // If there's an export quantity, perform an export (check stock inside ProductDAO.exportProduct)
                    if (expQty > 0) {
                        Product exportProduct = new Product(
                                0,
                                product,
                                supplier,
                                customer,
                                0,
                                0,
                                0.0,
                                expQty,
                                expPrice,
                                localDate
                        );
                        // Temporarily bypass transaction for ProductDAO calls
                        conn.setAutoCommit(true);
                        exportOk = ProductDAO.exportProduct(exportProduct);
                        conn.setAutoCommit(false);

                        if (!exportOk) {
                            // If export is not possible (insufficient stock), throw so caller can handle.
                            throw new SQLException("Shift failed: insufficient stock for export in target warehouse.");
                        }
                    }

                    // 3) delete the future trade row now that it's shifted (Point 11)
                    String delSql = "DELETE FROM future_trades WHERE id=? AND username=?";
                    try (PreparedStatement delPs = conn.prepareStatement(delSql)) {
                        delPs.setInt(1, id);
                        delPs.setString(2, username);
                        delPs.executeUpdate();
                    }

                    conn.commit();
                }
            }
        } catch (SQLException ex) {
            if (conn != null) conn.rollback();
            throw ex; // Re-throw the exception (including insufficient stock)
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }
}