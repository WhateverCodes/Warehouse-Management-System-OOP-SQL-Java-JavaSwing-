import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// NOTE: JDateChooser and JTextFieldDateEditor imports are removed

/**
 * Main GUI frame for the Warehouse Management System.
 * Handles view switching, data loading, and user interaction.
 */
public class WarehouseGUI extends JFrame {

    private JButton btnWarehouseList;
    private JButton btnFutureRecords;
    private JButton btnLogout;

    private JPanel leftPanel, centerPanel, rightPanel, bottomPanel;
    private DefaultListModel<String> listModel;
    private JTable centerTable;
    private DefaultTableModel centerTableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private enum Mode {WAREHOUSE_LIST, WAREHOUSE_RECORDS, FUTURE_TRADES}
    private Mode currentMode = Mode.WAREHOUSE_LIST;
    private String selectedWarehouse = null;

    public WarehouseGUI() {
        setTitle("Warehouse Management System - User: " + SessionManager.getCurrentUser());
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initTopPanel();
        initMainPanels();
        showWarehouseListView();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------------------- INITIALIZATION ----------------------
    private void initTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnWarehouseList = new JButton("Warehouse List");
        btnFutureRecords = new JButton("Future Trades");
        btnLogout = new JButton("Logout");

        btnWarehouseList.setPreferredSize(new Dimension(140, 32));
        btnFutureRecords.setPreferredSize(new Dimension(140, 32));
        btnLogout.setPreferredSize(new Dimension(100, 32));

        top.add(btnWarehouseList);
        top.add(btnFutureRecords);

        JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        rightFlow.add(btnLogout);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(top, BorderLayout.WEST);
        topContainer.add(rightFlow, BorderLayout.EAST);
        add(topContainer, BorderLayout.NORTH);


        btnWarehouseList.addActionListener(e -> showWarehouseListView());
        btnFutureRecords.addActionListener(e -> showFutureTradesView());

        btnLogout.addActionListener(e -> {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void initMainPanels() {
        JPanel middle = new JPanel(new BorderLayout());

        // LEFT panel: warehouses
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Warehouses"));
        listModel = new DefaultListModel<>();
        JList<String> warehouseJList = new JList<>(listModel);
        warehouseJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        warehouseJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int idx = warehouseJList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        String name = extractNameFromListEntry(listModel.get(idx));
                        openWarehouseRecords(name);
                    }
                }
            }
        });
        leftPanel.add(new JScrollPane(warehouseJList), BorderLayout.CENTER);

        // CENTER panel: main table
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Table"));
        centerTableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        centerTable = new JTable(centerTableModel);
        centerTable.setRowHeight(24);
        centerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        centerTable.getTableHeader().setReorderingAllowed(true);

        rowSorter = new TableRowSorter<>(centerTableModel);
        centerTable.setRowSorter(rowSorter);

        centerTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = centerTable.getSelectedRow();
                // Point 1: Display row details on click. Check current mode.
                if (row >= 0) {
                    int modelRow = centerTable.convertRowIndexToModel(row);
                    if (currentMode == Mode.WAREHOUSE_LIST) {
                        showWarehouseRowDetails(modelRow);
                    } else {
                        showRowDetails(modelRow);
                    }
                }
            }
        });
        centerPanel.add(new JScrollPane(centerTable), BorderLayout.CENTER);

        // RIGHT panel: controls
        rightPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        rightPanel.setPreferredSize(new Dimension(240, 0));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        // BOTTOM: input area
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(0, 200));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Input Panel"));

        middle.add(leftPanel, BorderLayout.WEST);
        middle.add(centerPanel, BorderLayout.CENTER);
        middle.add(rightPanel, BorderLayout.EAST);
        add(middle, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ---------------------- VIEW SWITCHERS ----------------------
    private void showWarehouseListView() {
        currentMode = Mode.WAREHOUSE_LIST;
        selectedWarehouse = null;
        btnWarehouseList.setEnabled(false);
        btnFutureRecords.setEnabled(true);
        loadWarehouseList();
        loadMasterWarehouseTable();
        showWarehouseControls();
        clearBottomPanel();
    }

    private void openWarehouseRecords(String warehouseName) {
        currentMode = Mode.WAREHOUSE_RECORDS;
        selectedWarehouse = warehouseName;
        btnWarehouseList.setEnabled(true);
        btnFutureRecords.setEnabled(true);
        loadWarehouseList();
        loadRecordsOfWarehouse(warehouseName);
        showRecordControls();
        clearBottomPanel();
    }

    private void showFutureTradesView() {
        currentMode = Mode.FUTURE_TRADES;
        selectedWarehouse = null;
        btnWarehouseList.setEnabled(true);
        btnFutureRecords.setEnabled(true);
        loadWarehouseList();
        loadFutureTrades();
        showFutureTradeControls();
        clearBottomPanel();
    }

    // ---------------------- DATA LOADERS ----------------------
    private void loadWarehouseList() {
        listModel.clear();
        List<WarehouseDAO.WarehouseInfo> warehouses = WarehouseDAO.getAllWarehouses();
        int idx = 1;
        for (WarehouseDAO.WarehouseInfo w : warehouses)
            listModel.addElement(idx++ + ". " + w.name);
    }

    private void loadMasterWarehouseTable() {
        // FIX: New Warehouse List Column Order (Date after ID)
        String[] cols = {"ID", "Inauguration_Date", "Warehouse_Name", "City", "Address", "Last_Activity_Date", "Notes"};
        centerTableModel.setDataVector(new Object[][]{}, cols);
        List<WarehouseDAO.WarehouseInfo> warehouses = WarehouseDAO.getAllWarehouses();
        int idx = 1;
        for (WarehouseDAO.WarehouseInfo w : warehouses)
            // Note: The DAO returns columns in the old order, so we must map them correctly here:
            // DAO: name, city, address, inauguration, lastActivity, notes
            centerTableModel.addRow(new Object[]{
                    idx++,
                    w.inauguration, // New Col 1
                    w.name,         // New Col 2
                    w.city,
                    w.address,
                    w.lastActivity,
                    w.notes
            });
        leftAlignAllColumns();
    }

    private void loadRecordsOfWarehouse(String warehouseName) {
        ProductDAO.setCurrentWarehouse(warehouseName);
        ArrayList<Product> products = ProductDAO.getAllProducts();
        // FIX: New Record/Future Trade Column Order (Date after Product)
        String[] cols = {"ID", "Product", "Date", "Supplier", "Customer",
                "Total_Quantity", "Import_Quantity", "Import_Price", "Export_Quantity", "Export_Price"};
        centerTableModel.setDataVector(new Object[][]{}, cols);
        for (Product p : products)
            centerTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getDateAdded(), // New Col 2
                    p.getSupplier(),
                    p.getCustomer(),
                    p.gettotQuantity(),
                    p.getimpQuantity(),
                    p.getimpPrice(),
                    p.getexpQuantity(),
                    p.getexpPrice()
            });
        leftAlignAllColumns();
    }

    private void loadFutureTrades() {
        ArrayList<FutureTrade> trades = FutureTradeDAO.getAllFutureTrades();
        // FIX: New Record/Future Trade Column Order (Date after Product)
        String[] cols = {"ID", "Warehouse_Name", "Product", "Date", "Supplier", "Customer",
                "Import_Quantity", "Import_Price", "Export_Quantity", "Export_Price"};
        centerTableModel.setDataVector(new Object[][]{}, cols);
        for (FutureTrade t : trades)
            centerTableModel.addRow(new Object[]{
                    t.getId(),
                    t.getWarehouse(),
                    t.getName(),
                    t.getDateAdded(), // New Col 3
                    t.getSupplier(),
                    t.getCustomer(),
                    t.getimpQuantity(),
                    t.getimpPrice(),
                    t.getexpQuantity(),
                    t.getexpPrice()
            });
        leftAlignAllColumns();
    }

    // ---------------------- CONTROLS ----------------------
    private void showWarehouseControls() {
        resetRightPanel();
        JButton add = new JButton("New Warehouse");
        JButton edit = new JButton("Edit Warehouse");
        JButton del = new JButton("Delete Warehouse");
        JButton exp = new JButton("Export Table to .txt");
        add.addActionListener(e -> showNewWarehouseForm());
        edit.addActionListener(e -> showEditWarehouseForm());
        del.addActionListener(e -> showDeleteWarehouseForm());
        exp.addActionListener(e -> exportTable());
        rightPanel.add(add); rightPanel.add(edit); rightPanel.add(del); rightPanel.add(exp);
        addFindBar();
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showRecordControls() {
        resetRightPanel();
        JButton imp = new JButton("Record Import");
        JButton exp = new JButton("Record Export");
        JButton upd = new JButton("Update Record");
        JButton del = new JButton("Delete Record");
        JButton exptxt = new JButton("Export Table to .txt");
        imp.addActionListener(e -> showRecordImportForm());
        exp.addActionListener(e -> showRecordExportForm());
        upd.addActionListener(e -> showRecordUpdateForm());
        del.addActionListener(e -> showRecordDeleteForm());
        exptxt.addActionListener(e -> exportTable());
        rightPanel.add(imp); rightPanel.add(exp); rightPanel.add(upd); rightPanel.add(del); rightPanel.add(exptxt);
        addFindBar();
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showFutureTradeControls() {
        resetRightPanel();
        JButton addImport = new JButton("Add Future Import");
        JButton addExport = new JButton("Add Future Export");
        JButton edit = new JButton("Edit Record");
        JButton del = new JButton("Delete Record");
        JButton shift = new JButton("Shift Record");
        JButton exp = new JButton("Export Table to .txt");
        addImport.addActionListener(e -> showFutureImportForm());
        addExport.addActionListener(e -> showFutureExportForm());
        edit.addActionListener(e -> showFutureUpdateForm());
        del.addActionListener(e -> showFutureDeleteForm());
        shift.addActionListener(e -> showShiftForm());
        exp.addActionListener(e -> exportTable());
        rightPanel.add(addImport); rightPanel.add(addExport); rightPanel.add(edit); rightPanel.add(del); rightPanel.add(shift); rightPanel.add(exp);
        addFindBar();
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // ============================================================
    // ----------  FORM SECTIONS (WAREHOUSE, RECORDS, FUTURE) ------
    // ============================================================

    // ---------- WAREHOUSE FORMS ----------
    private void showNewWarehouseForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JTextField tfName = new JTextField();
        JTextField tfCity = new JTextField();
        JTextField tfAddress = new JTextField();
        // DateInputPanel replaces JDateChooser
        DateInputPanel dateInaug = new DateInputPanel();
        JTextArea tfNotes = new JTextArea(3, 20);
        JScrollPane spNotes = new JScrollPane(tfNotes);

        normalizeFieldHeight(tfName);
        normalizeFieldHeight(tfCity);
        normalizeFieldHeight(tfAddress);
        normalizeFieldHeight(dateInaug); // Normalize panel height
        normalizeFieldHeight(spNotes);

        JButton submit = new JButton("Create");

        // FIX: New Warehouse Input Order (Inauguration Date after Name)
        form.add(new JLabel("Warehouse Name (Req):")); form.add(tfName);
        form.add(new JLabel("Inauguration Date (Opt):")); form.add(dateInaug); // Uses new Panel
        form.add(new JLabel("City (Opt):")); form.add(tfCity);
        form.add(new JLabel("Address (Opt):")); form.add(tfAddress);
        form.add(new JLabel("Notes (Opt):")); form.add(spNotes);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(submit, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                // Handle optional fields
                // Only use the date if it's set to something other than the default "today" date logic in the panel
                Date indate = null;
                if (dateInaug.getDate() != null && !dateInaug.getDate().isEqual(LocalDate.now())) {
                    indate = dateInaug.getSqlDate();
                } else if (tfName.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "❌ Warehouse Name is required.");
                    return;
                }

                WarehouseDAO.createWarehouse(
                        tfName.getText().trim(),
                        tfCity.getText().trim().isEmpty() ? null : tfCity.getText().trim(),
                        tfAddress.getText().trim().isEmpty() ? null : tfAddress.getText().trim(),
                        indate,
                        tfNotes.getText().trim().isEmpty() ? null : tfNotes.getText().trim()
                );

                loadWarehouseList();
                loadMasterWarehouseTable();

                JOptionPane.showMessageDialog(this, "✅ Warehouse created successfully.");
                clearBottomPanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to create warehouse: " + ex.getMessage());
            }
        });
    }

    private void showEditWarehouseForm() {
        clearBottomPanel();
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JComboBox<String> cbWarehouse = new JComboBox<>();
        WarehouseDAO.getAllWarehouses().forEach(w -> cbWarehouse.addItem(w.name));
        normalizeFieldHeight(cbWarehouse);
        JButton btnSelect = new JButton("Select Warehouse");

        inputPanel.add(new JLabel("Select Warehouse:"));
        inputPanel.add(cbWarehouse);
        inputPanel.add(btnSelect);
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.revalidate();
        bottomPanel.repaint();

        btnSelect.addActionListener(e -> {
            String selectedName = (String) cbWarehouse.getSelectedItem();
            if (selectedName == null) return;
            showEditWarehouseDetailsForm(selectedName);
        });
    }

    private void showEditWarehouseDetailsForm(String oldName) {
        clearBottomPanel();
        WarehouseDAO.WarehouseInfo info = WarehouseDAO.getWarehouseByName(oldName);
        if (info == null) {
            JOptionPane.showMessageDialog(this, "Warehouse data not found.", "Error", JOptionPane.ERROR_MESSAGE);
            showWarehouseListView(); // Revert to list view
            return;
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JTextField tfName = new JTextField(info.name);
        JTextField tfCity = new JTextField(info.city != null ? info.city : "");
        JTextField tfAddress = new JTextField(info.address != null ? info.address : "");
        DateInputPanel dateInaug = new DateInputPanel(); // Uses new Panel
        JTextArea tfNotes = new JTextArea(info.notes != null ? info.notes : "", 3, 20);
        JScrollPane spNotes = new JScrollPane(tfNotes);

        normalizeFieldHeight(tfName);
        normalizeFieldHeight(tfCity);
        normalizeFieldHeight(tfAddress);
        normalizeFieldHeight(dateInaug); // Normalize panel height
        normalizeFieldHeight(spNotes);

        if (info.inauguration != null) {
            dateInaug.setDate(info.inauguration.toLocalDate());
        }

        JButton update = new JButton("Update");

        // FIX: New Warehouse Input Order (Inauguration Date after Name)
        form.add(new JLabel("New Warehouse Name (Req):")); form.add(tfName);
        form.add(new JLabel("Inauguration Date (Opt):")); form.add(dateInaug); // Uses new Panel
        form.add(new JLabel("City (Opt):")); form.add(tfCity);
        form.add(new JLabel("Address (Opt):")); form.add(tfAddress);
        form.add(new JLabel("Notes (Opt):")); form.add(spNotes);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(update, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        update.addActionListener(e -> {
            try {
                if (tfName.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "❌ Warehouse Name is required.");
                    return;
                }

                // Handle date conversion from the panel
                Date newDate = dateInaug.getSqlDate();

                WarehouseDAO.editWarehouse(
                        oldName,
                        tfName.getText().trim(),
                        tfCity.getText().trim().isEmpty() ? null : tfCity.getText().trim(),
                        tfAddress.getText().trim().isEmpty() ? null : tfAddress.getText().trim(),
                        newDate,
                        tfNotes.getText().trim().isEmpty() ? null : tfNotes.getText().trim()
                );
                loadWarehouseList();
                loadMasterWarehouseTable();
                JOptionPane.showMessageDialog(this, "✅ Warehouse updated successfully.");
                clearBottomPanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to update warehouse: " + ex.getMessage());
            }
        });
    }

    private void showDeleteWarehouseForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        JComboBox<String> cbWarehouse = new JComboBox<>();
        WarehouseDAO.getAllWarehouses().forEach(w -> cbWarehouse.addItem(w.name));
        normalizeFieldHeight(cbWarehouse);
        JButton del = new JButton("Delete");

        form.add(new JLabel("Warehouse to delete:"));
        form.add(cbWarehouse);
        form.add(del);
        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.revalidate();

        del.addActionListener(e -> {
            String name = (String) cbWarehouse.getSelectedItem();
            if (name == null || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Select a warehouse to delete.");
                return;
            }

            try {
                // Pre-check for history integrity (Point 12)
                if (ProductDAO.hasNegativeStockHistory(name)) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Cannot delete: Deleting this warehouse would violate stock integrity (negative stock detected in history).",
                            "Stock Integrity Warning",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                // Handle cases where the records table doesn't exist yet (which is fine)
                if (!ex.getMessage().contains("Table") && !ex.getMessage().contains("exist")) {
                    JOptionPane.showMessageDialog(this, "❌ Pre-check failed: " + ex.getMessage());
                    return;
                }
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete \"" + name + "\"? This action cannot be undone and will delete all associated records.", "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                WarehouseDAO.deleteWarehouse(name);
                loadWarehouseList();
                loadMasterWarehouseTable();
                JOptionPane.showMessageDialog(this, "✅ Warehouse deleted successfully.");
                clearBottomPanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to delete warehouse: " + ex.getMessage());
            }
        });
    }


    // ---------- RECORD (PRODUCT) FORMS ----------
    private void showRecordImportForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JTextField tfProd = new JTextField(), tfSupp = new JTextField(), tfCust = new JTextField(),
                tfQty = new JTextField(), tfPrice = new JTextField();
        DateInputPanel dateChooser = new DateInputPanel(); // Uses new Panel
        normalizeFieldHeight(dateChooser);
        JButton submit = new JButton("Import");

        // FIX: New Input Order (Date first)
        form.add(new JLabel("Date:")); form.add(dateChooser); // MOVED UP
        form.add(new JLabel("Product:")); form.add(tfProd);
        form.add(new JLabel("Supplier:")); form.add(tfSupp);
        form.add(new JLabel("Customer:")); form.add(tfCust);
        form.add(new JLabel("Import Quantity:")); form.add(tfQty);
        form.add(new JLabel("Import Price:")); form.add(tfPrice);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(submit, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                Product p = new Product(0, tfProd.getText().trim(), tfSupp.getText().trim(),
                        tfCust.getText().trim(), 0, parseInt(tfQty),
                        parseDouble(tfPrice), 0, 0.0,
                        dateChooser.getDate()); // Uses getDate()
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                ProductDAO.addProduct(p);

                // FIX Point 4: Refresh the table immediately
                loadRecordsOfWarehouse(selectedWarehouse);

                JOptionPane.showMessageDialog(this, "✅ Import recorded successfully.");
                clearBottomPanel();
            } catch (SQLException ex) {
                // Handle stock warning or connection issue
                if (ex.getMessage().contains("Negative stock detected") || ex.getMessage().contains("Import aborted")) {
                    JOptionPane.showMessageDialog(this, "❌ Import aborted: " + ex.getMessage(), "Stock Integrity Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to record import: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showRecordExportForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JTextField tfProd = new JTextField(), tfSupp = new JTextField(), tfCust = new JTextField(),
                tfQty = new JTextField(), tfPrice = new JTextField();
        DateInputPanel dateChooser = new DateInputPanel(); // Uses new Panel
        normalizeFieldHeight(dateChooser);
        JButton submit = new JButton("Export");

        // FIX: New Input Order (Date first)
        form.add(new JLabel("Date:")); form.add(dateChooser); // MOVED UP
        form.add(new JLabel("Product:")); form.add(tfProd);
        form.add(new JLabel("Supplier:")); form.add(tfSupp);
        form.add(new JLabel("Customer:")); form.add(tfCust);
        form.add(new JLabel("Export Quantity:")); form.add(tfQty);
        form.add(new JLabel("Export Price:")); form.add(tfPrice);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(submit, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                Product p = new Product(0, tfProd.getText().trim(), tfSupp.getText().trim(),
                        tfCust.getText().trim(), 0, 0, 0.0, parseInt(tfQty),
                        parseDouble(tfPrice),
                        dateChooser.getDate()); // Uses getDate()
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                boolean ok = ProductDAO.exportProduct(p);

                if (ok) {
                    // FIX Point 4: Refresh the table immediately
                    loadRecordsOfWarehouse(selectedWarehouse);
                    JOptionPane.showMessageDialog(this, "✅ Export recorded successfully.");
                    clearBottomPanel();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Not enough stock for export.", "Stock Error", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                // Handle stock warning or connection issue
                if (ex.getMessage().contains("Negative stock detected") || ex.getMessage().contains("Export aborted")) {
                    JOptionPane.showMessageDialog(this, "❌ Export aborted: " + ex.getMessage(), "Stock Integrity Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to record export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showRecordUpdateForm() {
        clearBottomPanel();
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JTextField tfID = new JTextField(10);
        normalizeFieldHeight(tfID);
        JButton btnSelect = new JButton("Select Record ID");

        inputPanel.add(new JLabel("Enter Record ID to Edit:"));
        inputPanel.add(tfID);
        inputPanel.add(btnSelect);
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.revalidate();

        btnSelect.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfID.getText().trim());
                showRecordUpdateDetailsForm(id);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Please enter a valid numerical ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showRecordUpdateDetailsForm(int id) {
        clearBottomPanel();
        ProductDAO.setCurrentWarehouse(selectedWarehouse);
        Product p;
        try {
            p = ProductDAO.getProductById(id);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "❌ Record ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                showRecordControls();
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            showRecordControls();
            return;
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JTextField tfID = new JTextField(String.valueOf(p.getId())); tfID.setEditable(false);
        JTextField tfProd = new JTextField(p.getName());
        JTextField tfSupp = new JTextField(p.getSupplier());
        JTextField tfCust = new JTextField(p.getCustomer());
        JTextField tfImpQ = new JTextField(String.valueOf(p.getimpQuantity()));
        JTextField tfImpP = new JTextField(String.valueOf(p.getimpPrice()));
        JTextField tfExpQ = new JTextField(String.valueOf(p.getexpQuantity()));
        JTextField tfExpP = new JTextField(String.valueOf(p.getexpPrice()));
        DateInputPanel dateChooser = new DateInputPanel(); // Uses new Panel
        dateChooser.setDate(p.getDateAdded());

        normalizeFieldHeight(tfID); normalizeFieldHeight(tfProd); normalizeFieldHeight(tfSupp); normalizeFieldHeight(tfCust);
        normalizeFieldHeight(tfImpQ); normalizeFieldHeight(tfImpP); normalizeFieldHeight(tfExpQ); normalizeFieldHeight(tfExpP);
        normalizeFieldHeight(dateChooser);

        JButton submit = new JButton("Update");

        // FIX: New Update Input Order (Date first, then Product)
        form.add(new JLabel("Record ID:")); form.add(tfID);
        form.add(new JLabel("Date:")); form.add(dateChooser); // MOVED UP
        form.add(new JLabel("Product:")); form.add(tfProd);
        form.add(new JLabel("Supplier:")); form.add(tfSupp);
        form.add(new JLabel("Customer:")); form.add(tfCust);
        form.add(new JLabel("Import Qty:")); form.add(tfImpQ);
        form.add(new JLabel("Import Price:")); form.add(tfImpP);
        form.add(new JLabel("Export Qty:")); form.add(tfExpQ);
        form.add(new JLabel("Export Price:")); form.add(tfExpP);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(submit, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                Product updatedP = new Product(Integer.parseInt(tfID.getText().trim()), tfProd.getText().trim(),
                        tfSupp.getText().trim(), tfCust.getText().trim(), 0,
                        parseInt(tfImpQ), parseDouble(tfImpP), parseInt(tfExpQ),
                        parseDouble(tfExpP),
                        dateChooser.getDate()); // Uses getDate()
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                ProductDAO.updateProduct(updatedP);

                // FIX Point 6: Refresh the table immediately
                loadRecordsOfWarehouse(selectedWarehouse);

                JOptionPane.showMessageDialog(this, "✅ Record updated successfully. Stock history recalculated.");
                clearBottomPanel();
            } catch (SQLException ex) {
                // FIX Point 6: Handle stock warning or connection issue
                if (ex.getMessage().contains("Negative stock detected") || ex.getMessage().contains("Update aborted")) {
                    JOptionPane.showMessageDialog(this, "❌ Update aborted: " + ex.getMessage(), "Stock Integrity Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to update record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showRecordDeleteForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        JTextField tfID = new JTextField(10);
        normalizeFieldHeight(tfID);
        JButton del = new JButton("Delete Record");

        form.add(new JLabel("Record ID to Delete:")); form.add(tfID); form.add(del);
        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.revalidate();

        del.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfID.getText().trim());
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                ProductDAO.deleteProduct(id);

                // FIX Point 5: Refresh the table immediately
                loadRecordsOfWarehouse(selectedWarehouse);

                JOptionPane.showMessageDialog(this, "✅ Record deleted. Stock history recalculated.");
                clearBottomPanel();
            } catch (SQLException ex) {
                // FIX Point 5: Handle stock warning or connection issue
                if (ex.getMessage().contains("Negative stock detected")) {
                    JOptionPane.showMessageDialog(this, "❌ Deletion aborted: " + ex.getMessage(), "Stock Integrity Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to delete record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }


    // ---------- FUTURE TRADE FORMS ----------
    private void showFutureImportForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JComboBox<String> cbWarehouse = new JComboBox<>();
        WarehouseDAO.getAllWarehouses().forEach(w -> cbWarehouse.addItem(w.name));
        normalizeFieldHeight(cbWarehouse);
        JTextField tfProd = new JTextField(), tfSupp = new JTextField(), tfCust = new JTextField(),
                tfQty = new JTextField(), tfPrice = new JTextField();
        DateInputPanel dateChooser = new DateInputPanel(); // Uses new Panel
        normalizeFieldHeight(dateChooser);
        JButton submit = new JButton("Add Future Import");

        // FIX: New Input Order (Date first)
        form.add(new JLabel("Date:")); form.add(dateChooser); // MOVED UP
        form.add(new JLabel("Warehouse:")); form.add(cbWarehouse);
        form.add(new JLabel("Product:")); form.add(tfProd);
        form.add(new JLabel("Supplier:")); form.add(tfSupp);
        form.add(new JLabel("Customer:")); form.add(tfCust);
        form.add(new JLabel("Import Quantity:")); form.add(tfQty);
        form.add(new JLabel("Import Price:")); form.add(tfPrice);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(submit, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                FutureTrade f = new FutureTrade(0, (String) cbWarehouse.getSelectedItem(), tfProd.getText().trim(),
                        tfSupp.getText().trim(), tfCust.getText().trim(),
                        parseInt(tfQty), parseDouble(tfPrice),
                        0, 0.0,
                        dateChooser.getDate()); // Uses getDate()
                FutureTradeDAO.addFutureImport(f);
                loadFutureTrades();
                JOptionPane.showMessageDialog(this, "✅ Future import added.");
                clearBottomPanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to add future import: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showFutureExportForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JComboBox<String> cbWarehouse = new JComboBox<>();
        WarehouseDAO.getAllWarehouses().forEach(w -> cbWarehouse.addItem(w.name));
        normalizeFieldHeight(cbWarehouse);
        JTextField tfProd = new JTextField(), tfSupp = new JTextField(), tfCust = new JTextField(),
                tfQty = new JTextField(), tfPrice = new JTextField();
        DateInputPanel dateChooser = new DateInputPanel(); // Uses new Panel
        normalizeFieldHeight(dateChooser);
        JButton submit = new JButton("Add Future Export");

        // FIX: New Input Order (Date first)
        form.add(new JLabel("Date:")); form.add(dateChooser); // MOVED UP
        form.add(new JLabel("Warehouse:")); form.add(cbWarehouse);
        form.add(new JLabel("Product:")); form.add(tfProd);
        form.add(new JLabel("Supplier:")); form.add(tfSupp);
        form.add(new JLabel("Customer:")); form.add(tfCust);
        form.add(new JLabel("Export Quantity:")); form.add(tfQty);
        form.add(new JLabel("Export Price:")); form.add(tfPrice);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(submit, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                FutureTrade f = new FutureTrade(0, (String) cbWarehouse.getSelectedItem(), tfProd.getText().trim(),
                        tfSupp.getText().trim(), tfCust.getText().trim(),
                        0, 0.0,
                        parseInt(tfQty), parseDouble(tfPrice),
                        dateChooser.getDate()); // Uses getDate()
                FutureTradeDAO.addFutureExport(f);
                loadFutureTrades();
                JOptionPane.showMessageDialog(this, "✅ Future export added.");
                clearBottomPanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to add future export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showFutureUpdateForm() {
        clearBottomPanel();
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JTextField tfID = new JTextField(10);
        normalizeFieldHeight(tfID);
        JButton btnSelect = new JButton("Select Record ID");

        inputPanel.add(new JLabel("Enter Future Record ID to Edit:"));
        inputPanel.add(tfID);
        inputPanel.add(btnSelect);
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.revalidate();

        btnSelect.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfID.getText().trim());
                showFutureUpdateDetailsForm(id);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Please enter a valid numerical ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showFutureUpdateDetailsForm(int id) {
        clearBottomPanel();
        FutureTrade f;
        try {
            f = FutureTradeDAO.getFutureTradeById(id);
            if (f == null) {
                JOptionPane.showMessageDialog(this, "❌ Future Record ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                showFutureTradeControls();
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            showFutureTradeControls();
            return;
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        JTextField tfID = new JTextField(String.valueOf(f.getId())); tfID.setEditable(false);
        JComboBox<String> cbWarehouse = new JComboBox<>();
        WarehouseDAO.getAllWarehouses().forEach(w -> cbWarehouse.addItem(w.name));
        cbWarehouse.setSelectedItem(f.getWarehouse());
        JTextField tfProd = new JTextField(f.getName());
        JTextField tfSupp = new JTextField(f.getSupplier());
        JTextField tfCust = new JTextField(f.getCustomer());
        JTextField tfImpQ = new JTextField(String.valueOf(f.getimpQuantity()));
        JTextField tfImpP = new JTextField(String.valueOf(f.getimpPrice()));
        JTextField tfExpQ = new JTextField(String.valueOf(f.getexpQuantity()));
        JTextField tfExpP = new JTextField(String.valueOf(f.getexpPrice()));
        DateInputPanel dateChooser = new DateInputPanel(); // Uses new Panel
        dateChooser.setDate(f.getDateAdded());

        normalizeFieldHeight(tfID); normalizeFieldHeight(cbWarehouse); normalizeFieldHeight(tfProd); normalizeFieldHeight(tfSupp); normalizeFieldHeight(tfCust);
        normalizeFieldHeight(tfImpQ); normalizeFieldHeight(tfImpP); normalizeFieldHeight(tfExpQ); normalizeFieldHeight(tfExpP);
        normalizeFieldHeight(dateChooser);

        JButton submit = new JButton("Update");

        // FIX: New Update Input Order (Date first, then Product)
        form.add(new JLabel("Record ID:")); form.add(tfID);
        form.add(new JLabel("Date:")); form.add(dateChooser); // MOVED UP
        form.add(new JLabel("Warehouse:")); form.add(cbWarehouse);
        form.add(new JLabel("Product:")); form.add(tfProd);
        form.add(new JLabel("Supplier:")); form.add(tfSupp);
        form.add(new JLabel("Customer:")); form.add(tfCust);
        form.add(new JLabel("Import Qty:")); form.add(tfImpQ);
        form.add(new JLabel("Import Price:")); form.add(tfImpP);
        form.add(new JLabel("Export Qty:")); form.add(tfExpQ);
        form.add(new JLabel("Export Price:")); form.add(tfExpP);
        bottomPanel.add(new JScrollPane(form), BorderLayout.CENTER);
        bottomPanel.add(submit, BorderLayout.SOUTH);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                FutureTrade updatedF = new FutureTrade(Integer.parseInt(tfID.getText().trim()), (String) cbWarehouse.getSelectedItem(), tfProd.getText().trim(),
                        tfSupp.getText().trim(), tfCust.getText().trim(),
                        parseInt(tfImpQ), parseDouble(tfImpP), parseInt(tfExpQ),
                        parseDouble(tfExpP),
                        dateChooser.getDate()); // Uses getDate()
                FutureTradeDAO.updateFutureTrade(updatedF);
                loadFutureTrades();
                JOptionPane.showMessageDialog(this, "✅ Future Record updated successfully.");
                clearBottomPanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to update future record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showFutureDeleteForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        JTextField tfID = new JTextField(10);
        normalizeFieldHeight(tfID);
        JButton del = new JButton("Delete Future Record");
        form.add(new JLabel("Future Record ID to Delete:")); form.add(tfID); form.add(del);
        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.revalidate();

        del.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfID.getText().trim());
                FutureTradeDAO.deleteFutureTrade(id);
                loadFutureTrades();
                JOptionPane.showMessageDialog(this, "✅ Future trade deleted.");
                clearBottomPanel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to delete future record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showShiftForm() {
        clearBottomPanel();
        JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        JTextField tfID = new JTextField(10);
        normalizeFieldHeight(tfID);
        JButton shift = new JButton("Shift Record");
        form.add(new JLabel("Future Record ID to Shift:")); form.add(tfID); form.add(shift);
        bottomPanel.add(form, BorderLayout.CENTER);
        bottomPanel.revalidate();

        shift.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfID.getText().trim());
                FutureTradeDAO.shiftToWarehouse(id); // Shift logic handles auto-delete (Point 8)

                // FIX Point 8: Refresh the table immediately after shift/delete
                loadFutureTrades();

                // If we shift an export, it might fail due to low stock (handled by DAO exception)
                JOptionPane.showMessageDialog(this, "✅ Future trade shifted to warehouse records and deleted.");
                clearBottomPanel();
            } catch (SQLException ex) {
                // FIX Point 8: Specific error handling for connection/stock issue
                if (ex.getMessage().contains("Shift failed: insufficient stock")) {
                    JOptionPane.showMessageDialog(this, "❌ Shift failed: Insufficient stock for export in target warehouse.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                } else if (ex.getMessage().contains("Negative stock detected")) {
                    JOptionPane.showMessageDialog(this, "❌ Shift failed: " + ex.getMessage(), "Stock Integrity Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to shift record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }


    // ---------------------- TABLE & FORM HELPERS ----------------------
    private void leftAlignAllColumns() {
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 0; i < centerTable.getColumnCount(); i++)
            centerTable.getColumnModel().getColumn(i).setCellRenderer(left);
    }

    private void clearBottomPanel() {
        bottomPanel.removeAll();
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    // Point 1 Fix: Displays details of a standard record row (Product/Future Trade)
    private void showRowDetails(int modelRow) {
        clearBottomPanel();
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 6));
        for (int i = 0; i < centerTableModel.getColumnCount(); i++) {
            panel.add(new JLabel(centerTableModel.getColumnName(i) + ":"));
            JTextField tf = new JTextField(centerTableModel.getValueAt(modelRow, i) == null ? "" : centerTableModel.getValueAt(modelRow, i).toString());
            tf.setEditable(false);
            panel.add(tf);
        }
        bottomPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
        bottomPanel.revalidate();
    }

    // Point 1 Fix: Displays details of a Warehouse list row
    private void showWarehouseRowDetails(int modelRow) {
        clearBottomPanel();

        String warehouseName = (String) centerTableModel.getValueAt(modelRow, 2); // Name is now in Column 2
        WarehouseDAO.WarehouseInfo info = WarehouseDAO.getWarehouseByName(warehouseName);

        if (info == null) {
            showRowDetails(modelRow);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 6));

        // FIX: Display Input Order (Inauguration Date after Name)
        panel.add(new JLabel("Warehouse Name:"));
        panel.add(new JTextField(info.name));

        panel.add(new JLabel("Inauguration Date:")); // MOVED UP
        panel.add(new JTextField(info.inauguration != null ? info.inauguration.toString() : ""));

        panel.add(new JLabel("City:"));
        panel.add(new JTextField(info.city != null ? info.city : ""));

        panel.add(new JLabel("Address:"));
        panel.add(new JTextField(info.address != null ? info.address : ""));

        panel.add(new JLabel("Last Activity:"));
        panel.add(new JTextField(info.lastActivity != null ? info.lastActivity.toString() : ""));

        JTextArea notesArea = new JTextArea(info.notes != null ? info.notes : "", 3, 20);
        notesArea.setWrapStyleWord(true);
        notesArea.setLineWrap(true);
        notesArea.setEditable(false);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        panel.add(new JLabel("Notes:"));
        panel.add(notesScroll);

        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextField) {
                ((JTextField) comp).setEditable(false);
            }
        }

        bottomPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
        bottomPanel.revalidate();
    }

    // ---------------------- DATE HELPER (REMOVED JDateChooser) ----------------------
    // The JDateChooser component and its fixCalendarPopup method are removed
    // and replaced by the stable DateInputPanel in the forms.
    private void fixCalendarPopup(DateInputPanel chooser) {
        // No operation needed for DateInputPanel as it handles its own display
    }

    // ---------------------- EXPORT ----------------------
    private void exportTable() {
        if (centerTableModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "No data to export."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("export_" + System.currentTimeMillis() + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            int cols = centerTableModel.getColumnCount();
            for (int c = 0; c < cols; c++) bw.write(centerTableModel.getColumnName(c) + "\t");
            bw.newLine();
            for (int r = 0; r < centerTableModel.getRowCount(); r++) {
                for (int c = 0; c < cols; c++)
                    bw.write((centerTableModel.getValueAt(r, c) == null ? "" : centerTableModel.getValueAt(r, c).toString()) + "\t");
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "✅ Exported to " + f.getAbsolutePath());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage()); }
    }

    // ---------------------- UTILS ----------------------
    private String extractNameFromListEntry(String s) {
        int dot = s.indexOf('.');
        return (dot >= 0 && dot + 1 < s.length()) ? s.substring(dot + 1).trim() : s.trim();
    }

    // --- Helper methods re-added for compilation (previously missing) ---
    private void resetRightPanel() {
        rightPanel.removeAll();
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void addFindBar() {
        JPanel findPanel = new JPanel(new BorderLayout(5, 5));
        JTextField findField = new JTextField();
        JButton findButton = new JButton("Find");
        JButton resetButton = new JButton("Reset Table");

        JPanel container = new JPanel();
        container.setLayout(new GridLayout(2, 1, 5, 5));

        JPanel topRow = new JPanel(new BorderLayout(5, 5));
        topRow.add(new JLabel("Find:"), BorderLayout.WEST);
        topRow.add(findField, BorderLayout.CENTER);
        topRow.add(findButton, BorderLayout.EAST);

        container.add(topRow);
        container.add(resetButton);

        rightPanel.add(container);

        findButton.addActionListener(e -> {
            String text = findField.getText().trim();
            if (text.isEmpty()) {
                rowSorter.setRowFilter(null);
            } else {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        resetButton.addActionListener(e -> {
            findField.setText("");
            rowSorter.setRowFilter(null);
        });
    }

    // ---------- Small helper methods ----------
    private int parseInt(JTextField f) {
        try {
            return f.getText().trim().isEmpty() ? 0 : Integer.parseInt(f.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value: " + f.getText());
        }
    }

    private double parseDouble(JTextField f) {
        try {
            return f.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(f.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid decimal value: " + f.getText());
        }
    }

    // --- Standard text field size for all input forms ---
    private void normalizeFieldHeight(JComponent field) {
        Dimension d = field.getPreferredSize();
        d.height = 28;
        field.setPreferredSize(d);
        field.setMinimumSize(d);
        field.setMaximumSize(d);
    }
}