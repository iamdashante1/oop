package kpfl.model;

import kpfl.exception.LeagueException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registration approvals, official assignments and match-preparation decisions made by the
 * commissioner. Kept separate from LeagueCommissioner so it works (and is tested) without Person.
 * Officials and teams are referenced by id only.
 */
public class CommissionerRecords {
    public record Approval(String teamId, String approvedBy, LocalDate date) { }

    public record OfficialAssignment(String fixtureId, String officialId) { }

    public record PreparationDecision(String fixtureId, String decision, LocalDate date) { }

    private final List<Approval> approvals = new ArrayList<>();
    private final List<OfficialAssignment> assignments = new ArrayList<>();
    private final List<PreparationDecision> decisions = new ArrayList<>();

    public Approval approveTeam(String teamId, String approvedBy, LocalDate date) {
        check(teamId, approvedBy);
        if (isApproved(teamId)) {
            throw new LeagueException("Team " + teamId + " is already approved.");
        }
        Approval approval = new Approval(teamId, approvedBy, date);
        approvals.add(approval);
        return approval;
    }

    public boolean isApproved(String teamId) {
        return approvals.stream().anyMatch(a -> a.teamId().equals(teamId));
    }

    public OfficialAssignment assignOfficial(String fixtureId, String officialId) {
        check(fixtureId, officialId);
        OfficialAssignment assignment = new OfficialAssignment(fixtureId, officialId);
        if (assignments.contains(assignment)) {
            throw new LeagueException("Official " + officialId + " is already recorded for fixture " + fixtureId + ".");
        }
        assignments.add(assignment);
        return assignment;
    }

    public PreparationDecision recordDecision(String fixtureId, String decision, LocalDate date) {
        check(fixtureId, decision);
        PreparationDecision record = new PreparationDecision(fixtureId, decision, date);
        decisions.add(record);
        return record;
    }

    public List<Approval> getApprovals() { return Collections.unmodifiableList(approvals); }
    public List<OfficialAssignment> getAssignments() { return Collections.unmodifiableList(assignments); }
    public List<PreparationDecision> getDecisions() { return Collections.unmodifiableList(decisions); }

    /** Text must be non-empty and free of the '|' file separator and line breaks. */
    private static void check(String... values) {
        for (String v : values) {
            if (v == null || v.isBlank() || v.contains("|") || v.contains("\n") || v.contains("\r")) {
                throw new LeagueException("Values must be non-empty and must not contain '|' or line breaks.");
            }
        }
    }
}
