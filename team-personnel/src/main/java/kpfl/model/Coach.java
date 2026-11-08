package kpfl.model;

import kpfl.exception.ValidationException;

/** A coach and their current team assignment. */
public class Coach extends Person {
    private String qualification;
    private String coachingRole;
    private String teamRegNo;

    public Coach(String id, String name, int age, String gender, String qualification, String coachingRole) {
        super(id, name, age, gender);
        if (age < 18) throw new ValidationException("Coaches must be at least 18 years old.");
        setQualification(qualification);
        setCoachingRole(coachingRole);
    }

    @Override
    public String getRole() { return "Coach"; }

    public String getQualification() { return qualification; }
    public String getCoachingRole() { return coachingRole; }
    public String getTeamRegNo() { return teamRegNo; }

    @Override
    public void setAge(int age) {
        // Keep the existing adult-coach rule when a coach is created or edited.
        if (age < 18) throw new ValidationException("Coaches must be at least 18 years old.");
        super.setAge(age);
    }

    public void setQualification(String qualification) {
        // A blank qualification means it has not been recorded yet.
        this.qualification = qualification == null ? "" : qualification.trim();
    }

    public void setCoachingRole(String coachingRole) {
        if (coachingRole == null || coachingRole.trim().isEmpty())
            throw new ValidationException("Coaching role is required.");
        this.coachingRole = coachingRole.trim();
    }

    // Only FootballTeam changes an assignment when a coach joins or leaves.
    void setTeamRegNo(String teamRegNo) { this.teamRegNo = teamRegNo; }

    @Override
    public String toString() {
        return getName() + " (" + coachingRole + ", " + qualification + ")";
    }
}
