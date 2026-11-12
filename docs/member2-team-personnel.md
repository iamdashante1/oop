# Member 2: Team & Personnel

The four integrated files are in `src/main/java/kpfl/model/` because the rest of the app imports them from `kpfl.model`. A separate `team-personnel/` working folder contains just these four classes and the `ValidationException` helper needed to compile them on their own.

All members should follow the [shared Person contract](person-contract.md) when extending `Person`.

| Class | Purpose |
|---|---|
| `Person` | Abstract parent for everyone in the app. Keeps ID, name, age and gender. Each child supplies `getRole()`. |
| `Player` | Adds position, jersey number, goals, playing status and team assignment. |
| `Coach` | Adds qualification, coaching role and team assignment. |
| `FootballTeam` | Keeps registration number, name, parish, approval status, players and coaches. |

The person ID and team registration number cannot change after creation. Other fields have setters that check their input. Statuses are simple text values: `PENDING`, `ACTIVE`, or `SUSPENDED` for a team; `ACTIVE`, `INJURED`, or `SUSPENDED` for a player. A team starts `PENDING`; only an `ACTIVE` team can play a fixture. `getSquadSize()` counts players. `addPlayer()` checks for duplicate jersey numbers, and players/coaches can belong to only one team at a time. Remove someone from a team before assigning them elsewhere. To change an assigned player's jersey, use `FootballTeam.updatePlayerJersey()` so the team can check for a duplicate number.

The console's **Teams, players & coaches** menu now covers team registration, name/parish updates, search, and squad display. It also adds, updates, and removes players and coaches. Enter the team's registration number first; updates and removals look for that person only in the selected team. Team approval and suspension remain commissioner actions.

`Player` and `Coach` each override `getRole()` and `toString()`. This demonstrates polymorphism: even when both are stored as `Person`, Java calls the matching child's methods.

```java
Person[] people = {player, coach};
for (Person person : people) {
    System.out.println(person.getRole() + ": " + person);
}
```

League points are read from `FootballLeague.getStandings()`, where completed match results are counted. `FootballTeam` does not keep a second points value that could get out of sync.

**Confirmed project rule:** Players must be at least 16 (`Player.MIN_AGE`). Coaches must be at least 18. New teams register as `PENDING`; the commissioner approves them before fixtures can be scheduled. Registration numbers must be unique, and a team must be registered before adding its players and coaches.

Run this module's checks after compiling the main code:

```bash
javac -d out -encoding UTF-8 $(find src/main -name "*.java")
javac -d out -cp out src/test/java/kpfl/Member2Tests.java
java -cp out kpfl.Member2Tests
```
