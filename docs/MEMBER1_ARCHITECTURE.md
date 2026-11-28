# Member 1 – Architecture

Packages under `src/main/java/kpfl`:

| Class | Package | Role |
|---|---|---|
| `FootballLeague` | service | Holds teams, stadiums, fixtures and officials. Registration, search, scheduling checks, `recordResult`, `correctResult`, `getStandings`. |
| `MatchResult` | model | Immutable result. Rejects negative scores; gives winner, draw flag and points (win 3, draw 1 each, loss 0). |
| `StandingsEntry` | model | One row of the table. |
| `LeagueCommissioner` | model | Extends `Person`. Approves and suspends teams and keeps the decision log. |
| `ReportService` | service | Standings, fixtures (upcoming, completed, cancelled) and sales summary reports as text. |
| `FileStorage` | storage | Pipe-separated files; links saved as IDs and rebuilt on load. |
| `Main` | ui | Menu loop and routing. |
| `ValidationException` / `LeagueException` | exception | Rule violations. |

## Rules enforced
* A fixture gets exactly one result (`recordResult` rejects a second), so points can't be awarded twice. A wrong score is fixed with `correctResult`, which is logged.
* A fixture cannot be completed without a valid result; the `MatchResult` is built (and validated) first.
* Standings are computed from completed fixtures every time, never stored. Order: points, goal difference, goals scored, team name.

## Who owns what
Person, Player, Coach, FootballTeam (Member 2); Stadium, Fixture, MatchOfficial (Member 3); Spectator, Ticket, Product, Merchandise, FoodItem, Receipt, StadiumShop, TicketService (Member 4).
