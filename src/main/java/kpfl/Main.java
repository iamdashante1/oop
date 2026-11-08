package kpfl;

import kpfl.exception.LeagueException;
import kpfl.integration.FixtureInfo;
import kpfl.integration.MenuModule;
import kpfl.integration.SalesProvider;
import kpfl.model.FootballLeague;
import kpfl.model.MatchResult;
import kpfl.service.ReportService;
import kpfl.storage.FileStorage;
import kpfl.storage.LoadOutcome;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * The program starts here (the main method), and this class shows the main menu.
 *
 * Menu options 1, 2, 3 and 6 belong to other members. Until they register a MenuModule
 * for an option, we just print "not connected yet". Options 4, 5, 7, 8, 9 and 10 are ours.
 */
public class Main {

    // Names for the menu numbers, so the code reads better than bare numbers.
    public static final int TEAMS = 1;
    public static final int STADIUMS = 2;
    public static final int FIXTURES = 3;
    public static final int RESULTS = 4;
    public static final int STANDINGS = 5;
    public static final int COMMISSIONER = 6;
    public static final int REPORTS = 7;
    public static final int SAVE = 8;
    public static final int LOAD = 9;
    public static final int EXIT = 10;

    private final FootballLeague league;
    private final FileStorage storage;
    private final ReportService reports;
    private final Scanner in;      // reads what the user types
    private final PrintStream out; // prints to the screen

    // Menu number -> the other member's menu (filled in with registerModule).
    private final Map<Integer, MenuModule> modules = new HashMap<>();

    // Becomes true if the input ends (e.g. Ctrl+Z), so we stop instead of looping forever.
    private boolean inputClosed = false;

    public Main(FootballLeague league, FileStorage storage, ReportService reports, Scanner in, PrintStream out) {
        this.league = league;
        this.storage = storage;
        this.reports = reports;
        this.in = in;
        this.out = out;
    }

    /** Plugs another member's menu into one of the menu numbers (TEAMS, STADIUMS, FIXTURES or COMMISSIONER). */
    public void registerModule(int option, MenuModule module) {
        modules.put(option, module);
    }

    /** The program's starting point. */
    public static void main(String[] args) {
        FootballLeague league = new FootballLeague();

        // Member 4 will provide this later. null means "sales module not connected yet".
        SalesProvider salesProvider = null;

        Main app = new Main(
                league,
                new FileStorage(Paths.get("data")),
                new ReportService(league, salesProvider),
                new Scanner(System.in),
                System.out);

        app.load(); // bring back anything saved last time
        app.run();  // show the menu
    }

    /** The menu loop: show menu, read a choice, do it, repeat until the user exits. */
    public void run() {
        boolean running = true;

        while (running && !inputClosed) {
            printMenu();
            int choice = readInt("Choose an option: ");

            // If anything goes wrong with a league rule, show a friendly message instead of crashing.
            try {
                switch (choice) {
                    case TEAMS:
                    case STADIUMS:
                    case FIXTURES:
                    case COMMISSIONER:
                        routeToModule(choice);
                        break;
                    case RESULTS:
                        recordResult();
                        break;
                    case STANDINGS:
                        out.println(reports.standingsReport());
                        break;
                    case REPORTS:
                        showReports();
                        break;
                    case SAVE:
                        save();
                        break;
                    case LOAD:
                        load();
                        break;
                    case EXIT:
                        running = false;
                        break;
                    default:
                        out.println("Please choose a number from 1 to 10.");
                }
            } catch (LeagueException e) {
                out.println("Error: " + e.getMessage());
            }
        }
        out.println("Goodbye.");
    }

    // Prints the main menu.
    private void printMenu() {
        out.println();
        out.println("===== KPFL - Kingston Premier Football League =====");
        out.println(" 1. Team management");
        out.println(" 2. Stadium management");
        out.println(" 3. Fixture management");
        out.println(" 4. Match results");
        out.println(" 5. League standings");
        out.println(" 6. League commissioner operations");
        out.println(" 7. Management reports");
        out.println(" 8. Save data");
        out.println(" 9. Load data");
        out.println("10. Exit");
    }

    // Hands over to another member's menu, or explains that it is not connected yet.
    private void routeToModule(int option) {
        MenuModule module = modules.get(option);
        if (module == null) {
            out.println("This section is not connected yet (it is provided by another team member's module).");
        } else {
            module.run(in);
        }
    }

    // Menu option 4: record the score of a fixture.
    private void recordResult() {
        // First list the fixtures that still need a result.
        out.println("Fixtures awaiting a result:");
        boolean foundAny = false;
        for (FixtureInfo fixture : league.getFixtures()) {
            if (!fixture.isCompleted() && league.findResult(fixture.getFixtureId()) == null) {
                out.println("  " + fixture.getFixtureId() + ": " + fixture.getHomeTeamId()
                        + " v " + fixture.getAwayTeamId() + " on " + fixture.getDateTime());
                foundAny = true;
            }
        }
        if (!foundAny) {
            out.println("  None. Fixtures are added through the fixture management section.");
            return;
        }

        // Ask which fixture and what the score was.
        out.print("Fixture id: ");
        if (!in.hasNextLine()) {
            inputClosed = true;
            return;
        }
        String fixtureId = in.nextLine().trim();
        int homeScore = readInt("Home score: ");
        int awayScore = readInt("Away score: ");
        if (inputClosed) {
            return; // input ended halfway, so do not record a half-typed result
        }

        // The league checks everything. If something is wrong it throws a LeagueException,
        // which run() catches and shows to the user.
        MatchResult result = league.recordResult(fixtureId, homeScore, awayScore);
        out.println("Result recorded: " + result.getScoreLine() + " (" + result.getOutcome() + ")");
    }

    // Menu option 7: choose which report to show.
    private void showReports() {
        out.println("1. Standings  2. Upcoming fixtures  3. Completed fixtures  4. All fixtures  5. Sales summary");
        int choice = readInt("Choose a report: ");

        if (choice == 1) {
            out.println(reports.standingsReport());
        } else if (choice == 2) {
            out.println(reports.fixturesReport(ReportService.FixtureView.UPCOMING));
        } else if (choice == 3) {
            out.println(reports.fixturesReport(ReportService.FixtureView.COMPLETED));
        } else if (choice == 4) {
            out.println(reports.fixturesReport(ReportService.FixtureView.ALL));
        } else if (choice == 5) {
            out.println(reports.salesSummaryReport());
        } else {
            out.println("Please choose a number from 1 to 5.");
        }
    }

    // Menu option 8: save the match results to a file.
    private void save() {
        try {
            storage.saveResults(league.getResults());
            out.println("Saved " + league.getResults().size() + " match result(s).");
        } catch (IOException e) {
            out.println("Save failed: " + e.getMessage());
        }
    }

    // Menu option 9 (also runs when the program starts): load saved match results.
    private void load() {
        try {
            LoadOutcome<MatchResult> outcome = storage.loadResults();

            int added = 0;
            for (MatchResult result : outcome.getRecords()) {
                try {
                    league.restoreResult(result);
                    added++;
                } catch (LeagueException e) {
                    // e.g. this fixture already has a result in memory
                    out.println("Skipped result for " + result.getFixtureId() + ": " + e.getMessage());
                }
            }

            // Tell the user about any damaged lines that were skipped.
            for (String warning : outcome.getWarnings()) {
                out.println(warning);
            }
            out.println("Loaded " + added + " match result(s).");

        } catch (IOException e) {
            out.println("Load failed: " + e.getMessage());
        }
    }

    // Asks for a whole number and keeps asking until the user types one.
    private int readInt(String prompt) {
        while (true) {
            out.print(prompt);

            if (!in.hasNextLine()) {
                inputClosed = true; // no more input, so stop cleanly
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
