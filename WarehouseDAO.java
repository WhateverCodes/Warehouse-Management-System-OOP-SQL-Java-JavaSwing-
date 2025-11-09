import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WarehouseDAO
 * -------------------------------------------
 * Handles CRUD operations for user-specific warehouses.
 * Each user has their own warehouse list prefixed with their username.
 */
public class WarehouseDAO {

    // -----------------------------
    // Inner class for holding info
    // -----------------------------
    public static class WarehouseInfo {
        public String name;
        public String city;
        public String address;
        public Date inauguration;
        public Timestamp lastActivity;
        public String notes;

        public WarehouseInfo(String name, String city, String address,
                             Date inauguration, Timestamp lastActivity, String notes) {
            this.name = name;
            this.city = city;
            this.address = address;
            this.inauguration = inauguration;
            this.lastActivity = lastActivity;
            this.notes = notes;
        }
    }

    // -----------------------------
    // Sanitize table-safe names
    // -----------------------------
    public static String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    // -----------------------------
    // Create new warehouse (Point 9: fields can be null)
    // -----------------------------
    public static void createWarehouse(String name, String city, String address,
                                       Date inaugurationDate, String notes) throws SQLException {
        if (!SessionManager.isLoggedIn())
            throw new IllegalStateException("No user logged in.");

        String username = SessionManager.getCurrentUser();
        String master = "warehouses";

        try (Connection conn = DBConnection.getConnection()) {
            // Insert warehouse metadata
            String sql = "INSERT INTO " + master + " (username, warehouse_name, city, address, inauguration_date, last_activity_date, notes) "
                    + "VALUES (?, ?, ?, ?, ?, NOW(), ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, name);
                ps.setString(3, city);
                ps.setString(4, address);
                ps.setDate(5, inaugurationDate);
                ps.setString(6, notes);
                ps.executeUpdate();
            }

            // Create user-specific records table for this warehouse
            String safeName = sanitizeName(name);
            String tableName = SessionManager.prefixTable("records_" + safeName);
            String createTableSQL =
                    "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                            + "id INT AUTO_INCREMENT PRIMARY KEY,"
                            + "product VARCHAR(100) NOT NULL,"
                            + "supplier VARCHAR(100),"
                            + "customer VARCHAR(100),"
                            + "total_quantity INT DEFAULT 0,"
                            + "import_quantity INT DEFAULT 0,"
                            + "import_price DOUBLE(10,2) DEFAULT 0.00,"
                            + "export_quantity INT DEFAULT 0,"
                            + "export_price DOUBLE(10,2) DEFAULT 0.00,"
                            + "date DATE NOT NULL)";
            try (Statement st = conn.createStatement()) {
                st.execute(createTableSQL);
            }
        }
    }

    // -----------------------------
    // Edit existing warehouse
    // -----------------------------
    public static void editWarehouse(String oldName, String newName, String city,
                                     String address, Date inaugurationDate, String notes) throws SQLException {
        if (!SessionManager.isLoggedIn())
            throw new IllegalStateException("No user logged in.");

        String username = SessionManager.getCurrentUser();
        String master = "warehouses";

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE " + master
                    + " SET warehouse_name=?, city=?, address=?, inauguration_date=?, notes=?, last_activity_date=NOW() "
                    + "WHERE username=? AND warehouse_name=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newName);
                ps.setString(2, city);
                ps.setString(3, address);
                ps.setDate(4, inaugurationDate);
                ps.setString(5, notes);
                ps.setString(6, username);
                ps.setString(7, oldName);
                ps.executeUpdate();
            }

            // If name changed, rename records table too
            if (!oldName.equals(newName)) {
                String oldTable = SessionManager.prefixTable("records_" + sanitizeName(oldName));
                String newTable = SessionManager.prefixTable("records_" + sanitizeName(newName));
                try (Statement st = conn.createStatement()) {
                    st.execute("RENAME TABLE " + oldTable + " TO " + newTable);
                }
            }
        }
    }

    // -----------------------------
    // Delete warehouse
    // -----------------------------
    public static void deleteWarehouse(String name) throws SQLException {
        if (!SessionManager.isLoggedIn())
            throw new IllegalStateException("No user logged in.");

        String username = SessionManager.getCurrentUser();
        String master = "warehouses";

        try (Connection conn = DBConnection.getConnection()) {
            // Delete from master
            String sql = "DELETE FROM " + master + " WHERE username=? AND warehouse_name=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, name);
                ps.executeUpdate();
            }

            // Drop associated records table
            String safeName = sanitizeName(name);
            String table = SessionManager.prefixTable("records_" + safeName);
            try (Statement st = conn.createStatement()) {
                st.execute("DROP TABLE IF EXISTS " + table);
            }
        }
    }

    // -----------------------------
    // Get info for a single warehouse (used for pre-fill edit and row click)
    // -----------------------------
    public static WarehouseInfo getWarehouseByName(String name) {
        if (!SessionManager.isLoggedIn()) return null;
        String username = SessionManager.getCurrentUser();
        String sql = "SELECT warehouse_name, city, address, inauguration_date, last_activity_date, notes "
                + "FROM warehouses WHERE username=? AND warehouse_name=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new WarehouseInfo(
                            rs.getString("warehouse_name"),
                            rs.getString("city"),
                            rs.getString("address"),
                            rs.getDate("inauguration_date"),
                            rs.getTimestamp("last_activity_date"),
                            rs.getString("notes")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    // -----------------------------
    // Get all warehouses for user (Point 1 & 3 Fix: Order by creation ID)
    // -----------------------------
    public static List<WarehouseInfo> getAllWarehouses() {
        List<WarehouseInfo> list = new ArrayList<>();
        if (!SessionManager.isLoggedIn()) return list;

        String username = SessionManager.getCurrentUser();
        // FIX: Order by warehouse_id (the creation order), not last_activity_date
        String sql = "SELECT warehouse_name, city, address, inauguration_date, last_activity_date, notes "
                + "FROM warehouses WHERE username=? ORDER BY warehouse_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WarehouseInfo(
                            rs.getString("warehouse_name"),
                            rs.getString("city"),
                            rs.getString("address"),
                            rs.getDate("inauguration_date"),
                            rs.getTimestamp("last_activity_date"),
                            rs.getString("notes")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    // -----------------------------
    // Update last activity timestamp
    // -----------------------------
    public static void updateLastActivity(String warehouseName) {
        if (!SessionManager.isLoggedIn()) return;
        String username = SessionManager.getCurrentUser();
        String sql = "UPDATE warehouses SET last_activity_date=NOW() WHERE username=? AND warehouse_name=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, warehouseName);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}