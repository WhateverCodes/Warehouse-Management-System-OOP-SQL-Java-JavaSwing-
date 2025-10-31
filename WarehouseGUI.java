import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WarehouseGUI extends JFrame {

    // Top buttons
    private JButton btnWarehouseList;
    private JButton btnFutureRecords;

    // Panels
    private JPanel middlePanel;
    private JPanel leftPanel;
    private JPanel centerPanel;
    private JPanel rightPanel;
    private JPanel bottomPanel;

    private JList<String> warehouseJList;
    private DefaultListModel<String> listModel;

    private JTable centerTable;
    private DefaultTableModel centerTableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private enum Mode { WAREHOUSE_LIST, WAREHOUSE_RECORDS, FUTURE_TRADES }
    private Mode currentMode = Mode.WAREHOUSE_LIST;

    private String selectedWarehouse = null;

    public WarehouseGUI() {
        setTitle("Warehouse Management System");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initTopPanel();
        initMiddlePanels();
        initBottomPanel();

        showWarehouseListView();
        setLocationRelativeTo(null);
    }

    // ---------- INITIALIZATION ----------

    private void initTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnWarehouseList = new JButton("Warehouse List");
        btnFutureRecords = new JButton("Future Records");
        btnWarehouseList.setPreferredSize(new Dimension(140, 32));
        btnFutureRecords.setPreferredSize(new Dimension(140, 32));

        top.add(btnWarehouseList);
        top.add(btnFutureRecords);
        add(top, BorderLayout.NORTH);

        btnWarehouseList.addActionListener(e -> showWarehouseListView());
        btnFutureRecords.addActionListener(e -> showFutureRecordsView());
    }

    private void initMiddlePanels() {
        middlePanel = new JPanel(new BorderLayout());

        // Left panel
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Warehouses"));

        listModel = new DefaultListModel<>();
        warehouseJList = new JList<>(listModel);
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

        // Center table
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Table"));
        centerTableModel = new DefaultTableModel();
        centerTable = new JTable(centerTableModel);
        centerTable.setRowHeight(24);
        centerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        centerPanel.add(new JScrollPane(centerTable), BorderLayout.CENTER);

        // Right control panel
        rightPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        rightPanel.setPreferredSize(new Dimension(220, 0));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        middlePanel.add(leftPanel, BorderLayout.WEST);
        middlePanel.add(centerPanel, BorderLayout.CENTER);
        middlePanel.add(rightPanel, BorderLayout.EAST);
        add(middlePanel, BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Input Panel"));
        bottomPanel.setPreferredSize(new Dimension(0, 210));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ---------- VIEW SWITCHERS ----------

    private void showWarehouseListView() {
        currentMode = Mode.WAREHOUSE_LIST;
        selectedWarehouse = null;
        btnWarehouseList.setEnabled(false);
        btnFutureRecords.setEnabled(true);
        loadWarehouseListIntoLeftPanel();
        loadMasterWarehouseTable();
        showWarehouseMasterControls();
        clearBottomPanel();
    }

    private void showFutureRecordsView() {
        currentMode = Mode.FUTURE_TRADES;
        selectedWarehouse = null;
        btnWarehouseList.setEnabled(true);
        btnFutureRecords.setEnabled(false);
        loadWarehouseListIntoLeftPanel();
        loadFutureTradesIntoCenter();
        showFutureTradeControls();
        clearBottomPanel();
    }

    private void openWarehouseRecords(String warehouseName) {
        if (warehouseName == null || warehouseName.isBlank()) return;
        currentMode = Mode.WAREHOUSE_RECORDS;
        selectedWarehouse = warehouseName;
        btnWarehouseList.setEnabled(true);
        btnFutureRecords.setEnabled(true);
        loadWarehouseListIntoLeftPanel();
        highlightWarehouseInList(warehouseName);
        loadRecordsOfWarehouseIntoCenter(warehouseName);
        showRecordControls();
        clearBottomPanel();
    }

    // ---------- DATA LOADERS ----------

    private void loadWarehouseListIntoLeftPanel() {
        listModel.clear();
        List<WarehouseDAO.WarehouseInfo> warehouses = WarehouseDAO.getAllWarehouses();
        int idx = 1;
        for (WarehouseDAO.WarehouseInfo w : warehouses)
            listModel.addElement(idx++ + ". " + w.name);
    }

    private void loadMasterWarehouseTable() {
        String[] cols = {"Index", "Warehouse_Name", "City", "Address", "Inauguration_Date", "Last_Activity_Date", "Notes"};
        centerTableModel.setDataVector(new Object[][]{}, cols);
        List<WarehouseDAO.WarehouseInfo> warehouses = WarehouseDAO.getAllWarehouses();
        int idx = 1;
        for (WarehouseDAO.WarehouseInfo w : warehouses)
            centerTableModel.addRow(new Object[]{idx++, w.name, w.city, w.address, w.inauguration, w.lastActivity, w.notes});
        rowSorter = new TableRowSorter<>(centerTableModel);
        centerTable.setRowSorter(rowSorter);
        leftAlignAllCenterTableColumns();
    }

    private void loadRecordsOfWarehouseIntoCenter(String warehouseName) {
        ProductDAO.setCurrentWarehouse(warehouseName);
        ArrayList<Product> products = ProductDAO.getAllProducts();
        String[] cols = {"ID", "Product", "Supplier", "Customer", "Total_Quantity",
                "Import_Quantity", "Import_Price", "Export_Quantity", "Export_Price", "Date"};
        centerTableModel.setDataVector(new Object[][]{}, cols);
        for (Product p : products)
            centerTableModel.addRow(new Object[]{p.getId(), p.getName(), p.getSupplier(), p.getCustomer(),
                    p.gettotQuantity(), p.getimpQuantity(), p.getimpPrice(),
                    p.getexpQuantity(), p.getexpPrice(), p.getDateAdded()});
        rowSorter = new TableRowSorter<>(centerTableModel);
        centerTable.setRowSorter(rowSorter);
        leftAlignAllCenterTableColumns();
    }

    private void loadFutureTradesIntoCenter() {
        ArrayList<FutureTrade> trades = FutureTradeDAO.getAllFutureTrades();
        String[] cols = {"ID", "Warehouse_Name", "Product", "Supplier", "Customer",
                "Import_Quantity", "Import_Price", "Export_Quantity", "Export_Price", "Date"};
        centerTableModel.setDataVector(new Object[][]{}, cols);
        for (FutureTrade t : trades)
            centerTableModel.addRow(new Object[]{t.getId(), t.getWarehouse(), t.getName(), t.getSupplier(),
                    t.getCustomer(), t.getimpQuantity(), t.getimpPrice(),
                    t.getexpQuantity(), t.getexpPrice(), t.getDateAdded()});
        rowSorter = new TableRowSorter<>(centerTableModel);
        centerTable.setRowSorter(rowSorter);
        leftAlignAllCenterTableColumns();
    }

    // ---------- RIGHT PANEL CONTROLS ----------

    private void showWarehouseMasterControls() {
        rightPanel.removeAll();
        JButton btnAdd = new JButton("New Warehouse");
        JButton btnEdit = new JButton("Edit Warehouse");
        JButton btnDelete = new JButton("Delete Warehouse");
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showRecordControls() {
        rightPanel.removeAll();
        JButton btnImport = new JButton("Record Import");
        JButton btnExport = new JButton("Record Export");
        JButton btnUpdate = new JButton("Update Record");
        JButton btnDelete = new JButton("Delete Record");
        rightPanel.add(btnImport);
        rightPanel.add(btnExport);
        rightPanel.add(btnUpdate);
        rightPanel.add(btnDelete);
        rightPanel.revalidate();
        rightPanel.repaint();

        btnImport.addActionListener(e -> showRecordImportForm());
        btnExport.addActionListener(e -> showRecordExportForm());
        btnUpdate.addActionListener(e -> showRecordUpdateForm());
        btnDelete.addActionListener(e -> showRecordDeleteForm());
    }

    private void showFutureTradeControls() {
        rightPanel.removeAll();
        JButton btnImport = new JButton("Record Import");
        JButton btnExport = new JButton("Record Export");
        JButton btnUpdate = new JButton("Update Record");
        JButton btnDelete = new JButton("Delete Record");
        rightPanel.add(btnImport);
        rightPanel.add(btnExport);
        rightPanel.add(btnUpdate);
        rightPanel.add(btnDelete);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // ---------- RECORD FORMS ----------

    private void clearBottomPanel() {
        bottomPanel.removeAll();
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    private JPanel buildForm(String[] labels, JTextField[] fields, JButton button) {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int row = 0;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = row;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            fields[i] = new JTextField(20);
            form.add(fields[i], gbc);
            row++;
        }
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(button, gbc);
        return form;
    }

    private void showRecordImportForm() {
        if (selectedWarehouse == null) { showStatus("Select a warehouse first"); return; }
        clearBottomPanel();

        String[] labels = {"Product:", "Supplier:", "Customer:", "Import Qty:", "Import Price:", "Date (YYYY-MM-DD):"};
        JTextField[] fields = new JTextField[labels.length];
        JButton submit = new JButton("Import");
        JPanel form = buildForm(labels, fields, submit);
        fields[5].setText(LocalDate.now().toString());
        JScrollPane scroll = new JScrollPane(form);
        bottomPanel.add(scroll, BorderLayout.CENTER);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                Product p = new Product(0, fields[0].getText().trim(), fields[1].getText().trim(), fields[2].getText().trim(),
                        0, Integer.parseInt(fields[3].getText().trim()), Double.parseDouble(fields[4].getText().trim()),
                        0, 0.0, LocalDate.parse(fields[5].getText().trim()));
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                ProductDAO.addProduct(p);
                loadRecordsOfWarehouseIntoCenter(selectedWarehouse);
                showStatus("✅ Import recorded.");
                clearBottomPanel();
            } catch (Exception ex) {
                showStatus("❌ " + ex.getMessage());
            }
        });
    }

    private void showRecordExportForm() {
        if (selectedWarehouse == null) { showStatus("Select a warehouse first"); return; }
        clearBottomPanel();

        String[] labels = {"Product:", "Supplier:", "Customer:", "Export Qty:", "Export Price:", "Date (YYYY-MM-DD):"};
        JTextField[] fields = new JTextField[labels.length];
        JButton submit = new JButton("Export");
        JPanel form = buildForm(labels, fields, submit);
        fields[5].setText(LocalDate.now().toString());
        JScrollPane scroll = new JScrollPane(form);
        bottomPanel.add(scroll, BorderLayout.CENTER);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                Product p = new Product(0, fields[0].getText().trim(), fields[1].getText().trim(), fields[2].getText().trim(),
                        0, 0, 0.0,
                        Integer.parseInt(fields[3].getText().trim()),
                        Double.parseDouble(fields[4].getText().trim()),
                        LocalDate.parse(fields[5].getText().trim()));
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                boolean ok = ProductDAO.exportProduct(p);
                if (ok) {
                    loadRecordsOfWarehouseIntoCenter(selectedWarehouse);
                    showStatus("✅ Export recorded.");
                    clearBottomPanel();
                } else showStatus("❌ Export denied: insufficient stock.");
            } catch (Exception ex) { showStatus("❌ " + ex.getMessage()); }
        });
    }

    private void showRecordUpdateForm() {
        if (selectedWarehouse == null) { showStatus("Select a warehouse first"); return; }
        clearBottomPanel();

        String[] labels = {"ID to Update:", "Product:", "Supplier:", "Customer:",
                "Import Qty:", "Import Price:", "Export Qty:", "Export Price:", "Date (YYYY-MM-DD):"};
        JTextField[] fields = new JTextField[labels.length];
        JButton submit = new JButton("Update");
        JPanel form = buildForm(labels, fields, submit);
        fields[8].setText(LocalDate.now().toString());
        JScrollPane scroll = new JScrollPane(form);
        bottomPanel.add(scroll, BorderLayout.CENTER);
        bottomPanel.revalidate();

        submit.addActionListener(e -> {
            try {
                int id = Integer.parseInt(fields[0].getText().trim());
                Product p = new Product(id, fields[1].getText().trim(), fields[2].getText().trim(), fields[3].getText().trim(), 0,
                        Integer.parseInt(fields[4].getText().trim().isEmpty() ? "0" : fields[4].getText().trim()),
                        Double.parseDouble(fields[5].getText().trim().isEmpty() ? "0" : fields[5].getText().trim()),
                        Integer.parseInt(fields[6].getText().trim().isEmpty() ? "0" : fields[6].getText().trim()),
                        Double.parseDouble(fields[7].getText().trim().isEmpty() ? "0" : fields[7].getText().trim()),
                        LocalDate.parse(fields[8].getText().trim()));
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                ProductDAO.updateProduct(p);
                loadRecordsOfWarehouseIntoCenter(selectedWarehouse);
                showStatus("✅ Record updated.");
                clearBottomPanel();
            } catch (Exception ex) { showStatus("❌ " + ex.getMessage()); }
        });
    }

    private void showRecordDeleteForm() {
        if (selectedWarehouse == null) { showStatus("Select a warehouse first"); return; }
        clearBottomPanel();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("ID to Delete:"), gbc);
        gbc.gridx = 1;
        JTextField tfID = new JTextField(20);
        form.add(tfID, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton delete = new JButton("Delete");
        form.add(delete, gbc);

        JScrollPane scroll = new JScrollPane(form);
        bottomPanel.add(scroll, BorderLayout.CENTER);
        bottomPanel.revalidate();

        delete.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfID.getText().trim());
                ProductDAO.setCurrentWarehouse(selectedWarehouse);
                ProductDAO.deleteProduct(id);
                loadRecordsOfWarehouseIntoCenter(selectedWarehouse);
                showStatus("🗑️ Deleted record ID " + id);
                clearBottomPanel();
            } catch (Exception ex) { showStatus("❌ " + ex.getMessage()); }
        });
    }

    // ---------- UTILITIES ----------

    private String extractNameFromListEntry(String s) {
        int dot = s.indexOf('.');
        return (dot >= 0 && dot + 1 < s.length()) ? s.substring(dot + 1).trim() : s.trim();
    }

    private void highlightWarehouseInList(String name) {
        for (int i = 0; i < listModel.size(); i++) {
            if (extractNameFromListEntry(listModel.get(i)).equals(name)) {
                warehouseJList.setSelectedIndex(i);
                warehouseJList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    private void leftAlignAllCenterTableColumns() {
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 0; i < centerTable.getColumnCount(); i++)
            centerTable.getColumnModel().getColumn(i).setCellRenderer(left);
    }

    private void showStatus(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    // ---------- MAIN ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WarehouseGUI().setVisible(true));
    }
}
