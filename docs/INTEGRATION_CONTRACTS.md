# Integration contracts

How the modules connect. The shared classes live in `kpfl.model` / `kpfl.service`; the small interfaces in `kpfl.integration` keep the IDs the modules use to refer to each other stable.

| Interface | Implemented by | Methods |
|---|---|---|
| `TeamInfo` | `FootballTeam` (Member 2) | `getTeamId()`, `getTeamName()` |
| `StadiumInfo` | `Stadium` (Member 3) | `getStadiumId()`, `getStadiumName()` |
| `FixtureInfo` | `Fixture` (Member 3) | `getFixtureId()`, `getHomeTeamId()`, `getAwayTeamId()`, `getStadiumId()`, `getDateTime()`, `isCompleted()`, `markCompleted()` |
| `SalesProvider` (`kpfl.service`) | `AppData.sales()` (Member 4) | `ticketsSold`, `ticketRevenue`, `shopRevenue`, `shopTransactionCount`, `ticketsRefunded`, `refundTotal` |

## Rules for everyone
* All IDs are `String`; dates and times are `LocalDate` / `LocalDateTime`.
* Every person class extends `Person` (see `person-contract.md`).
* Relationships are saved as IDs and rebuilt on load, so an ID must never change after creation.
* A fixture only becomes COMPLETED through `Fixture.complete(result)`. A cancelled fixture can no longer take a result, officials or ticket sales.
* Anything that cancels a fixture should go through `AppData.cancelFixture(id, reason)`, which also refunds the tickets.

## File storage
`FileStorage` (Member 1) saves one pipe-separated file per record type in `data/`. The full list of files and columns is in its class comment. Characters that would break the format (`|`, line breaks) are replaced when saving. Product IDs may not contain `|`, `:` or `;` because sales are saved as `productId:qty:price;...`.
