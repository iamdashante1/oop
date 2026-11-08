package kpfl.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A completed shop sale. Totals include 15% GCT; the GCT part is total x 15/115. */
public class Receipt {
    public static class Line {
        private final String productId;
        private final String name;
        private final int quantity;
        private final double unitPrice;

        public Line(String productId, String name, int quantity, double unitPrice) {
            this.productId = productId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductId() { return productId; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getLineTotal() { return Money.round2(unitPrice * quantity); }
    }

    private final String receiptId;
    private final LocalDateTime time;
    private final List<Line> lines;
    private final double total;
    private final double payment;

    public Receipt(String receiptId, LocalDateTime time, List<Line> lines, double payment) {
        this.receiptId = receiptId;
        this.time = time;
        this.lines = new ArrayList<>(lines);
        double sum = 0;
        for (Line l : lines) sum += l.getLineTotal();
        this.total = Money.round2(sum);
        this.payment = payment;
    }

    public String getReceiptId() { return receiptId; }
    public LocalDateTime getTime() { return time; }
    public List<Line> getLines() { return Collections.unmodifiableList(lines); }
    public double getTotal() { return total; }
    public double getPayment() { return payment; }
    public double getChange() { return Money.round2(payment - total); }
    public double getGct() { return Money.gctIncluded(total); }
    public double getSubtotal() { return Money.preTax(total); }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("---------- KPFL STADIUM SHOP ----------\n");
        sb.append("Receipt ").append(receiptId).append("  ")
          .append(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        for (Line l : lines) {
            sb.append(String.format("%-22s %2d x %10s = %12s%n", l.getName(), l.getQuantity(),
                    Money.format(l.getUnitPrice()), Money.format(l.getLineTotal())));
        }
        sb.append(String.format("%-38s %12s%n", "Subtotal (before GCT)", Money.format(getSubtotal())));
        sb.append(String.format("%-38s %12s%n", "GCT 15% (included)", Money.format(getGct())));
        sb.append(String.format("%-38s %12s%n", "TOTAL", Money.format(total)));
        sb.append(String.format("%-38s %12s%n", "Paid", Money.format(payment)));
        sb.append(String.format("%-38s %12s%n", "Change", Money.format(getChange())));
        sb.append("---------------------------------------\n");
        return sb.toString();
    }
}
