package kpfl;

import kpfl.exception.LeagueException;
import kpfl.integration.FixtureInfo;
import kpfl.integration.MenuModule;
import kpfl.integration.SalesProvider;
import kpfl.model.FootballLeague;
import kpfl.service.ReportService;
import kpfl.storage.FileStorage;
import kpfl.storage.LoadOutcome;
import kpfl.model.MatchResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Console entry point and menu router.
 * Options 1, 2, 3 and 6 belong to other members: they run only once a {@link MenuModule}
 * has been registered for that option number, otherwise a "not connected" message is shown.
 */
public class Main {
    public static final int TEAMS = 1, STADIUMS = 2, FIXTURES = 3, RESULTS = 4, STANDINGS = 5,
            COMMISSIONER = 6, REPORTS = 7, SAVE = 8, LOAD = 9, EXIT = 10;

    private final FootballLeague league;
    private final FileStorage storage;
    private final ReportService reports;
    private final Scanner in;
    private final PrintStream out;
    private final Map<Integer, MenuModule> modules = new HashMap<>();
    private boolean inputClosed;

    public Main(FootballLeague league, FileStorage storage, ReportService reports, Scanner in, PrintStream out) {
        this.league = league;
        this.storage = storage;
        this.reports = reports;
        this.in = in;
        this.out = out;
    }

    /** Connects another member's menu section to a menu option (TEAMS, STADIUMS, FIXTURES or COMMISSIONER). */
    public void registerModule(int option, MenuModule module) {
        modules.put(option, module);
    }

    public static void main(String[] args) {
        FootballLeague league = new FootballLeague();
        SalesProvider salesProvider = null; // Member 4 supplies this once their module exists
        Main app = new Main(league, new FileStorage(Path.of("data")),
                new ReportService(league, salesProvider), new Scanner(System.in), System.out);
        app.load();
        app.run();
    }

    public void run() {
        boolean running = true;
        while (running && !inputClosed) {
            printMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case TEAMS, STADIUMS, FIXTURES, COMMISSIONER -> routeToModule(choice);
                    case RESULTS -> recordResult();
                    case STANDINGS -> out.println(reports.standingsReport());
                    case REPORTS -> showReports();
                    case SAVE -> save();
                    case LOAD -> load();
                    case EXIT -> running = false;
                    default -> out.println("Please choose a number from 1 to 10.");
                }
            } catch (LeagueException e) {
                out.println("Error: " + e.getMessage());
            }
        }
        out.println("Goodbye.");
    }

    private void printMenu() {
        out.println("""

                ===== KPFL - Kingston Premier Football League =====
                 1. Team management
                 2. Stadium management
                 3. Fixture management
                 4. Match results
                 5. League standings
                 6. League commissioner operations
                 7. Management reports
                 8. Save data
                 9. Load data
                10. Exit""");
    }

    private void routeToModule(int option) {
        MenuModule module = modules.get(option);
        if (module == null) {
            out.println("This section is not connected yet (it is provided by another team member's module).");
        } else {
            module.run(in);
        }
    }

    private void recordResult() {
        out.println("Fixtures awaiting a result:");
        boolean any = false;
        for (FixtureInfo f : league.getFixtures()) {
            if (!f.isCompleted() && league.findResult(f.getFixtureId()) == null) {
                out.println("  " + f.getFixtureId() + ": " + f.getHomeTeamId() + " v " + f.getAwayTeamId()
                        + " on " + f.getDateTime());
                any = true;
            }
        }
        if (!any) {
            out.println("  None. Fixtures are added through the fixture management section.");
            return;
        }
        out.print("Fixture id: ");
        String fixtureId = in.nextLine().trim();
        int home = readInt("Home score: ");
        int away = readInt("Away score: ");
        if (inputClosed) return;
        MatchResult result = league.recordResult(fixtureId, home, away);
        out.println("Result recorded: " + result.getScoreLine() + " (" + result.getOutcome() + ")");
    }

    private void showReports() {
        out.println("1. Standings  2. Upcoming fixtures  3. Completed fixtures  4. All fixtures  5. Sales summary");
        switch (readInt("Choose a report: ")) {
            case 1 -> out.println(reports.standingsReport());
            case 2 -> out.println(reports.fixturesReport(ReportService.FixtureView.UPCOMING));
            case 3 -> out.println(reports.fixturesReport(ReportService.FixtureView.COMPLETED));
            case 4 -> out.println(reports.fixturesReport(ReportService.FixtureView.ALL));
            case 5 -> out.println(reports.salesSummaryReport());
            default -> out.println("Please choose a number from 1 to 5.");
        }
    }

    private void save() {
        try {
            storage.saveResults(league.getResults());
            out.println("Saved " + league.getResults().size() + " match result(s).");
        } catch (IOException e) {
            out.println("Save failed: " + e.getMessage());
        }
    }

    private void load() {
        try {
            LoadOutcome<MatchResult> outcome = storage.loadResults();
            int added = 0;
            for (MatchResult r : outcome.records()) {
                try {
                    league.restoreResult(r);
                    added++;
                } catch (LeagueException e) {
                    out.println("Skipped result for " + r.getFixtureId() + ": " + e.getMessage());
                }
            }
            outcome.warnings().forEach(out::println);
            out.println("Loaded " + added + " match result(s).");
        } catch (IOException e) {
            out.println("Load failed: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        while (true) {
            out.print(prompt);
            if (!in.hasNextLine()) {
                inputClosed = true; // input closed: leave cleanly
                return EXIT;
            }
            String line = in.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                out.println("Please enter a whole number.");
            }
        }
    }
}
