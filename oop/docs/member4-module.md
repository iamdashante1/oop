# Member 4 – Ticketing, Stadium Shop & Payment

Owner: Member 4. Package layout follows the group plan: `kpfl.model` (data classes), `kpfl.service` (rules), `kpfl.storage` (files), `kpfl.ui` (menu).

## Classes

| Class | Package | What it does |
|---|---|---|
| `Spectator` | model | Extends Member 2's `Person`. Holds the IDs of the tickets bought. Role is `"Spectator"`. |
| `Ticket` | model | One ticket for one fixture. Has a category (GENERAL or VIP) and a status (SOLD or CANCELLED). Price is tax-inclusive. |
| `Product` (abstract) | model | Shop item: ID, name, stock and tax-inclusive unit price. Subclasses override `getType()`. |
| `Merchandise`, `FoodItem` | model | Extend `Product` (jerseys/flags, food/beverages). |
| `Receipt` (+ `Money`) | model | A completed sale: lines, subtotal, GCT, total, payment, change. `Money` holds the GCT helpers. |
| `TicketService` | service | Spectators and ticket sales, checked against the fixture's stadium capacity. |
| `StadiumShop` | service | Inventory, multi-item sales, payment checks and receipts. |
| `AppData.sales()` | service | Read-only `SalesProvider` used by Member 1's sales report. |

## Product inheritance (second hierarchy)

`Product` is abstract and declares `getType()` and `getExtra()`. `Merchandise` returns `"Merchandise (size M)"`, and `FoodItem` returns `"Food"` or `"Beverage"`.
Code that holds a `Product` reference (the inventory list, the receipt, the menu) calls `getType()` and gets the right answer for each subclass. That is the polymorphism demonstration.
`Spectator` is one more subclass of Member 2's `Person`, like `Player` and `Coach`.

## Business rules

* Tickets sold are counted **per fixture** against that fixture's stadium capacity; a full stadium rejects the next sale. Cancelling a ticket frees its seat. No tickets for a completed fixture.
* Ticket price = stadium ticket price x category multiplier (GENERAL x1, VIP x2).
* A shop sale is checked completely before any stock is removed, so a failed sale changes nothing.
* Rejected: empty basket, zero or negative quantity, more than the stock, unknown product, insufficient payment.
* **GCT:** every price already includes 15% GCT. GCT = total x 15/115, so J$1,150 = J$1,000 + J$150 (never add 15% again).

## Sample receipt

```
---------- KPFL STADIUM SHOP ----------
Receipt R0001  2026-11-25 23:44
Jersey                  1 x J$6,900.00 =   J$6,900.00
Jerk Chicken Box        2 x J$1,150.00 =   J$2,300.00
Subtotal (before GCT)                    J$8,000.00
GCT 15% (included)                       J$1,200.00
TOTAL                                    J$9,200.00
Paid                                    J$10,000.00
Change                                     J$800.00
---------------------------------------
```

## Saved files (pipe-separated, relationships by ID)

```
spectators.txt   spectatorId|name|age|gender
tickets.txt      ticketId|fixtureId|spectatorId|category|price|status
products.txt     type(M/F)|productId|name|stock|unitPrice|extra   (extra = size, or true for a beverage)
sales.txt        receiptId|time|payment|productId:qty:price;productId:qty:price
```

## Tests

`src/test/java/kpfl/Member4Tests.java` (52 checks). Results are in `docs/member4-test-evidence.txt`.

```
javac -d out -cp out src/test/java/kpfl/Member4Tests.java
java -cp out kpfl.Member4Tests
```

Covers the required cases: a stadium with 2 seats accepts exactly 2 tickets and rejects the third; a valid payment succeeds; insufficient payment and negative quantities fail; stock reduces correctly. Also covers GCT, mixed bad baskets, cancellation, completed fixtures, the sales feed and save/reload.
