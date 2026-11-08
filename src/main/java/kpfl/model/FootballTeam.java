package kpfl.model;

import kpfl.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/** A registered team with its players and coaches. */
public class FootballTeam {
    private final String registrationNo;
    private String name;
    private String parish;
    // A new team waits for commissioner approval before it can play.
    private String status = "PENDING";
    private final List<Player> players = new ArrayList<>();
    private final List<Coach> coaches = new ArrayList<>();

    public FootballTeam(String registrationNo, String name, String parish) {
        if (registrationNo == null || registrationNo.trim().isEmpty())
            throw new ValidationException("Team registration number is required.");
        this.registrationNo = registrationNo.trim();
        setName(name);
        setParish(parish);
    }

    public String getRegistrationNo() { return registrationNo; }
    public String getName() { return name; }
    public String getParish() { return parish; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        if (status == null) throw new ValidationException("Team status is required.");
        status = status.trim().toUpperCase();
        // The commissioner may approve or suspend a team; new teams stay pending.
        if (!status.equals("PENDING") && !status.equals("ACTIVE") && !status.equals("SUSPENDED"))
            throw new ValidationException("Team status must be PENDING, ACTIVE or SUSPENDED.");
        this.status = status;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Team name is required.");
        this.name = name.trim();
    }

    public void setParish(String parish) {
        if (parish == null || parish.trim().isEmpty())
            throw new ValidationException("Parish is required.");
        this.parish = parish.trim();
    }

    /** Only approved (ACTIVE) teams may be scheduled. */
    public boolean isEligibleForFixture() { return status.equals("ACTIVE"); }

    // Squad size is the number of players. Team status records commissioner approval.

    public void addPlayer(Player p) {
        if (p == null) throw new ValidationException("Player is required.");
        // Reject an existing assignment before changing either team's list.
        if (p.getTeamRegNo() != null)
            throw new ValidationException("Player " + p.getId() + " is already assigned to a team.");
        // IDs identify people; shirt numbers must also be unique within this squad.
        for (Player q : players) {
            if (q.getId().equals(p.getId())) throw new ValidationException("Player " + p.getId() + " is already in this team.");
            if (q.getJerseyNumber() == p.getJerseyNumber())
                throw new ValidationException("Jersey number " + p.getJerseyNumber() + " is already taken in " + name + ".");
        }
        for (Coach c : coaches)
            if (c.getId().equals(p.getId()))
                throw new ValidationException("Person ID " + p.getId() + " is already used in this team.");
        // Update the list and the player's team reference together.
        players.add(p);
        p.setTeamRegNo(registrationNo);
    }

    public boolean removePlayer(String playerId) {
        // Clearing the assignment allows this player to join another team later.
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.getId().equals(playerId)) {
                players.remove(i);
                p.setTeamRegNo(null);
                return true;
            }
        }
        return false;
    }

    public void addCoach(Coach c) {
        if (c == null) throw new ValidationException("Coach is required.");
        // The same coach cannot appear on two teams' coach lists.
        if (c.getTeamRegNo() != null)
            throw new ValidationException("Coach " + c.getId() + " is already assigned to a team.");
        for (Coach k : coaches)
            if (k.getId().equals(c.getId())) throw new ValidationException("Coach " + c.getId() + " is already in this team.");
        for (Player p : players)
            if (p.getId().equals(c.getId()))
                throw new ValidationException("Person ID " + c.getId() + " is already used in this team.");
        coaches.add(c);
        c.setTeamRegNo(registrationNo);
    }

    public boolean removeCoach(String coachId) {
        for (int i = 0; i < coaches.size(); i++) {
            Coach c = coaches.get(i);
            if (c.getId().equals(coachId)) {
                coaches.remove(i);
                c.setTeamRegNo(null);
                return true;
            }
        }
        return false;
    }

    // Return copies so callers cannot change the team's lists by accident.
    public List<Player> getPlayers() { return new ArrayList<>(players); }
    public List<Coach> getCoaches() { return new ArrayList<>(coaches); }
    public int getSquadSize() { return players.size(); }

    // League points are available through FootballLeague.getStandings(). The league
    // calculates them from match results, so this class does not store another total.

    @Override
    public String toString() {
        return registrationNo + " | " + name + " (" + parish + ") [" + status + "] squad " + players.size()
                + ", coaches " + coaches.size();
    }
}
