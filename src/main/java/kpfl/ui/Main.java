package kpfl.ui;

import kpfl.exception.ValidationException;
import kpfl.model.Coach;
import kpfl.model.FoodItem;
import kpfl.model.Fixture;
import kpfl.model.FootballTeam;
import kpfl.model.LeagueCommissioner;
import kpfl.model.MatchOfficial;
import kpfl.model.MatchResult;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Console menu for the Kingston Premier Football League. Routes input to the services. */
public class Main {
    private static final DateTimeFormatter INPUT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Scanner in = new Scanner(System.in);
    private final FileStorage storage = new FileStorage("data");
    private AppData app = new AppData();
    private boolean running = true;

    public static void main(String[] args) {
        new Main().run();
    }

    private FootballLeague league() { return app.getLeague(); }

    private void run() {
        System.out.println("=== Kingston Premier Football League (KPFL) ===");
        if (storage.hasSavedData()) {
            attempt(() -> {
                app = storage.load();
                System.out.println("Loaded saved data from the data/ folder.");
            });
        } else {
            league().setCommissioner(new LeagueCommissioner("C001", "League Commissioner", 45, "N/A"));
        }

        while (running) {
            System.out.println();
            System.out.println("MAIN MENU");
            System.out.println("1  Teams, players & coaches");
            System.out.println("2  Stadiums, fixtures & officials");
            System.out.println("3  Record match result");
            System.out.println("4  Tickets & stadium shop");
            System.out.println("5  Reports");
            System.out.println("6  Data (save / reload / sample data)");
            System.out.println("0  Exit");
            switch (prompt("Choose an option")) {
                case "1": teamsMenu(); break;
                case "2": fixturesMenu(); break;
                case "3": attempt(this::recordResult); break;
                case "4": salesMenu(); break;
                case "5": reportsMenu(); break;
                case "6": dataMenu(); break;
                case "0": exit(); break;
                default: System.out.println("Please choose a number from the menu.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void exit() {
        if (prompt("Save before exiting? (y/n)").equalsIgnoreCase("y")) attempt(this::save);
        running = false;
    }

    /** Shows a submenu until the user enters 0. Each action is guarded so errors never crash the app. */
    private void subMenu(String title, String[] labels, Runnable[] actions) {
        while (true) {
            System.out.println();
            System.out.println(title.toUpperCase());
            for (int i = 0; i < labels.length; i++) System.out.println((i + 1) + "  " + labels[i]);
            System.out.println("0  Back");
            String choice = prompt("Choose an option");
            if (choice.equals("0")) return;
            try {
                int i = Integer.parseInt(choice);
                if (i >= 1 && i <= actions.length) {
                    attempt(actions[i - 1]);
                    continue;
                }
            } catch (NumberFormatException ignored) {
                // fall through to message
            }
            System.out.println("Please choose a number from the menu.");
        }
    }

    private void teamsMenu() {
        subMenu("Teams, players & coaches",
                new String[] {"Register team", "Approve team", "Suspend team", "List / search teams",
                        "Add player", "Remove player", "Add coach", "Show a team's squad"},
                new Runnable[] {this::registerTeam, this::approveTeam, this::suspendTeam, this::listTeams,
                        this::addPlayer, this::removePlayer, this::addCoach, this::showSquad});
    }

    private void fixturesMenu() {
        subMenu("Stadiums, fixtures & officials",
                new String[] {"Register stadium", "List stadiums", "Schedule fixture", "Register match official",
                        "Assign official to fixture", "Record match-preparation note", "Show commissioner decisions"},
                new Runnable[] {this::registerStadium, this::listStadiums, this::scheduleFixture,
                        this::registerOfficial, this::assignOfficial, this::preparationNote, this::showDecisions});
    }

    private void salesMenu() {
        subMenu("Tickets & stadium shop",
                new String[] {"Add spectator", "Sell ticket", "Cancel ticket", "Add product", "Restock product",
                        "List inventory", "Shop sale (with receipt)"},
                new Runnable[] {this::addSpectator, this::sellTicket, this::cancelTicket, this::addProduct,
                        this::restock, this::listInventory, this::shopSale});
    }

    private void reportsMenu() {
        subMenu("Reports",
                new String[] {"League standings", "Fixtures (upcoming & completed)", "Ticket & shop sales summary"},
                new Runnable[] {() -> System.out.println(reports().standingsReport()),
                        () -> System.out.println(reports().fixturesReport()),
                        () -> System.out.println(reports().salesReport())});
    }

    private void dataMenu() {
        subMenu("Data",
                new String[] {"Save data", "Reload saved data", "Load sample data"},
                new Runnable[] {this::save, () -> { app = storage.load(); System.out.println("Data reloaded."); },
                        () -> { app = SampleData.create(); System.out.println("Sample data loaded."); }});
    }

    private void save() {
        storage.save(app);
        System.out.println("Data saved to the data/ folder.");
    }

    // ---- teams and people ----

    private void registerTeam() {
        FootballTeam team = new FootballTeam(prompt("Registration no"), prompt("Team name"), prompt("Parish"));
        league().registerTeam(team);
        System.out.println("Registered " + team.getName() + " (PENDING approval).");
    }

    private void approveTeam() {
        league().approveTeam(prompt("Registration no to approve"));
        System.out.println("Team approved.");
    }

    private void suspendTeam() {
        league().suspendTeam(prompt("Registration no to suspend"));
        System.out.println("Team suspended.");
    }

    private void listTeams() {
        List<FootballTeam> teams = league().searchTeams(prompt("Search (blank for all)"));
        if (teams.isEmpty()) System.out.println("No teams found.");
        for (FootballTeam t : teams) System.out.println(t);
    }

    private void addPlayer() {
        String teamNo = prompt("Team registration no");
        league().addPlayer(teamNo, new Player(prompt("Player ID"), prompt("Name"), readInt("Age"),
                prompt("Gender"), prompt("Position"), readInt("Jersey number")));
        System.out.println("Player added.");
    }

    private void removePlayer() {
        league().removePlayer(prompt("Team registration no"), prompt("Player ID"));
        System.out.println("Player removed.");
    }

    private void addCoach() {
        String teamNo = prompt("Team registration no");
        league().addCoach(teamNo, new Coach(prompt("Coach ID"), prompt("Name"), readInt("Age"),
                prompt("Gender"), prompt("Qualification"), prompt("Coaching role")));
        System.out.println("Coach added.");
    }

    private void showSquad() {
        FootballTeam t = league().findTeam(prompt("Team registration no"));
        System.out.println(t);
        for (Player p : t.getPlayers()) System.out.println("  Player " + p.getId() + ": " + p);
        for (Coach c : t.getCoaches()) System.out.println("  Coach  " + c.getId() + ": " + c);
    }

    // ---- stadiums, fixtures, officials ----

    private void registerStadium() {
        Stadium s = new Stadium(prompt("Stadium ID"), prompt("Name"), prompt("Location"),
                readInt("Seating capacity"), readMoney("Ticket price (GCT incl.)"), prompt("Field condition"));
        league().addStadium(s);
        System.out.println("Registered " + s.getName() + ".");
    }

    private void listStadiums() {
        if (league().getStadiums().isEmpty()) System.out.println("No stadiums registered.");
        for (Stadium s : league().getStadiums()) System.out.println(s);
    }

    private void scheduleFixture() {
        String id = prompt("Fixture ID");
        FootballTeam home = league().findTeam(prompt("Home team registration no"));
        FootballTeam away = league().findTeam(prompt("Away team registration no"));
        Stadium stadium = league().findStadium(prompt("Stadium ID"));
        LocalDateTime when = readDateTime("Date and time (yyyy-MM-dd HH:mm)");
        Fixture f = new Fixture(id, home, away, stadium, when);
        league().addFixture(f);
        System.out.println("Scheduled: " + f);
    }

    private void registerOfficial() {
        MatchOfficial o = new MatchOfficial(prompt("Official ID"), prompt("Name"), readInt("Age"),
                prompt("Gender"), prompt("Rank (e.g. Referee)"));
        league().registerOfficial(o);
        System.out.println("Registered " + o + ".");
    }

    private void assignOfficial() {
        league().assignOfficial(prompt("Fixture ID"), prompt("Official ID"));
        System.out.println("Official assigned.");
    }

    private void preparationNote() {
        league().recordPreparationNote(prompt("Fixture ID"), prompt("Note"));
        System.out.println("Note recorded.");
    }

    private void showDecisions() {
        LeagueCommissioner c = league().getCommissioner();
        if (c == null || c.getDecisions().isEmpty()) System.out.println("No decisions recorded.");
        else for (String d : c.getDecisions()) System.out.println("- " + d);
    }

    private void recordResult() {
        String id = prompt("Fixture ID");
        int home = readInt("Home score");
        int away = readInt("Away score");
        MatchResult r = league().recordResult(id, home, away);
        System.out.println("Result recorded: " + r + " (" + r.getOutcome() + ")");
    }

    // ---- tickets and shop ----

    private void addSpectator() {
        app.getTickets().addSpectator(new Spectator(prompt("Spectator ID"), prompt("Name"), readInt("Age"),
                prompt("Gender")));
        System.out.println("Spectator added.");
    }

    private void sellTicket() {
        String fixtureId = prompt("Fixture ID");
        String spectatorId = prompt("Spectator ID");
        Ticket.Category category;
        try {
            category = Ticket.Category.valueOf(prompt("Category (GENERAL/VIP)").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Category must be GENERAL or VIP.");
        }
        Ticket t = app.getTickets().sellTicket(fixtureId, spectatorId, category);
        System.out.println("Ticket sold: " + t);
    }

    private void cancelTicket() {
        app.getTickets().cancelTicket(prompt("Ticket ID"));
        System.out.println("Ticket cancelled.");
    }

    private void addProduct() {
        String kind = prompt("Type (M = merchandise, F = food, B = beverage)").toUpperCase();
        String id = prompt("Product ID");
        String name = prompt("Name");
        int stock = readInt("Stock quantity");
        double price = readMoney("Unit price (GCT incl.)");
        Product p;
        if (kind.equals("M")) p = new Merchandise(id, name, stock, price, prompt("Size"));
        else if (kind.equals("F") || kind.equals("B")) p = new FoodItem(id, name, stock, price, kind.equals("B"));
        else throw new ValidationException("Type must be M, F or B.");
        app.getShop().addProduct(p);
        System.out.println("Added " + p);
    }

    private void restock() {
        app.getShop().restock(prompt("Product ID"), readInt("Quantity to add"));
        System.out.println("Stock updated.");
    }

    private void listInventory() {
        if (app.getShop().getInventory().isEmpty()) System.out.println("Inventory is empty.");
        for (Product p : app.getShop().getInventory()) System.out.println(p);
    }

    private void shopSale() {
        Map<String, Integer> basket = new LinkedHashMap<>();
        System.out.println("Enter items; leave product ID blank to finish.");
        while (true) {
            String id = prompt("Product ID");
            if (id.isEmpty()) break;
            basket.merge(id, readInt("Quantity"), Integer::sum);
        }
        double payment = readMoney("Amount paid");
        Receipt r = app.getShop().checkout(basket, payment);
        System.out.println(r.format());
    }

    // ---- input helpers ----

    private ReportService reports() {
        return new ReportService(league(), app.sales());
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    private int readInt(String label) {
        String text = prompt(label);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' is not a whole number.");
        }
    }

    private double readMoney(String label) {
        String text = prompt(label);
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' is not a valid amount.");
        }
    }

    private LocalDateTime readDateTime(String label) {
        String text = prompt(label);
        try {
            return LocalDateTime.parse(text, INPUT_TIME);
        } catch (DateTimeParseException e) {
            throw new ValidationException("'" + text + "' is not a valid date/time. Use yyyy-MM-dd HH:mm.");
        }
    }

    /** Runs an action and turns any rule violation into a friendly message instead of a crash. */
    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Unexpected problem: " + e.getMessage());
        }
    }
}
