# Shared `Person` contract (all members)

The app uses one abstract `kpfl.model.Person` class. Member 2 owns it, but the classes for **all four members** use it. Its main copy is `src/main/java/kpfl/model/Person.java`; the `team-personnel/` folder has a matching copy for Member 2's standalone work.

```java
protected Person(String id, String name, int age, String gender)
public String getId()
public String getName()
public int getAge()
public String getGender()
public abstract String getRole()
public String toString()
public void setName(String name)
public void setAge(int age)
public void setGender(String gender)
```

Every child constructor calls `super(id, name, age, gender)`. `Person` checks the fields when an object is created:

- `id`, `name`, and `gender` must contain non-space text. They are trimmed before storage.
- `age` must be greater than zero.
- Invalid values throw `ValidationException` from `kpfl.exception`.

The ID cannot change after construction. The three setters apply the same shared checks when a name, age, or gender changes. A child class may add stricter rules, such as the minimum age for a player or coach.

`Person.toString()` provides a basic role, ID, and name display. `Player` and `Coach` override it to show their own details. Java uses the child's version of `getRole()` and `toString()` even when a variable is declared as `Person`.

Each child implements `getRole()` with its own role name:

| Member | Child class | `getRole()` result |
|---|---|---|
| 1 | `LeagueCommissioner` | `"League Commissioner"` |
| 2 | `Player`, `Coach` | `"Player"`, `"Coach"` |
| 3 | `MatchOfficial` | `"Match Official"` |
| 4 | `Spectator` | `"Spectator"` |

For example, a child starts like this:

```java
public class Spectator extends Person {
    public Spectator(String id, String name, int age, String gender) {
        super(id, name, age, gender);
    }

    @Override
    public String getRole() { return "Spectator"; }
}
```

Keep the `Person` API and validation above the same in both copies. If Member 2 changes the class, copy the update into `src/main/java/kpfl/model/Person.java` before building the full app.
