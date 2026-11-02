package kpfl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A match-day buyer. Keeps the IDs of the tickets bought. */
public class Spectator extends Person {
    private final List<String> ticketIds = new ArrayList<>();

    public Spectator(String id, String name, int age, String gender) {
        super(id, name, age, gender);
    }

    @Override
    public String getRole() { return "Spectator"; }

    public void addTicketId(String ticketId) { ticketIds.add(ticketId); }
    public List<String> getTicketIds() { return Collections.unmodifiableList(ticketIds); }
}
