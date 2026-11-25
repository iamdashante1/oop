package kpfl.model;

import kpfl.exception.ValidationException;

/** One ticket for one fixture. The price is tax-inclusive (15% GCT included). */
public class Ticket {
    // Each category carries its price multiplier: VIP costs double the stadium's base price
    public enum Category {
        GENERAL(1.0), VIP(2.0);

        private final double priceMultiplier;

        Category(double priceMultiplier) { this.priceMultiplier = priceMultiplier; }

        public double getPriceMultiplier() { return priceMultiplier; }
    }

    // A cancelled ticket stops counting against the stadium capacity
    public enum Status { SOLD, CANCELLED }

    // Fixture and spectator are stored by ID, matching how the project saves relationships
    private final String ticketId;
    private final String fixtureId;
    private final String spectatorId;
    private final Category category;
    private final double price;
    private Status status = Status.SOLD;

    public Ticket(String ticketId, String fixtureId, String spectatorId, Category category, double price) {
        // Validate everything so a bad ticket can never exist
        if (isBlank(ticketId)) throw new ValidationException("Ticket ID is required.");
        if (isBlank(fixtureId)) throw new ValidationException("Fixture ID is required.");
        if (isBlank(spectatorId)) throw new ValidationException("Spectator ID is required.");
        if (category == null) throw new ValidationException("Ticket category is required.");
        if (!Double.isFinite(price) || price < 0) throw new ValidationException("Ticket price cannot be negative.");
        this.ticketId = ticketId.trim();
        this.fixtureId = fixtureId.trim();
        this.spectatorId = spectatorId.trim();
        this.category = category;
        this.price = price;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public String getTicketId() { return ticketId; }
    public String getFixtureId() { return fixtureId; }
    public String getSpectatorId() { return spectatorId; }
    public Category getCategory() { return category; }
    public double getPrice() { return price; }
    public Status getStatus() { return status; }

    // Used by TicketService.cancelTicket and when reloading saved tickets
    public void setStatus(Status status) {
        if (status == null) throw new ValidationException("Ticket status is required.");
        this.status = status;
    }

    public boolean isSold() { return status == Status.SOLD; }

    @Override
    public String toString() {
        return ticketId + " | fixture " + fixtureId + " | " + category + " | " + Money.format(price) + " | " + status;
    }
}
