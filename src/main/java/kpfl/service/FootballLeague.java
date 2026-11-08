package kpfl.service;

import kpfl.exception.ValidationException;
import kpfl.model.Coach;
import kpfl.model.Fixture;
import kpfl.model.FootballTeam;
import kpfl.model.LeagueCommissioner;
import kpfl.model.MatchOfficial;
import kpfl.model.MatchResult;
import kpfl.model.Player;
import kpfl.model.Stadium;
import kpfl.model.StandingsEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the league's teams, stadiums and fixtures and coordinates operations between them.
 * Standings are always derived from completed fixtures, so a fixture can never award points twice.
 */
public class FootballLeague {
    private final Map<String, FootballTeam> teams = new LinkedHashMap<>();
    private final Map<String, Stadium> stadiums = new LinkedHashMap<>();
    private final Map<String, Fixture> fixtures = new LinkedHashMap<>();
    private final Map<String, MatchOfficial> officials = new LinkedHashMap<>();
    private LeagueCommissioner commissioner;

    // ---- commissioner ----

    public LeagueCommissioner getCommissioner() { return commissioner; }
    public void setCommissioner(LeagueCommissioner commissioner) { this.commissioner = commissioner; }

    // ---- teams ----

    /** Registers a team as PENDING; the commissioner must approve it before it can be scheduled. */
    public void registerTeam(FootballTeam team) {
        if (team == null) throw new ValidationException("Team is required.");
        if (teams.containsKey(team.getRegistrationNo()))
            throw new ValidationException("Team registration number " + team.getRegistrationNo() + " already exists.");
        teams.put(team.getRegistrationNo(), team);
    }

    public void approveTeam(String registrationNo) {
        if (commissioner == null) throw new ValidationException("No league commissioner has been set.");
        commissioner.approveTeam(findTeam(registrationNo));
    }

    public void suspendTeam(String registrationNo) {
        if (commissioner == null) throw new ValidationException("No league commissioner has been set.");
        commissioner.suspendTeam(findTeam(registrationNo));
    }

    public FootballTeam findTeam(String registrationNo) {
        FootballTeam team = registrationNo == null ? null : teams.get(registrationNo.trim());
        if (team == null) throw new ValidationException("No team with registration number " + registrationNo + ".");
        return team;
    }

    /** Case-insensitive search on registration number, name or parish. */
    public List<FootballTeam> searchTeams(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        List<FootballTeam> found = new ArrayList<>();
        for (FootballTeam t : teams.values()) {
            if (t.getRegistrationNo().toLowerCase().contains(q) || t.getName().toLowerCase().contains(q)
                    || t.getParish().toLowerCase().contains(q)) {
                found.add(t);
            }
        }
        return found;
    }

    public List<FootballTeam> getTeams() { return new ArrayList<>(teams.values()); }

    // ---- players and coaches (person IDs are unique across the league) ----

    public void addPlayer(String teamRegNo, Player player) {
        FootballTeam team = findTeam(teamRegNo);
        if (player == null) throw new ValidationException("Player is required.");
        requireNewPersonId(player.getId());
        team.addPlayer(player);
    }

    public void addCoach(String teamRegNo, Coach coach) {
        FootballTeam team = findTeam(teamRegNo);
        if (coach == null) throw new ValidationException("Coach is required.");
        requireNewPersonId(coach.getId());
        team.addCoach(coach);
    }

    public void removePlayer(String teamRegNo, String playerId) {
        if (!findTeam(teamRegNo).removePlayer(playerId))
            throw new ValidationException("No player " + playerId + " in that team.");
    }

    private void requireNewPersonId(String personId) {
        for (FootballTeam t : teams.values()) {
            for (Player p : t.getPlayers())
                if (p.getId().equals(personId)) throw new ValidationException("Person ID " + personId + " is already used.");
            for (Coach c : t.getCoaches())
                if (c.getId().equals(personId)) throw new ValidationException("Person ID " + personId + " is already used.");
        }
        if (officials.containsKey(personId)) throw new ValidationException("Person ID " + personId + " is already used.");
    }

    // ---- match officials ----

    public void registerOfficial(MatchOfficial official) {
        if (official == null) throw new ValidationException("Official is required.");
        requireNewPersonId(official.getId());
        officials.put(official.getId(), official);
    }

    public MatchOfficial findOfficial(String officialId) {
        MatchOfficial o = officialId == null ? null : officials.get(officialId.trim());
        if (o == null) throw new ValidationException("No official with ID " + officialId + ".");
        return o;
    }

    public List<MatchOfficial> getOfficials() { return new ArrayList<>(officials.values()); }

    /**
     * The commissioner assigns an official to a fixture. Rejected if the fixture is completed, the official is
     * unavailable or already assigned, or already working another fixture at the same date and time.
     */
    public void assignOfficial(String fixtureId, String officialId) {
        if (commissioner == null) throw new ValidationException("No league commissioner has been set.");
        Fixture fixture = findFixture(fixtureId);
        MatchOfficial official = findOfficial(officialId);
        if (fixture.getStatus() == Fixture.Status.COMPLETED)
            throw new ValidationException("Fixture " + fixtureId + " is already completed.");
        if (!official.isAvailable())
            throw new ValidationException(official.getName() + " is not available for assignments.");
        if (fixture.hasOfficial(official.getId()))
            throw new ValidationException(official.getName() + " is already assigned to " + fixtureId + ".");
        for (Fixture other : fixtures.values()) {
            if (other != fixture && other.getDateTime().equals(fixture.getDateTime()) && other.hasOfficial(official.getId()))
                throw new ValidationException(official.getName() + " is already assigned to fixture "
                        + other.getFixtureId() + " at that time.");
        }
        fixture.addOfficial(official);
        commissioner.recordDecision("Assigned " + official.getName() + " (" + official.getRank() + ") to fixture " + fixtureId);
    }

    /** Used when reloading saved data: links an official to a fixture without re-checking rules. */
    public void restoreOfficialAssignment(String fixtureId, String officialId) {
        findFixture(fixtureId).addOfficial(findOfficial(officialId));
    }

    /** The commissioner records a match-preparation decision (e.g. a pitch inspection result). */
    public void recordPreparationNote(String fixtureId, String note) {
        if (commissioner == null) throw new ValidationException("No league commissioner has been set.");
        findFixture(fixtureId);
        commissioner.recordDecision("Fixture " + fixtureId + ": " + note);
    }

    // ---- stadiums ----

    public void addStadium(Stadium stadium) {
        if (stadium == null) throw new ValidationException("Stadium is required.");
        if (stadiums.containsKey(stadium.getStadiumId()))
            throw new ValidationException("Stadium ID " + stadium.getStadiumId() + " already exists.");
        stadiums.put(stadium.getStadiumId(), stadium);
    }

    public Stadium findStadium(String stadiumId) {
        Stadium s = stadiumId == null ? null : stadiums.get(stadiumId.trim());
        if (s == null) throw new ValidationException("No stadium with ID " + stadiumId + ".");
        return s;
    }

    public List<Stadium> getStadiums() { return new ArrayList<>(stadiums.values()); }

    // ---- fixtures ----

    /**
     * Adds a fixture to the league. Basic league-level checks only: teams and stadium must be
     * registered and both teams eligible. Member 3 adds date/time and official conflict checks.
     */
    public void addFixture(Fixture fixture) {
        if (fixture == null) throw new ValidationException("Fixture is required.");
        if (fixtures.containsKey(fixture.getFixtureId()))
            throw new ValidationException("Fixture ID " + fixture.getFixtureId() + " already exists.");
        requireEligible(fixture.getHomeTeam());
        requireEligible(fixture.getAwayTeam());
        if (!stadiums.containsKey(fixture.getStadium().getStadiumId()))
            throw new ValidationException("Stadium " + fixture.getStadium().getName() + " is not registered.");
        for (Fixture other : fixtures.values()) {
            if (!other.getDateTime().equals(fixture.getDateTime())) continue;
            if (other.involves(fixture.getHomeTeam()))
                throw new ValidationException(fixture.getHomeTeam().getName() + " already plays in fixture "
                        + other.getFixtureId() + " at that time.");
            if (other.involves(fixture.getAwayTeam()))
                throw new ValidationException(fixture.getAwayTeam().getName() + " already plays in fixture "
                        + other.getFixtureId() + " at that time.");
            if (other.getStadium().getStadiumId().equals(fixture.getStadium().getStadiumId()))
                throw new ValidationException(fixture.getStadium().getName() + " is already booked for fixture "
                        + other.getFixtureId() + " at that time.");
        }
        fixtures.put(fixture.getFixtureId(), fixture);
    }

    /** Used when reloading saved data: skips eligibility checks so history is not lost. */
    public void restoreFixture(Fixture fixture) {
        fixtures.put(fixture.getFixtureId(), fixture);
    }

    private void requireEligible(FootballTeam team) {
        if (!teams.containsKey(team.getRegistrationNo()))
            throw new ValidationException("Team " + team.getName() + " is not registered.");
        if (!team.isEligibleForFixture())
            throw new ValidationException("Team " + team.getName() + " is not eligible for fixtures (status "
                    + team.getStatus() + ").");
    }

    public Fixture findFixture(String fixtureId) {
        Fixture f = fixtureId == null ? null : fixtures.get(fixtureId.trim());
        if (f == null) throw new ValidationException("No fixture with ID " + fixtureId + ".");
        return f;
    }

    public List<Fixture> getFixtures() {
        List<Fixture> list = new ArrayList<>(fixtures.values());
        list.sort(Comparator.comparing(Fixture::getDateTime));
        return list;
    }

    public List<Fixture> getFixtures(Fixture.Status status) {
        List<Fixture> list = new ArrayList<>();
        for (Fixture f : getFixtures()) {
            if (f.getStatus() == status) list.add(f);
        }
        return list;
    }

    // ---- results & standings ----

    /** Validates the scores, creates the result and completes the fixture. Rejects a second result. */
    public MatchResult recordResult(String fixtureId, int homeScore, int awayScore) {
        Fixture fixture = findFixture(fixtureId);
        if (fixture.getStatus() == Fixture.Status.COMPLETED)
            throw new ValidationException("Fixture " + fixtureId + " already has a result.");
        MatchResult result = new MatchResult(fixture.getFixtureId(), homeScore, awayScore);
        fixture.complete(result);
        return result;
    }

    /** League table: points, then goal difference, then goals scored, then name. */
    public List<StandingsEntry> getStandings() {
        Map<String, StandingsEntry> table = new LinkedHashMap<>();
        for (FootballTeam t : teams.values()) {
            if (t.getStatus() != FootballTeam.Status.PENDING) {
                table.put(t.getRegistrationNo(), new StandingsEntry(t));
            }
        }
        for (Fixture f : fixtures.values()) {
            MatchResult r = f.getResult();
            if (f.getStatus() != Fixture.Status.COMPLETED || r == null) continue;
            StandingsEntry home = table.computeIfAbsent(f.getHomeTeam().getRegistrationNo(),
                    k -> new StandingsEntry(f.getHomeTeam()));
            StandingsEntry away = table.computeIfAbsent(f.getAwayTeam().getRegistrationNo(),
                    k -> new StandingsEntry(f.getAwayTeam()));
            home.apply(r.getHomeScore(), r.getAwayScore(), r.homePoints());
            away.apply(r.getAwayScore(), r.getHomeScore(), r.awayPoints());
        }
        List<StandingsEntry> rows = new ArrayList<>(table.values());
        rows.sort(Comparator.comparingInt(StandingsEntry::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(StandingsEntry::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(StandingsEntry::getGoalsFor).reversed())
                .thenComparing(e -> e.getTeam().getName()));
        return rows;
    }
}
