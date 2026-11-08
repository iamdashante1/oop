package kpfl.model;

import kpfl.exception.ValidationException;

/** A team coach. */
public class Coach extends Person {
    private String qualification;
    private String coachingRole;
    private String teamRegNo;

    public Coach(String id, String name, int age, String gender, String qualification, String coachingRole) {
        super(id, name, age, gender);
        if (age < 18) throw new ValidationException("Coaches must be at least 18 years old.");
        if (coachingRole == null || coachingRole.trim().isEmpty()) throw new ValidationException("Coaching role is required.");
        this.qualification = qualification == null ? "" : qualification.trim();
        this.coachingRole = coachingRole.trim();
    }

    @Override
    public String getRole() { return "Coach"; }

    public String getQualification() { return qualification; }
    public String getCoachingRole() { return coachingRole; }
    public String getTeamRegNo() { return teamRegNo; }

    void setTeamRegNo(String teamRegNo) { this.teamRegNo = teamRegNo; }

    @Override
    public String toString() {
        return getName() + " (" + coachingRole + ", " + qualification + ")";
    }
}
