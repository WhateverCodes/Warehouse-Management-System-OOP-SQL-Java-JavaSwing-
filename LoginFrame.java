import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;

public class LoginFrame extends JFrame {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin, btnRegister, btnUserRecords; // Added btnUserRecords (Point 7)

    public LoginFrame() {
        setTitle("Warehouse Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 280);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Title panel ---
        JLabel lblTitle = new JLabel("Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        // --- Center panel (form) ---
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        tfUsername = new JTextField(20);
        pfPassword = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0; center.add(lblUser, gbc);
        gbc.gridx = 1; center.add(tfUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 1; center.add(lblPass, gbc);
        gbc.gridx = 1; center.add(pfPassword, gbc);

        add(center, BorderLayout.CENTER);

        // --- Bottom buttons (Point 7) ---
        JPanel bottom = new JPanel();
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");
        btnUserRecords = new JButton("User Records"); // Point 7

        bottom.add(btnLogin);
        bottom.add(btnRegister);
        bottom.add(btnUserRecords);

        add(bottom, BorderLayout.SOUTH);

        // --- Button actions ---
        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });
        // Point 7: User Records Button Action
        btnUserRecords.addActionListener(e -> showUserRecords());
    }

    /**
     * Handles login validation.
     * If valid → sets session user → opens WarehouseGUI.
     */
    private void login() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Uses the updated UserDAO which relies on DBConnection.getConnection()
        if (UserDAO.validateLogin(username, password)) {
            JOptionPane.showMessageDialog(this, "Welcome, " + username + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            // Point 6: The main screen will open and contain the logout button
            new WarehouseGUI().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows a dialog displaying all user records (Point 7).
     */
    private void showUserRecords() {
        // First, prompt for password '1234'
        String password = JOptionPane.showInputDialog(this, "Enter Admin Password :", "User Records Access", JOptionPane.QUESTION_MESSAGE);

        if (password == null || !password.equals("1234")) {
            JOptionPane.showMessageDialog(this, "Incorrect Password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Fetch all users
        List<UserDAO.UserInfo> users = UserDAO.getAllUsers();

        // Prepare table model
        String[] columnNames = {"Username", "Password", "Email"};
        // FIX: The array size is correctly set to 3 columns
        Object[][] data = new Object[users.size()][3];

        for (int i = 0; i < users.size(); i++) {
            UserDAO.UserInfo user = users.get(i);
            data[i][0] = user.username;
            data[i][1] = user.password;
            // FIX: Accesses the now-available 'email' field.
            data[i][2] = user.email;
        }

        JTable table = new JTable(data, columnNames);
        table.setEnabled(false); // Make table read-only
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        JDialog dialog = new JDialog(this, "All Registered User Records", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}