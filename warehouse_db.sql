-- 1. Creating Database
CREATE DATABASE warehouse_db;
USE warehouse_db;

-- 2. Creating Main Records Table
CREATE TABLE IF NOT EXISTS records (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Product VARCHAR(100) NOT NULL,
    Total_Quantity INT DEFAULT 0,
    Import_Quantity INT DEFAULT 0,
    Import_Price DOUBLE(10,2) DEFAULT 0.00,
    Export_Quantity INT DEFAULT 0,
    Export_Price DOUBLE(10,2) DEFAULT 0.00,
    Date DATE NOT NULL
);

-- 3. Creating Future Trades Records Table
CREATE TABLE IF NOT EXISTS future_trades (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Product VARCHAR(100) NOT NULL,
    Import_Quantity INT DEFAULT 0,
    Import_Price DOUBLE(10,2) DEFAULT 0.00,
    Export_Quantity INT DEFAULT 0,
    Export_Price DOUBLE(10,2) DEFAULT 0.00,
    Date DATE NOT NULL
);

-- 4. Creating a Trigger to Automatically Update the Total Quantity
DELIMITER $$
CREATE TRIGGER update_total_quantity
    BEFORE INSERT ON records
    FOR EACH ROW
    BEGIN
        DECLARE current_total INT;
  
       -- Get current total stock for this product
        SELECT IFNULL(SUM(Import_Quantity) - SUM(Export_Quantity), 0)
        INTO current_total
        FROM records
        WHERE Product = NEW.Product;
  
        -- Update Total_Quantity for the new row
        SET NEW.Total_Quantity = current_total + (NEW.Import_Quantity - NEW.Export_Quantity);
    END$$
DELIMITER ;
