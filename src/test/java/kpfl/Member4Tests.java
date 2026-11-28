package kpfl;

import kpfl.exception.LeagueException;
import kpfl.exception.ValidationException;
import kpfl.model.FoodItem;
import kpfl.model.Fixture;
import kpfl.model.FootballTeam;
import kpfl.model.LeagueCommissioner;
import kpfl.model.Merchandise;
import kpfl.model.Money;
import kpfl.model.Person;
import kpfl.model.Product;
import kpfl.model.Receipt;
import kpfl.model.Spectator;
import kpfl.model.Stadium;
import kpfl.model.Ticket;
import kpfl.service.AppData;
import kpfl.service.FootballLeague;
import kpfl.service.SalesProvider;
import kpfl.service.StadiumShop;
import kpfl.service.TicketService;
import kpfl.storage.FileStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Checks for Ticketing, Stadium Shop and Payment. Run: java -cp out kpfl.Member4Tests */
public class Member4Tests {
    private static int passed;

    public static void main(String[] args) throws Exception {
        ticketTests();
        shopTests();
        gctTests();
        salesAndStorageTests();
        System.out.println(passed + " Ticketing & Shop checks passed.");
    }

    // ---------- tickets ----------
    private static void ticketTests() {
        AppData app = new AppData();
        FootballLeague league = app.getLeague();
        TicketService tickets = app.getTickets();
        // Teams must be approved by the commissioner before they can play in a fixture.
        league.setCommissioner(new LeagueCommissioner("C1", "League Commissioner", 45, "N/A"));
        // A tiny stadium (2 seats) makes the capacity rule easy to prove.
        league.registerTeam(new FootballTeam("T1", "Harbour FC", "Kingston"));
        league.registerTeam(new FootballTeam("T2", "Hill FC", "St Andrew"));
        league.approveTeam("T1");
        league.approveTeam("T2");
        league.addStadium(new Stadium("S1", "Tiny Ground", "Kingston", 2, 1000, "Good"));
        league.addFixture(new Fixture("F1", league.findTeam("T1"), league.findTeam("T2"),
                league.findStadium("S1"), LocalDateTime.of(2026, 12, 5, 15, 0)));
        for (int i = 1; i <= 4; i++) tickets.addSpectator(new Spectator("SP" + i, "Fan " + i, 20 + i, "F"));

        Person asPerson = tickets.findSpectator("SP1");
        check("spectator is a Person with its own role", asPerson.getRole().equals("Spectator"));
        check("duplicate spectator rejected", rejects(() -> tickets.addSpectator(new Spectator("SP1", "Copy", 30, "M"))));
        check("invalid spectator age rejected", rejects(() -> new Spectator("SP9", "Bad Age", 0, "M")));

        Ticket first = tickets.sellTicket("F1", "SP1", Ticket.Category.GENERAL);
        tickets.sellTicket("F1", "SP2", Ticket.Category.GENERAL);
        check("two seats accept exactly two tickets", tickets.ticketsSold("F1") == 2);
        check("third ticket rejected (sold out)", rejects(() -> tickets.sellTicket("F1", "SP3", Ticket.Category.GENERAL)));
        check("rejected sale does not count", tickets.ticketsSold("F1") == 2);
        check("ticket stores fixture and spectator IDs", first.getFixtureId().equals("F1")
                && first.getSpectatorId().equals("SP1") && first.getTicketId().startsWith("TK"));
        check("spectator keeps the ticket ID", tickets.findSpectator("SP1").getTicketIds().contains(first.getTicketId()));

        tickets.cancelTicket(first.getTicketId());
        check("cancelled ticket frees a seat", tickets.ticketsSold("F1") == 1);
        Ticket vip = tickets.sellTicket("F1", "SP3", Ticket.Category.VIP);
        check("VIP costs twice the stadium price", vip.getPrice() == 2000.0);
        check("cancelling twice rejected", rejects(() -> tickets.cancelTicket(first.getTicketId())));
        check("ticket revenue counts only sold tickets", tickets.ticketRevenue("F1") == 3000.0);

        check("unknown spectator rejected", rejects(() -> tickets.sellTicket("F1", "NOPE", Ticket.Category.GENERAL)));
        check("unknown fixture rejected", rejects(() -> tickets.sellTicket("NOPE", "SP4", Ticket.Category.GENERAL)));
        check("missing category rejected", rejects(() -> tickets.sellTicket("F1", "SP4", null)));
        check("negative ticket price rejected", rejects(() -> new Ticket("X", "F1", "SP1", Ticket.Category.GENERAL, -1)));

        league.recordResult("F1", 1, 0);
        check("no tickets for a completed fixture", rejects(() -> tickets.sellTicket("F1", "SP4", Ticket.Category.GENERAL)));
    }

    // ---------- shop ----------
    private static void shopTests() {
        StadiumShop shop = new StadiumShop();
        shop.addProduct(new Merchandise("M1", "Jersey", 5, 6900, "M"));
        shop.addProduct(new FoodItem("F1", "Jerk Chicken Box", 10, 1150, false));
        shop.addProduct(new FoodItem("B1", "Water", 10, 250, true));

        // Polymorphism: the same call gives a different answer for each Product subclass.
        Product[] items = {shop.findProduct("M1"), shop.findProduct("F1"), shop.findProduct("B1")};
        check("getType() is overridden by each subclass", items[0].getType().startsWith("Merchandise")
                && items[1].getType().equals("Food") && items[2].getType().equals("Beverage"));
        check("duplicate product ID rejected", rejects(() -> shop.addProduct(new FoodItem("F1", "Copy", 1, 100, false))));
        check("negative stock rejected", rejects(() -> new FoodItem("X", "Bad", -1, 100, false)));
        check("negative price rejected", rejects(() -> new FoodItem("X", "Bad", 1, -5, false)));
        check("unknown product rejected", rejects(() -> shop.findProduct("NOPE")));

        Map<String, Integer> basket = new LinkedHashMap<>();
        basket.put("M1", 1);
        basket.put("F1", 2);
        Receipt r = shop.checkout(basket, 10000);
        check("valid multi-item payment succeeds", r.getTotal() == 9200.0);
        check("change is payment minus total", r.getChange() == 800.0);
        check("stock reduces correctly", shop.findProduct("M1").getStock() == 4
                && shop.findProduct("F1").getStock() == 8);
        check("sale is recorded", shop.getSales().size() == 1 && shop.totalRevenue() == 9200.0);
        System.out.println(r.format());

        Map<String, Integer> small = new LinkedHashMap<>();
        small.put("B1", 2);
        check("insufficient payment rejected", rejects(() -> shop.checkout(small, 499)));
        check("failed payment leaves stock unchanged", shop.findProduct("B1").getStock() == 10);
        check("exact payment accepted", shop.checkout(small, 500).getChange() == 0.0);

        Map<String, Integer> negative = new LinkedHashMap<>();
        negative.put("B1", -1);
        check("negative quantity rejected", rejects(() -> shop.checkout(negative, 1000)));
        Map<String, Integer> zero = new LinkedHashMap<>();
        zero.put("B1", 0);
        check("zero quantity rejected", rejects(() -> shop.checkout(zero, 1000)));
        Map<String, Integer> tooMany = new LinkedHashMap<>();
        tooMany.put("M1", 99);
        check("more than the stock rejected", rejects(() -> shop.checkout(tooMany, 999999)));
        check("empty basket rejected", rejects(() -> shop.checkout(new LinkedHashMap<>(), 100)));

        // A sale with one good item and one bad item must not remove any stock.
        int waterBefore = shop.findProduct("B1").getStock();
        Map<String, Integer> mixed = new LinkedHashMap<>();
        mixed.put("B1", 1);
        mixed.put("M1", 99);
        check("mixed bad sale rejected", rejects(() -> shop.checkout(mixed, 999999)));
        check("mixed bad sale changes nothing", shop.findProduct("B1").getStock() == waterBefore);

        shop.restock("B1", 5);
        check("restock adds stock", shop.findProduct("B1").getStock() == waterBefore + 5);
        check("restock of zero rejected", rejects(() -> shop.restock("B1", 0)));
    }

    // ---------- GCT ----------
    private static void gctTests() {
        // The brief says prices already include 15% GCT: J$1,150 = J$1,000 + J$150.
        check("GCT inside J$1,150 is J$150", Money.gctIncluded(1150) == 150.0);
        check("pre-tax part of J$1,150 is J$1,000", Money.preTax(1150) == 1000.0);

        StadiumShop shop = new StadiumShop();
        shop.addProduct(new FoodItem("F1", "Jerk Chicken Box", 10, 1150, false));
        Map<String, Integer> basket = new LinkedHashMap<>();
        basket.put("F1", 1);
        Receipt r = shop.checkout(basket, 1150);
        check("receipt total is not taxed again", r.getTotal() == 1150.0);
        check("receipt shows subtotal 1000 and GCT 150", r.getSubtotal() == 1000.0 && r.getGct() == 150.0);
        check("receipt text lists the GCT line", r.format().contains("GCT 15% (included)"));
    }

    // ---------- report feed and save/reload ----------
    private static void salesAndStorageTests() throws Exception {
        // Own small data set: one upcoming fixture, two tickets, a few products and one sale.
        AppData app = new AppData();
        FootballLeague league = app.getLeague();
        league.setCommissioner(new LeagueCommissioner("C1", "League Commissioner", 45, "N/A"));
        league.registerTeam(new FootballTeam("T1", "Harbour FC", "Kingston"));
        league.registerTeam(new FootballTeam("T2", "Hill FC", "St Andrew"));
        league.approveTeam("T1");
        league.approveTeam("T2");
        league.addStadium(new Stadium("S1", "Test Ground", "Kingston", 100, 1500, "Good"));
        league.addFixture(new Fixture("F1", league.findTeam("T1"), league.findTeam("T2"),
                league.findStadium("S1"), LocalDateTime.of(2026, 12, 5, 15, 0)));
        app.getTickets().addSpectator(new Spectator("SP1", "Tanya Morgan", 29, "F"));
        app.getTickets().addSpectator(new Spectator("SP2", "Omar Francis", 33, "M"));
        app.getTickets().sellTicket("F1", "SP1", Ticket.Category.GENERAL);
        Ticket cancelled = app.getTickets().sellTicket("F1", "SP2", Ticket.Category.VIP);
        app.getTickets().cancelTicket(cancelled.getTicketId());
        app.getShop().addProduct(new Merchandise("M1", "Lions Jersey", 20, 6900, "M"));
        app.getShop().addProduct(new FoodItem("B1", "Bottled Water", 200, 250, true));
        Map<String, Integer> basket = new LinkedHashMap<>();
        basket.put("M1", 1);
        basket.put("B1", 4);
        app.getShop().checkout(basket, 8000);

        SalesProvider sales = app.sales();
        check("sales view counts sold tickets only", sales.ticketsSold("F1") == 1);
        check("sales view ticket revenue", sales.ticketRevenue("F1") == 1500.0);
        check("sales view shop revenue and count", sales.shopRevenue() == 7900.0 && sales.shopTransactionCount() == 1);

        Path dir = Files.createTempDirectory("kpfl-m4");
        new FileStorage(dir.toString()).save(app);
        AppData back = new FileStorage(dir.toString()).load();
        check("tickets survive save and reload", back.getTickets().getTickets().size() == 2
                && back.getTickets().ticketsSold("F1") == 1);
        check("cancelled status survives reload", !back.getTickets().getTickets().get(1).isSold());
        check("spectators keep their tickets", back.getTickets().findSpectator("SP1").getTicketIds().size() == 1);
        check("inventory and stock survive", back.getShop().findProduct("M1").getStock() == 19
                && back.getShop().findProduct("B1").getStock() == 196);
        check("product subclasses survive", back.getShop().findProduct("B1").getType().equals("Beverage")
                && back.getShop().findProduct("M1") instanceof Merchandise);
        check("sales and revenue survive", back.getShop().getSales().size() == 1
                && back.getShop().totalRevenue() == 7900.0);
        check("sold-out rule still works after reload", back.getTickets().ticketsSold("F1")
                < back.getLeague().findStadium("S1").getCapacity());
    }

    private static boolean rejects(Runnable action) {
        try { action.run(); return false; }
        catch (ValidationException | LeagueException expected) { return true; }
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        passed++;
        System.out.println("PASS  " + label);
    }
}
