package kpfl;

import kpfl.exception.ValidationException;
import kpfl.integration.FixtureInfo;
import kpfl.integration.StadiumInfo;
import kpfl.model.Fixture;
import kpfl.model.FootballTeam;
import kpfl.model.LeagueCommissioner;
import kpfl.model.MatchOfficial;
import kpfl.model.MatchResult;
import kpfl.model.Person;
import kpfl.model.Stadium;
import kpfl.service.AppData;
import kpfl.service.FootballLeague;
import kpfl.storage.FileStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/** Tests for the stadium, fixture and match official module. Run: java -cp out kpfl.Member3Tests */
public class Member3Tests {
    private static int passed, failed;
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 12, 1, 15, 0);

    public static void main(String[] args) throws Exception {
        stadiumRules();
        stadiumLeagueOperations();
        fixtureCreationRules();
        schedulingConflicts();
        rescheduleAndCancel();
        officialRules();
        officialAssignment();
        resultsAndCorrection();
        integrationContracts();
        saveAndReload();
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    /** A league with a commissioner, teams A, B, C (all approved), stadiums S1 and S2, official O1 and fixture F1 (A v B). */
    private static FootballLeague base() {
        FootballLeague l = new FootballLeague();
        l.setCommissioner(new LeagueCommissioner("C1", "Comm", 40, "F"));
        for (String id : new String[] {"A", "B", "C"}) {
            l.registerTeam(new FootballTeam(id, "Team " + id, "Kingston"));
            l.approveTeam(id);
        }
        l.addStadium(new Stadium("S1", "Small Ground", "Kingston", 2, 1000, "Good"));
        l.addStadium(new Stadium("S2", "Other Ground", "Portmore", 500, 800, "Good"));
        l.registerOfficial(new MatchOfficial("O1", "Ref One", 40, "M", "Referee"));
        l.addFixture(new Fixture("F1", l.findTeam("A"), l.findTeam("B"), l.findStadium("S1"), T1));
        return l;
    }

    private static Fixture fixture(FootballLeague l, String id, String home, String away, String stadium, LocalDateTime when) {
        return new Fixture(id, l.findTeam(home), l.findTeam(away), l.findStadium(stadium), when);
    }

    // ---- Stadium class ----

    private static void stadiumRules() {
        Stadium s = new Stadium("S9", "  Test Park ", " Kingston ", 100, 500, " Good ");
        check("stadium fields are trimmed", s.getName().equals("Test Park") && s.getLocation().equals("Kingston")
                && s.getFieldCondition().equals("Good"));
        check("short constructor defaults price 0 and condition Good", new Stadium("S8", "A", "B", 10).getTicketPrice() == 0
                && new Stadium("S8", "A", "B", 10).getFieldCondition().equals("Good"));
        check("blank ID rejected", rejects(() -> new Stadium(" ", "A", "B", 10)));
        check("blank name rejected", rejects(() -> new Stadium("S", "", "B", 10)));
        check("blank location rejected", rejects(() -> new Stadium("S", "A", " ", 10)));
        check("zero capacity rejected", rejects(() -> new Stadium("S", "A", "B", 0)));
        check("negative capacity rejected", rejects(() -> new Stadium("S", "A", "B", -5)));
        check("negative price rejected", rejects(() -> new Stadium("S", "A", "B", 10, -1, "Good")));
        check("NaN price rejected", rejects(() -> new Stadium("S", "A", "B", 10, Double.NaN, "Good")));
        check("infinite price rejected", rejects(() -> new Stadium("S", "A", "B", 10, Double.POSITIVE_INFINITY, "Good")));
        check("blank field condition rejected", rejects(() -> new Stadium("S", "A", "B", 10, 0, " ")));
        s.setCapacity(250);
        s.setTicketPrice(750);
        s.setName("New Name");
        s.setLocation("Portmore");
        s.setFieldCondition("Excellent");
        check("setters change the fields", s.getCapacity() == 250 && s.getTicketPrice() == 750
                && s.getName().equals("New Name") && s.getLocation().equals("Portmore")
                && s.getFieldCondition().equals("Excellent") && s.getStadiumId().equals("S9"));
        check("setter rejects bad capacity", rejects(() -> s.setCapacity(0)) && s.getCapacity() == 250);
        check("setter rejects bad price", rejects(() -> s.setTicketPrice(-1)) && s.getTicketPrice() == 750);
        check("toString shows capacity and price", s.toString().contains("capacity 250") && s.toString().contains("J$750.00"));
    }

    private static void stadiumLeagueOperations() {
        FootballLeague l = base();
        check("duplicate stadium ID rejected", rejects(() -> l.addStadium(new Stadium("S1", "Copy", "X", 10))));
        check("null stadium rejected", rejects(() -> l.addStadium(null)));
        check("find unknown stadium rejected", rejects(() -> l.findStadium("NOPE")));
        check("search by location, case-insensitive", l.searchStadiums("PORTMORE").size() == 1);
        check("search by name", l.searchStadiums("small").size() == 1);
        check("search by ID", l.searchStadiums("s2").size() == 1);
        check("blank search lists all", l.searchStadiums("").size() == 2 && l.getStadiums().size() == 2);
        check("search with no match is empty", l.searchStadiums("zzz").isEmpty());
        l.updateStadium("S1", "Updated Ground", "Spanish Town", 300, 1250, "Excellent");
        Stadium s = l.findStadium("S1");
        check("update changes every editable field", s.getName().equals("Updated Ground")
                && s.getLocation().equals("Spanish Town") && s.getCapacity() == 300
                && s.getTicketPrice() == 1250 && s.getFieldCondition().equals("Excellent"));
        check("bad update rejected and changes nothing", rejects(() -> l.updateStadium("S1", "Other", "Kingston", 0, 10, "Good"))
                && s.getName().equals("Updated Ground") && s.getCapacity() == 300);
        check("update of unknown stadium rejected", rejects(() -> l.updateStadium("NOPE", "A", "B", 10, 1, "Good")));
    }

    // ---- Fixture class and scheduling ----

    private static void fixtureCreationRules() {
        FootballLeague l = base();
        check("same team twice rejected", rejects(() -> fixture(l, "X", "A", "A", "S1", T1)));
        check("blank fixture ID rejected", rejects(() -> fixture(l, " ", "A", "B", "S1", T1)));
        check("missing team rejected", rejects(() -> new Fixture("X", null, l.findTeam("B"), l.findStadium("S1"), T1)));
        check("missing stadium rejected", rejects(() -> new Fixture("X", l.findTeam("A"), l.findTeam("B"), null, T1)));
        check("missing date rejected", rejects(() -> new Fixture("X", l.findTeam("A"), l.findTeam("B"), l.findStadium("S1"), null)));
        check("new fixture is SCHEDULED with no result", l.findFixture("F1").getStatus() == Fixture.Status.SCHEDULED
                && l.findFixture("F1").getResult() == null);
        check("duplicate fixture ID rejected", rejects(() -> l.addFixture(fixture(l, "F1", "A", "C", "S2", T1.plusDays(5)))));
        check("involves reports both teams", l.findFixture("F1").involves(l.findTeam("A"))
                && l.findFixture("F1").involves(l.findTeam("B")) && !l.findFixture("F1").involves(l.findTeam("C")));

        FootballLeague l2 = base();
        l2.registerTeam(new FootballTeam("P", "Pending FC", "St Ann"));
        check("pending team cannot be scheduled", rejects(() -> l2.addFixture(fixture(l2, "F5", "A", "P", "S2", T1.plusDays(2)))));
        l2.approveTeam("P");
        l2.suspendTeam("P");
        check("suspended team cannot be scheduled", rejects(() -> l2.addFixture(fixture(l2, "F6", "A", "P", "S2", T1.plusDays(2)))));
        FootballTeam stranger = new FootballTeam("Z", "Unregistered", "X");
        stranger.setStatus("ACTIVE");
        check("unregistered team cannot be scheduled", rejects(() -> l2.addFixture(
                new Fixture("F7", l2.findTeam("A"), stranger, l2.findStadium("S2"), T1.plusDays(2)))));
        check("unregistered stadium cannot be used", rejects(() -> l2.addFixture(new Fixture("F8", l2.findTeam("A"),
                l2.findTeam("C"), new Stadium("S77", "Ghost", "X", 10), T1.plusDays(2)))));
    }

    private static void schedulingConflicts() {
        FootballLeague l = base();
        check("same team at same time rejected", rejects(() -> l.addFixture(fixture(l, "F2", "A", "C", "S2", T1))));
        check("same stadium at same time rejected", rejects(() -> l.addFixture(fixture(l, "F3", "C", "A", "S1", T1))));
        check("overlap by 90 minutes rejected", rejects(() -> l.addFixture(fixture(l, "F4", "A", "C", "S2", T1.plusMinutes(90)))));
        check("overlap 90 minutes earlier rejected", rejects(() -> l.addFixture(fixture(l, "F4", "A", "C", "S2", T1.minusMinutes(90)))));
        check("different teams, stadium and time is allowed", !rejects(() -> l.addFixture(fixture(l, "F5", "C", "A", "S2", T1.plusDays(1)))));
        LocalDateTime justAfter = T1.plusMinutes(Fixture.DURATION_MINUTES);
        check("back-to-back at exactly the slot length is allowed",
                !rejects(() -> l.addFixture(fixture(l, "F6", "B", "C", "S2", justAfter))));
        check("a fixture overlaps its own slot", l.findFixture("F1").overlaps(T1) && l.findFixture("F1").overlaps(T1.plusMinutes(119)));
        check("a fixture does not overlap a full slot away", !l.findFixture("F1").overlaps(T1.plusMinutes(120))
                && !l.findFixture("F1").overlaps(T1.minusMinutes(120)));
        check("fixtures are listed in date order", l.getFixtures().get(0).getFixtureId().equals("F1")
                && l.getFixtures().get(l.getFixtures().size() - 1).getFixtureId().equals("F5"));
        check("find unknown fixture rejected", rejects(() -> l.findFixture("NOPE")));
        check("search by team name", l.searchFixtures("team c").size() == 2);
        check("search by stadium name", l.searchFixtures("small ground").size() == 1);
        check("search by status", l.searchFixtures("scheduled").size() == l.getFixtures().size());
        check("search by fixture ID", l.searchFixtures("f5").size() == 1);
        check("filter by status", l.getFixtures(Fixture.Status.SCHEDULED).size() == 3
                && l.getFixtures(Fixture.Status.COMPLETED).isEmpty());
    }

    private static void rescheduleAndCancel() {
        FootballLeague l = base();
        l.addFixture(fixture(l, "F2", "C", "A", "S2", T1.plusDays(7)));
        LocalDateTime moved = T1.plusDays(8);
        l.rescheduleFixture("F2", moved);
        check("fixture rescheduled", l.findFixture("F2").getDateTime().equals(moved));
        check("clashing reschedule rejected", rejects(() -> l.rescheduleFixture("F2", T1.plusMinutes(30))));
        check("rejected reschedule keeps the old time", l.findFixture("F2").getDateTime().equals(moved));
        check("rescheduling to an overlapping own slot is allowed", !rejects(() -> l.rescheduleFixture("F2", moved.plusMinutes(30))));
        check("reschedule to null rejected", rejects(() -> l.rescheduleFixture("F2", null))
                && l.findFixture("F2").getDateTime().equals(moved.plusMinutes(30)));
        check("reschedule is logged", lastDecision(l).startsWith("Moved fixture F2"));
        check("reschedule needs a commissioner", rejects(() -> {
            FootballLeague bare = new FootballLeague();
            bare.rescheduleFixture("F1", T1);
        }));

        check("cancel needs a reason", rejects(() -> l.cancelFixture("F2", "  ")));
        l.cancelFixture("F2", "Pitch flooded");
        check("fixture cancelled", l.findFixture("F2").getStatus() == Fixture.Status.CANCELLED);
        check("cancel is logged with the reason", lastDecision(l).equals("Cancelled fixture F2: Pitch flooded"));
        check("cancelling twice rejected", rejects(() -> l.cancelFixture("F2", "Again")));
        check("cancelled fixture cannot be moved", rejects(() -> l.rescheduleFixture("F2", T1.plusDays(20))));
        check("cancelled fixture takes no result", rejects(() -> l.recordResult("F2", 1, 0)));
        check("cancelled fixture takes no official", rejects(() -> l.assignOfficial("F2", "O1")));
        check("cancelled fixture frees its slot", !rejects(() -> l.addFixture(fixture(l, "F3", "C", "A", "S2", moved.plusMinutes(30)))));
        check("cancelled fixture is filtered by status", l.getFixtures(Fixture.Status.CANCELLED).size() == 1);

        l.recordResult("F1", 1, 0);
        check("completed fixture cannot be cancelled", rejects(() -> l.cancelFixture("F1", "Too late")));
        check("completed fixture cannot be moved", rejects(() -> l.rescheduleFixture("F1", T1.plusDays(30))));
    }

    // ---- Match officials ----

    private static void officialRules() {
        MatchOfficial o = new MatchOfficial("O9", "Ref Nine", 35, "F", "Linesman");
        check("official is a Person with its own role", ((Person) o).getRole().equals("Match Official"));
        check("official is available by default", o.isAvailable());
        check("official needs a rank", rejects(() -> new MatchOfficial("O8", "X", 30, "M", " ")));
        check("official uses the shared Person checks", rejects(() -> new MatchOfficial("O8", "X", 0, "M", "Referee")));
        check("official needs an ID", rejects(() -> new MatchOfficial(" ", "X", 30, "M", "Referee")));
        o.setRank("Referee");
        o.setAvailable(false);
        check("rank and availability can change", o.getRank().equals("Referee") && !o.isAvailable());
        check("blank rank rejected on update", rejects(() -> o.setRank("")) && o.getRank().equals("Referee"));
        check("toString shows unavailability", o.toString().contains("unavailable"));

        FootballLeague l = base();
        check("duplicate official ID rejected", rejects(() -> l.registerOfficial(new MatchOfficial("O1", "Copy", 30, "M", "Referee"))));
        check("official ID cannot match a player ID", rejects(() -> {
            l.addPlayer("A", new kpfl.model.Player("P1", "Pat", 20, "M", "Striker", 9));
            l.registerOfficial(new MatchOfficial("P1", "Clash", 30, "M", "Referee"));
        }));
        check("find unknown official rejected", rejects(() -> l.findOfficial("NOPE")));
        l.registerOfficial(new MatchOfficial("O2", "Ref Two", 36, "F", "Assistant"));
        check("search officials by rank", l.searchOfficials("assistant").size() == 1);
        check("search officials by name", l.searchOfficials("ref one").size() == 1);
        check("search officials by ID", l.searchOfficials("o2").size() == 1);
        check("blank search lists all officials", l.searchOfficials("").size() == 2 && l.getOfficials().size() == 2);
    }

    private static void officialAssignment() {
        FootballLeague l = base();
        l.registerTeam(new FootballTeam("D", "Team D", "St Ann"));
        l.approveTeam("D");
        l.addFixture(fixture(l, "F2", "C", "D", "S2", T1));
        l.addFixture(fixture(l, "F3", "C", "D", "S2", T1.plusMinutes(Fixture.DURATION_MINUTES)));
        l.assignOfficial("F1", "O1");
        check("official assigned", l.findFixture("F1").hasOfficial("O1") && l.findFixture("F1").getOfficials().size() == 1);
        check("assignment is logged", lastDecision(l).startsWith("Assigned Ref One"));
        check("assigning twice rejected", rejects(() -> l.assignOfficial("F1", "O1")));
        check("same official at the same time rejected", rejects(() -> l.assignOfficial("F2", "O1")));
        check("same official in the next free slot allowed", !rejects(() -> l.assignOfficial("F3", "O1")));
        l.registerOfficial(new MatchOfficial("O2", "Ref Two", 36, "F", "Referee"));
        l.findOfficial("O2").setAvailable(false);
        check("unavailable official rejected", rejects(() -> l.assignOfficial("F2", "O2")));
        l.findOfficial("O2").setAvailable(true);
        check("available again after being unavailable", !rejects(() -> l.assignOfficial("F2", "O2")));
        check("unknown fixture rejected", rejects(() -> l.assignOfficial("NOPE", "O1")));
        check("unknown official rejected", rejects(() -> l.assignOfficial("F2", "NOPE")));

        l.unassignOfficial("F1", "O1");
        check("official unassigned", !l.findFixture("F1").hasOfficial("O1"));
        check("unassigning is logged", lastDecision(l).startsWith("Removed Ref One"));
        check("unassigning someone not assigned rejected", rejects(() -> l.unassignOfficial("F1", "O1")));
        l.assignOfficial("F1", "O1");
        check("official can be assigned again after unassigning", l.findFixture("F1").hasOfficial("O1"));
        check("rescheduling onto the official's other fixture is rejected", rejects(() -> l.rescheduleFixture("F3", T1)));
        check("official list is read-only", rejects(() -> {
            try {
                l.findFixture("F1").getOfficials().clear();
            } catch (UnsupportedOperationException e) {
                throw new ValidationException("read-only");
            }
        }));

        l.recordResult("F1", 2, 2);
        check("completed fixture takes no new official", rejects(() -> l.assignOfficial("F1", "O2")));
        check("completed fixture keeps its officials", l.findFixture("F1").hasOfficial("O1")
                && rejects(() -> l.unassignOfficial("F1", "O1")));
        check("assignment needs a commissioner", rejects(() -> {
            FootballLeague bare = new FootballLeague();
            bare.assignOfficial("F1", "O1");
        }));
    }

    // ---- Results as they touch fixtures ----

    private static void resultsAndCorrection() {
        FootballLeague l = base();
        MatchResult r = l.recordResult("F1", 3, 1);
        check("result completes the fixture", l.findFixture("F1").getStatus() == Fixture.Status.COMPLETED
                && l.findFixture("F1").isCompleted() && l.findFixture("F1").getResult() == r);
        check("second result rejected", rejects(() -> l.recordResult("F1", 0, 0)));
        check("negative score rejected", rejects(() -> {
            l.addFixture(fixture(l, "F2", "C", "A", "S2", T1.plusDays(3)));
            l.recordResult("F2", -1, 0);
        }));
        check("fixture stays scheduled after a rejected result", l.findFixture("F2").getStatus() == Fixture.Status.SCHEDULED);
        check("fixture cannot be completed without a result", rejects(() -> l.findFixture("F2").markCompleted()));
        check("result for a different fixture rejected", rejects(() -> l.findFixture("F2").complete(new MatchResult("F1", 1, 0))));
        check("null result rejected", rejects(() -> l.findFixture("F2").complete(null)));
        MatchResult fixed = l.correctResult("F1", 0, 1);
        check("score can be corrected", l.findFixture("F1").getResult() == fixed && fixed.getAwayScore() == 1);
        check("standings follow the correction", l.getStandings().get(0).getTeam().getName().equals("Team B"));
        check("correction is logged", lastDecision(l).startsWith("Corrected result of fixture F1"));
        check("negative correction rejected", rejects(() -> l.correctResult("F1", -2, 0)) && l.findFixture("F1").getResult() == fixed);
        check("cannot correct a fixture with no result", rejects(() -> l.correctResult("F2", 1, 1)));
        check("fixture toString shows teams, stadium and status", l.findFixture("F1").toString().contains("Team A vs Team B")
                && l.findFixture("F1").toString().contains("Small Ground") && l.findFixture("F1").toString().contains("COMPLETED"));
    }

    private static void integrationContracts() {
        FootballLeague l = base();
        StadiumInfo si = l.findStadium("S1");
        FixtureInfo fi = l.findFixture("F1");
        check("stadium implements StadiumInfo", si.getStadiumId().equals("S1") && si.getStadiumName().equals("Small Ground"));
        check("fixture implements FixtureInfo", fi.getFixtureId().equals("F1") && fi.getHomeTeamId().equals("A")
                && fi.getAwayTeamId().equals("B") && fi.getStadiumId().equals("S1") && fi.getDateTime().equals(T1));
        check("FixtureInfo reports completion", !fi.isCompleted());
        l.recordResult("F1", 1, 1);
        check("FixtureInfo sees the completed fixture", fi.isCompleted());
        check("ticketing can read capacity through the fixture", l.findFixture("F1").getStadium().getCapacity() == 2);
    }

    private static void saveAndReload() throws Exception {
        AppData app = new AppData();
        FootballLeague l = app.getLeague();
        l.setCommissioner(new LeagueCommissioner("C1", "Comm", 40, "F"));
        for (String id : new String[] {"A", "B", "C"}) {
            l.registerTeam(new FootballTeam(id, "Team " + id, "Kingston"));
            l.approveTeam(id);
        }
        l.addStadium(new Stadium("S1", "Small Ground", "Kingston", 2, 1000, "Good"));
        l.addStadium(new Stadium("S2", "Other Ground", "Portmore", 500, 800, "Excellent"));
        l.registerOfficial(new MatchOfficial("O1", "Ref One", 40, "M", "Referee"));
        l.registerOfficial(new MatchOfficial("O2", "Ref Two", 36, "F", "Assistant"));
        l.addFixture(fixture(l, "F1", "A", "B", "S1", T1));
        l.addFixture(fixture(l, "F2", "B", "C", "S2", T1.plusDays(7)));
        l.addFixture(fixture(l, "F3", "A", "C", "S2", T1.plusDays(14)));
        l.assignOfficial("F1", "O1");
        l.assignOfficial("F2", "O2");
        l.recordResult("F1", 2, 1);
        l.cancelFixture("F2", "Weather");
        l.findOfficial("O2").setAvailable(false);
        l.updateStadium("S2", "Renamed Ground", "Portmore", 600, 900, "Fair");

        Path tmp = Files.createTempDirectory("kpfl-m3");
        FileStorage storage = new FileStorage(tmp.toString());
        storage.save(app);
        FootballLeague r = storage.load().getLeague();
        check("reload: stadium update kept", r.findStadium("S2").getName().equals("Renamed Ground")
                && r.findStadium("S2").getCapacity() == 600 && r.findStadium("S2").getTicketPrice() == 900
                && r.findStadium("S2").getFieldCondition().equals("Fair"));
        check("reload: all fixtures present", r.getFixtures().size() == 3);
        check("reload: completed fixture keeps result", r.findFixture("F1").getStatus() == Fixture.Status.COMPLETED
                && r.findFixture("F1").getResult().getHomeScore() == 2 && r.findFixture("F1").getResult().getAwayScore() == 1);
        check("reload: cancelled fixture stays cancelled", r.findFixture("F2").getStatus() == Fixture.Status.CANCELLED);
        check("reload: scheduled fixture stays scheduled", r.findFixture("F3").getStatus() == Fixture.Status.SCHEDULED);
        check("reload: fixture date and stadium kept", r.findFixture("F3").getDateTime().equals(T1.plusDays(14))
                && r.findFixture("F3").getStadium().getStadiumId().equals("S2"));
        check("reload: officials and availability kept", r.getOfficials().size() == 2 && !r.findOfficial("O2").isAvailable()
                && r.findOfficial("O1").isAvailable());
        check("reload: assignments kept, including on completed and cancelled fixtures",
                r.findFixture("F1").hasOfficial("O1") && r.findFixture("F2").hasOfficial("O2"));
        check("reload: commissioner decisions kept", r.getCommissioner().getDecisions().size() == l.getCommissioner().getDecisions().size());
        check("reload: standings identical", r.getStandings().size() == l.getStandings().size()
                && r.getStandings().get(0).getPoints() == l.getStandings().get(0).getPoints());
    }

    // ---- helpers ----

    private static String lastDecision(FootballLeague l) {
        java.util.List<String> d = l.getCommissioner().getDecisions();
        return d.get(d.size() - 1);
    }

    private static boolean rejects(Runnable action) {
        try {
            action.run();
            return false;
        } catch (ValidationException expected) {
            return true;
        }
    }

    private static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
    }
}
