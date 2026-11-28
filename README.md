# Kingston Premier Football League (KPFL) – Java console app

## Build and run (JDK 17+)

```
mkdir out
javac -d out -encoding UTF-8 $(find src/main -name "*.java")      # Git Bash
java -cp out kpfl.ui.Main
```

PowerShell: `javac -d out -encoding UTF-8 (Get-ChildItem -Recurse src/main -Filter *.java).FullName`

Run from the project folder so the `data/` folder is found. With Maven: `mvn package` builds `target/kpfl-1.0.jar` (start it with `java -jar`), and `mvn verify` runs all the tests.

## Tests
```
javac -d out -encoding UTF-8 $(find src/main src/test -name "*.java")
java -cp out kpfl.Member1Tests
java -cp out kpfl.Member2Tests
java -cp out kpfl.Member3Tests
java -cp out kpfl.Member4Tests
java -cp out kpfl.IntegrationTests
```
Results are in `docs/test-evidence.txt`; a scripted console session is in `docs/demo-evidence.txt`.

## Saving and reloading
Menu 6 > Save / Reload. On startup the app loads `data/` automatically if it exists (delete the folder contents for a fresh start).
Plain pipe-separated text files, one per record type (see the `FileStorage` class comment for every file and its columns).
Relationships are stored as IDs and rebuilt on load.

## Module status

**Shared class for every member:** Read the [Person contract](docs/person-contract.md) before changing `LeagueCommissioner`, `Player`, `Coach`, `MatchOfficial`, or `Spectator`. Their constructors call `super(id, name, age, gender)`, and each class provides `getRole()`.

| Package/class | Owner | Notes |
|---|---|---|
| `FootballLeague`, `MatchResult`, `LeagueCommissioner`, `StandingsEntry`, `ReportService`, `FileStorage`, `Main` | Member 1 | done; results can be corrected |
| `Person`, `Player`, `Coach`, `FootballTeam` | Member 2 | notes in `docs/member2-team-personnel.md`. Minimum player age is **16** (`Player.MIN_AGE`) |
| `Stadium`, `Fixture`, `MatchOfficial` | Member 3 | notes in `docs/MEMBER3_MODULE.md`. Search/update stadiums, fixtures and officials; reschedule, cancel; unassign officials. Practice-stadium request workflow not built |
| `Spectator`, `Ticket`, `Product`, `Merchandise`, `FoodItem`, `Receipt`, `StadiumShop`, `TicketService` | Member 4 | notes in `docs/member4-module.md`. `AppData.sales()` feeds the sales report |

## Rules implemented
- Win 3, draw 1, loss 0. Standings are computed from completed fixtures, so points can never be awarded twice.
- Negative scores rejected; a fixture only becomes COMPLETED through a valid linked `MatchResult`; a second result is rejected.
- Teams must be approved by the commissioner (ACTIVE) before they appear in a fixture; suspended teams are rejected.
- A team, a stadium or an official cannot be in two fixtures whose time slots overlap (each match is treated as 120 minutes, `Fixture.DURATION_MINUTES`). Cancelled fixtures free their slot.
- A scheduled fixture can be rescheduled or cancelled (reason required). Cancelling refunds its tickets; a cancelled fixture cannot take a result, officials or ticket sales.
- Tickets sold per fixture cannot exceed that stadium's capacity (VIP = 2x base price; cancelling frees a seat and shows as a refund in the sales report). A cancelled ticket cannot be sold again.
- Shop: negative/zero quantities, exhausted stock and insufficient payment are rejected; a failed sale changes nothing.
- All prices include 15% GCT: GCT = total x 15/115 (J$1,150 -> J$1,000 + J$150).
- The commissioner logs approvals, suspensions, official assignments and match-preparation notes (saved and reloaded).
