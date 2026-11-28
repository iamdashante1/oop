# Member 3 Module — Stadium, Fixture and Match Official Management

## Scope

Member 3 is responsible for the venue and match-scheduling domain: `Stadium`, `Fixture`, `MatchOfficial`, and the management operations exposed through the KPFL console menu.

## Classes

- **Stadium** — stores immutable stadium ID plus editable name, location, seating capacity, ticket price and field condition. It implements `StadiumInfo`.
- **Fixture** — connects two different football teams, one registered stadium and a `LocalDateTime`. It stores assigned officials, status and an optional `MatchResult`. It implements `FixtureInfo`.
- **MatchOfficial** — extends `Person`; stores rank and availability.
- **FootballLeague** — coordinates stadium/fixture/official registries, searches, updates, eligibility and conflict checks.

## Management workflows

### Stadiums
1. Register a stadium with unique ID, name, location, positive seating capacity, finite non-negative ticket price and field condition.
2. Search by partial ID, name or location (case-insensitive).
3. Update name, location, capacity, ticket price and field condition while retaining the original ID.
4. List all registered stadiums. Capacity is available to the ticketing service through the fixture’s stadium reference.

### Fixtures
1. Schedule a fixture using existing teams and stadium.
2. Reject identical home/away teams, unregistered teams/stadiums, and teams that are not ACTIVE.
3. Reject a team, stadium or official already used by another fixture whose time slot overlaps (every match counts as `Fixture.DURATION_MINUTES` = 120 minutes). Cancelled fixtures are ignored.
4. Search by fixture ID, team name, stadium name or status; list fixtures in date/time order.
5. Link a valid result to the fixture and prevent a second result. The commissioner can correct a wrong score (logged).
6. Reschedule a scheduled fixture (the old time is kept if the new one clashes) or cancel it with a reason. Cancelling also refunds its tickets.

### Match officials
1. Register officials with a unique person ID and non-empty rank/role.
2. Search by ID, name or rank; list all registered officials.
3. Update availability (menu: *Set official availability*).
4. Assign an official only to a scheduled fixture when the official is available and not already assigned to an overlapping fixture.
5. Unassign an official from a scheduled fixture.

## Integration contracts

- `Stadium implements StadiumInfo`: `getStadiumId()` and `getStadiumName()`.
- `Fixture implements FixtureInfo`: fixture/team/stadium IDs, date/time, completion status and `markCompleted()`.
- `FootballTeam implements TeamInfo`: `getTeamId()` and `getTeamName()`.
- The ticket service obtains capacity from `fixture.getStadium().getCapacity()`.
- The league service records results against fixture IDs; the fixture stores the matching result and transitions to `COMPLETED`.

## Build and test

The project targets Java 17. From the repository root:

```bash
mkdir -p out
javac -d out -encoding UTF-8 $(find src/main src/test -name '*.java')
java -cp out kpfl.Member3Tests      # this module: 132 checks
java -cp out kpfl.IntegrationTests
java -cp out kpfl.Member1Tests
```

The test programs print individual PASS/FAIL checks and exit with a non-zero status if any check fails. Record the actual output from the current checkout in `test-evidence.txt`; do not rely on old counts.

## Related documents

- `MEMBER3_UML.md` — UML class diagram and relationship notes.
- `MEMBER3_PRESENTATION.md` — presentation script, demo checklist and likely questions.
- `INTEGRATION_CONTRACTS.md` — shared module contracts.
