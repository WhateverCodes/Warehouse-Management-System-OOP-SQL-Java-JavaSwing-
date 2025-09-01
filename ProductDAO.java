import java.sql.*;
import java.util.ArrayList;

public class ProductDAO {

    // Import
    public static void addProduct(Product product) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO records (Product, Total_Quantity, Import_Quantity, Import_Price, Export_Quantity, Export_Price, Date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.gettotQuantity());
            stmt.setInt(3, product.getimpQuantity());
            stmt.setDouble(4, product.getimpPrice());
            stmt.setInt(5, 0);
            stmt.setDouble(6, 0);
            stmt.setDate(7, java.sql.Date.valueOf(product.getDateAdded()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Export
    public static boolean exportProduct(Product product) {
        try (Connection conn = DBConnection.getConnection()) {
            // Step 1: Check current total stock for this product
            String checkQuery = "SELECT SUM(Import_Quantity) - SUM(Export_Quantity) AS Available " +
                    "FROM records WHERE Product=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, product.getName());
            ResultSet rs = checkStmt.executeQuery();

            int available = 0;
            if (rs.next()) {
                available = rs.getInt("Available");
            }

            // Step 2: Prevent negative total
            if (product.getexpQuantity() > available) {
                System.out.println("❌ Export denied: Not enough stock for " + product.getName());
                return false;  // <-- reject export
            }

            // Step 3: Proceed with export
            String query = "INSERT INTO records (Product, Total_Quantity, Import_Quantity, Import_Price, Export_Quantity, Export_Price, Date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, product.getName());
            stmt.setInt(2, available - product.getexpQuantity()); // updated total quantity
            stmt.setInt(3, 0);
            stmt.setDouble(4, 0);
            stmt.setInt(5, product.getexpQuantity());
            stmt.setDouble(6, product.getexpPrice());
            stmt.setDate(7, java.sql.Date.valueOf(product.getDateAdded()));
            stmt.executeUpdate();

            return true; // ✅ success
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Update by ID
    public static void updateProduct(Product product) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE records SET Product=?, Import_Quantity=?, Import_Price=?, Export_Quantity=?, Export_Price=?, Date=? WHERE ID=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getimpQuantity());
            stmt.setDouble(3, product.getimpPrice());
            stmt.setInt(4, product.getexpQuantity());
            stmt.setDouble(5, product.getexpPrice());
            stmt.setDate(6, java.sql.Date.valueOf(product.getDateAdded()));
            stmt.setInt(7, product.getId());  // ✅ Using ID now
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Delete by ID
    public static void deleteProduct(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "DELETE FROM records WHERE ID=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);  // ✅ Using ID now
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // View all
    public static ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM records");
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("ID"),
                        rs.getString("Product"),
                        rs.getInt("Total_Quantity"),
                        rs.getInt("Import_Quantity"),
                        rs.getDouble("Import_Price"),
                        rs.getInt("Export_Quantity"),
                        rs.getDouble("Export_Price"),
                        rs.getDate("Date").toLocalDate()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
