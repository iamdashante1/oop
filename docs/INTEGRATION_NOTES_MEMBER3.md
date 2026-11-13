# Member 3 Integration Notes

The current application entry point is `kpfl.ui.Main`, and the active league coordinator is `kpfl.service.FootballLeague`.

Two older duplicate source files (`kpfl.Main` and `kpfl.model.FootballLeague`) referenced obsolete APIs and caused whole-project compilation errors. They have been preserved as text archives in `docs/legacy/` and removed from the Java source tree so they cannot shadow or break the active application. No active source file depends on these archived versions.

The current source tree can be compiled together with the test programs using the Java 17 commands in `MEMBER3_MODULE.md`.
