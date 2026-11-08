# Member 2: Team & Personnel

The four integrated files are in `src/main/java/kpfl/model/` because the rest of the app imports them from `kpfl.model`. A separate `team-personnel/` working folder contains just these four classes and the `ValidationException` helper needed to compile them on their own.

| Class | Purpose |
|---|---|
| `Person` | Abstract parent for everyone in the app. Keeps ID, name, age and gender. Each child supplies `getRole()`. |
| `Player` | Adds position, jersey number, goals, playing status and team assignment. |
| `Coach` | Adds qualification, coaching role and team assignment. |
| `FootballTeam` | Keeps registration number, name, parish, approval status, players and coaches. |

The person ID and team registration number cannot change after creation. Other fields have setters that check their input. Statuses are simple text values: `PENDING`, `ACTIVE`, or `SUSPENDED` for a team; `ACTIVE`, `INJURED`, or `SUSPENDED` for a player. A team starts `PENDING`; only an `ACTIVE` team can play a fixture. `getSquadSize()` counts players. `addPlayer()` checks for duplicate jersey numbers, and players/coaches can belong to only one team at a time. Remove someone from a team before assigning them elsewhere. Remove a player before changing their jersey number, then add them back so the new number is checked.

League points are read from `FootballLeague.getStandings()`, where completed match results are counted. `FootballTeam` does not keep a second points value that could get out of sync.

**Rule awaiting confirmation:** `Player.MIN_AGE` is 16 because that is the current project assumption. Change the constant if the lecturer gives a different minimum age. The existing rule that coaches must be at least 18 is preserved.

Run this module's checks after compiling the main code:

```bash
javac -d out -encoding UTF-8 $(find src/main -name "*.java")
javac -d out -cp out src/test/java/kpfl/Member2Tests.java
java -cp out kpfl.Member2Tests
```
