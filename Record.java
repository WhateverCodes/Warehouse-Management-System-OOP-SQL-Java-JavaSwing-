import java.time.LocalDate;

/**
 * Base class for all record types (Product, FutureTrade).
 * Implements common fields and methods for ID and dateAdded.
 */
public abstract class Record {

    protected int id;
    protected LocalDate dateAdded;

    public Record(int id, LocalDate dateAdded) {
        this.id = id;
        this.dateAdded = dateAdded;
    }

    public Record() {
        this.dateAdded = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }
}
