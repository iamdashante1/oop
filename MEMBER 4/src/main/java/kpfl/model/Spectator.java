package kpfl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A match-day buyer. Extends Person and keeps the IDs (not objects) of the tickets bought. */
public class Spectator extends Person {
    // IDs are stored so the file storage can rebuild the link when data is reloaded
    private final List<String> ticketIds = new ArrayList<>();

    public Spectator(String id, String name, int age, String gender) {
        super(id, name, age, gender); // Person validates the shared details
    }

    @Override
    public String getRole() { return "Spectator"; } // overrides Person.getRole() (polymorphism)

    // Called by TicketService when a ticket is sold or reloaded; ignores an ID that is already listed
    public void addTicketId(String ticketId) {
        if (ticketId != null && !ticketIds.contains(ticketId)) ticketIds.add(ticketId);
    }

    // Read-only view so outside code cannot edit the list directly
    public List<String> getTicketIds() { return Collections.unmodifiableList(ticketIds); }
}
