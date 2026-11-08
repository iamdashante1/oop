# Integration contracts (proposed – needs group agreement)

These are Member 1's proposed interfaces in `kpfl.integration`. Changing them affects Member 1's code, so please agree changes first.
All ids are `String`; dates/times use `LocalDate` / `LocalDateTime`.

## Member 2
* `FootballTeam implements TeamInfo` → `getTeamId()`, `getTeamName()`. Names must be unique (league rejects duplicates).
* `Person` must provide `Person(String personId, String name)`, `getPersonId()`, `getName()`. `LeagueCommissioner extends Person`.
* Registration: call `league.registerTeam(team)`.

## Member 3
* `Stadium implements StadiumInfo` → `getStadiumId()`, `getStadiumName()`.
* `Fixture implements FixtureInfo` → `getFixtureId()`, `getHomeTeamId()`, `getAwayTeamId()`, `getStadiumId()`, `getDateTime()`, `isCompleted()`, `markCompleted()`.
* Add stadiums/fixtures with `league.addStadium(..)` / `league.addFixture(..)` (teams and stadium must already be registered).
* `markCompleted()` is called by the league after a result is stored. Scheduling-conflict checks stay in Member 3's code.
* Officials are referenced by id only: `commissioner.assignOfficialToFixture(fixtureId, officialId)`.

## Member 4
* Provide a `SalesProvider` (on `StadiumShop` or a small facade): `getTotalTicketRevenue()`, `getTotalShopRevenue()` (`BigDecimal`), `getTicketsSold()`, `getShopTransactionCount()`.
* Pass it to `new ReportService(league, salesProvider)` in `Main.main` (currently `null` → report says data unavailable).

## Menus
Implement `MenuModule.run(Scanner)` for your section and register it:
`app.registerModule(Main.TEAMS | STADIUMS | FIXTURES | COMMISSIONER, module)`.

## File storage
Use `FileStorage.saveLines(fileName, lines)` / `readLines(fileName)` for your own files (safe save with `.bak`, missing file = empty list).
Format: one record per line, fields separated by `|`, relationships stored as ids (e.g. `fixtureId|homeTeamId|awayTeamId|...`).
Fields must not contain `|` or line breaks. Suggested files: `teams.txt`, `stadiums.txt`, `fixtures.txt`, `tickets.txt`, `sales.txt`.
Member 1's files: `results.txt` (`fixtureId|homeTeamId|awayTeamId|homeScore|awayScore|date`) and `commissioner.txt`.

## Current limitations
Nothing in options 1, 2, 3, 6 works until those modules exist. Teams/fixtures are not persisted by Member 1 (only results and commissioner records); on load, results are restored without needing the fixtures to exist yet.
`Main` saves only league results; saving `commissioner.txt` must be wired in when `LeagueCommissioner` is connected (`FileStorage.saveCommissionerRecords`).
