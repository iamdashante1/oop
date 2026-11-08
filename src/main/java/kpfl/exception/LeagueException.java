package kpfl.exception;

/**
 * Our own exception type for league rule problems.
 * Examples: a negative score, a duplicate result, or a fixture that does not exist.
 *
 * It extends RuntimeException, which means we do not have to write
 * "throws LeagueException" on every method that can throw it.
 */
public class LeagueException extends RuntimeException {

    // The message is what we show the user, e.g. "Scores cannot be negative."
    public LeagueException(String message) {
        super(message); // pass the message up to the normal Exception class
    }
}
