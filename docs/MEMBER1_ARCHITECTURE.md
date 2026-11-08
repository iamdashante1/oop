# Member 1 – Architecture

Packages under `src/main/java/kpfl`:

| Class | Package | Role |
|---|---|---|
| `FootballLeague` | model | Holds teams, stadiums, fixtures and results (by id). Registration, search, `recordResult`, `calculateStandings`. Uses only the `integration` interfaces. |
| `MatchResult` | model | Immutable result. Rejects negative scores; gives winner, draw flag and points (win 3, draw 1 each, loss 0). |
| `StandingsEntry` | model | One row of the table (supports `FootballLeague`). |
| `LeagueCommissioner` | model | Extends Member 2's `Person`. Delegates to `CommissionerRecords`. |
| `CommissionerRecords` | model | Team approvals, official assignments (by id), preparation decisions. Separate so it can be tested without `Person`. |
| `ReportService` | service | Standings, fixtures (all/upcoming/completed) and sales summary reports as text. |
| `FileStorage` (+ `LoadOutcome`) | storage | Line-based `|`-separated files; temp-file + `.bak` on save; bad lines skipped with warnings. |
| `Main` | kpfl | Menu loop and routing. |
| `LeagueException` | exception | Rule violations. |
| interfaces | integration | `TeamInfo`, `StadiumInfo`, `FixtureInfo`, `SalesProvider`, `MenuModule` – the contracts other members implement. |

## Rules enforced
* A fixture gets exactly one result (`recordResult` rejects a second; `restoreResult` rejects duplicates), so points can't be awarded twice.
* A fixture cannot be completed without a valid result: the `MatchResult` is built (and validated) before `markCompleted()` is called.
* Standings are computed from stored results every time, never stored or hard-coded.
  Order: points, goal difference, goals scored, then team name.

## Not implemented here (owned by others)
Person, Player, Coach, FootballTeam (M2); Stadium, Fixture, MatchOfficial (M3); Spectator, Ticket, Product, Merchandise, FoodItem, StadiumShop (M4).
