package kpfl.service;

import kpfl.exception.ValidationException;
import kpfl.model.Money;
import kpfl.model.Product;
import kpfl.model.Receipt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Stadium shop: inventory, multi-item sales, payment checks and receipts. All prices include 15% GCT. */
public class StadiumShop {
    private final Map<String, Product> inventory = new LinkedHashMap<>();
    private final List<Receipt> sales = new ArrayList<>();

    public void addProduct(Product p) {
        if (p == null) throw new ValidationException("Product is required.");
        if (inventory.containsKey(p.getProductId()))
            throw new ValidationException("Product ID " + p.getProductId() + " already exists.");
        inventory.put(p.getProductId(), p);
    }

    public Product findProduct(String id) {
        Product p = id == null ? null : inventory.get(id.trim());
        if (p == null) throw new ValidationException("No product with ID " + id + ".");
        return p;
    }

    public void restock(String productId, int qty) {
        findProduct(productId).addStock(qty);
    }

    public List<Product> getInventory() { return new ArrayList<>(inventory.values()); }

    /**
     * Completes a sale of several items (productId to quantity). Everything is checked before any stock is
     * removed, so a failed sale changes nothing.
     */
    public Receipt checkout(Map<String, Integer> basket, double payment) {
        if (basket == null || basket.isEmpty()) throw new ValidationException("The basket is empty.");
        List<Receipt.Line> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> e : basket.entrySet()) {
            Product p = findProduct(e.getKey());
            int qty = e.getValue();
            if (qty <= 0) throw new ValidationException("Quantity for " + p.getName() + " must be positive.");
            if (qty > p.getStock())
                throw new ValidationException("Not enough " + p.getName() + " in stock (have " + p.getStock() + ").");
            lines.add(new Receipt.Line(p.getProductId(), p.getName(), qty, p.getUnitPrice()));
        }
        Receipt draft = new Receipt(nextReceiptId(), LocalDateTime.now(), lines, payment);
        if (payment < draft.getTotal())
            throw new ValidationException("Insufficient payment: total is " + Money.format(draft.getTotal())
                    + " but only " + Money.format(payment) + " was paid.");
        for (Receipt.Line l : lines) findProduct(l.getProductId()).removeStock(l.getQuantity());
        sales.add(draft);
        return draft;
    }

    /** Used when reloading saved data. */
    public void restoreSale(Receipt r) { sales.add(r); }

    public List<Receipt> getSales() { return Collections.unmodifiableList(sales); }

    public double totalRevenue() {
        double sum = 0;
        for (Receipt r : sales) sum += r.getTotal();
        return Money.round2(sum);
    }

    private String nextReceiptId() {
        return String.format("R%04d", sales.size() + 1);
    }
}
