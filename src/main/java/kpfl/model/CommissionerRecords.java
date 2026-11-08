package kpfl.model;

import kpfl.exception.LeagueException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The notes the commissioner keeps:
 *   - which teams have been approved
 *   - which official was assigned to which fixture
 *   - decisions made while preparing a match
 *
 * Teams, fixtures and officials are stored by ID only, so this class does not depend on
 * the other members' classes.
 *
 * Text values cannot contain the "|" character or line breaks, because the file
 * format uses "|" to separate fields and one line per record.
 */
public class CommissionerRecords {

    // ---- Three small record types (plain classes with getters) ----

    /** One approved team. */
    public static class Approval {
        private final String teamId;
        private final String approvedBy;   // ID of the commissioner who approved
        private final LocalDate date;

        public Approval(String teamId, String approvedBy, LocalDate date) {
            this.teamId = teamId;
            this.approvedBy = approvedBy;
            this.date = date;
        }

        public String getTeamId() { return teamId; }
        public String getApprovedBy() { return approvedBy; }
        public LocalDate getDate() { return date; }
    }

    /** One official assigned to one fixture. */
    public static class OfficialAssignment {
        private final String fixtureId;
        private final String officialId;

        public OfficialAssignment(String fixtureId, String officialId) {
            this.fixtureId = fixtureId;
            this.officialId = officialId;
        }

        public String getFixtureId() { return fixtureId; }
        public String getOfficialId() { return officialId; }
    }

    /** One decision made while preparing a match, e.g. "Kick-off moved to 4pm". */
    public static class PreparationDecision {
        private final String fixtureId;
        private final String decision;
        private final LocalDate date;

        public PreparationDecision(String fixtureId, String decision, LocalDate date) {
            this.fixtureId = fixtureId;
            this.decision = decision;
            this.date = date;
        }

        public String getFixtureId() { return fixtureId; }
        public String getDecision() { return decision; }
        public LocalDate getDate() { return date; }
    }

    // The three lists that hold everything.
    private final List<Approval> approvals = new ArrayList<>();
    private final List<OfficialAssignment> assignments = new ArrayList<>();
    private final List<PreparationDecision> decisions = new ArrayList<>();

    // ------------------------------------------------------------------
    // Team approvals
    // ------------------------------------------------------------------

    /** Approves a team. A team can only be approved once. */
    public Approval approveTeam(String teamId, String approvedBy, LocalDate date) {
        checkText(teamId);
        checkText(approvedBy);
        if (isApproved(teamId)) {
            throw new LeagueException("Team " + teamId + " is already approved.");
        }
        Approval approval = new Approval(teamId, approvedBy, date);
        approvals.add(approval);
        return approval;
    }

    /** true if this team has been approved. */
    public boolean isApproved(String teamId) {
        for (Approval approval : approvals) {
            if (approval.getTeamId().equals(teamId)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Official assignments
    // ------------------------------------------------------------------

    /** Records that an official is assigned to a fixture. The same pair cannot be added twice. */
    public OfficialAssignment assignOfficial(String fixtureId, String officialId) {
        checkText(fixtureId);
        checkText(officialId);
        for (OfficialAssignment existing : assignments) {
            if (existing.getFixtureId().equals(fixtureId) && existing.getOfficialId().equals(officialId)) {
                throw new LeagueException("Official " + officialId + " is already recorded for fixture " + fixtureId + ".");
            }
        }
        OfficialAssignment assignment = new OfficialAssignment(fixtureId, officialId);
        assignments.add(assignment);
        return assignment;
    }

    // ------------------------------------------------------------------
    // Match preparation decisions
    // ------------------------------------------------------------------

    /** Records a decision made while preparing a match. */
    public PreparationDecision recordDecision(String fixtureId, String decision, LocalDate date) {
        checkText(fixtureId);
        checkText(decision);
        PreparationDecision record = new PreparationDecision(fixtureId, decision, date);
        decisions.add(record);
        return record;
    }

    // ------------------------------------------------------------------
    // Read-only access to the lists (used when saving to a file)
    // ------------------------------------------------------------------

    public List<Approval> getApprovals() {
        return Collections.unmodifiableList(approvals);
    }

    public List<OfficialAssignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public List<PreparationDecision> getDecisions() {
        return Collections.unmodifiableList(decisions);
    }

    // Rejects empty text, and text containing "|" or a line break (these would break the file format).
    private void checkText(String text) {
        if (text == null || text.trim().isEmpty()
                || text.contains("|") || text.contains("\n") || text.contains("\r")) {
            throw new LeagueException("Values must be non-empty and must not contain '|' or line breaks.");
        }
    }
}
