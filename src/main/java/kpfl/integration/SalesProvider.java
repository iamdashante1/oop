package kpfl.integration;

import java.math.BigDecimal;

/**
 * The sales numbers that the sales report needs.
 *
 * Member 4 must write a class that implements this (for example on StadiumShop),
 * then pass it to ReportService. Until then the report says "not available".
 *
 * BigDecimal is used for money because double can give tiny rounding errors.
 */
public interface SalesProvider {

    // Total money earned from ticket sales.
    BigDecimal getTotalTicketRevenue();

    // Total money earned from shop sales.
    BigDecimal getTotalShopRevenue();

    // How many tickets have been sold.
    int getTicketsSold();

    // How many shop sales (transactions) have happened.
    int getShopTransactionCount();
}
