package kpfl.integration;

import java.util.Scanner;

/** A menu section owned by another member. Main routes to it once it is registered. */
public interface MenuModule {
    /** Runs the module's own sub-menu until the user returns to the main menu. */
    void run(Scanner in);
}
