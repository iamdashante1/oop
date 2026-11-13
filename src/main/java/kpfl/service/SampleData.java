package kpfl.service;

import kpfl.model.Coach;
import kpfl.model.FoodItem;
import kpfl.model.Fixture;
import kpfl.model.FootballTeam;
import kpfl.model.LeagueCommissioner;
import kpfl.model.Merchandise;
import kpfl.model.MatchOfficial;
import kpfl.model.Player;
import kpfl.model.Spectator;
import kpfl.model.Stadium;
import kpfl.model.Ticket;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds a small, valid data set for the demonstration. */
public final class SampleData {
    private SampleData() { }

    public static AppData create() {
        AppData app = new AppData();
        FootballLeague league = app.getLeague();
        league.setCommissioner(new LeagueCommissioner("C001", "League Commissioner", 45, "N/A"));

        // Register and approve teams before scheduling them in fixtures.
        String[][] teams = {
                {"T001", "Kingston Lions", "Kingston"}, {"T002", "Portmore Falcons", "St Catherine"},
                {"T003", "Spanish Town Eagles", "St Catherine"}, {"T004", "Montego Bay United", "St James"}};
        for (String[] t : teams) {
            league.registerTeam(new FootballTeam(t[0], t[1], t[2]));
            league.approveTeam(t[0]);
        }
        league.addPlayer("T001", new Player("P001", "Andre Campbell", 24, "M", "Striker", 9));
        league.addPlayer("T001", new Player("P002", "Devon Reid", 27, "M", "Goalkeeper", 1));
        league.addPlayer("T002", new Player("P003", "Jamal Brown", 22, "M", "Midfielder", 8));
        league.addPlayer("T002", new Player("P004", "Kyle Grant", 19, "M", "Defender", 4));
        league.addCoach("T001", new Coach("K001", "Winston Palmer", 52, "M", "CAF B Licence", "Head Coach"));
        league.addCoach("T002", new Coach("K002", "Simone Hall", 44, "F", "JFF Level 2", "Head Coach"));

        league.addStadium(new Stadium("S001", "National Stadium", "Kingston", 35000, 1500, "Excellent"));
        league.addStadium(new Stadium("S002", "Montego Bay Sports Complex", "Montego Bay", 7000, 1000, "Good"));

        league.registerOfficial(new MatchOfficial("O001", "Marcus Bailey", 38, "M", "Referee"));
        league.registerOfficial(new MatchOfficial("O002", "Linda Clarke", 35, "F", "Assistant Referee"));

        league.addFixture(new Fixture("F001", league.findTeam("T001"), league.findTeam("T002"),
                league.findStadium("S001"), LocalDateTime.of(2026, 11, 7, 15, 0)));
        league.addFixture(new Fixture("F002", league.findTeam("T003"), league.findTeam("T004"),
                league.findStadium("S002"), LocalDateTime.of(2026, 11, 7, 17, 0)));
        league.addFixture(new Fixture("F003", league.findTeam("T001"), league.findTeam("T003"),
                league.findStadium("S001"), LocalDateTime.of(2026, 11, 14, 15, 0)));
        league.assignOfficial("F001", "O001");
        league.assignOfficial("F003", "O001");
        league.recordPreparationNote("F003", "Pitch inspection passed");
        // The first two matches are completed; F003 remains available for ticket sales.
        league.recordResult("F001", 2, 1);
        league.recordResult("F002", 0, 0);

        app.getTickets().addSpectator(new Spectator("SP001", "Tanya Morgan", 29, "F"));
        app.getTickets().addSpectator(new Spectator("SP002", "Omar Francis", 33, "M"));
        app.getTickets().sellTicket("F003", "SP001", Ticket.Category.GENERAL);
        app.getTickets().sellTicket("F003", "SP002", Ticket.Category.VIP);

        StadiumShop shop = app.getShop();
        shop.addProduct(new Merchandise("M001", "Lions Jersey", 20, 6900, "M"));
        shop.addProduct(new Merchandise("M002", "Team Flag", 50, 1500, "One size"));
        shop.addProduct(new FoodItem("F001", "Jerk Chicken Box", 100, 1150, false));
        shop.addProduct(new FoodItem("B001", "Bottled Water", 200, 250, true));
        Map<String, Integer> basket = new LinkedHashMap<>();
        basket.put("M001", 1);
        basket.put("F001", 2);
        shop.checkout(basket, 10000);
        return app;
    }
}
