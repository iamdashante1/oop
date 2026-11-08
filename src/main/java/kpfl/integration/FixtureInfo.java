package kpfl.integration;

import java.time.LocalDateTime;

/**
 * What the league needs to know about a fixture (a scheduled match).
 *
 * Member 3's Fixture class must "implement" this interface
 * (it must have all of these methods).
 */
public interface FixtureInfo {

    // The fixture's unique ID, e.g. "F1".
    String getFixtureId();

    // The ID of the home team (matches TeamInfo.getTeamId()).
    String getHomeTeamId();

    // The ID of the away team (matches TeamInfo.getTeamId()).
    String getAwayTeamId();

    // The ID of the stadium where the match is played (matches StadiumInfo.getStadiumId()).
    String getStadiumId();

    // The date AND time of the match.
    LocalDateTime getDateTime();

    // true once a result has been recorded for this fixture.
    boolean isCompleted();

    // The league calls this after it has saved a valid result, so the fixture knows it is finished.
    void markCompleted();
}
