# Member 1 – Contribution Log

| Week | Work done |
|---|---|
| 1 | Set up project and package structure. Wrote FootballLeague, MatchResult, LeagueCommissioner, ReportService, FileStorage and Main. Wrote README and integration contracts. Compiled all classes except LeagueCommissioner (needs Member 2's Person). Recorded a run of the app in `docs/demo-run.txt`. |
| 2 | _(to fill in)_ |
| 3 | _(to fill in)_ |


## Status
All four modules are merged and the app runs end to end. Compile and test commands are in the README; the latest results are in `docs/test-evidence.txt`.

## Still open
* Practice-stadium request workflow (not built; the rules for it are not written down anywhere in the repo).
* Money is stored as `double` rounded to cents; switching to `BigDecimal` would remove any chance of rounding drift.
* Team approval does not require a minimum squad, and suspending a team does not touch its scheduled fixtures. These need a group decision first.
