# Member 3 Presentation Guide — Stadiums, Fixtures and Match Officials

## Suggested 3–4 minute presentation

### 1. Introduction
“My responsibility in the Kingston Premier Football League project was the stadium, fixture and match-official module. This module supports the operational side of the league: it manages match venues, schedules matches between eligible teams, and assigns officials without creating scheduling conflicts.”

### 2. Stadium management
“The `Stadium` class stores a unique stadium ID, name, location, seating capacity, ticket price and field condition. The system validates required information, requires a positive capacity, and rejects negative or non-finite ticket prices. Stadiums can be registered, searched by ID/name/location, displayed and updated. The stadium ID remains permanent. The capacity is also exposed so the ticketing module can enforce the venue limit.”

**Demonstrate:** register a stadium, search for it, update its capacity and ticket price, then list stadiums.

### 3. Fixture scheduling
“The `Fixture` class links a home team, away team, stadium and date/time. It rejects missing information and a team playing itself. The league service checks that both teams are registered and ACTIVE, that the stadium is registered, and that neither team nor the stadium is already booked at the same date and time. Fixtures can be searched by fixture ID, team, stadium or status.”

**Demonstrate:** schedule a valid fixture, then attempt to schedule the same team or stadium at the same time and show the validation message.

### 4. Match officials
“`MatchOfficial` extends the shared `Person` class and adds rank and availability. Officials can be registered, searched and listed, and their availability can be changed. Before assignment, the system checks that the fixture is not completed, the official is available, the official is not already assigned to that fixture, and the official is not assigned to another fixture at the same date and time.”

**Demonstrate:** register an official, assign them to a fixture, then attempt a conflicting assignment and show that it is rejected.

### 5. Integration and testing
“To integrate with the other members, `Stadium` implements `StadiumInfo`, `Fixture` implements `FixtureInfo`, and `FootballTeam` implements `TeamInfo`. These interfaces provide stable IDs and display data without forcing modules to depend on each other’s internal implementation. Fixtures are connected to results by fixture ID and become completed only after a valid result is recorded. Stadium capacity is used by ticketing.”

“Automated integration tests cover stadium validation and updates, searching, team eligibility, team and stadium scheduling conflicts, official availability and double-booking, result completion, ticket capacity and saving/reloading data.”

### 6. Conclusion
“This module makes sure matches are scheduled at registered venues, only eligible teams participate, and officials are assigned fairly without time conflicts. It also provides the information required by the results and ticketing modules.”

## Demo checklist

1. Load sample data or register two ACTIVE teams and one stadium.
2. Search for a stadium by name or location.
3. Update the stadium and show the changed details.
4. Schedule a fixture using the two ACTIVE teams.
5. Search and list fixtures.
6. Register an official and search/list officials.
7. Assign the official to a fixture.
8. Attempt a conflicting fixture or official assignment and show the validation message.
9. Record a valid result and show the fixture as completed.
10. Save and reload data to demonstrate persistence.

## Likely questions and answers

**Why are IDs used for relationships?** IDs are stable and unique, so files and modules can refer to the same entity without copying all of its data.

**Why can’t a PENDING team be scheduled?** The league requires commissioner approval. Only ACTIVE teams are eligible to play.

**What conflicts are checked?** A team cannot appear in two fixtures at the same date/time, a stadium cannot host two fixtures at the same date/time, and an official cannot officiate two fixtures at the same date/time.

**Why does a fixture have a status and a result?** Status indicates whether it is scheduled or completed; the linked result stores the scores. The fixture is completed only when a valid result is recorded.

**How does ticketing use the stadium?** The fixture references a stadium, and the ticket service reads the stadium capacity to prevent selling more tickets than available seats.
