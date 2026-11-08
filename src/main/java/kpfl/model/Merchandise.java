package kpfl.model;

/** Jerseys, flags and other club merchandise. */
public class Merchandise extends Product {
    private final String size;

    public Merchandise(String id, String name, int stock, double unitPrice, String size) {
        super(id, name, stock, unitPrice);
        this.size = size == null ? "" : size.trim();
    }

    @Override
    public String getType() { return size.isEmpty() ? "Merchandise" : "Merchandise (size " + size + ")"; }

    @Override
    public String getExtra() { return size; }
}
