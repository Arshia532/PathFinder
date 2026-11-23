package pathfinder.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The algorithms a client can request. JSON uses the lowercase names shown,
 * matching {@code AlgorithmId} in {@code frontend/src/api/types.ts}.
 */
public enum AlgorithmId {
    /** Breadth-first search, {@code "bfs"}. */
    @JsonProperty("bfs")
    BFS,
    /** Depth-first search, {@code "dfs"}. */
    @JsonProperty("dfs")
    DFS,
    /** A* search, {@code "astar"}. */
    @JsonProperty("astar")
    ASTAR,
    /** Dijkstra's algorithm, {@code "dijkstra"}. */
    @JsonProperty("dijkstra")
    DIJKSTRA
}