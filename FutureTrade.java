import java.time.LocalDate;

/**
 * Represents a scheduled future trade across warehouses.
 * Extends Record for shared fields.
 */
public class FutureTrade extends Record {

    private String warehouse;
    private String name;
    private String supplier;
    private String customer;
    private int importQuantity;
    private double importPrice;
    private int exportQuantity;
    private double exportPrice;

    public FutureTrade(int id, String warehouse, String name, String supplier, String customer,
                       int importQuantity, double importPrice, int exportQuantity, double exportPrice, LocalDate dateAdded) {
        super(id, dateAdded);
        this.warehouse = warehouse;
        this.name = name;
        this.supplier = supplier;
        this.customer = customer;
        this.importQuantity = importQuantity;
        this.importPrice = importPrice;
        this.exportQuantity = exportQuantity;
        this.exportPrice = exportPrice;
    }

    public FutureTrade() {
        super();
    }

    // Getters and setters
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public int getimpQuantity() { return importQuantity; }
    public void setimpQuantity(int importQuantity) { this.importQuantity = importQuantity; }

    public double getimpPrice() { return importPrice; }
    public void setimpPrice(double importPrice) { this.importPrice = importPrice; }

    public int getexpQuantity() { return exportQuantity; }
    public void setexpQuantity(int exportQuantity) { this.exportQuantity = exportQuantity; }

    public double getexpPrice() { return exportPrice; }
    public void setexpPrice(double exportPrice) { this.exportPrice = exportPrice; }
}
