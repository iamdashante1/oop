package kpfl.storage;

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
import kpfl.service.StadiumShop;
import kpfl.service.TicketService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sequential text-file storage: one file per record type, one record per line, fields separated by '|'.
 * Relationships are saved as IDs and the object links are rebuilt on load (teams, then people, stadiums,
 * fixtures, officials, then sales).
 *
 *   commissioner.txt      id|name|age|gender
 *   preparation.txt       decision text
 *   teams.txt             registrationNo|name|parish|status
 *   players.txt           id|name|age|gender|teamNo|position|jersey|goals|status
 *   coaches.txt           id|name|age|gender|teamNo|qualification|role
 *   stadiums.txt          stadiumId|name|location|capacity|ticketPrice|fieldCondition
 *   officials.txt         id|name|age|gender|rank|available
 *   fixtures.txt          fixtureId|homeRegNo|awayRegNo|stadiumId|dateTime|status|homeScore|awayScore
 *   fixture_officials.txt fixtureId|officialId
 *   spectators.txt        id|name|age|gender
 *   tickets.txt           ticketId|fixtureId|spectatorId|category|price|status
 *   products.txt          kind(M/F)|id|name|stock|unitPrice|extra
 *   sales.txt             receiptId|dateTime|payment|items(productId:qty:unitPrice;...)
 */
public class FileStorage {
    private static final String SEP = "|";

    private final Path dir;

    public FileStorage(String directory) {
        this.dir = Paths.get(directory);
    }

    public boolean hasSavedData() {
        // teams.txt is the marker that a saved league exists in this folder.
        return Files.exists(dir.resolve("teams.txt"));
    }

    public void save(AppData app) {
        try {
            Files.createDirectories(dir);
            FootballLeague league = app.getLeague();

            List<String> lines = new ArrayList<>();
            LeagueCommissioner c = league.getCommissioner();
            if (c != null) lines.add(join(c.getId(), c.getName(), String.valueOf(c.getAge()), c.getGender()));
            writeLines("commissioner.txt", lines);

            lines = new ArrayList<>();
            if (c != null) for (String d : c.getDecisions()) lines.add(join(d));
            writeLines("preparation.txt", lines);

            List<String> teamLines = new ArrayList<>();
            List<String> playerLines = new ArrayList<>();
            List<String> coachLines = new ArrayList<>();
            // Save each person with a team number, not a whole FootballTeam object.
            for (FootballTeam t : league.getTeams()) {
                teamLines.add(join(t.getRegistrationNo(), t.getName(), t.getParish(), t.getStatus()));
                for (Player p : t.getPlayers())
                    playerLines.add(join(p.getId(), p.getName(), String.valueOf(p.getAge()), p.getGender(),
                            t.getRegistrationNo(), p.getPosition(), String.valueOf(p.getJerseyNumber()),
                            String.valueOf(p.getGoals()), p.getPlayerStatus()));
                for (Coach k : t.getCoaches())
                    coachLines.add(join(k.getId(), k.getName(), String.valueOf(k.getAge()), k.getGender(),
                            t.getRegistrationNo(), k.getQualification(), k.getCoachingRole()));
            }
            writeLines("teams.txt", teamLines);
            writeLines("players.txt", playerLines);
            writeLines("coaches.txt", coachLines);

            lines = new ArrayList<>();
            for (Stadium s : league.getStadiums())
                lines.add(join(s.getStadiumId(), s.getName(), s.getLocation(), String.valueOf(s.getCapacity()),
                        String.valueOf(s.getTicketPrice()), s.getFieldCondition()));
            writeLines("stadiums.txt", lines);

            lines = new ArrayList<>();
            for (MatchOfficial o : league.getOfficials())
                lines.add(join(o.getId(), o.getName(), String.valueOf(o.getAge()), o.getGender(), o.getRank(),
                        String.valueOf(o.isAvailable())));
            writeLines("officials.txt", lines);

            List<String> fixtureLines = new ArrayList<>();
            List<String> assignLines = new ArrayList<>();
            // Fixtures store team/stadium IDs; official assignments get their own file.
            for (Fixture f : league.getFixtures()) {
                MatchResult r = f.getResult();
                fixtureLines.add(join(f.getFixtureId(), f.getHomeTeam().getRegistrationNo(),
                        f.getAwayTeam().getRegistrationNo(), f.getStadium().getStadiumId(),
                        f.getDateTime().toString(), f.getStatus().name(),
                        r == null ? "" : String.valueOf(r.getHomeScore()),
                        r == null ? "" : String.valueOf(r.getAwayScore())));
                for (MatchOfficial o : f.getOfficials()) assignLines.add(join(f.getFixtureId(), o.getId()));
            }
            writeLines("fixtures.txt", fixtureLines);
            writeLines("fixture_officials.txt", assignLines);

            lines = new ArrayList<>();
            for (Spectator s : app.getTickets().getSpectators())
                lines.add(join(s.getId(), s.getName(), String.valueOf(s.getAge()), s.getGender()));
            writeLines("spectators.txt", lines);

            lines = new ArrayList<>();
            for (Ticket t : app.getTickets().getTickets())
                lines.add(join(t.getTicketId(), t.getFixtureId(), t.getSpectatorId(), t.getCategory().name(),
                        String.valueOf(t.getPrice()), t.getStatus().name()));
            writeLines("tickets.txt", lines);

            lines = new ArrayList<>();
            for (Product p : app.getShop().getInventory())
                lines.add(join(p instanceof Merchandise ? "M" : "F", p.getProductId(), p.getName(),
                        String.valueOf(p.getStock()), String.valueOf(p.getUnitPrice()), p.getExtra()));
            writeLines("products.txt", lines);

            lines = new ArrayList<>();
            for (Receipt r : app.getShop().getSales()) {
                StringBuilder items = new StringBuilder();
                // A receipt row keeps all purchased items in one semicolon-separated field.
                for (Receipt.Line l : r.getLines()) {
                    if (items.length() > 0) items.append(';');
                    items.append(l.getProductId()).append(':').append(l.getQuantity()).append(':').append(l.getUnitPrice());
                }
                lines.add(join(r.getReceiptId(), r.getTime().toString(), String.valueOf(r.getPayment()), items.toString()));
            }
            writeLines("sales.txt", lines);
        } catch (IOException e) {
            throw new ValidationException("Could not save data: " + e.getMessage());
        }
    }

    public AppData load() {
        if (!hasSavedData()) throw new ValidationException("No saved data found in " + dir.toAbsolutePath() + ".");
        try {
            // Build a fresh object graph; load parents before records that refer to them.
            AppData app = new AppData();
            FootballLeague league = app.getLeague();

            for (String[] p : readLines("commissioner.txt", 4))
                league.setCommissioner(new LeagueCommissioner(p[0], p[1], Integer.parseInt(p[2]), p[3]));
            if (league.getCommissioner() != null)
                for (String[] p : readLines("preparation.txt", 1)) league.getCommissioner().recordDecision(p[0]);

            // Teams must exist before saved players and coaches can be assigned.
            for (String[] p : readLines("teams.txt", 4)) {
                FootballTeam t = new FootballTeam(p[0], p[1], p[2]);
                t.setStatus(p[3]);
                league.registerTeam(t);
            }
            for (String[] p : readLines("players.txt", 9)) {
                Player pl = new Player(p[0], p[1], Integer.parseInt(p[2]), p[3], p[5], Integer.parseInt(p[6]));
                pl.setGoals(Integer.parseInt(p[7]));
                pl.setPlayerStatus(p[8]);
                league.addPlayer(p[4], pl);
            }
            for (String[] p : readLines("coaches.txt", 7))
                league.addCoach(p[4], new Coach(p[0], p[1], Integer.parseInt(p[2]), p[3], p[5], p[6]));

            for (String[] p : readLines("stadiums.txt", 6))
                league.addStadium(new Stadium(p[0], p[1], p[2], Integer.parseInt(p[3]), Double.parseDouble(p[4]), p[5]));

            for (String[] p : readLines("officials.txt", 6)) {
                MatchOfficial o = new MatchOfficial(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4]);
                o.setAvailable(Boolean.parseBoolean(p[5]));
                league.registerOfficial(o);
            }

            // A fixture needs its teams and stadium; a completed one also needs its result.
            for (String[] p : readLines("fixtures.txt", 8)) {
                Fixture f = new Fixture(p[0], league.findTeam(p[1]), league.findTeam(p[2]),
                        league.findStadium(p[3]), LocalDateTime.parse(p[4]));
                if (Fixture.Status.valueOf(p[5]) == Fixture.Status.COMPLETED)
                    f.complete(new MatchResult(p[0], Integer.parseInt(p[6]), Integer.parseInt(p[7])));
                league.restoreFixture(f);
            }
            for (String[] p : readLines("fixture_officials.txt", 2)) league.restoreOfficialAssignment(p[0], p[1]);

            TicketService tickets = app.getTickets();
            for (String[] p : readLines("spectators.txt", 4))
                tickets.addSpectator(new Spectator(p[0], p[1], Integer.parseInt(p[2]), p[3]));
            for (String[] p : readLines("tickets.txt", 6)) {
                Ticket t = new Ticket(p[0], p[1], p[2], Ticket.Category.valueOf(p[3]), Double.parseDouble(p[4]));
                t.setStatus(Ticket.Status.valueOf(p[5]));
                tickets.restoreTicket(t);
            }

            StadiumShop shop = app.getShop();
            for (String[] p : readLines("products.txt", 6)) {
                int stock = Integer.parseInt(p[3]);
                double price = Double.parseDouble(p[4]);
                shop.addProduct("M".equals(p[0]) ? new Merchandise(p[1], p[2], stock, price, p[5])
                        : new FoodItem(p[1], p[2], stock, price, Boolean.parseBoolean(p[5])));
            }
            for (String[] p : readLines("sales.txt", 4)) {
                List<Receipt.Line> lines = new ArrayList<>();
                // Rebuild receipt lines using each product's saved quantity and price.
                for (String item : p[3].split(";")) {
                    String[] f = item.split(":");
                    Product prod = shop.findProduct(f[0]);
                    lines.add(new Receipt.Line(f[0], prod.getName(), Integer.parseInt(f[1]), Double.parseDouble(f[2])));
                }
                shop.restoreSale(new Receipt(p[0], LocalDateTime.parse(p[1]), lines, Double.parseDouble(p[2])));
            }
            return app;
        } catch (IOException | RuntimeException e) {
            if (e instanceof ValidationException) throw (ValidationException) e;
            throw new ValidationException("Saved data is damaged or unreadable: " + e.getMessage());
        }
    }

    // ---- helpers ----

    public void writeLines(String fileName, List<String> lines) throws IOException {
        Files.write(dir.resolve(fileName), lines, StandardCharsets.UTF_8);
    }

    /** Reads a file written by writeLines; a missing file gives an empty list. */
    public List<String[]> readLines(String fileName, int fieldCount) throws IOException {
        List<String[]> rows = new ArrayList<>();
        Path file = dir.resolve(fileName);
        if (!Files.exists(file)) return rows;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) continue;
            // -1 keeps empty columns, such as missing scores for an upcoming fixture.
            String[] parts = line.split("\\|", -1);
            if (parts.length != fieldCount)
                throw new ValidationException(fileName + " has a malformed line: " + line);
            rows.add(parts);
        }
        return rows;
    }

    /** Joins fields, replacing the separator character so a name can never break the format. */
    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(SEP);
            sb.append(fields[i] == null ? "" : fields[i].replace('|', '/').replace('\n', ' '));
        }
        return sb.toString();
    }
}
