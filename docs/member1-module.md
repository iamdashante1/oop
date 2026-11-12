# Member 1 – League Operations & Integration (module write-up)

## What it does
- `FootballLeague` coordinates teams, stadiums and fixtures; it never duplicates Member 2/3 business rules.
- `MatchResult` validates scores (>= 0) and maps outcome to points: win 3, draw 1, loss 0.
- Standings are **derived** from completed fixtures every time they are requested, so a fixture cannot award points twice.
- `Fixture.complete()` is the only way a fixture reaches COMPLETED, and it needs a valid, matching `MatchResult`.
- `LeagueCommissioner extends Person` (polymorphic `getRole()`); approves/suspends teams. Only ACTIVE teams can be scheduled.
- `ReportService`: standings, upcoming/completed fixtures, ticket & shop sales summary (via `SalesProvider`, implemented by Member 4).
- `FileStorage`: sequential pipe-delimited files in `data/`; links saved as IDs and rebuilt on load.

## Test evidence
- `docs/test-evidence.txt` - 107 automated checks (Member1Tests 22, Member2Tests 32, IntegrationTests 53), all PASS.
- `docs/demo-evidence.txt` - scripted console sessions: sample data, fixtures report with officials, a rejected and a successful shop sale with receipt, duplicate official assignment, negative score, save, restart, identical reports after reload.

| Required test | Result |
|---|---|
| Win/draw/loss update points | PASS |
| Negative score rejected | PASS |
| Completed fixture needs valid result / no second result | PASS |
| Reports agree with stored data | PASS |
| Save, close, restart restores records (teams, people, fixtures, officials, tickets, shop, decisions) | PASS |
| Commissioner decisions (approvals, assignments, preparation notes) logged and reloaded | PASS |

## Cross-module work done by Member 1 (to be reviewed by owners)
Working versions of Members 2, 3 and 4's classes were written so the whole app runs end to end; each owner
should review, adjust and explain their module. The player minimum age is confirmed as 16. Open items:
practice-stadium request workflow (not built), more detailed GCT receipt layout if the lecturer wants one.

## Weekly contribution log (Member 1)
| Week | Work | Evidence |
|---|---|---|
| 1 | Design agreement, repo structure, shared IDs | this repo layout |
| 2 | FootballLeague/Main skeleton | `FootballLeague.java`, `Main.java` |
| 3 | Results, standings, commissioner | `MatchResult.java`, tests |
| 4 | Reports, file persistence | `ReportService.java`, `FileStorage.java`, demo evidence |
| 5 | Integration, bug fixes, demo | (pending team merge) |
