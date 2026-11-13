package kpfl.service;

/**
 * Read-only view of completed sales, used by the sales report.
 * AppData supplies these values from TicketService and StadiumShop.
 * All money amounts are tax-inclusive (15% GCT included).
 */
public interface SalesProvider {
    /** Tickets sold (not refunded) for one fixture. */
    int ticketsSold(String fixtureId);

    /** Total tax-inclusive ticket revenue for one fixture. */
    double ticketRevenue(String fixtureId);

    /** Total tax-inclusive revenue from all stadium shop sales. */
    double shopRevenue();

    /** Number of completed shop transactions. */
    int shopTransactionCount();

    /** Fallback for a report created without a sales source: reports zero sales. */
    SalesProvider NONE = new SalesProvider() {
        @Override public int ticketsSold(String fixtureId) { return 0; }
        @Override public double ticketRevenue(String fixtureId) { return 0; }
        @Override public double shopRevenue() { return 0; }
        @Override public int shopTransactionCount() { return 0; }
    };
}
