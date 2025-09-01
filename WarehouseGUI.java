import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;

public class WarehouseGUI extends JFrame {
    // shared input fields
    private JTextField nameField, impqntfield, imppriceField, expqntfield, exppriceField, dateField;
    private JButton dateButton, viewTable;
    private JButton importButton, exportButton, deleteButton, updateButton;
    private JButton enterButton;
    private JTable table;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JPanel inputPanel;
    private JButton filterButton, sortButton;

    // future trades controls
    private JButton futureTradesButton;
    private JButton ftImportButton, ftExportButton, ftUpdateButton, ftDeleteButton, ftShiftButton, ftViewButton;

    private javax.swing.table.TableRowSorter<DefaultTableModel> sorter;

    public WarehouseGUI() {
        setTitle("Warehouse Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- TOP PANEL ----------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importButton = new JButton("Record Import");
        exportButton = new JButton("Record Export");
        updateButton = new JButton("Update Record");
        deleteButton = new JButton("Delete Record");
        viewTable = new JButton("View Table");
        filterButton = new JButton("Filter Table");
        sortButton = new JButton("Sort By");
        futureTradesButton = new JButton("Future Trades"); // initialize here

        topPanel.add(importButton);
        topPanel.add(exportButton);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);
        topPanel.add(viewTable);
        topPanel.add(filterButton);
        topPanel.add(sortButton);
        topPanel.add(futureTradesButton);

        add(topPanel, BorderLayout.NORTH);

        // ---------- CENTER (TABLE) ----------
        table = new JTable();
        scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ---------- BOTTOM (INPUT + STATUS) ----------
        inputPanel = new JPanel(new GridLayout(0, 2, 6, 6)); // initialize BEFORE use
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Section"));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // ---------- SHARED FIELDS ----------
        nameField = new JTextField(12);
        impqntfield = new JTextField(6);
        imppriceField = new JTextField(8);
        expqntfield = new JTextField(6);
        exppriceField = new JTextField(8);
        dateField = new JTextField(10);
        dateButton = new JButton("Today");
        dateButton.addActionListener(e -> dateField.setText(LocalDate.now().toString()));

        enterButton = new JButton("Enter");

        // ---------- BUTTON LISTENERS: Records (existing) ----------
        importButton.addActionListener(e -> showRecordImportForm());
        exportButton.addActionListener(e -> showRecordExportForm());
        updateButton.addActionListener(e -> showRecordUpdateForm());
        deleteButton.addActionListener(e -> showRecordDeleteForm());

        viewTable.addActionListener(e -> showRecordsTable());

        filterButton.addActionListener(e -> {
            if (sorter == null) {
                statusLabel.setText("❌ Please load a table first (View Table).");
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter product name to filter:");
            if (input != null && !input.trim().isEmpty()) {
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + input, 1));
                statusLabel.setText("🔍 Filtering by: " + input);
            } else {
                sorter.setRowFilter(null);
                statusLabel.setText("✅ Filter cleared.");
            }
        });

        sortButton.addActionListener(e -> {
            if (sorter == null) {
                statusLabel.setText("❌ Please load a table first (View Table).");
                return;
            }
            String[] options = {"ID", "Product", "Total Qty", "Import Qty", "Import Price", "Export Qty", "Export Price", "Date"};
            String choice = (String) JOptionPane.showInputDialog(
                    this, "Sort by:", "Sort Options",
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]
            );
            if (choice != null) {
                int columnIndex = switch (choice) {
                    case "ID" -> 0;
                    case "Product" -> 1;
                    case "Total Qty" -> 2;
                    case "Import Qty" -> 3;
                    case "Import Price" -> 4;
                    case "Export Qty" -> 5;
                    case "Export Price" -> 6;
                    case "Date" -> 7;
                    default -> 0;
                };
                sorter.setSortKeys(java.util.List.of(new javax.swing.RowSorter.SortKey(columnIndex, javax.swing.SortOrder.ASCENDING)));
                sorter.sort();
                statusLabel.setText("📊 Sorted by " + choice);
            }
        });

        setupFutureTradesActions();

        // show records by default
        showRecordsTable();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------------------- Records helper forms & actions ----------------------
    private void showRecordImportForm() {
        resetInputPanel();
        inputPanel.add(new JLabel("Product Name:")); inputPanel.add(nameField);
        inputPanel.add(new JLabel("Import Quantity:")); inputPanel.add(impqntfield);
        inputPanel.add(new JLabel("Import Price:")); inputPanel.add(imppriceField);
        inputPanel.add(new JLabel("Date:")); inputPanel.add(dateField);
        inputPanel.add(dateButton);
        inputPanel.add(enterButton);
        refreshPanel();

        clearEnterListeners();
        enterButton.addActionListener(ev -> {
            try {
                String product = nameField.getText().trim();
                int importQty = Integer.parseInt(impqntfield.getText().trim());
                double importPrice = Double.parseDouble(imppriceField.getText().trim());
                LocalDate date = LocalDate.parse(dateField.getText().trim());

                ProductDAO.addProduct(new Product(0, product, 0, importQty, importPrice, 0, 0, date));
                statusLabel.setText("✅ Import recorded successfully!");
                clearInputFields();
                resetInputPanel();
                showRecordsTable();
            } catch (Exception ex) {
                statusLabel.setText("❌ Error: " + ex.getMessage());
            }
        });
    }

    private void showRecordExportForm() {
        resetInputPanel();
        inputPanel.add(new JLabel("Product Name:")); inputPanel.add(nameField);
        inputPanel.add(new JLabel("Export Quantity:")); inputPanel.add(expqntfield);
        inputPanel.add(new JLabel("Export Price:")); inputPanel.add(exppriceField);
        inputPanel.add(new JLabel("Date:")); inputPanel.add(dateField);
        inputPanel.add(dateButton);
        inputPanel.add(enterButton);
        refreshPanel();

        clearEnterListeners();
        enterButton.addActionListener(ev -> {
            try {
                String product = nameField.getText().trim();
                int exportQty = Integer.parseInt(expqntfield.getText().trim());
                double exportPrice = Double.parseDouble(exppriceField.getText().trim());
                LocalDate date = LocalDate.parse(dateField.getText().trim());

                boolean success = ProductDAO.exportProduct(new Product(0, product, 0, 0, 0, exportQty, exportPrice, date));
                if (success) {
                    statusLabel.setText("✅ Export recorded successfully!");
                    showRecordsTable();
                } else {
                    statusLabel.setText("❌ Not enough stock! Export denied.");
                }
                clearInputFields();
                resetInputPanel();
            } catch (Exception ex) {
                statusLabel.setText("❌ Error: " + ex.getMessage());
            }
        });
    }

    private void showRecordUpdateForm() {
        resetInputPanel();
        JTextField srnoField = new JTextField(6);
        inputPanel.add(new JLabel("Sr No (ID):")); inputPanel.add(srnoField);
        inputPanel.add(new JLabel("Product Name:")); inputPanel.add(nameField);
        inputPanel.add(new JLabel("New Import Qty:")); inputPanel.add(impqntfield);
        inputPanel.add(new JLabel("New Import Price:")); inputPanel.add(imppriceField);
        inputPanel.add(new JLabel("New Export Qty:")); inputPanel.add(expqntfield);
        inputPanel.add(new JLabel("New Export Price:")); inputPanel.add(exppriceField);
        inputPanel.add(new JLabel("Date:")); inputPanel.add(dateField);
        inputPanel.add(dateButton);
        inputPanel.add(enterButton);
        refreshPanel();

        clearEnterListeners();
        enterButton.addActionListener(ev -> {
            try {
                int id = Integer.parseInt(srnoField.getText().trim());
                String product = nameField.getText().trim();
                int importQty = Integer.parseInt(impqntfield.getText().trim());
                double importPrice = Double.parseDouble(imppriceField.getText().trim());
                int exportQty = Integer.parseInt(expqntfield.getText().trim());
                double exportPrice = Double.parseDouble(exppriceField.getText().trim());
                LocalDate date = LocalDate.parse(dateField.getText().trim());

                ProductDAO.updateProduct(new Product(id, product, 0, importQty, importPrice, exportQty, exportPrice, date));
                statusLabel.setText("✅ Product updated successfully!");
                clearInputFields();
                resetInputPanel();
                showRecordsTable();
            } catch (Exception ex) {
                statusLabel.setText("❌ Error: " + ex.getMessage());
            }
        });
    }

    private void showRecordDeleteForm() {
        resetInputPanel();
        JTextField srnoField = new JTextField(6);
        inputPanel.add(new JLabel("Sr No (ID) to Delete:")); inputPanel.add(srnoField);
        inputPanel.add(enterButton);
        refreshPanel();

        clearEnterListeners();
        enterButton.addActionListener(ev -> {
            try {
                int id = Integer.parseInt(srnoField.getText().trim());
                ProductDAO.deleteProduct(id);
                statusLabel.setText("🗑️ Product deleted successfully!");
                clearInputFields();
                resetInputPanel();
                showRecordsTable();
            } catch (Exception ex) {
                statusLabel.setText("❌ Error: " + ex.getMessage());
            }
        });
    }

    private void showRecordsTable() {
        ArrayList<Product> list = ProductDAO.getAllProducts();
        String[] columnNames = {"ID", "Product", "Total Qty", "Import Qty", "Import Price", "Export Qty", "Export Price", "Date"};
        Object[][] data = new Object[list.size()][columnNames.length];

        for (int i = 0; i < list.size(); i++) {
            Product p = list.get(i);
            data[i][0] = p.getId();
            data[i][1] = p.getName();
            data[i][2] = p.gettotQuantity();
            data[i][3] = p.getimpQuantity();
            data[i][4] = p.getimpPrice();
            data[i][5] = p.getexpQuantity();
            data[i][6] = p.getexpPrice();
            data[i][7] = p.getDateAdded();
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 2, 3, 5 -> Integer.class;
                    case 4, 6 -> Double.class;
                    case 7 -> java.time.LocalDate.class;
                    default -> String.class;
                };
            }
        };

        table.setModel(model);
        table.setRowHeight(24);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // left align
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);

        sorter = new javax.swing.table.TableRowSorter<>(model);
        // optional explicit comparators
        sorter.setComparator(0, (a,b) -> Integer.compare((Integer)a, (Integer)b));
        sorter.setComparator(2, (a,b) -> Integer.compare((Integer)a, (Integer)b));
        sorter.setComparator(3, (a,b) -> Integer.compare((Integer)a, (Integer)b));
        sorter.setComparator(5, (a,b) -> Integer.compare((Integer)a, (Integer)b));
        sorter.setComparator(4, (a,b) -> Double.compare((Double)a, (Double)b));
        sorter.setComparator(6, (a,b) -> Double.compare((Double)a, (Double)b));
        sorter.setComparator(7, (a,b) -> ((java.time.LocalDate)a).compareTo((java.time.LocalDate)b));
        table.setRowSorter(sorter);

        statusLabel.setText("📊 Showing main records table.");
    }

    // ---------------------- Future Trades sub-actions ----------------------
    // Attach handlers to FT sub-buttons. This method should be called once (from constructor).
    private void setupFutureTradesActions() {
        futureTradesButton.addActionListener(e -> {
            resetInputPanel();

            // Create Future Trades submenu
            JPanel ftButtonPanel = new JPanel(new GridLayout(1, 6, 8, 8));
            ftImportButton = new JButton("FT Import");
            ftExportButton = new JButton("FT Export");
            ftUpdateButton = new JButton("FT Update");
            ftDeleteButton = new JButton("FT Delete");
            ftShiftButton = new JButton("Shift to Records");
            ftViewButton = new JButton("View FT Table"); // ✅ Added this back

            ftButtonPanel.add(ftImportButton);
            ftButtonPanel.add(ftExportButton);
            ftButtonPanel.add(ftUpdateButton);
            ftButtonPanel.add(ftDeleteButton);
            ftButtonPanel.add(ftShiftButton);
            ftButtonPanel.add(ftViewButton);

            inputPanel.add(ftButtonPanel);
            refreshPanel();

            statusLabel.setText("📂 Future Trades mode enabled.");

            // Now setup sub-buttons
            setupFutureTradesSubActions();
        });
    }

    private void setupFutureTradesSubActions() {
        // --- Import ---
        ftImportButton.addActionListener(e -> {
            resetInputPanel();
            inputPanel.add(new JLabel("Product Name:")); inputPanel.add(nameField);
            inputPanel.add(new JLabel("Import Qty:")); inputPanel.add(impqntfield);
            inputPanel.add(new JLabel("Import Price:")); inputPanel.add(imppriceField);
            inputPanel.add(new JLabel("Date:")); inputPanel.add(dateField);
            inputPanel.add(dateButton);
            inputPanel.add(enterButton);
            refreshPanel();

            clearEnterListeners();
            enterButton.addActionListener(ev -> {
                try {
                    String product = nameField.getText();
                    int qty = Integer.parseInt(impqntfield.getText());
                    double price = Double.parseDouble(imppriceField.getText());
                    LocalDate date = LocalDate.parse(dateField.getText());

                    FutureTradeDAO.addFutureTrade(new FutureTrade(0, product, qty, price, 0, 0, date));
                    statusLabel.setText("✅ Future trade import added.");
                    clearInputFields(); resetInputPanel();
                } catch (Exception ex) {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                }
            });
        });

        // --- Export ---
        ftExportButton.addActionListener(e -> {
            resetInputPanel();
            inputPanel.add(new JLabel("Product Name:")); inputPanel.add(nameField);
            inputPanel.add(new JLabel("Export Qty:")); inputPanel.add(expqntfield);
            inputPanel.add(new JLabel("Export Price:")); inputPanel.add(exppriceField);
            inputPanel.add(new JLabel("Date:")); inputPanel.add(dateField);
            inputPanel.add(dateButton);
            inputPanel.add(enterButton);
            refreshPanel();

            clearEnterListeners();
            enterButton.addActionListener(ev -> {
                try {
                    String product = nameField.getText();
                    int qty = Integer.parseInt(expqntfield.getText());
                    double price = Double.parseDouble(exppriceField.getText());
                    LocalDate date = LocalDate.parse(dateField.getText());

                    FutureTradeDAO.addFutureTrade(new FutureTrade(0, product, 0, 0, qty, price, date));
                    statusLabel.setText("✅ Future trade export added.");
                    clearInputFields(); resetInputPanel();
                } catch (Exception ex) {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                }
            });
        });

        // --- Update ---
        ftUpdateButton.addActionListener(e -> {
            resetInputPanel();
            JTextField idField = new JTextField(5);
            inputPanel.add(new JLabel("ID:")); inputPanel.add(idField);
            inputPanel.add(new JLabel("Product Name:")); inputPanel.add(nameField);
            inputPanel.add(new JLabel("Import Qty:")); inputPanel.add(impqntfield);
            inputPanel.add(new JLabel("Import Price:")); inputPanel.add(imppriceField);
            inputPanel.add(new JLabel("Export Qty:")); inputPanel.add(expqntfield);
            inputPanel.add(new JLabel("Export Price:")); inputPanel.add(exppriceField);
            inputPanel.add(new JLabel("Date:")); inputPanel.add(dateField);
            inputPanel.add(dateButton);
            inputPanel.add(enterButton);
            refreshPanel();

            clearEnterListeners();
            enterButton.addActionListener(ev -> {
                try {
                    int id = Integer.parseInt(idField.getText());
                    String product = nameField.getText();
                    int impQty = Integer.parseInt(impqntfield.getText());
                    double impPrice = Double.parseDouble(imppriceField.getText());
                    int expQty = Integer.parseInt(expqntfield.getText());
                    double expPrice = Double.parseDouble(exppriceField.getText());
                    LocalDate date = LocalDate.parse(dateField.getText());

                    FutureTradeDAO.updateFutureTrade(new FutureTrade(id, product, impQty, impPrice, expQty, expPrice, date));
                    statusLabel.setText("✅ Future trade updated.");
                    clearInputFields(); resetInputPanel();
                } catch (Exception ex) {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                }
            });
        });

        // --- Delete ---
        ftDeleteButton.addActionListener(e -> {
            resetInputPanel();
            JTextField idField = new JTextField(5);
            inputPanel.add(new JLabel("ID to Delete:")); inputPanel.add(idField);
            inputPanel.add(enterButton);
            refreshPanel();

            clearEnterListeners();
            enterButton.addActionListener(ev -> {
                try {
                    int id = Integer.parseInt(idField.getText());
                    FutureTradeDAO.deleteFutureTrade(id);
                    statusLabel.setText("🗑️ Future trade deleted.");
                    resetInputPanel();
                } catch (Exception ex) {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                }
            });
        });

        // --- Shift ---
        ftShiftButton.addActionListener(e -> {
            resetInputPanel();
            JTextField idField = new JTextField(5);
            inputPanel.add(new JLabel("Future Trade ID to Shift:")); inputPanel.add(idField);
            inputPanel.add(enterButton);
            refreshPanel();

            clearEnterListeners();
            enterButton.addActionListener(ev -> {
                try {
                    int id = Integer.parseInt(idField.getText());
                    FutureTradeDAO.shiftToMain(id);
                    statusLabel.setText("➡️ Trade shifted to main records.");
                    resetInputPanel();
                } catch (Exception ex) {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                }
            });
        });

        // --- View Table ---
        ftViewButton.addActionListener(e -> {
            ArrayList<FutureTrade> list = FutureTradeDAO.getAllFutureTrades();
            String[] colNames = {"ID", "Product", "Import Qty", "Import Price", "Export Qty", "Export Price", "Date"};
            Object[][] data = new Object[list.size()][colNames.length];
            for (int i=0; i<list.size(); i++) {
                FutureTrade t = list.get(i);
                data[i][0] = t.getId();
                data[i][1] = t.getName();
                data[i][2] = t.getimpQuantity();
                data[i][3] = t.getimpPrice();
                data[i][4] = t.getexpQuantity();
                data[i][5] = t.getexpPrice();
                data[i][6] = t.getDateAdded();
            }

            DefaultTableModel model = new DefaultTableModel(data, colNames) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
                @Override public Class<?> getColumnClass(int col) {
                    return switch (col) {
                        case 0,2,4 -> Integer.class;
                        case 3,5 -> Double.class;
                        case 6 -> java.time.LocalDate.class;
                        default -> String.class;
                    };
                }
            };

            table.setModel(model);
            table.setRowHeight(25);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            // ✅ Left align all columns
            DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            }

            sorter = new javax.swing.table.TableRowSorter<>(model);
            table.setRowSorter(sorter);

            statusLabel.setText("📊 Showing Future Trades table.");
        });

    }

    // Helper to attach listener only once (prevents accidental duplicate attachments)
    private void attachOnce(JButton btn, java.awt.event.ActionListener listener) {
        if (btn == null) return;
        for (ActionListener al : btn.getActionListeners()) {
            if (al == listener) return;
        }
        btn.addActionListener(listener);
    }

    // ---------- HELPERS ----------
    private void clearInputFields() {
        nameField.setText("");
        impqntfield.setText("");
        imppriceField.setText("");
        expqntfield.setText("");
        exppriceField.setText("");
        dateField.setText("");
    }

    private void resetInputPanel() {
        inputPanel.removeAll();
        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void refreshPanel() {
        inputPanel.revalidate();
        inputPanel.repaint();
    }

    // ensures enterButton doesn't accumulate old listeners
    private void clearEnterListeners() {
        for (ActionListener al : enterButton.getActionListeners()) enterButton.removeActionListener(al);
    }
}
