# Kingston Premier Football League (KPFL) – Java console app

## Build and run (JDK 11+)

```
mkdir out
javac -d out -encoding UTF-8 $(find src/main -name "*.java")      # Git Bash
java -cp out kpfl.ui.Main
```

PowerShell: `javac -d out -encoding UTF-8 (Get-ChildItem -Recurse src/main -Filter *.java).FullName`

Tests (Member 1): `javac -d out -cp out src/test/java/kpfl/Member1Tests.java && java -cp out kpfl.Member1Tests`

Run from the project folder so the `data/` folder is found.

## Tests
```
javac -d out -cp out src/test/java/kpfl/Member1Tests.java src/test/java/kpfl/IntegrationTests.java
java -cp out kpfl.Member1Tests
java -cp out kpfl.IntegrationTests
```
Results are in `docs/test-evidence.txt`; a scripted console session is in `docs/demo-evidence.txt`.

## Saving and reloading
Menu 6 > Save / Reload. On startup the app loads `data/` automatically if it exists (delete the folder contents for a fresh start).
Plain pipe-separated text files, one per record type (see the `FileStorage` class comment for every file and its columns).
Relationships are stored as IDs and rebuilt on load.

## Module status
| Package/class | Owner | Notes |
|---|---|---|
| `FootballLeague`, `MatchResult`, `LeagueCommissioner`, `StandingsEntry`, `ReportService`, `FileStorage`, `Main` | Member 1 | done |
| `Person`, `Player`, `Coach`, `FootballTeam` | Member 2 | working first version written by Member 1 so the app runs. Member 2 should review/replace; keep signatures. Minimum player age is a **assumed 16** (`Player.MIN_AGE`) pending the lecturer |
| `Stadium`, `Fixture`, `MatchOfficial` | Member 3 | working first version (double-booking checks for teams, stadiums and officials live in `FootballLeague.addFixture` / `assignOfficial`). Practice-stadium request workflow not built |
| `Spectator`, `Ticket`, `Product`, `Merchandise`, `FoodItem`, `Receipt`, `StadiumShop`, `TicketService` | Member 4 | working first version; `AppData.sales()` feeds the sales report |

## Rules implemented
- Win 3, draw 1, loss 0. Standings are computed from completed fixtures, so points can never be awarded twice.
- Negative scores rejected; a fixture only becomes COMPLETED through a valid linked `MatchResult`; a second result is rejected.
- Teams must be approved by the commissioner (ACTIVE) before they appear in a fixture; suspended teams are rejected.
- A team, a stadium or an official cannot be in two fixtures at the same date and time.
- Tickets sold per fixture cannot exceed that stadium's capacity (VIP = 2x base price; cancelling frees a seat).
- Shop: negative/zero quantities, exhausted stock and insufficient payment are rejected; a failed sale changes nothing.
- All prices include 15% GCT: GCT = total x 15/115 (J$1,150 -> J$1,000 + J$150).
- The commissioner logs approvals, suspensions, official assignments and match-preparation notes (saved and reloaded).
