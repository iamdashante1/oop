package kpfl.service;

import kpfl.exception.ValidationException;
import kpfl.model.Fixture;
import kpfl.model.Money;
import kpfl.model.Spectator;
import kpfl.model.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Spectators and ticket sales. Tickets sold are counted per fixture against that fixture's stadium capacity. */
public class TicketService {
    private final FootballLeague league;
    private final Map<String, Spectator> spectators = new LinkedHashMap<>();
    private final List<Ticket> tickets = new ArrayList<>();

    public TicketService(FootballLeague league) {
        this.league = league;
    }

    public void addSpectator(Spectator s) {
        if (s == null) throw new ValidationException("Spectator is required.");
        if (spectators.containsKey(s.getId())) throw new ValidationException("Spectator ID " + s.getId() + " already exists.");
        spectators.put(s.getId(), s);
    }

    public Spectator findSpectator(String id) {
        Spectator s = id == null ? null : spectators.get(id.trim());
        if (s == null) throw new ValidationException("No spectator with ID " + id + ".");
        return s;
    }

    public List<Spectator> getSpectators() { return new ArrayList<>(spectators.values()); }

    /** Sells one ticket; rejected if the fixture is unknown/completed or the stadium is full. */
    public Ticket sellTicket(String fixtureId, String spectatorId, Ticket.Category category) {
        Fixture fixture = league.findFixture(fixtureId);
        Spectator spectator = findSpectator(spectatorId);
        if (category == null) throw new ValidationException("Ticket category is required.");
        if (fixture.getStatus() == Fixture.Status.COMPLETED)
            throw new ValidationException("Fixture " + fixtureId + " has already been played.");
        int capacity = fixture.getStadium().getCapacity();
        // Only SOLD tickets occupy seats; a cancelled ticket frees its seat.
        if (ticketsSold(fixtureId) >= capacity)
            throw new ValidationException("Sold out: " + fixture.getStadium().getName() + " holds " + capacity + ".");
        double price = Money.round2(fixture.getStadium().getTicketPrice() * category.getPriceMultiplier());
        // The stadium sets the base price; the category supplies the multiplier.
        Ticket t = new Ticket(nextTicketId(), fixture.getFixtureId(), spectator.getId(), category, price);
        tickets.add(t);
        spectator.addTicketId(t.getTicketId());
        return t;
    }

    public void cancelTicket(String ticketId) {
        for (Ticket t : tickets) {
            if (t.getTicketId().equals(ticketId)) {
                if (!t.isSold()) throw new ValidationException("Ticket " + ticketId + " is already cancelled.");
                t.setStatus(Ticket.Status.CANCELLED);
                return;
            }
        }
        throw new ValidationException("No ticket with ID " + ticketId + ".");
    }

    /** Used when reloading saved data. */
    public void restoreTicket(Ticket t) {
        tickets.add(t);
        Spectator s = spectators.get(t.getSpectatorId());
        if (s != null) s.addTicketId(t.getTicketId());
    }

    public int ticketsSold(String fixtureId) {
        int n = 0;
        // Ignore cancelled tickets so capacity can be reused.
        for (Ticket t : tickets) if (t.isSold() && t.getFixtureId().equals(fixtureId)) n++;
        return n;
    }

    public double ticketRevenue(String fixtureId) {
        double sum = 0;
        for (Ticket t : tickets) if (t.isSold() && t.getFixtureId().equals(fixtureId)) sum += t.getPrice();
        return Money.round2(sum);
    }

    public List<Ticket> getTickets() { return Collections.unmodifiableList(tickets); }

    private String nextTicketId() {
        int n = tickets.size() + 1;
        // Scan existing IDs because reloaded data may contain gaps.
        while (true) {
            String id = String.format("TK%04d", n);
            boolean used = false;
            for (Ticket t : tickets) if (t.getTicketId().equals(id)) used = true;
            if (!used) return id;
            n++;
        }
    }
}
