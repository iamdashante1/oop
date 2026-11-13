package kpfl.model;

/** Food and beverages. */
public class FoodItem extends Product {
    private final boolean beverage;

    public FoodItem(String id, String name, int stock, double unitPrice, boolean beverage) {
        super(id, name, stock, unitPrice);
        this.beverage = beverage;
    }

    @Override
    public String getType() { return beverage ? "Beverage" : "Food"; }

    @Override
    public String getExtra() { return String.valueOf(beverage); }
}
