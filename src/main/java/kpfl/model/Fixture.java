package kpfl.model;

import kpfl.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A scheduled match between two different teams at a stadium, with assigned officials and a result once played. */
public class Fixture {
    public enum Status { SCHEDULED, COMPLETED }

    private final String fixtureId;
    private final FootballTeam homeTeam;
    private final FootballTeam awayTeam;
    private final Stadium stadium;
    private final LocalDateTime dateTime;
    private final List<MatchOfficial> officials = new ArrayList<>();
    private Status status = Status.SCHEDULED;
    private MatchResult result;

    public Fixture(String fixtureId, FootballTeam homeTeam, FootballTeam awayTeam,
                   Stadium stadium, LocalDateTime dateTime) {
        if (fixtureId == null || fixtureId.trim().isEmpty()) throw new ValidationException("Fixture ID is required.");
        if (homeTeam == null || awayTeam == null) throw new ValidationException("Both teams are required.");
        if (homeTeam.getRegistrationNo().equals(awayTeam.getRegistrationNo()))
            throw new ValidationException("A team cannot play itself.");
        if (stadium == null) throw new ValidationException("A stadium is required.");
        if (dateTime == null) throw new ValidationException("A date and time is required.");
        this.fixtureId = fixtureId.trim();
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.dateTime = dateTime;
    }

    public String getFixtureId() { return fixtureId; }
    public FootballTeam getHomeTeam() { return homeTeam; }
    public FootballTeam getAwayTeam() { return awayTeam; }
    public Stadium getStadium() { return stadium; }
    public LocalDateTime getDateTime() { return dateTime; }
    public Status getStatus() { return status; }
    public MatchResult getResult() { return result; }
    public List<MatchOfficial> getOfficials() { return Collections.unmodifiableList(officials); }

    public boolean involves(FootballTeam team) {
        // Compare stable registration numbers instead of object references.
        String no = team.getRegistrationNo();
        return homeTeam.getRegistrationNo().equals(no) || awayTeam.getRegistrationNo().equals(no);
    }

    public boolean hasOfficial(String officialId) {
        for (MatchOfficial o : officials) if (o.getId().equals(officialId)) return true;
        return false;
    }

    /** Low-level add; FootballLeague.assignOfficial performs the availability and conflict checks. */
    public void addOfficial(MatchOfficial official) {
        if (!hasOfficial(official.getId())) officials.add(official);
    }

    /** A fixture only becomes COMPLETED through a valid result linked to this fixture. */
    public void complete(MatchResult result) {
        if (result == null) throw new ValidationException("A valid result is required to complete a fixture.");
        if (!fixtureId.equals(result.getFixtureId()))
            throw new ValidationException("Result belongs to a different fixture.");
        if (status == Status.COMPLETED)
            throw new ValidationException("Fixture " + fixtureId + " already has a result.");
        // Only after every check passes do we change the stored result and status.
        this.result = result;
        this.status = Status.COMPLETED;
    }

    @Override
    public String toString() {
        return fixtureId + " | " + homeTeam.getName() + " vs " + awayTeam.getName()
                + " @ " + stadium.getName() + " " + dateTime.toString().replace('T', ' ')
                + " [" + status + (result != null ? " " + result : "") + "]";
    }
}
