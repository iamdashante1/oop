package kpfl.integration;

import java.util.Scanner;

/**
 * A menu section that belongs to another member (e.g. Team management).
 *
 * Each member writes a class that implements this, then Main "plugs it in"
 * with registerModule(...). Main does not need to know how their menu works.
 */
public interface MenuModule {

    /**
     * Show the module's own menu and handle the user's choices.
     * Return from this method when the user wants to go back to the main menu.
     *
     * @param in the shared Scanner for reading what the user types
     */
    void run(Scanner in);
}
