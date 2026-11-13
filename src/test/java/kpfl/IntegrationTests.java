package kpfl;

import kpfl.exception.ValidationException;
import kpfl.model.Coach;
import kpfl.model.FoodItem;
import kpfl.model.Fixture;
import kpfl.model.FootballTeam;
import kpfl.model.LeagueCommissioner;
import kpfl.model.MatchOfficial;
import kpfl.model.Merchandise;
import kpfl.model.Player;
import kpfl.model.Product;
import kpfl.model.Receipt;
import kpfl.model.Spectator;
import kpfl.model.Stadium;
import kpfl.model.Ticket;
import kpfl.service.AppData;
import kpfl.service.FootballLeague;
import kpfl.service.ReportService;
import kpfl.service.SampleData;
import kpfl.storage.FileStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Tests for the people, fixture and sales rules and the whole-app save/reload. Run: java -cp out kpfl.IntegrationTests */
public class IntegrationTests {
    private static int passed, failed;
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 12, 1, 15, 0);

    public static void main(String[] args) throws Exception {
        playersAndCoaches();
        fixtureConflicts();
        stadiumManagement();
        fixtureAndOfficialSearch();
        officialRules();
        commissionerLog();
        ticketCapacity();
        shopRules();
        gctReceipt();
        fullLifecycleSaveReload();
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    private static AppData base() {
        AppData app = new AppData();
        FootballLeague l = app.getLeague();
        l.setCommissioner(new LeagueCommissioner("C1", "Comm", 40, "F"));
        for (String id : new String[] {"A", "B", "C"}) {
            l.registerTeam(new FootballTeam(id, "Team " + id, "Kingston"));
            l.approveTeam(id);
        }
        l.addStadium(new Stadium("S1", "Small Ground", "Kingston", 2, 1000, "Good"));
        l.addStadium(new Stadium("S2", "Other Ground", "Portmore", 500, 800, "Good"));
        l.registerOfficial(new MatchOfficial("O1", "Ref One", 40, "M", "Referee"));
        l.addFixture(new Fixture("F1", l.findTeam("A"), l.findTeam("B"), l.findStadium("S1"), T1));
        return app;
    }

    private static void playersAndCoaches() {
        FootballLeague l = base().getLeague();
        l.addPlayer("A", new Player("P1", "Pat", 20, "M", "Striker", 9));
        l.addCoach("A", new Coach("K1", "Coach", 45, "M", "B Licence", "Head Coach"));
        check("player and coach added to squad", l.findTeam("A").getSquadSize() == 1 && l.findTeam("A").getCoaches().size() == 1);
        check("underage player rejected", throwsValidation(() -> new Player("P2", "Kid", 12, "M", "Winger", 7)));
        check("duplicate jersey rejected", throwsValidation(() -> l.addPlayer("A", new Player("P3", "Sam", 21, "M", "Back", 9))));
        check("duplicate person ID rejected", throwsValidation(() -> l.addPlayer("B", new Player("P1", "Dup", 22, "M", "Back", 3))));
        l.removePlayer("A", "P1");
        check("player removed", l.findTeam("A").getSquadSize() == 0);
        check("polymorphic role", new Player("P9", "X", 20, "M", "GK", 1).getRole().equals("Player")
                && new Coach("K9", "Y", 30, "M", "", "Assistant").getRole().equals("Coach"));
    }

    private static void fixtureConflicts() {
        FootballLeague l = base().getLeague();
        check("same team double-booked rejected", throwsValidation(() -> l.addFixture(new Fixture("F2",
                l.findTeam("A"), l.findTeam("C"), l.findStadium("S2"), T1))));
        check("same stadium double-booked rejected", throwsValidation(() -> l.addFixture(new Fixture("F3",
                l.findTeam("C"), l.findTeam("A"), l.findStadium("S1"), T1.plusHours(0)))));
        check("team may play at another time", !throwsValidation(() -> l.addFixture(new Fixture("F4",
                l.findTeam("A"), l.findTeam("C"), l.findStadium("S2"), T1.plusDays(7)))));
        check("fixture with same team twice rejected", throwsValidation(() -> new Fixture("F5",
                l.findTeam("A"), l.findTeam("A"), l.findStadium("S1"), T1)));
        check("invalid stadium capacity rejected", throwsValidation(() -> new Stadium("S9", "Bad", "X", 0)));
        check("negative ticket price rejected", throwsValidation(() -> new Stadium("S9", "Bad", "X", 10, -5, "Good")));
    }


    private static void stadiumManagement() {
        FootballLeague l = base().getLeague();
        check("stadium interface exposes display name", l.findStadium("S1").getStadiumName().equals("Small Ground"));
        check("stadium search finds location", l.searchStadiums("kingston").size() == 1);
        l.updateStadium("S1", "Updated Ground", "Spanish Town", 300, 1250, "Excellent");
        Stadium updated = l.findStadium("S1");
        check("stadium update changes all editable fields", updated.getName().equals("Updated Ground")
                && updated.getLocation().equals("Spanish Town") && updated.getCapacity() == 300
                && updated.getTicketPrice() == 1250 && updated.getFieldCondition().equals("Excellent"));
        check("invalid stadium update rejected", throwsValidation(() ->
                l.updateStadium("S1", "Bad Update", "Kingston", 0, 100, "Good")));
        check("invalid stadium update is atomic", updated.getName().equals("Updated Ground") && updated.getCapacity() == 300);
        check("NaN ticket price rejected", throwsValidation(() -> new Stadium("SN", "Bad", "X", 10, Double.NaN, "Good")));
    }

    private static void fixtureAndOfficialSearch() {
        FootballLeague l = base().getLeague();
        check("fixture implements integration contract", l.findFixture("F1").getHomeTeamId().equals("A")
                && l.findFixture("F1").getAwayTeamId().equals("B") && l.findFixture("F1").getStadiumId().equals("S1"));
        check("fixture search by team", l.searchFixtures("team a").size() == 1);
        check("official search by rank", l.searchOfficials("referee").size() == 1);
        check("team implements integration contract", l.findTeam("A").getTeamId().equals("A")
                && l.findTeam("A").getTeamName().equals("Team A"));
    }

    private static void officialRules() {
        FootballLeague l = base().getLeague();
        l.registerTeam(new FootballTeam("D", "Team D", "St Ann"));
        l.approveTeam("D");
        l.addFixture(new Fixture("F6", l.findTeam("C"), l.findTeam("D"), l.findStadium("S2"), T1));
        l.assignOfficial("F1", "O1");
        check("official assigned", l.findFixture("F1").hasOfficial("O1"));
        check("official double-booked rejected", throwsValidation(() -> l.assignOfficial("F6", "O1")));
        l.registerOfficial(new MatchOfficial("O2", "Ref Two", 36, "F", "Referee"));
        l.findOfficial("O2").setAvailable(false);
        check("unavailable official rejected", throwsValidation(() -> l.assignOfficial("F6", "O2")));
        l.recordResult("F1", 1, 0);
        l.registerOfficial(new MatchOfficial("O3", "Ref Three", 41, "M", "Referee"));
        check("cannot assign to completed fixture", throwsValidation(() -> l.assignOfficial("F1", "O3")));
    }

    private static void commissionerLog() {
        FootballLeague l = base().getLeague();
        l.assignOfficial("F1", "O1");
        l.recordPreparationNote("F1", "Pitch inspection passed");
        int n = l.getCommissioner().getDecisions().size();
        check("decisions logged (3 approvals + assignment + note)", n == 5);
        check("note for unknown fixture rejected", throwsValidation(() -> l.recordPreparationNote("NOPE", "x")));
    }

    private static void ticketCapacity() {
        AppData app = base();
        app.getTickets().addSpectator(new Spectator("SP1", "One", 20, "M"));
        app.getTickets().addSpectator(new Spectator("SP2", "Two", 21, "F"));
        app.getTickets().addSpectator(new Spectator("SP3", "Three", 22, "M"));
        app.getTickets().sellTicket("F1", "SP1", Ticket.Category.GENERAL);
        Ticket vip = app.getTickets().sellTicket("F1", "SP2", Ticket.Category.VIP);
        check("VIP costs double the base price", vip.getPrice() == 2000.0);
        check("third ticket rejected at capacity 2", throwsValidation(() ->
                app.getTickets().sellTicket("F1", "SP3", Ticket.Category.GENERAL)));
        check("exactly 2 tickets sold", app.getTickets().ticketsSold("F1") == 2);
        app.getTickets().cancelTicket(vip.getTicketId());
        check("cancelling frees a seat", !throwsValidation(() ->
                app.getTickets().sellTicket("F1", "SP3", Ticket.Category.GENERAL)));
        check("sales provider reports tickets", app.sales().ticketsSold("F1") == 2);
    }

    private static Map<String, Integer> basket(String id, int qty) {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put(id, qty);
        return m;
    }

    private static void shopRules() {
        AppData app = base();
        app.getShop().addProduct(new Merchandise("M1", "Jersey", 5, 1150, "L"));
        app.getShop().addProduct(new FoodItem("F1", "Patty", 10, 300, false));
        check("insufficient payment rejected", throwsValidation(() -> app.getShop().checkout(basket("M1", 1), 1000)));
        check("stock unchanged after failed sale", app.getShop().findProduct("M1").getStock() == 5);
        check("negative quantity rejected", throwsValidation(() -> app.getShop().checkout(basket("M1", -1), 5000)));
        check("zero quantity rejected", throwsValidation(() -> app.getShop().checkout(basket("M1", 0), 5000)));
        check("exhausted stock rejected", throwsValidation(() -> app.getShop().checkout(basket("M1", 6), 99999)));
        app.getShop().checkout(basket("M1", 2), 3000);
        check("stock reduced after valid sale", app.getShop().findProduct("M1").getStock() == 3);
        Product p = app.getShop().findProduct("F1");
        check("product subclass override via Product reference", p.getType().equals("Food"));
        check("negative stock rejected", throwsValidation(() -> new FoodItem("X", "Bad", -1, 10, false)));
    }

    private static void gctReceipt() {
        AppData app = base();
        app.getShop().addProduct(new Merchandise("M1", "Jersey", 5, 1150, "L"));
        Receipt r = app.getShop().checkout(basket("M1", 1), 2000);
        check("total J$1,150 -> pre-tax 1000 and GCT 150", r.getSubtotal() == 1000.0 && r.getGct() == 150.0);
        check("change = payment - total", r.getChange() == 850.0);
        check("receipt text prints GCT line", r.format().contains("GCT 15% (included)"));
    }

    private static void fullLifecycleSaveReload() throws Exception {
        AppData app = SampleData.create();
        Path tmp = Files.createTempDirectory("kpfl-int");
        new FileStorage(tmp.toString()).save(app);
        AppData r = new FileStorage(tmp.toString()).load();
        FootballLeague l = r.getLeague();
        check("reload: players and coaches", l.findTeam("T001").getSquadSize() == 2 && l.findTeam("T001").getCoaches().size() == 1);
        check("reload: officials and assignments", l.findFixture("F003").hasOfficial("O001"));
        check("reload: commissioner decisions", l.getCommissioner().getDecisions().size()
                == app.getLeague().getCommissioner().getDecisions().size());
        check("reload: tickets per fixture", r.getTickets().ticketsSold("F003") == 2);
        check("reload: shop stock and sales", r.getShop().findProduct("M001").getStock() == 19 && r.getShop().getSales().size() == 1);
        check("reload: shop revenue", r.getShop().totalRevenue() == app.getShop().totalRevenue());
        String before = new ReportService(app.getLeague(), app.sales()).salesReport();
        String after = new ReportService(l, r.sales()).salesReport();
        check("reload: sales report identical", before.equals(after));
        check("reload: standings report identical", new ReportService(app.getLeague(), app.sales()).standingsReport()
                .equals(new ReportService(l, r.sales()).standingsReport()));
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
