package kpfl.model;

import kpfl.exception.ValidationException;

/** One ticket for one fixture. The price is tax-inclusive (15% GCT included). */
public class Ticket {
    public enum Category {
        GENERAL(1.0), VIP(2.0);

        private final double priceMultiplier;

        Category(double priceMultiplier) { this.priceMultiplier = priceMultiplier; }

        public double getPriceMultiplier() { return priceMultiplier; }
    }

    public enum Status { SOLD, CANCELLED }

    private final String ticketId;
    private final String fixtureId;
    private final String spectatorId;
    private final Category category;
    private final double price;
    private Status status = Status.SOLD;

    public Ticket(String ticketId, String fixtureId, String spectatorId, Category category, double price) {
        if (price < 0) throw new ValidationException("Ticket price cannot be negative.");
        this.ticketId = ticketId;
        this.fixtureId = fixtureId;
        this.spectatorId = spectatorId;
        this.category = category;
        this.price = price;
    }

    public String getTicketId() { return ticketId; }
    public String getFixtureId() { return fixtureId; }
    public String getSpectatorId() { return spectatorId; }
    public Category getCategory() { return category; }
    public double getPrice() { return price; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isSold() { return status == Status.SOLD; }

    @Override
    public String toString() {
        return ticketId + " | fixture " + fixtureId + " | " + category + " | " + Money.format(price) + " | " + status;
    }
}
