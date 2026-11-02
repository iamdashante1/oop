package kpfl;

import kpfl.exception.ValidationException;
import kpfl.model.Fixture;
import kpfl.model.FootballTeam;
import kpfl.model.LeagueCommissioner;
import kpfl.model.MatchResult;
import kpfl.model.Stadium;
import kpfl.model.StandingsEntry;
import kpfl.service.AppData;
import kpfl.service.FootballLeague;
import kpfl.service.ReportService;
import kpfl.service.SalesProvider;
import kpfl.storage.FileStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/** Dependency-free tests for Member 1's module. Run: java -cp out kpfl.Member1Tests */
public class Member1Tests {
    private static int passed, failed;

    public static void main(String[] args) throws Exception {
        winUpdatesPoints();
        drawUpdatesPoints();
        lossUpdatesPoints();
        negativeScoreRejected();
        secondResultRejected();
        unknownFixtureRejected();
        ineligibleTeamCannotBeScheduled();
        reportsMatchStoredData();
        saveAndReloadRestoresRecords();
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    private static FootballLeague league() {
        return setup(new FootballLeague());
    }

    private static FootballLeague setup(FootballLeague l) {
        l.setCommissioner(new LeagueCommissioner("C1", "Comm", 40, "F"));
        for (String id : new String[] {"A", "B"}) {
            l.registerTeam(new FootballTeam(id, "Team " + id, "Kingston"));
            l.approveTeam(id);
        }
        l.addStadium(new Stadium("S1", "National", "Kingston", 100));
        l.addFixture(new Fixture("F1", l.findTeam("A"), l.findTeam("B"), l.findStadium("S1"),
                LocalDateTime.of(2026, 11, 7, 15, 0)));
        return l;
    }

    private static StandingsEntry row(FootballLeague l, String teamNo) {
        for (StandingsEntry e : l.getStandings())
            if (e.getTeam().getRegistrationNo().equals(teamNo)) return e;
        throw new IllegalStateException("no row " + teamNo);
    }

    private static void winUpdatesPoints() {
        FootballLeague l = league();
        l.recordResult("F1", 2, 0);
        check("win: 3 pts winner, 0 loser", row(l, "A").getPoints() == 3 && row(l, "B").getPoints() == 0);
    }

    private static void drawUpdatesPoints() {
        FootballLeague l = league();
        l.recordResult("F1", 1, 1);
        check("draw: 1 pt each", row(l, "A").getPoints() == 1 && row(l, "B").getPoints() == 1);
    }

    private static void lossUpdatesPoints() {
        FootballLeague l = league();
        l.recordResult("F1", 0, 3);
        check("loss: home 0, away 3", row(l, "A").getPoints() == 0 && row(l, "B").getPoints() == 3);
    }

    private static void negativeScoreRejected() {
        FootballLeague l = league();
        check("negative score rejected", throwsValidation(() -> l.recordResult("F1", -1, 2)));
        check("fixture stays SCHEDULED after bad score", l.findFixture("F1").getStatus() == Fixture.Status.SCHEDULED);
        check("direct MatchResult rejects negative", throwsValidation(() -> new MatchResult("F1", 1, -5)));
    }

    private static void secondResultRejected() {
        FootballLeague l = league();
        l.recordResult("F1", 1, 0);
        check("second result rejected", throwsValidation(() -> l.recordResult("F1", 5, 0)));
        check("points not awarded twice", row(l, "A").getPoints() == 3);
        check("complete(null) rejected", throwsValidation(() -> new Fixture("F9", l.findTeam("A"), l.findTeam("B"),
                l.findStadium("S1"), LocalDateTime.now()).complete(null)));
    }

    private static void unknownFixtureRejected() {
        check("unknown fixture rejected", throwsValidation(() -> league().recordResult("NOPE", 1, 0)));
    }

    private static void ineligibleTeamCannotBeScheduled() {
        FootballLeague l = league();
        l.registerTeam(new FootballTeam("C", "Pending FC", "St Ann"));
        check("pending team cannot be scheduled", throwsValidation(() -> l.addFixture(new Fixture("F2",
                l.findTeam("A"), l.findTeam("C"), l.findStadium("S1"), LocalDateTime.of(2026, 12, 1, 15, 0)))));
        l.suspendTeam("B");
        check("suspended team cannot be scheduled", throwsValidation(() -> l.addFixture(new Fixture("F3",
                l.findTeam("A"), l.findTeam("B"), l.findStadium("S1"), LocalDateTime.of(2026, 12, 2, 15, 0)))));
        check("duplicate team ID rejected", throwsValidation(() -> l.registerTeam(new FootballTeam("A", "Dup", "X"))));
    }

    private static void reportsMatchStoredData() {
        FootballLeague l = league();
        l.recordResult("F1", 3, 1);
        ReportService rs = new ReportService(l, SalesProvider.NONE);
        String standings = rs.standingsReport();
        check("standings lists winner with 3 pts", standings.contains("Team A") && standings.contains(" 3"));
        check("fixtures report shows result", rs.fixturesReport().contains("3 - 1"));
        check("sales report has totals", rs.salesReport().contains("TOTAL"));
    }

    private static void saveAndReloadRestoresRecords() throws Exception {
        Path tmp = Files.createTempDirectory("kpfl-test");
        AppData app = new AppData();
        FootballLeague l = setup(app.getLeague());
        l.recordResult("F1", 2, 2);
        FileStorage fs = new FileStorage(tmp.toString());
        fs.save(app);
        FootballLeague r = new FileStorage(tmp.toString()).load().getLeague();
        check("reload: teams", r.getTeams().size() == 2);
        check("reload: stadium", r.findStadium("S1").getCapacity() == 100);
        Fixture f = r.findFixture("F1");
        check("reload: fixture links by ID", f.getHomeTeam() == r.findTeam("A") && f.getAwayTeam() == r.findTeam("B"));
        check("reload: result", f.getStatus() == Fixture.Status.COMPLETED && f.getResult().getHomeScore() == 2);
        List<StandingsEntry> table = r.getStandings();
        check("reload: standings identical", table.get(0).getPoints() == 1 && table.get(1).getPoints() == 1);
        check("reload: commissioner", r.getCommissioner() != null);
    }

    private static boolean throwsValidation(Runnable r) {
        try {
            r.run();
            return false;
        } catch (ValidationException e) {
            return true;
        }
    }

    private static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
    }
}
