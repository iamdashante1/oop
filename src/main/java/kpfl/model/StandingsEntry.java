package kpfl.model;

/** One row of the league table, derived from completed fixtures. */
public class StandingsEntry {
    private final FootballTeam team;
    private int played, won, drawn, lost, goalsFor, goalsAgainst, points;

    public StandingsEntry(FootballTeam team) {
        this.team = team;
    }

    /** Applies one match from this team's point of view. */
    public void apply(int scored, int conceded, int pointsEarned) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        points += pointsEarned;
        if (pointsEarned == MatchResult.WIN_POINTS) won++;
        else if (pointsEarned == MatchResult.DRAW_POINTS) drawn++;
        else lost++;
    }

    public FootballTeam getTeam() { return team; }
    public int getPlayed() { return played; }
    public int getWon() { return won; }
    public int getDrawn() { return drawn; }
    public int getLost() { return lost; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getGoalDifference() { return goalsFor - goalsAgainst; }
    public int getPoints() { return points; }
}
