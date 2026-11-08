package kpfl.storage;

import java.util.List;

/** Records that were loaded successfully plus a warning for every line that had to be skipped. */
public record LoadOutcome<T>(List<T> records, List<String> warnings) { }
