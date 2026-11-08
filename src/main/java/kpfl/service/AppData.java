package kpfl.service;

/** Everything the application keeps in memory and saves to disk: league, ticket office and shop. */
public class AppData {
    private final FootballLeague league = new FootballLeague();
    private final TicketService tickets = new TicketService(league);
    private final StadiumShop shop = new StadiumShop();

    public FootballLeague getLeague() { return league; }
    public TicketService getTickets() { return tickets; }
    public StadiumShop getShop() { return shop; }

    /** Sales view used by the sales report. */
    public SalesProvider sales() {
        return new SalesProvider() {
            @Override public int ticketsSold(String fixtureId) { return tickets.ticketsSold(fixtureId); }
            @Override public double ticketRevenue(String fixtureId) { return tickets.ticketRevenue(fixtureId); }
            @Override public double shopRevenue() { return shop.totalRevenue(); }
            @Override public int shopTransactionCount() { return shop.getSales().size(); }
        };
    }
}
