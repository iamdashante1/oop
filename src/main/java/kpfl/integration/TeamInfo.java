package kpfl.integration;

/**
 * What the league needs to know about a team.
 *
 * Member 2's FootballTeam class must "implement" this interface,
 * which just means FootballTeam must have these two methods.
 * That way Member 1's code never has to change when Member 2 adds more to FootballTeam.
 */
public interface TeamInfo {

    // The team's unique ID, e.g. "T1". The ID never changes, so we use it to link things together.
    String getTeamId();

    // The team's display name, e.g. "Alpha FC".
    String getTeamName();
}
