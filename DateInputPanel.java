import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.IntStream;

/**
 * Custom JPanel containing three JComboBoxes for Year, Month, and Day input.
 * Provides necessary validation and returns a LocalDate object.
 */
public class DateInputPanel extends JPanel {

    private JComboBox<Integer> cbDay;
    private JComboBox<String> cbMonth;
    private JComboBox<Integer> cbYear;

    public DateInputPanel() {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // --- 1. Year ---
        int currentYear = LocalDate.now().getYear();
        // Provides years from 10 years ago to 5 years in the future
        Integer[] years = IntStream.rangeClosed(currentYear - 50, currentYear + 50).boxed().toArray(Integer[]::new);
        cbYear = new JComboBox<>(years);

        // --- 2. Month ---
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        cbMonth = new JComboBox<>(months);

        // --- 3. Day ---
        // Initial capacity up to 31, updated dynamically by listener
        cbDay = new JComboBox<>(IntStream.rangeClosed(1, 31).boxed().toArray(Integer[]::new));

        // Add components
        add(cbYear);
        add(cbMonth);
        add(cbDay);

        // Default to today's date
        LocalDate today = LocalDate.now();
        cbYear.setSelectedItem(today.getYear());
        cbMonth.setSelectedIndex(today.getMonthValue() - 1);
        cbDay.setSelectedItem(today.getDayOfMonth());

        // Listener to refresh days when month/year changes (to handle leap years/30-day months)
        cbMonth.addActionListener(e -> updateDays());
        cbYear.addActionListener(e -> updateDays());

        // Ensure initial days are correct
        updateDays();
    }

    /**
     * Re-populates the day dropdown based on the selected month and year.
     */
    private void updateDays() {
        // Ensure valid selection indices before proceeding
        if (cbYear.getSelectedItem() == null || cbMonth.getSelectedIndex() < 0) return;

        Integer year = (Integer) cbYear.getSelectedItem();
        int monthIndex = cbMonth.getSelectedIndex() + 1; // 1-based month value

        YearMonth yearMonth = YearMonth.of(year, monthIndex);
        int daysInMonth = yearMonth.lengthOfMonth();

        Integer currentDay = (Integer) cbDay.getSelectedItem();

        // Temporarily remove items
        cbDay.removeAllItems();

        // Add correct number of days
        for (int i = 1; i <= daysInMonth; i++) {
            cbDay.addItem(i);
        }

        // Restore selected day, or clamp to max days in new month
        if (currentDay != null && currentDay <= daysInMonth) {
            cbDay.setSelectedItem(currentDay);
        } else if (daysInMonth > 0) {
            // If the old day was invalid (e.g., 31st in February), select the last valid day
            cbDay.setSelectedItem(Math.min(currentDay != null ? currentDay : 1, daysInMonth));
        }
    }

    /**
     * Sets the date displayed in the panel.
     */
    public void setDate(LocalDate date) {
        if (date == null) {
            // If null, set to a default (e.g., today) or clear
            LocalDate today = LocalDate.now();
            cbYear.setSelectedItem(today.getYear());
            cbMonth.setSelectedIndex(today.getMonthValue() - 1);
            cbDay.setSelectedItem(today.getDayOfMonth());
            return;
        }
        cbYear.setSelectedItem(date.getYear());
        cbMonth.setSelectedIndex(date.getMonthValue() - 1);
        cbDay.setSelectedItem(date.getDayOfMonth());
    }

    /**
     * Gets the LocalDate object based on current selections.
     */
    public LocalDate getDate() {
        try {
            int year = (Integer) cbYear.getSelectedItem();
            int month = cbMonth.getSelectedIndex() + 1;
            int day = (Integer) cbDay.getSelectedItem();
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            // Log error or throw if components are null/unselected, but return safe date for UI stability
            System.err.println("Error parsing date from JComboBoxes: " + e.getMessage());
            return LocalDate.now();
        }
    }

    /**
     * Gets the date as a java.sql.Date.
     */
    public java.sql.Date getSqlDate() {
        return java.sql.Date.valueOf(getDate());
    }

    /**
     * Gets the size of the combined panel.
     */
    @Override
    public Dimension getPreferredSize() {
        // Return a fixed dimension suitable for horizontal placement
        return new Dimension(300, 28);
    }
}