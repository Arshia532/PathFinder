package pathfinder.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Which search produced a step; always {@link #FORWARD} unless the search is
 * bidirectional.
 */
public enum SearchSide {
    /** The search that starts at the start cell, {@code "forward"}. */
    @JsonProperty("forward")
    FORWARD,
    /** The search that starts at the end cell, {@code "backward"}. */
    @JsonProperty("backward")
    BACKWARD
}