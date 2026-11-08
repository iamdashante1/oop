package kpfl.storage;

import java.util.List;

/**
 * What we get back after loading a file:
 *   - records:  the lines that loaded fine
 *   - warnings: a message for every line we had to skip because it was damaged
 *
 * "T" is a placeholder for the kind of record (e.g. MatchResult).
 */
public class LoadOutcome<T> {

    private final List<T> records;
    private final List<String> warnings;

    public LoadOutcome(List<T> records, List<String> warnings) {
        this.records = records;
        this.warnings = warnings;
    }

    public List<T> getRecords() {
        return records;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
