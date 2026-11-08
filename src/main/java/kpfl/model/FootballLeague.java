package kpfl.model;

import kpfl.exception.LeagueException;
import kpfl.integration.FixtureInfo;
import kpfl.integration.StadiumInfo;
import kpfl.integration.TeamInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The central class of the league.
 *
 * It keeps lists of teams, stadiums, fixtures and match results, lets us search them,
 * records results, and works out the league table (standings).
 *
 * It does NOT contain team/stadium/fixture logic. That belongs to Members 2 and 3.
 * It only talks to their classes through the small interfaces in kpfl.integration.
 */
public class FootballLeague {

    // Each Map is "ID -> object", so looking something up by ID is quick.
    // LinkedHashMap keeps items in the order they were added.
    private final Map<String, TeamInfo> teams = new LinkedHashMap<>();
    private final Map<String, StadiumInfo> stadiums = new LinkedHashMap<>();
    private final Map<String, FixtureInfo> fixtures = new LinkedHashMap<>();

    // fixture ID -> its result. A fixture can only have ONE entry here,
    // which is how we stop points being awarded twice.
    private final Map<String, MatchResult> results = new LinkedHashMap<>();

    // ------------------------------------------------------------------
    // Registering things
    // ------------------------------------------------------------------

    /** Adds a team to the league. Rejects missing teams and duplicate IDs or names. */
    public void registerTeam(TeamInfo team) {
        check(team != null, "Team is required.");
        check(!teams.containsKey(team.getTeamId()), "Team id already registered: " + team.getTeamId());
        check(findTeamByName(team.getTeamName()) == null, "Team name already registered: " + team.getTeamName());
        teams.put(team.getTeamId(), team);
    }

    /** Adds a stadium to the league. Rejects missing stadiums and duplicate IDs. */
    public void addStadium(StadiumInfo stadium) {
        check(stadium != null, "Stadium is required.");
        check(!stadiums.containsKey(stadium.getStadiumId()), "Stadium id already added: " + stadium.getStadiumId());
        stadiums.put(stadium.getStadiumId(), stadium);
    }

    /**
     * Adds a fixture to the league.
     * Both teams and the stadium must already be registered.
     * (Scheduling-conflict checks are Member 3's job, not ours.)
     */
    public void addFixture(FixtureInfo fixture) {
        check(fixture != null, "Fixture is required.");
        check(!fixtures.containsKey(fixture.getFixtureId()), "Fixture id already added: " + fixture.getFixtureId());
        check(teams.containsKey(fixture.getHomeTeamId()), "Home team is not registered: " + fixture.getHomeTeamId());
        check(teams.containsKey(fixture.getAwayTeamId()), "Away team is not registered: " + fixture.getAwayTeamId());
        check(!fixture.getHomeTeamId().equals(fixture.getAwayTeamId()), "A team cannot play itself.");
        check(stadiums.containsKey(fixture.getStadiumId()), "Stadium is not added: " + fixture.getStadiumId());
        fixtures.put(fixture.getFixtureId(), fixture);
    }

    // ------------------------------------------------------------------
    // Searching
    // ------------------------------------------------------------------

    /** Finds a team by ID. Returns null if there is no such team. */
    public TeamInfo findTeamById(String id) {
        return teams.get(id);
    }

    /** Finds a team by name, ignoring upper/lower case. Returns null if not found. */
    public TeamInfo findTeamByName(String name) {
        if (name == null) {
            return null;
        }
        // Look through every team until the name matches.
        for (TeamInfo team : teams.values()) {
            if (team.getTeamName().equalsIgnoreCase(name.trim())) {
                return team;
            }
        }
        return null;
    }

    /** Finds a stadium by ID. Returns null if not found. */
    public StadiumInfo findStadiumById(String id) {
        return stadiums.get(id);
    }

    /** Finds a fixture by ID. Returns null if not found. */
    public FixtureInfo findFixtureById(String id) {
        return fixtures.get(id);
    }

    /** Finds the result for a fixture. Returns null if the match has no result yet. */
    public MatchResult findResult(String fixtureId) {
        return results.get(fixtureId);
    }

    // ------------------------------------------------------------------
    // Getting the whole lists
    // The "unmodifiable" wrapper stops other classes from adding/removing items by accident.
    // ------------------------------------------------------------------

    public Collection<TeamInfo> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public Collection<StadiumInfo> getStadiums() {
        return Collections.unmodifiableCollection(stadiums.values());
    }

    public Collection<FixtureInfo> getFixtures() {
        return Collections.unmodifiableCollection(fixtures.values());
    }

    public Collection<MatchResult> getResults() {
        return Collections.unmodifiableCollection(results.values());
    }

    // ------------------------------------------------------------------
    // Recording results
    // ------------------------------------------------------------------

    /**
     * Records the score of a fixture and marks the fixture as completed.
     *
     * Steps:
     *   1. the fixture must exist
     *   2. it must not already have a result (so points are never given twice)
     *   3. build the MatchResult (this throws if a score is negative)
     *   4. only AFTER the result is valid do we store it and mark the fixture completed
     */
    public MatchResult recordResult(String fixtureId, int homeScore, int awayScore) {
        FixtureInfo fixture = fixtures.get(fixtureId);

        check(fixture != null, "No such fixture: " + fixtureId);
        check(!results.containsKey(fixtureId) && !fixture.isCompleted(),
                "Fixture " + fixtureId + " already has a result.");

        // If the scores are bad, this line throws and nothing below runs.
        MatchResult result = new MatchResult(fixtureId, fixture.getHomeTeamId(), fixture.getAwayTeamId(),
                homeScore, awayScore, fixture.getDateTime().toLocalDate());

        results.put(fixtureId, result);
        fixture.markCompleted();
        return result;
    }

    /**
     * Puts a result that was loaded from a file back into the league.
     * The fixture does not have to exist yet, because Member 3's own storage restores fixtures.
     * A second result for the same fixture is still rejected.
     */
    public void restoreResult(MatchResult result) {
        check(result != null, "Result is required.");
        check(!results.containsKey(result.getFixtureId()),
                "Fixture " + result.getFixtureId() + " already has a result.");
        results.put(result.getFixtureId(), result);
    }

    // ------------------------------------------------------------------
    // Standings
    // ------------------------------------------------------------------

    /**
     * Builds the league table from the stored results.
     * It is worked out fresh each time, so it always matches the real data.
     *
     * Order: most points first. If tied, better goal difference, then more goals scored,
     * then team name (A to Z) so the order is always the same.
     */
    public List<StandingsEntry> calculateStandings() {

        // Step 1: give every registered team an empty row (so teams with no matches still appear).
        Map<String, StandingsEntry> table = new LinkedHashMap<>();
        for (TeamInfo team : teams.values()) {
            table.put(team.getTeamId(), new StandingsEntry(team.getTeamId(), team.getTeamName()));
        }

        // Step 2: add every result to both teams' rows.
        for (MatchResult result : results.values()) {
            StandingsEntry homeRow = table.get(result.getHomeTeamId());
            StandingsEntry awayRow = table.get(result.getAwayTeamId());

            // A row can be missing if the team is not registered, so check for null first.
            if (homeRow != null) {
                homeRow.addMatch(result.getHomeScore(), result.getAwayScore(), result.getHomePoints());
            }
            if (awayRow != null) {
                awayRow.addMatch(result.getAwayScore(), result.getHomeScore(), result.getAwayPoints());
            }
        }

        // Step 3: sort the rows into table order.
        List<StandingsEntry> rows = new ArrayList<>(table.values());
        Collections.sort(rows, new Comparator<StandingsEntry>() {
            @Override
            public int compare(StandingsEntry a, StandingsEntry b) {
                // Compare b to a so that BIGGER numbers come first.
                if (a.getPoints() != b.getPoints()) {
                    return Integer.compare(b.getPoints(), a.getPoints());
                }
                if (a.getGoalDifference() != b.getGoalDifference()) {
                    return Integer.compare(b.getGoalDifference(), a.getGoalDifference());
                }
                if (a.getGoalsFor() != b.getGoalsFor()) {
                    return Integer.compare(b.getGoalsFor(), a.getGoalsFor());
                }
                // Still tied: sort by name A to Z.
                return a.getTeamName().compareTo(b.getTeamName());
            }
        });
        return rows;
    }

    // Small helper: throws our exception with the message if the condition is false.
    private void check(boolean condition, String message) {
        if (!condition) {
            throw new LeagueException(message);
        }
    }
}
