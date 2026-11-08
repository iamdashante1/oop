package kpfl.exception;

/** Thrown when a league operation breaks a rule (bad score, duplicate result, unknown id...). */
public class LeagueException extends RuntimeException {
    public LeagueException(String message) {
        super(message);
    }
}
