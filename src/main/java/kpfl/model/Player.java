package kpfl.model;

import kpfl.exception.ValidationException;

/** A registered player. Minimum age is an assumption until the lecturer confirms the real rule. */
public class Player extends Person {
    public static final int MIN_AGE = 16;

    public enum PlayerStatus { ACTIVE, INJURED, SUSPENDED }

    private String position;
    private int jerseyNumber;
    private int goals;
    private PlayerStatus playerStatus = PlayerStatus.ACTIVE;
    private String teamRegNo;

    public Player(String id, String name, int age, String gender, String position, int jerseyNumber) {
        super(id, name, age, gender);
        if (age < MIN_AGE) throw new ValidationException("Players must be at least " + MIN_AGE + " years old.");
        if (position == null || position.trim().isEmpty()) throw new ValidationException("Playing position is required.");
        if (jerseyNumber < 1 || jerseyNumber > 99) throw new ValidationException("Jersey number must be 1-99.");
        this.position = position.trim();
        this.jerseyNumber = jerseyNumber;
    }

    @Override
    public String getRole() { return "Player"; }

    public String getPosition() { return position; }
    public int getJerseyNumber() { return jerseyNumber; }
    public int getGoals() { return goals; }
    public PlayerStatus getPlayerStatus() { return playerStatus; }
    public String getTeamRegNo() { return teamRegNo; }

    void setTeamRegNo(String teamRegNo) { this.teamRegNo = teamRegNo; }
    public void setPlayerStatus(PlayerStatus status) { this.playerStatus = status; }

    public void setGoals(int goals) {
        if (goals < 0) throw new ValidationException("Goals cannot be negative.");
        this.goals = goals;
    }

    @Override
    public String toString() {
        return "#" + jerseyNumber + " " + getName() + " (" + position + ", " + playerStatus + ", " + goals + " goals)";
    }
}
