package kpfl.integration;

import java.math.BigDecimal;

/** Contract Member 4 must implement (e.g. on StadiumShop or a facade) to feed the sales report. */
public interface SalesProvider {
    BigDecimal getTotalTicketRevenue();

    BigDecimal getTotalShopRevenue();

    int getTicketsSold();

    int getShopTransactionCount();
}
