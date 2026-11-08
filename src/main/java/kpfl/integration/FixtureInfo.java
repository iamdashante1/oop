package kpfl.integration;

import java.time.LocalDateTime;

/** Contract Member 3's Fixture must implement. */
public interface FixtureInfo {
    String getFixtureId();

    String getHomeTeamId();

    String getAwayTeamId();

    String getStadiumId();

    LocalDateTime getDateTime();

    boolean isCompleted();

    /** Called by FootballLeague once a valid result has been recorded. */
    void markCompleted();
}
