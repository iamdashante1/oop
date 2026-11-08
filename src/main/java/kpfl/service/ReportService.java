package kpfl.service;

import kpfl.model.Fixture;
import kpfl.model.MatchOfficial;
import kpfl.model.Money;
import kpfl.model.StandingsEntry;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Builds the three management reports as printable text. */
public class ReportService {
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FootballLeague league;
    private final SalesProvider sales;

    public ReportService(FootballLeague league, SalesProvider sales) {
        this.league = league;
        this.sales = sales == null ? SalesProvider.NONE : sales;
    }

    public String standingsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== KPFL LEAGUE STANDINGS ===\n");
        sb.append(String.format("%-3s %-24s %3s %3s %3s %3s %4s %4s %4s %4s%n",
                "#", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts"));
        // FootballLeague calculates and sorts the rows; this class only prints them.
        List<StandingsEntry> rows = league.getStandings();
        if (rows.isEmpty()) sb.append("(no approved teams yet)\n");
        int pos = 1;
        for (StandingsEntry e : rows) {
            sb.append(String.format("%-3d %-24s %3d %3d %3d %3d %4d %4d %4d %4d%n",
                    pos++, e.getTeam().getName(), e.getPlayed(), e.getWon(), e.getDrawn(), e.getLost(),
                    e.getGoalsFor(), e.getGoalsAgainst(), e.getGoalDifference(), e.getPoints()));
        }
        return sb.toString();
    }

    public String fixturesReport() {
        StringBuilder sb = new StringBuilder("=== KPFL FIXTURES ===\n");
        // Show matches in two groups based on whether a result has been recorded.
        appendFixtures(sb, "Upcoming", league.getFixtures(Fixture.Status.SCHEDULED));
        appendFixtures(sb, "Completed", league.getFixtures(Fixture.Status.COMPLETED));
        return sb.toString();
    }

    private void appendFixtures(StringBuilder sb, String title, List<Fixture> list) {
        sb.append("-- ").append(title).append(" (").append(list.size()).append(") --\n");
        if (list.isEmpty()) sb.append("(none)\n");
        for (Fixture f : list) {
            sb.append(String.format("%-6s %s  %-20s %s %-20s  @ %s%n",
                    f.getFixtureId(), f.getDateTime().format(WHEN), f.getHomeTeam().getName(),
                    f.getResult() == null ? "vs" : f.getResult().toString(),
                    f.getAwayTeam().getName(), f.getStadium().getName()));
            if (!f.getOfficials().isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (MatchOfficial o : f.getOfficials()) {
                    if (names.length() > 0) names.append(", ");
                    names.append(o.getName()).append(" (").append(o.getRank()).append(")");
                }
                sb.append("       Officials: ").append(names).append("\n");
            }
        }
    }

    public String salesReport() {
        StringBuilder sb = new StringBuilder("=== KPFL TICKET & SHOP SALES SUMMARY ===\n");
        sb.append(String.format("%-6s %-34s %8s %8s %12s%n", "Fix", "Match", "Sold", "Cap", "Revenue"));
        int totalTickets = 0;
        double ticketTotal = 0;
        // Gather ticket sales for each fixture, then add shop revenue below.
        for (Fixture f : league.getFixtures()) {
            int sold = sales.ticketsSold(f.getFixtureId());
            double revenue = sales.ticketRevenue(f.getFixtureId());
            totalTickets += sold;
            ticketTotal += revenue;
            sb.append(String.format("%-6s %-34s %8d %8d %12s%n", f.getFixtureId(),
                    f.getHomeTeam().getName() + " v " + f.getAwayTeam().getName(),
                    sold, f.getStadium().getCapacity(), money(revenue)));
        }
        double shop = sales.shopRevenue();
        double grand = ticketTotal + shop;
        // All revenue already includes GCT, so display the tax portion separately.
        sb.append("-----\n");
        sb.append(String.format("Tickets sold: %d   Ticket revenue: %s%n", totalTickets, money(ticketTotal)));
        sb.append(String.format("Shop transactions: %d   Shop revenue: %s%n", sales.shopTransactionCount(), money(shop)));
        sb.append(String.format("TOTAL (GCT incl.): %s   of which GCT 15%%: %s   pre-tax: %s%n",
                money(grand), money(Money.gctIncluded(grand)), money(Money.preTax(grand))));
        return sb.toString();
    }

    private static String money(double amount) {
        return Money.format(amount);
    }
}
