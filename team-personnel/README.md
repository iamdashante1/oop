# Team & Personnel working folder

This folder contains the four classes assigned to you:

- `Person.java` — shared name, age, gender and ID.
- `Player.java` — position, jersey, goals, status and team assignment.
- `Coach.java` — qualification, role and team assignment.
- `FootballTeam.java` — team details and its player/coach lists.

`ValidationException.java` is the one shared helper these classes need to compile. You do not need to edit it for your section.

The [shared Person contract](../docs/person-contract.md) lists the constructor, getters, role method, and common validation used by **all members**, including the commissioner, match official, and spectator classes.

Team and player statuses are simple `String` values, such as `"ACTIVE"`. Their setters check the allowed words. When comparing strings in Java, use `.equals(...)` rather than `==`.

To change an assigned player's jersey number, call `FootballTeam.updatePlayerJersey(playerId, number)`; it checks that the number is free in that squad. `findPlayer()` and `findCoach()` look up people within one team.

The files use the same `kpfl.model` package as the full project. The copies in `src/main/java/kpfl/model/` are used by the complete app. When you finish changes here, copy your four model files back into that folder so the app receives your updates.

From the project root, compile and run your section's checks with:

```bash
mkdir -p out-member2
javac -d out-member2 -encoding UTF-8 $(find team-personnel/src/main/java -name "*.java")
javac -d out-member2 -cp out-member2 src/test/java/kpfl/Member2Tests.java
java -cp out-member2 kpfl.Member2Tests
```

The tests live outside this working folder because they are checks, not classes you are required to implement. The full app's league points come from `FootballLeague.getStandings()`. The confirmed project minimum player age is 16.
