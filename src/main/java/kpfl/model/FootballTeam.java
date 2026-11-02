package kpfl.model;

import kpfl.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A football team with its squad. Contract used by other modules:
 * getRegistrationNo(), getName(), getStatus(), isEligibleForFixture().
 */
public class FootballTeam {
    public enum Status { PENDING, ACTIVE, SUSPENDED }

    private final String registrationNo;
    private String name;
    private String parish;
    private Status status = Status.PENDING;
    private final List<Player> players = new ArrayList<>();
    private final List<Coach> coaches = new ArrayList<>();

    public FootballTeam(String registrationNo, String name, String parish) {
        if (registrationNo == null || registrationNo.trim().isEmpty())
            throw new ValidationException("Team registration number is required.");
        if (name == null || name.trim().isEmpty())
            throw new ValidationException("Team name is required.");
        this.registrationNo = registrationNo.trim();
        this.name = name.trim();
        this.parish = parish == null ? "" : parish.trim();
    }

    public String getRegistrationNo() { return registrationNo; }
    public String getName() { return name; }
    public String getParish() { return parish; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Team name is required.");
        this.name = name.trim();
    }

    public void setParish(String parish) { this.parish = parish == null ? "" : parish.trim(); }

    /** Only approved (ACTIVE) teams may be scheduled. */
    public boolean isEligibleForFixture() { return status == Status.ACTIVE; }

    // ---- squad ----

    public void addPlayer(Player p) {
        if (p == null) throw new ValidationException("Player is required.");
        for (Player q : players) {
            if (q.getId().equals(p.getId())) throw new ValidationException("Player " + p.getId() + " is already in this team.");
            if (q.getJerseyNumber() == p.getJerseyNumber())
                throw new ValidationException("Jersey number " + p.getJerseyNumber() + " is already taken in " + name + ".");
        }
        players.add(p);
        p.setTeamRegNo(registrationNo);
    }

    public boolean removePlayer(String playerId) {
        for (Player p : players) {
            if (p.getId().equals(playerId)) {
                players.remove(p);
                p.setTeamRegNo(null);
                return true;
            }
        }
        return false;
    }

    public void addCoach(Coach c) {
        if (c == null) throw new ValidationException("Coach is required.");
        for (Coach k : coaches)
            if (k.getId().equals(c.getId())) throw new ValidationException("Coach " + c.getId() + " is already in this team.");
        coaches.add(c);
        c.setTeamRegNo(registrationNo);
    }

    public boolean removeCoach(String coachId) {
        for (Coach c : coaches) {
            if (c.getId().equals(coachId)) {
                coaches.remove(c);
                c.setTeamRegNo(null);
                return true;
            }
        }
        return false;
    }

    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<Coach> getCoaches() { return Collections.unmodifiableList(coaches); }
    public int getSquadSize() { return players.size(); }

    @Override
    public String toString() {
        return registrationNo + " | " + name + " (" + parish + ") [" + status + "] squad " + players.size()
                + ", coaches " + coaches.size();
    }
}
