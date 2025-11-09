import java.time.LocalDate;

/**
 * Represents a single product trade record within a warehouse.
 * Extends the base Record class.
 */
public class Product extends Record {

    private String name;
    private String supplier;
    private String customer;
    private int totalQuantity;
    private int importQuantity;
    private double importPrice;
    private int exportQuantity;
    private double exportPrice;

    public Product(int id, String name, String supplier, String customer,
                   int totalQuantity, int importQuantity, double importPrice,
                   int exportQuantity, double exportPrice, LocalDate dateAdded) {
        super(id, dateAdded);
        this.name = name;
        this.supplier = supplier;
        this.customer = customer;
        this.totalQuantity = totalQuantity;
        this.importQuantity = importQuantity;
        this.importPrice = importPrice;
        this.exportQuantity = exportQuantity;
        this.exportPrice = exportPrice;
    }

    public Product() {
        super();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public int gettotQuantity() { return totalQuantity; }
    public void settotQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public int getimpQuantity() { return importQuantity; }
    public void setimpQuantity(int importQuantity) { this.importQuantity = importQuantity; }

    public double getimpPrice() { return importPrice; }
    public void setimpPrice(double importPrice) { this.importPrice = importPrice; }

    public int getexpQuantity() { return exportQuantity; }
    public void setexpQuantity(int exportQuantity) { this.exportQuantity = exportQuantity; }

    public double getexpPrice() { return exportPrice; }
    public void setexpPrice(double exportPrice) { this.exportPrice = exportPrice; }
}
