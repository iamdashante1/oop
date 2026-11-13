package kpfl.model;

import kpfl.exception.ValidationException;

/** A referee or other match official who can be assigned to fixtures. */
public class MatchOfficial extends Person {
    private String rank;
    private boolean available = true;

    public MatchOfficial(String id, String name, int age, String gender, String rank) {
        super(id, name, age, gender);
        if (rank == null || rank.trim().isEmpty()) throw new ValidationException("Official rank/role is required.");
        this.rank = rank.trim();
    }

    @Override
    public String getRole() { return "Match Official"; }

    public String getRank() { return rank; }
    public void setRank(String rank) {
        if (rank == null || rank.trim().isEmpty()) throw new ValidationException("Official rank/role is required.");
        this.rank = rank.trim();
    }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return getName() + " (" + rank + (available ? "" : ", unavailable") + ")";
    }
}
