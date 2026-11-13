package kpfl.model;

import kpfl.exception.ValidationException;

/** The recorded outcome of one fixture. Immutable; goals can never be negative. */
public class MatchResult {
    public enum Outcome { HOME_WIN, AWAY_WIN, DRAW }

    public static final int WIN_POINTS = 3;
    public static final int DRAW_POINTS = 1;
    public static final int LOSS_POINTS = 0;

    private final String fixtureId;
    private final int homeScore;
    private final int awayScore;

    public MatchResult(String fixtureId, int homeScore, int awayScore) {
        if (fixtureId == null || fixtureId.trim().isEmpty())
            throw new ValidationException("A result must be linked to a fixture.");
        if (homeScore < 0 || awayScore < 0)
            throw new ValidationException("Scores cannot be negative.");
        this.fixtureId = fixtureId.trim();
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public String getFixtureId() { return fixtureId; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }

    public Outcome getOutcome() {
        if (homeScore > awayScore) return Outcome.HOME_WIN;
        if (homeScore < awayScore) return Outcome.AWAY_WIN;
        return Outcome.DRAW;
    }

    public int homePoints() {
        // Translate the outcome into the league's 3/1/0 points rule.
        switch (getOutcome()) {
            case HOME_WIN: return WIN_POINTS;
            case DRAW: return DRAW_POINTS;
            default: return LOSS_POINTS;
        }
    }

    public int awayPoints() {
        switch (getOutcome()) {
            case AWAY_WIN: return WIN_POINTS;
            case DRAW: return DRAW_POINTS;
            default: return LOSS_POINTS;
        }
    }

    @Override
    public String toString() {
        return homeScore + " - " + awayScore;
    }
}
