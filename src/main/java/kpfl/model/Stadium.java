package kpfl.model;

import kpfl.exception.ValidationException;
import kpfl.integration.StadiumInfo;

/** A venue. Capacity must be positive and the base ticket price (GCT included) cannot be negative. */
public class Stadium implements StadiumInfo {
    private final String stadiumId;
    private String name;
    private String location;
    private int capacity;
    private double ticketPrice;
    private String fieldCondition;

    public Stadium(String stadiumId, String name, String location, int capacity) {
        this(stadiumId, name, location, capacity, 0, "Good");
    }

    public Stadium(String stadiumId, String name, String location, int capacity,
                   double ticketPrice, String fieldCondition) {
        if (stadiumId == null || stadiumId.trim().isEmpty()) throw new ValidationException("Stadium ID is required.");
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Stadium name is required.");
        if (location == null || location.trim().isEmpty()) throw new ValidationException("Stadium location is required.");
        if (capacity <= 0) throw new ValidationException("Stadium capacity must be positive.");
        if (fieldCondition == null || fieldCondition.trim().isEmpty()) throw new ValidationException("Field condition is required.");
        if (!Double.isFinite(ticketPrice) || ticketPrice < 0) throw new ValidationException("Ticket price must be a finite, non-negative amount.");
        this.stadiumId = stadiumId.trim();
        this.name = name.trim();
        this.location = location.trim();
        this.capacity = capacity;
        this.ticketPrice = ticketPrice;
        this.fieldCondition = fieldCondition.trim();
    }

    public String getStadiumId() { return stadiumId; }
    public String getName() { return name; }
    @Override public String getStadiumName() { return name; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public double getTicketPrice() { return ticketPrice; }
    public String getFieldCondition() { return fieldCondition; }

    public void setCapacity(int capacity) {
        if (capacity <= 0) throw new ValidationException("Stadium capacity must be positive.");
        this.capacity = capacity;
    }

    public void setTicketPrice(double ticketPrice) {
        if (!Double.isFinite(ticketPrice) || ticketPrice < 0) throw new ValidationException("Ticket price must be a finite, non-negative amount.");
        this.ticketPrice = ticketPrice;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Stadium name is required.");
        this.name = name.trim();
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) throw new ValidationException("Stadium location is required.");
        this.location = location.trim();
    }

    public void setFieldCondition(String fieldCondition) {
        if (fieldCondition == null || fieldCondition.trim().isEmpty())
            throw new ValidationException("Field condition is required.");
        this.fieldCondition = fieldCondition.trim();
    }

    @Override
    public String toString() {
        return stadiumId + " | " + name + ", " + location + " (capacity " + capacity + ", ticket "
                + Money.format(ticketPrice) + ", field " + fieldCondition + ")";
    }
}
