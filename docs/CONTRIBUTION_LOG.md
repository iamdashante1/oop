# Member 1 – Contribution Log

| Week | Work done |
|---|---|
| 1 | Set up project and package structure. Wrote FootballLeague, MatchResult, LeagueCommissioner, ReportService, FileStorage and Main. Wrote README and integration contracts. Compiled all classes except LeagueCommissioner (needs Member 2's Person). Recorded a run of the app in `docs/demo-run.txt`. |
| 2 | _(to fill in)_ |
| 3 | _(to fill in)_ |

## Still waiting on other members
* Member 2: Person, FootballTeam (needed to compile LeagueCommissioner and register teams).
* Member 3: Stadium, Fixture (needed before results can be recorded in the running app).
* Member 4: a SalesProvider for the sales report.

## Not yet verified
No automated tests exist (removed at the group's request). Win/draw/loss points, negative-score rejection and
"results survive restart" have **not** been demonstrated end to end, because no teams or fixtures can be created until
Members 2 and 3 deliver their classes. `docs/demo-run.txt` only shows the menu, input validation, empty reports, save and load.
