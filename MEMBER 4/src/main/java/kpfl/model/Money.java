package kpfl.model;

/** Money helpers. All KPFL prices include 15% GCT, so the GCT part of a total is total x 15/115. */
public final class Money {
    public static final double GCT_RATE = 0.15;

    private Money() { } // utility class: nobody should create one

    // Round to cents so totals like 1149.999999 print as 1150.00
    public static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    // Prices already include GCT, so we EXTRACT the tax: total x 0.15 / 1.15 (never total x 0.15)
    public static double gctIncluded(double total) { return round2(total * GCT_RATE / (1 + GCT_RATE)); }

    // What the total was before tax
    public static double preTax(double total) { return round2(total - gctIncluded(total)); }

    public static String format(double v) { return String.format("J$%,.2f", v); }
}
