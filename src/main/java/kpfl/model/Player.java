package kpfl.model;

import kpfl.exception.ValidationException;

/** One football player and their playing record. */
public class Player extends Person {

    public static final int MIN_AGE = 16;

    private String position;
    private int jerseyNumber;
    private int goals;
    // A new player is active until their status is changed.
    private String playerStatus = "ACTIVE";
    private String teamRegNo;

    //primary constructor for Player
    public Player(String id, String name, int age, String gender, String position, int jerseyNumber) {
        super(id, name, age, gender);
        if (age < MIN_AGE)
            throw new ValidationException("Players must be at least " + MIN_AGE + " years old.");
        // These setters reject a missing position or an invalid shirt number.
        setPosition(position);
        setJerseyNumber(jerseyNumber);
    }

    @Override
    public String getRole() { return "Player"; }

    public String getPosition() { return position; }
    public int getJerseyNumber() { return jerseyNumber; }
    public int getGoals() { return goals; }
    public String getPlayerStatus() { return playerStatus; }
    public String getTeamRegNo() { return teamRegNo; }

    @Override
    public void setAge(int age) {
        // A player has a stricter age rule than a general Person.
        if (age < MIN_AGE)
            throw new ValidationException("Players must be at least " + MIN_AGE + " years old.");
        super.setAge(age);
    }

    public void setPosition(String position) {
        if (position == null || position.trim().isEmpty())
            throw new ValidationException("Playing position is required.");
        this.position = position.trim();
    }

    public void setJerseyNumber(int jerseyNumber) {
        if (jerseyNumber < 1 || jerseyNumber > 99)
            throw new ValidationException("Jersey number must be 1-99.");
        // Remove the player from the squad before changing the number, then add them back.
        // This lets FootballTeam check that the new number is not already taken.
        if (teamRegNo != null && this.jerseyNumber != jerseyNumber)
            throw new ValidationException("Remove the player from the team before changing their jersey number.");
        this.jerseyNumber = jerseyNumber;
    }

    public void setGoals(int goals) {
        // Goals start at zero and can only be set to a nonnegative total.
        if (goals < 0) throw new ValidationException("Goals cannot be negative.");
        this.goals = goals;
    }

    public void setPlayerStatus(String status) {
        if (status == null) throw new ValidationException("Player status is required.");
        status = status.trim().toUpperCase();
        // Only these three words describe a valid playing status.
        if (!status.equals("ACTIVE") && !status.equals("INJURED") && !status.equals("SUSPENDED"))
            throw new ValidationException("Player status must be ACTIVE, INJURED or SUSPENDED.");
        this.playerStatus = status;
    }

    // Only FootballTeam changes an assignment when a player joins or leaves.
    void setTeamRegNo(String teamRegNo) { this.teamRegNo = teamRegNo; }

    @Override
    public String toString() {
        return "#" + jerseyNumber + " " + getName() + " (" + position + ", " + playerStatus + ", " + goals + " goals)";
    }
}
