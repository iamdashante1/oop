package kpfl;

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
import kpfl.service.FootballLeague;
import kpfl.service.StadiumShop;
import kpfl.service.TicketService;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Tests for Member 4 (tickets and stadium shop). Run: java -cp out kpfl.Member4Tests */
public class Member4Tests {
    private static int passed, failed;

    public static void main(String[] args) {
        ticketCapacity();
        ticketPricingAndCancel();
        ticketValidation();
        shopSales();
        shopRejections();
        gctAndReceipt();
        polymorphism();
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    // A league with two teams, a 2-seat stadium (base price J$1,000) and fixture F1
    private static FootballLeague league() {
        FootballLeague l = new FootballLeague();
        l.setCommissioner(new LeagueCommissioner("C1", "Comm", 40, "F"));
        for (String id : new String[] {"A", "B", "C"}) {
            l.registerTeam(new FootballTeam(id, "Team " + id, "Kingston"));
            l.approveTeam(id);
        }
        l.addStadium(new Stadium("S1", "Small Ground", "Kingston", 2, 1000, "Good"));
        l.addStadium(new Stadium("S2", "Big Ground", "Portmore", 500, 800, "Good"));
        LocalDateTime t = LocalDateTime.of(2026, 12, 1, 15, 0);
        l.addFixture(new Fixture("F1", l.findTeam("A"), l.findTeam("B"), l.findStadium("S1"), t));
        l.addFixture(new Fixture("F2", l.findTeam("C"), l.findTeam("A"), l.findStadium("S2"), t.plusDays(7)));
        return l;
    }

    private static TicketService office(FootballLeague l) {
        TicketService ts = new TicketService(l);
        ts.addSpectator(new Spectator("SP1", "One", 20, "M"));
        ts.addSpectator(new Spectator("SP2", "Two", 21, "F"));
        ts.addSpectator(new Spectator("SP3", "Three", 22, "M"));
        return ts;
    }

    private static void ticketCapacity() {
        FootballLeague l = league();
        TicketService ts = office(l);
        ts.sellTicket("F1", "SP1", Ticket.Category.GENERAL);
        ts.sellTicket("F1", "SP2", Ticket.Category.GENERAL);
        check("2-seat stadium sells exactly 2 tickets", ts.ticketsSold("F1") == 2);
        check("third ticket rejected (sold out)", throwsValidation(() -> ts.sellTicket("F1", "SP3", Ticket.Category.GENERAL)));
        check("capacity is per fixture (F2 unaffected)", !throwsValidation(() -> ts.sellTicket("F2", "SP3", Ticket.Category.GENERAL)));
        check("spectator records ticket IDs", ts.findSpectator("SP1").getTicketIds().size() == 1);
    }

    private static void ticketPricingAndCancel() {
        FootballLeague l = league();
        TicketService ts = office(l);
        Ticket general = ts.sellTicket("F1", "SP1", Ticket.Category.GENERAL);
        Ticket vip = ts.sellTicket("F1", "SP2", Ticket.Category.VIP);
        check("GENERAL costs the stadium base price", general.getPrice() == 1000.0);
        check("VIP costs double", vip.getPrice() == 2000.0);
        check("ticket revenue adds up", ts.ticketRevenue("F1") == 3000.0);
        ts.cancelTicket(vip.getTicketId());
        check("cancelled ticket stops counting", ts.ticketsSold("F1") == 1 && ts.ticketRevenue("F1") == 1000.0);
        check("cancelling frees a seat", !throwsValidation(() -> ts.sellTicket("F1", "SP3", Ticket.Category.GENERAL)));
        check("cancelling twice rejected", throwsValidation(() -> ts.cancelTicket(vip.getTicketId())));
        check("unknown ticket rejected", throwsValidation(() -> ts.cancelTicket("NOPE")));
    }

    private static void ticketValidation() {
        FootballLeague l = league();
        TicketService ts = office(l);
        check("unknown fixture rejected", throwsValidation(() -> ts.sellTicket("NOPE", "SP1", Ticket.Category.GENERAL)));
        check("unknown spectator rejected", throwsValidation(() -> ts.sellTicket("F1", "NOPE", Ticket.Category.GENERAL)));
        check("missing category rejected", throwsValidation(() -> ts.sellTicket("F1", "SP1", null)));
        check("duplicate spectator ID rejected", throwsValidation(() -> ts.addSpectator(new Spectator("SP1", "Dup", 30, "M"))));
        check("negative ticket price rejected", throwsValidation(() -> new Ticket("T", "F1", "SP1", Ticket.Category.VIP, -1)));
        l.recordResult("F1", 1, 0);
        check("no tickets for a completed fixture", throwsValidation(() -> ts.sellTicket("F1", "SP1", Ticket.Category.GENERAL)));
    }

    private static Map<String, Integer> basket(String id, int qty) {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put(id, qty);
        return m;
    }

    private static StadiumShop shop() {
        StadiumShop s = new StadiumShop();
        s.addProduct(new Merchandise("M1", "Jersey", 5, 1150, "L"));
        s.addProduct(new FoodItem("F1", "Patty", 10, 300, false));
        s.addProduct(new FoodItem("B1", "Water", 20, 200, true));
        return s;
    }

    private static void shopSales() {
        StadiumShop s = shop();
        Map<String, Integer> multi = new LinkedHashMap<>();
        multi.put("M1", 1);
        multi.put("F1", 2);
        Receipt r = s.checkout(multi, 2000);
        check("multi-item total correct (1150 + 2x300)", r.getTotal() == 1750.0);
        check("change = payment - total", r.getChange() == 250.0);
        check("stock reduced after sale", s.findProduct("M1").getStock() == 4 && s.findProduct("F1").getStock() == 8);
        check("shop revenue tracked", s.totalRevenue() == 1750.0 && s.getSales().size() == 1);
        s.restock("M1", 3);
        check("restock adds stock", s.findProduct("M1").getStock() == 7);
        check("exact payment accepted", !throwsValidation(() -> s.checkout(basket("B1", 1), 200)));
    }

    private static void shopRejections() {
        StadiumShop s = shop();
        check("insufficient payment rejected", throwsValidation(() -> s.checkout(basket("M1", 1), 1000)));
        check("failed payment leaves stock unchanged", s.findProduct("M1").getStock() == 5 && s.getSales().isEmpty());
        check("negative quantity rejected", throwsValidation(() -> s.checkout(basket("M1", -1), 5000)));
        check("zero quantity rejected", throwsValidation(() -> s.checkout(basket("M1", 0), 5000)));
        check("exhausted stock rejected", throwsValidation(() -> s.checkout(basket("M1", 6), 99999)));
        check("unknown product rejected", throwsValidation(() -> s.checkout(basket("NOPE", 1), 5000)));
        check("empty basket rejected", throwsValidation(() -> s.checkout(new LinkedHashMap<>(), 5000)));
        Map<String, Integer> mixed = new LinkedHashMap<>();
        mixed.put("F1", 1);
        mixed.put("M1", 99); // second item is invalid
        check("one bad item cancels the whole sale", throwsValidation(() -> s.checkout(mixed, 99999))
                && s.findProduct("F1").getStock() == 10);
        check("duplicate product ID rejected", throwsValidation(() -> s.addProduct(new FoodItem("F1", "Dup", 1, 1, false))));
        check("negative stock rejected", throwsValidation(() -> new FoodItem("X", "Bad", -1, 10, false)));
        check("negative price rejected", throwsValidation(() -> new FoodItem("X", "Bad", 1, -10, false)));
        check("bad restock rejected", throwsValidation(() -> s.restock("M1", 0)));
    }

    private static void gctAndReceipt() {
        StadiumShop s = shop();
        Receipt r = s.checkout(basket("M1", 1), 2000);
        check("J$1,150 -> pre-tax 1000 + GCT 150", r.getSubtotal() == 1000.0 && r.getGct() == 150.0);
        check("GCT never added on top", r.getSubtotal() + r.getGct() == r.getTotal());
        check("Money.gctIncluded(8740) = 1140", Money.gctIncluded(8740) == 1140.0);
        String text = r.format();
        check("receipt shows GCT, total, paid and change", text.contains("GCT 15% (included)") && text.contains("TOTAL")
                && text.contains("Paid") && text.contains("Change"));
    }

    private static void polymorphism() {
        StadiumShop s = shop();
        Product jersey = s.findProduct("M1"), patty = s.findProduct("F1"), water = s.findProduct("B1");
        check("Product reference calls subclass getType()", jersey.getType().startsWith("Merchandise")
                && patty.getType().equals("Food") && water.getType().equals("Beverage"));
        Person spectator = new Spectator("SP9", "Poly", 25, "F");
        check("Spectator role through Person reference", spectator.getRole().equals("Spectator"));
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
