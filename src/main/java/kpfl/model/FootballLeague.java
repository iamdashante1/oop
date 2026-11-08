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
 * Central league class. Holds teams, stadiums, fixtures and results, and calculates standings.
 * It works through the integration interfaces and does not contain team/stadium/fixture logic.
 */
public class FootballLeague {
    private final Map<String, TeamInfo> teams = new LinkedHashMap<>();
    private final Map<String, StadiumInfo> stadiums = new LinkedHashMap<>();
    private final Map<String, FixtureInfo> fixtures = new LinkedHashMap<>();
    private final Map<String, MatchResult> results = new LinkedHashMap<>();

    public void registerTeam(TeamInfo team) {
        require(team != null, "Team is required.");
        require(!teams.containsKey(team.getTeamId()), "Team id already registered: " + team.getTeamId());
        require(findTeamByName(team.getTeamName()) == null, "Team name already registered: " + team.getTeamName());
        teams.put(team.getTeamId(), team);
    }

    public void addStadium(StadiumInfo stadium) {
        require(stadium != null, "Stadium is required.");
        require(!stadiums.containsKey(stadium.getStadiumId()), "Stadium id already added: " + stadium.getStadiumId());
        stadiums.put(stadium.getStadiumId(), stadium);
    }

    public void addFixture(FixtureInfo fixture) {
        require(fixture != null, "Fixture is required.");
        require(!fixtures.containsKey(fixture.getFixtureId()), "Fixture id already added: " + fixture.getFixtureId());
        require(teams.containsKey(fixture.getHomeTeamId()), "Home team is not registered: " + fixture.getHomeTeamId());
        require(teams.containsKey(fixture.getAwayTeamId()), "Away team is not registered: " + fixture.getAwayTeamId());
        require(!fixture.getHomeTeamId().equals(fixture.getAwayTeamId()), "A team cannot play itself.");
        require(stadiums.containsKey(fixture.getStadiumId()), "Stadium is not added: " + fixture.getStadiumId());
        fixtures.put(fixture.getFixtureId(), fixture);
    }

    public TeamInfo findTeamById(String id) {
        return teams.get(id);
    }

    /** Case-insensitive name search; null if not found. */
    public TeamInfo findTeamByName(String name) {
        if (name == null) return null;
        for (TeamInfo team : teams.values()) {
            if (team.getTeamName().equalsIgnoreCase(name.trim())) return team;
        }
        return null;
    }

    public StadiumInfo findStadiumById(String id) {
        return stadiums.get(id);
    }

    public FixtureInfo findFixtureById(String id) {
        return fixtures.get(id);
    }

    public Collection<TeamInfo> getTeams() { return Collections.unmodifiableCollection(teams.values()); }
    public Collection<StadiumInfo> getStadiums() { return Collections.unmodifiableCollection(stadiums.values()); }
    public Collection<FixtureInfo> getFixtures() { return Collections.unmodifiableCollection(fixtures.values()); }
    public Collection<MatchResult> getResults() { return Collections.unmodifiableCollection(results.values()); }

    public MatchResult findResult(String fixtureId) {
        return results.get(fixtureId);
    }

    /**
     * Records the score for a fixture and marks the fixture completed.
     * A fixture can only ever receive one result, so points are never awarded twice.
     */
    public MatchResult recordResult(String fixtureId, int homeScore, int awayScore) {
        FixtureInfo fixture = fixtures.get(fixtureId);
        require(fixture != null, "No such fixture: " + fixtureId);
        require(!results.containsKey(fixtureId) && !fixture.isCompleted(),
                "Fixture " + fixtureId + " already has a result.");
        MatchResult result = new MatchResult(fixtureId, fixture.getHomeTeamId(), fixture.getAwayTeamId(),
                homeScore, awayScore, fixture.getDateTime().toLocalDate());
        results.put(fixtureId, result);
        fixture.markCompleted();
        return result;
    }

    /**
     * Re-adds a result loaded from file. The fixture itself is restored by Member 3's storage,
     * so it is not required to exist yet. Duplicates are rejected.
     */
    public void restoreResult(MatchResult result) {
        require(result != null, "Result is required.");
        require(!results.containsKey(result.getFixtureId()),
                "Fixture " + result.getFixtureId() + " already has a result.");
        results.put(result.getFixtureId(), result);
    }

    /** Builds the table from recorded results; sorted by points, goal difference, goals scored, then name. */
    public List<StandingsEntry> calculateStandings() {
        Map<String, StandingsEntry> table = new LinkedHashMap<>();
        for (TeamInfo team : teams.values()) {
            table.put(team.getTeamId(), new StandingsEntry(team.getTeamId(), team.getTeamName()));
        }
        for (MatchResult r : results.values()) {
            StandingsEntry home = table.get(r.getHomeTeamId());
            StandingsEntry away = table.get(r.getAwayTeamId());
            if (home != null) home.addMatch(r.getHomeScore(), r.getAwayScore(), r.getHomePoints());
            if (away != null) away.addMatch(r.getAwayScore(), r.getHomeScore(), r.getAwayPoints());
        }
        List<StandingsEntry> sorted = new ArrayList<>(table.values());
        sorted.sort(Comparator.comparingInt(StandingsEntry::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(StandingsEntry::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(StandingsEntry::getGoalsFor).reversed())
                .thenComparing(StandingsEntry::getTeamName));
        return sorted;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new LeagueException(message);
    }
}
