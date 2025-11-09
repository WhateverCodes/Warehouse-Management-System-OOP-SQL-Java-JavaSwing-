import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // -----------------------------
    // Inner class for holding user info (for 'User Records' feature)
    // FIX: Added email field
    // -----------------------------
    public static class UserInfo {
        public String username;
        public String password;
        public String email; // Added email field

        public UserInfo(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }
    }

    // -----------------------------
    // Register a new user
    // -----------------------------
    public static boolean registerUser(String username, String password, String email) {
        String checkSql = "SELECT username FROM users WHERE username=?";
        String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    System.err.println("Username already exists: " + username);
                    return false;
                }
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, username);
                insertPs.setString(2, password);
                insertPs.setString(3, email.isEmpty() ? null : email);
                insertPs.executeUpdate();
            }

            return true;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.err.println("Username already exists: " + username);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------
    // Validate login
    // -----------------------------
    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE Username=? AND Password=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SessionManager.setCurrentUser(username);
                return true;
            } else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------
    // Get all users (Point 7)
    // FIX: Selects 'email' and passes it to the constructor
    // -----------------------------
    public static List<UserInfo> getAllUsers() {
        List<UserInfo> list = new ArrayList<>();
        // FIX: Added email to SELECT statement
        String sql = "SELECT username, password, email FROM users ORDER BY username ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new UserInfo(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email") // Pass email to the new constructor
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}