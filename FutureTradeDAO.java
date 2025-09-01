import java.sql.*;
import java.util.ArrayList;

public class FutureTradeDAO {

    // Add
    public static void addFutureTrade(FutureTrade trade) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO future_trades (Product, Import_Quantity, Import_Price, Export_Quantity, Export_Price, Date) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, trade.getName());
            stmt.setInt(2, trade.getimpQuantity());
            stmt.setDouble(3, trade.getimpPrice());
            stmt.setInt(4, trade.getexpQuantity());
            stmt.setDouble(5, trade.getexpPrice());
            stmt.setDate(6, java.sql.Date.valueOf(trade.getDateAdded()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update
    public static void updateFutureTrade(FutureTrade trade) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE future_trades SET Product=?, Import_Quantity=?, Import_Price=?, Export_Quantity=?, Export_Price=?, Date=? WHERE ID=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, trade.getName());
            stmt.setInt(2, trade.getimpQuantity());
            stmt.setDouble(3, trade.getimpPrice());
            stmt.setInt(4, trade.getexpQuantity());
            stmt.setDouble(5, trade.getexpPrice());
            stmt.setDate(6, java.sql.Date.valueOf(trade.getDateAdded()));
            stmt.setInt(7, trade.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Delete
    public static void deleteFutureTrade(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "DELETE FROM future_trades WHERE ID=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get all
    public static ArrayList<FutureTrade> getAllFutureTrades() {
        ArrayList<FutureTrade> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM future_trades");
            while (rs.next()) {
                list.add(new FutureTrade(
                        rs.getInt("ID"),
                        rs.getString("Product"),
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

    // Shift → Move to main `records` table
    public static void shiftToMain(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Get the trade details
            String select = "SELECT * FROM future_trades WHERE ID=?";
            PreparedStatement ps = conn.prepareStatement(select);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String product = rs.getString("Product");
                int impQty = rs.getInt("Import_Quantity");
                double impPrice = rs.getDouble("Import_Price");
                int expQty = rs.getInt("Export_Quantity");
                double expPrice = rs.getDouble("Export_Price");
                java.sql.Date date = rs.getDate("Date");

                // 2. Insert into records
                String insert = "INSERT INTO records (Product, Total_Quantity, Import_Quantity, Import_Price, Export_Quantity, Export_Price, Date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insert);
                stmt.setString(1, product);
                stmt.setInt(2, impQty - expQty);
                stmt.setInt(3, impQty);
                stmt.setDouble(4, impPrice);
                stmt.setInt(5, expQty);
                stmt.setDouble(6, expPrice);
                stmt.setDate(7, date);
                stmt.executeUpdate();

                // 3. Delete from future_trades
                deleteFutureTrade(id);
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
