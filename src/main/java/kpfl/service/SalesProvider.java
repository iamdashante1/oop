package kpfl.service;

/**
 * Read-only view of Member 4's completed sales, used by the sales report.
 * Member 4: implement this (e.g. in a class wrapping StadiumShop and the ticket list) and pass it to Main.
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

    /** Default used until Member 4's module is plugged in: reports zero sales. */
    SalesProvider NONE = new SalesProvider() {
        @Override public int ticketsSold(String fixtureId) { return 0; }
        @Override public double ticketRevenue(String fixtureId) { return 0; }
        @Override public double shopRevenue() { return 0; }
        @Override public int shopTransactionCount() { return 0; }
    };
}
