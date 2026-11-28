# Member 3 Integration Notes

The application entry point is `kpfl.ui.Main` and the league coordinator is `kpfl.service.FootballLeague`.

Earlier duplicate versions (`kpfl.Main`, `kpfl.model.FootballLeague`, `CommissionerRecords` and the `MenuModule` interface) used an older design that no longer compiled against the rest of the project. They were removed from the source tree; the history is in git. Nothing in the active code depends on them.

Build and test with the commands in `MEMBER3_MODULE.md`, or `mvn verify`.
