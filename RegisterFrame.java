import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * RegisterFrame
 * -------------------------------------------
 * Handles user registration for the Warehouse Management System.
 * Extends JFrame (demonstrating inheritance).
 * Inserts new users into the 'users' table.
 */
public class RegisterFrame extends JFrame {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JTextField tfEmail;
    private JButton btnRegister, btnBack;

    public RegisterFrame() {
        setTitle("Warehouse Management System - Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Create New Account", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        JLabel lblEmail = new JLabel("Email:");

        tfUsername = new JTextField(20);
        pfPassword = new JPasswordField(20);
        tfEmail = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; center.add(lblUser, gbc);
        gbc.gridx = 1; center.add(tfUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 1; center.add(lblPass, gbc);
        gbc.gridx = 1; center.add(pfPassword, gbc);
        gbc.gridx = 0; gbc.gridy = 2; center.add(lblEmail, gbc);
        gbc.gridx = 1; center.add(tfEmail, gbc);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        btnRegister = new JButton("Register");
        btnBack = new JButton("Back to Login");
        bottom.add(btnRegister);
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> registerUser());
        btnBack.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    /**
     * Handles inserting new users into the database.
     * Ensures username uniqueness.
     */
    private void registerUser() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();
        String email = tfEmail.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String checkSql = "SELECT username FROM users WHERE username=?";
        String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, username);
                insertPs.setString(2, password);
                insertPs.setString(3, email.isEmpty() ? null : email);
                insertPs.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Registration successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}
