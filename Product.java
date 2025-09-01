import java.time.LocalDate;

public class Product {
    private int ID;
    private String Product;
    private int totquantity;
    private int impquantity;
    private double impprice;
    private int expquantity;
    private double expprice;
    private LocalDate dateAdded;

    // Constructor
    public Product(int ID, String Product, int Total_Quantity, int Import_Quantity, double Import_Price, int Export_Quantity, double Export_Price, LocalDate dateAdded) {
        this.ID = ID;
        this.Product = Product;
        this.totquantity = Total_Quantity;
        this.impquantity = Import_Quantity;
        this.impprice = Import_Price;
        this.expquantity = Export_Quantity;
        this.expprice = Export_Price;
        this.dateAdded = dateAdded;
    }

    // Getters
    public int getId() { return ID; }
    public String getName() { return Product; }
    public int gettotQuantity() { return totquantity; }
    public int getimpQuantity() { return impquantity; }
    public double getimpPrice() { return impprice; }
    public int getexpQuantity() { return expquantity; }
    public double getexpPrice() { return expprice; }
    public LocalDate getDateAdded() { return dateAdded; }
}