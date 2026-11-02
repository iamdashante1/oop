package kpfl.model;

/** Money helpers. All KPFL prices include 15% GCT, so the GCT part of a total is total x 15/115. */
public final class Money {
    public static final double GCT_RATE = 0.15;

    private Money() { }

    public static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    public static double gctIncluded(double total) { return round2(total * GCT_RATE / (1 + GCT_RATE)); }

    public static double preTax(double total) { return round2(total - gctIncluded(total)); }

    public static String format(double v) { return String.format("J$%,.2f", v); }
}
