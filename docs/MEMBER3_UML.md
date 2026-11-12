# Member 3 UML Class Relationships

The diagram below describes the Member 3 domain classes and their relationships to the shared league classes. `FootballLeague` coordinates registration and scheduling; the model classes own their own data and validation.

```mermaid
classDiagram
    class Person {
      <<abstract>>
      -String id
      -String name
      -int age
      -String gender
      +getId() String
      +getName() String
      +getRole() String
    }
    class MatchOfficial {
      -String rank
      -boolean available
      +getRank() String
      +isAvailable() boolean
      +setAvailable(boolean) void
      +setRank(String) void
    }
    class Stadium {
      -String stadiumId
      -String name
      -String location
      -int capacity
      -double ticketPrice
      -String fieldCondition
      +getStadiumId() String
      +getStadiumName() String
      +setCapacity(int) void
      +setTicketPrice(double) void
    }
    class Fixture {
      -String fixtureId
      -LocalDateTime dateTime
      -Status status
      -MatchResult result
      +getFixtureId() String
      +getHomeTeamId() String
      +getAwayTeamId() String
      +getStadiumId() String
      +isCompleted() boolean
      +complete(MatchResult) void
    }
    class FootballTeam {
      -String registrationNo
      -String name
      -Status status
      +isEligibleForFixture() boolean
    }
    class MatchResult {
      -String fixtureId
      -int homeScore
      -int awayScore
    }
    class FootballLeague {
      +addStadium(Stadium) void
      +updateStadium(...) void
      +addFixture(Fixture) void
      +assignOfficial(String,String) void
      +searchStadiums(String) List
      +searchFixtures(String) List
      +searchOfficials(String) List
    }
    class StadiumInfo {
      <<interface>>
      +getStadiumId() String
      +getStadiumName() String
    }
    class FixtureInfo {
      <<interface>>
      +getFixtureId() String
      +getHomeTeamId() String
      +getAwayTeamId() String
      +getStadiumId() String
      +getDateTime() LocalDateTime
      +isCompleted() boolean
      +markCompleted() void
    }
    class TeamInfo {
      <<interface>>
      +getTeamId() String
      +getTeamName() String
    }
    Person <|-- MatchOfficial
    Stadium ..|> StadiumInfo
    Fixture ..|> FixtureInfo
    FootballTeam ..|> TeamInfo
    Fixture "0..*" --> "1" FootballTeam : home team
    Fixture "0..*" --> "1" FootballTeam : away team
    Fixture "0..*" --> "1" Stadium : venue
    Fixture "0..*" o-- "0..*" MatchOfficial : assigned officials
    Fixture "0..1" --> "1" MatchResult : completed with
    FootballLeague "1" o-- "0..*" Stadium : registers
    FootballLeague "1" o-- "0..*" Fixture : schedules
    FootballLeague "1" o-- "0..*" MatchOfficial : registers
```

## Relationship notes

- `MatchOfficial` inherits identity and personal details from the abstract `Person` class.
- `Stadium`, `Fixture`, and `FootballTeam` implement their corresponding integration interfaces so other modules depend on stable contracts.
- Each `Fixture` references exactly one home team, one away team, and one stadium. The teams must be different and registered/eligible before scheduling.
- A fixture can have zero or more assigned officials. An official cannot be unavailable or assigned to another fixture at the same date and time.
- A fixture has at most one result. It is marked completed only when a valid result is linked.
- `FootballLeague` owns the registries and coordinates uniqueness and scheduling-conflict checks.
