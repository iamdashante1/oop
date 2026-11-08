package kpfl.model;

import kpfl.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The league official who approves team registrations, suspends teams, and keeps a log of
 * decisions (approvals, official assignments, match-preparation notes).
 */
public class LeagueCommissioner extends Person {
    private final List<String> decisions = new ArrayList<>();

    public LeagueCommissioner(String id, String name, int age, String gender) {
        super(id, name, age, gender);
    }

    @Override
    public String getRole() {
        return "League Commissioner";
    }

    public void approveTeam(FootballTeam team) {
        if (team == null) throw new ValidationException("Team not found.");
        if (team.getStatus().equals("ACTIVE"))
            throw new ValidationException(team.getName() + " is already approved.");
        team.setStatus("ACTIVE");
        // Keep a readable decision log alongside the change to team status.
        recordDecision("Approved team " + team.getRegistrationNo() + " (" + team.getName() + ")");
    }

    public void suspendTeam(FootballTeam team) {
        if (team == null) throw new ValidationException("Team not found.");
        team.setStatus("SUSPENDED");
        recordDecision("Suspended team " + team.getRegistrationNo() + " (" + team.getName() + ")");
    }

    public void recordDecision(String text) {
        if (text == null || text.trim().isEmpty()) throw new ValidationException("A decision note cannot be empty.");
        decisions.add(text.trim());
    }

    public List<String> getDecisions() { return Collections.unmodifiableList(decisions); }
}
