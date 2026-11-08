package kpfl.integration;

/**
 * What the league needs to know about a stadium.
 *
 * Member 3's Stadium class must "implement" this interface
 * (it must have these two methods).
 */
public interface StadiumInfo {

    // The stadium's unique ID, e.g. "S1".
    String getStadiumId();

    // The stadium's display name, e.g. "National Stadium".
    String getStadiumName();
}
