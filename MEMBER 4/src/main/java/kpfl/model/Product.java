package kpfl.model;

import kpfl.exception.ValidationException;

/** Something the stadium shop sells. Unit price is tax-inclusive. Parent of Merchandise and FoodItem. */
public abstract class Product {
    private final String productId; // fixed after creation, used for lookups and saving
    private String name;
    private int stock;
    private double unitPrice;

    protected Product(String productId, String name, int stock, double unitPrice) {
        // Reject blank IDs/names, negative stock and negative (or non-finite) prices
        if (productId == null || productId.trim().isEmpty()) throw new ValidationException("Product ID is required.");
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Product name is required.");
        if (stock < 0) throw new ValidationException("Stock cannot be negative.");
        if (!Double.isFinite(unitPrice) || unitPrice < 0) throw new ValidationException("Price cannot be negative.");
        this.productId = productId.trim();
        this.name = name.trim();
        this.stock = stock;
        this.unitPrice = unitPrice;
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
        if (!Double.isFinite(unitPrice) || unitPrice < 0) throw new ValidationException("Price cannot be negative.");
        this.unitPrice = unitPrice;
    }

    // Restocking: quantity must be positive
    public void addStock(int qty) {
        if (qty <= 0) throw new ValidationException("Restock quantity must be positive.");
        stock += qty;
    }

    // Used by StadiumShop after a valid sale; blocks overselling
    public void removeStock(int qty) {
        if (qty <= 0) throw new ValidationException("Quantity must be positive.");
        if (qty > stock) throw new ValidationException("Not enough " + name + " in stock (have " + stock + ").");
        stock -= qty;
    }

    /** Overridden by subclasses: what kind of product this is (polymorphism). */
    public abstract String getType();

    /** Value saved in the last column of the products file (size for merchandise, true/false for food). */
    public abstract String getExtra();

    @Override
    public String toString() {
        return productId + " | " + getType() + ": " + name + " | " + Money.format(unitPrice) + " | stock " + stock;
    }
}
