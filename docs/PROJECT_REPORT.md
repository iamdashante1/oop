# KPFL project report: how it works and how it was tested

Kingston Premier Football League (KPFL) is a Java 17 console application. It has no third-party libraries. It manages teams and people, stadiums, fixtures and officials, match results and standings, ticket sales and a stadium shop, and it saves everything to text files.

## 1. How to run it

```
javac -d out -encoding UTF-8 $(find src/main -name "*.java")
java -cp out kpfl.ui.Main
```

Run it from the project folder. On start it loads `data/` if that folder exists. Menu 6 then 3 loads sample data (4 teams, 2 stadiums, 3 fixtures, officials, spectators and shop stock).

## 2. Structure (31 source files, about 2,900 lines; tests about 780 lines)

| Package | Classes | Job |
|---|---|---|
| `kpfl.ui` | `Main` | The menu. It reads input, calls services and prints results. It contains no business rules. |
| `kpfl.service` | `FootballLeague`, `TicketService`, `StadiumShop`, `ReportService`, `AppData`, `SalesProvider`, `SampleData` | The rules. `AppData` holds one league, one ticket office and one shop, and ties them together. |
| `kpfl.model` | `Person` (abstract) with `Player`, `Coach`, `MatchOfficial`, `Spectator`, `LeagueCommissioner`; `FootballTeam`, `Stadium`, `Fixture`, `MatchResult`, `StandingsEntry`; `Product` (abstract) with `Merchandise`, `FoodItem`; `Ticket`, `Receipt`, `Money` | The data. Each class validates its own fields in its constructor and setters. |
| `kpfl.storage` | `FileStorage` | Saves and loads pipe-separated text files in `data/`. |
| `kpfl.integration` | `TeamInfo`, `StadiumInfo`, `FixtureInfo` | Small interfaces that give the modules stable IDs to refer to each other. |
| `kpfl.exception` | `ValidationException`, `LeagueException` | Rule violations. |

Two inheritance trees show polymorphism: `Person` (each child gives its own `getRole()` and `toString()`) and `Product` (each child gives its own `getType()`).

## 3. How a request flows

1. The user picks a menu number. `Main.subMenu` calls the matching method inside `attempt(...)`.
2. That method reads the inputs and calls a service method such as `league().scheduleFixture`, `AppData.cancelFixture` or `shop.checkout`.
3. The service checks the rules. Any violation throws `ValidationException`.
4. `attempt` catches it and prints `Error: <message>`, so a bad entry never crashes the app or half-changes data. Checks run before any state changes, and several operations restore the old value on failure.

## 4. Business rules (all enforced in code)

**Teams and people**
- A team registers as PENDING. Only the commissioner can approve it (ACTIVE) or suspend it, and every decision is logged.
- Only ACTIVE teams can be scheduled.
- Players must be 16 or older and coaches 18 or older. Jersey numbers are unique within a team.
- Person IDs are unique across players, coaches and officials.
- Registration numbers cannot be reused.

**Fixtures and officials**
- The home and away teams must be different, registered and ACTIVE, and the stadium must be registered.
- Each match occupies a 120-minute slot (`Fixture.DURATION_MINUTES`). A team, stadium or official cannot be in two fixtures whose slots overlap. Matches exactly 120 minutes apart are allowed. Cancelled fixtures free their slot.
- A scheduled fixture can be rescheduled. If the new time clashes, the old time is kept. It can also be cancelled with a required reason.
- An official must be available, assigned once per fixture, and free in that time slot. Officials can be unassigned from scheduled fixtures.

**Results and standings**
- A fixture reaches COMPLETED only by recording a valid result. Scores cannot be negative, and a second result is rejected.
- The commissioner can correct a wrong score, and the correction is logged.
- Win scores 3 points, draw 1 each, loss 0.
- Standings are recalculated from completed fixtures on every request. The order is points, goal difference, goals scored, then team name. They can never double-count a match.

**Tickets and shop**
- Tickets sold per fixture cannot exceed the stadium's capacity. A VIP ticket costs twice the stadium price. Cancelling a ticket frees its seat.
- A cancelled ticket cannot be sold again. No tickets are sold for completed or cancelled fixtures.
- Cancelling a fixture refunds every sold ticket for it. The sales report shows tickets refunded and the amount.
- A shop sale is checked in full before any stock is removed. Empty baskets, quantities of 0 or less, quantities above stock, unknown products and insufficient payment are all rejected.
- All prices include 15% GCT. The tax part of a total is `total × 15 / 115`, so J$1,150 splits into J$1,000 and J$150.
- A product that has been sold cannot be removed.

## 5. Saving and loading

`FileStorage` writes one file per record type (13 files: teams, players, coaches, stadiums, officials, fixtures, fixture_officials, spectators, tickets, products, sales, commissioner, preparation). Links are saved as IDs and rebuilt on load: teams first, then people, stadiums, fixtures and officials, then sales. `|` and line breaks inside names are replaced when saving.

If a saved file is damaged, the app prints the error, moves the folder to `data_damaged_<timestamp>/` and starts with an empty league. It does not overwrite the damaged files. A missing optional file loads as empty.

## 6. Menu map

- **1 Teams, players and coaches:** register, update, approve, suspend, search, show squad, add/update/remove player, add/update/remove coach.
- **2 Stadiums, fixtures and officials:** register, list/search and update stadiums; schedule, list/search, reschedule and cancel fixtures; register, list/search and set availability for officials; assign and unassign officials; preparation notes; commissioner decision log.
- **3 Match results:** record a result, correct a result.
- **4 Tickets and shop:** add and search spectators, show a spectator's tickets, sell and cancel tickets; add, update, remove and restock products; list inventory; shop sale with receipt; reprint a receipt.
- **5 Reports:** standings, fixtures (upcoming, completed, cancelled), ticket and shop sales summary.
- **6 Data:** save, reload, load sample data.

## 7. Testing

Environment: Temurin JDK 17.0.21. Compiled with `javac -Xlint:all`: no errors, no warnings.

**Automated: 323 checks, 0 failures**

| Program | Checks | Covers |
|---|---|---|
| `Member1Tests` | 22 | points, negative scores, one result per fixture, standings, reports |
| `Member2Tests` | 32 | `Person` polymorphism, age rules, jersey rules, squads, statuses |
| `Member3Tests` | 132 | stadium validation, search and update, fixture creation rules, overlap and back-to-back slots, reschedule and cancel, officials (rules, availability, assign, unassign), results and correction, interface contracts, save and reload of fixtures and officials |
| `Member4Tests` | 52 | capacity, VIP price, cancellation, shop checks, stock, GCT, sales feed, save/reload |
| `IntegrationTests` | 85 | clashes and overlaps, reschedule/cancel, officials, result correction, refunds, product maintenance, full save and reload |

**End-to-end: the real app through the console (89 steps, 631 inputs, then a restart)**

- Every menu option was used at least once.
- 38 deliberate bad inputs each produced the correct message: duplicate registration, approve twice, underage player and coach, duplicate jersey, duplicate person ID, non-numeric age, zero capacity, NaN price, overlapping team and stadium slots, same team twice, bad date, unknown stadium, unavailable official, double assignment, unassigning someone not assigned, sold-out stadium, bad category, double ticket cancel, bad product type and ID, negative restock, insufficient payment, quantity above stock, removing a sold product, cancelling without a reason, tickets and officials on a cancelled fixture, second result, negative score, result on a cancelled fixture, unknown fixture, correcting a scheduled fixture, cancelling a completed fixture, and a ticket for a played fixture.
- No crash and no "Unexpected problem" message appeared anywhere.
- Hand-checked figures: ticket revenue J$3,600; refunds J$2,000 (J$1,200 cancelled ticket plus J$800 from the cancelled fixture); shop J$8,000; GCT J$1,513.04 on J$11,600; receipt split J$6,956.52 plus J$1,043.48 GCT. Standings follow the corrected 1-1 score.
- After a restart, the standings, fixtures and sales reports were byte-identical to those before it. The saved files hold the right statuses (cancelled fixture, cancelled tickets).
- A missing `players.txt` loads as empty. A damaged `teams.txt` is set aside as described in section 5.

**Problems found by testing and fixed**
1. Reloading a completed fixture with officials threw an error, because my stricter assignment rule blocked the restore path.
2. A cancelled fixture came back as scheduled after a restart.
3. A damaged data file left the app running with no commissioner, so team approval failed and the next save would have overwritten the damaged files.
4. The existing tests called methods that did not exist (`updateStadium`, `getTeamId`, `getTeamName`), so they could not have compiled.
5. The old `kpfl.Main` and `model.FootballLeague` did not compile against the rest of the code. They were removed, along with the duplicate `oop/` and `team-personnel/` copies.

## 8. Known limits

- **Not built:** the practice-stadium request workflow. The repo does not say what it should do.
- **Money is a `double`**, rounded to cents. A `BigDecimal` change would remove any chance of drift but touches almost every file.
- **Open rule decisions:** team approval has no minimum squad size, and suspending a team does not touch its scheduled fixtures.
- **Tests are plain `main()` programs, not JUnit.** `mvn verify` is set up to run them, but Maven was not available here, so that build is untested.
- **Menu input only:** there is no GUI, and the UI class has no automated tests beyond the scripted console runs above.
