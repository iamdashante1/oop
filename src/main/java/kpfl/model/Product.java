package kpfl.model;

import kpfl.exception.ValidationException;

/** Something the stadium shop sells. Unit price is tax-inclusive. */
public abstract class Product {
    private final String productId;
    private String name;
    private int stock;
    private double unitPrice;

    protected Product(String productId, String name, int stock, double unitPrice) {
        if (productId == null || productId.trim().isEmpty()) throw new ValidationException("Product ID is required.");
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Product name is required.");
        if (stock < 0) throw new ValidationException("Stock cannot be negative.");
        // These characters separate fields in the saved sales file, so they cannot be part of an ID.
        if (productId.contains("|") || productId.contains(":") || productId.contains(";"))
            throw new ValidationException("Product ID cannot contain | : or ;");
        this.productId = productId.trim();
        setName(name);
        this.stock = stock;
        setUnitPrice(unitPrice);
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public int getStock() { return stock; }
    public double getUnitPrice() { return unitPrice; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Product name is required.");
        this.name = name.trim();
    }

    public void setUnitPrice(double unitPrice) {
        if (!Double.isFinite(unitPrice) || unitPrice < 0) throw new ValidationException("Price must be a finite, non-negative amount.");
        this.unitPrice = unitPrice;
    }

    public void addStock(int qty) {
        if (qty <= 0) throw new ValidationException("Restock quantity must be positive.");
        stock += qty;
    }

    public void removeStock(int qty) {
        if (qty <= 0) throw new ValidationException("Quantity must be positive.");
        if (qty > stock) throw new ValidationException("Not enough " + name + " in stock (have " + stock + ").");
        stock -= qty;
    }

    /** Overridden by subclasses: what kind of product this is. */
    public abstract String getType();

    /** Value saved in the last column of the products file. */
    public abstract String getExtra();

    @Override
    public String toString() {
        return productId + " | " + getType() + ": " + name + " | " + Money.format(unitPrice) + " | stock " + stock;
    }
}
