# 0006: Seeded maze generation

Status: Implemented in the Java backend.

## Context

PathFinder needs a repeatable maze with connected corridors, alongside the
existing random obstacle generator.

## Decision

Use a seeded depth-first backtracker with an explicit stack. Begin with walls
and carve between unvisited interior cells two positions apart. Keep the border
closed and preserve the requested dimensions. Even dimensions leave a spare
wall strip at the bottom or right. Return suggested start and destination
positions alongside the immutable grid. Every terrain cost is 1.

## Consequences

Every open cell is reachable, with one route between any two open cells.
Identical dimensions and seed reproduce the same maze. Generation takes
O(rows × columns) time and memory. A one-room maze has coincident markers.

## Verification

Tests traverse the generated grid to check connectivity and the absence of
cycles across odd, even, and narrow dimensions. They check terrain costs and
reject dimensions that are too small or overflow integer indexing.
