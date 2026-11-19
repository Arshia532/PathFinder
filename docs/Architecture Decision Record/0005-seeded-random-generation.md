# 0005: Seeded random map generation

Status: Implemented. `RandomGridGenerator` creates random walls from a seed.
Random terrain costs and other generation strategies, such as mazes, are not
implemented.

## Context

PathFinder lets a user generate a map instead of drawing one. A generated map
must be reproducible: entering the same settings again, on any machine, must
give the same map, so that a map can be shared or revisited by its seed. The
generator must produce a `Grid` that follows the rules in
[0001](0001-grid-foundation.md).

## Decision

- Add `RandomGridGenerator` in the package `pathfinder.generation`, with one
  method: `generate(rowCount, columnCount, seed, wallProbability)`.
- Decide each cell independently. A cell becomes a wall when the next random
  number, a value from 0 (inclusive) to 1 (exclusive), is less than
  `wallProbability`. A probability of `0` therefore produces no walls and `1`
  produces only walls.
- Draw exactly one random number per cell, in row-major order (see 0001).
- Use `java.util.Random` created from the seed. Its algorithm is fixed by the
  Java specification, so a seed produces the same sequence on every JDK and
  operating system.
- Give every cell terrain cost `1`, and use the grid's default terrain limit.
- Keep the generator stateless. It has no fields and creates a new `Random` for
  each call, so earlier calls never affect later ones and one instance can be
  shared between threads.
- Validate the arguments before allocating anything: dimensions must be
  positive, the cell count must fit an `int`, and the probability must be a
  finite number from 0 to 1. Invalid arguments cause `IllegalArgumentException`.
- Do not choose endpoints and do not check that a route exists. An unreachable
  destination is a normal search outcome (see
  [0004](0004-shared-search-engine.md)).

## Consequences

- A map is identified by four values: row count, column count, seed, and wall
  probability. All four are needed to reproduce it.
- The wall probability is a chance per cell, not an exact share. A probability
  of `0.30` gives roughly, not exactly, 30% walls.
- Because each cell always uses the same random number for a given seed, raising
  the probability only adds walls; it never removes one. Grids with the same
  seed and the same number of cells also receive the same sequence of wall
  decisions, whatever their shape.
- Generation takes O(rowCount * columnCount) time and memory.
- The generator may place a wall on a cell the user later selects as the start
  or destination, and the search rejects walls as endpoints. The team has not
  yet decided whether the generator or the API layer should keep those cells
  open.
- Dense maps often have no route. If the product should guarantee a route, that
  needs a different strategy, such as generating a maze or retrying with a new
  seed.

## Verification

`RandomGridGeneratorTest` checks that:

- the same arguments reproduce the same grid, regardless of earlier calls or of
  which generator instance is used;
- a fixed seed produces a known, hard-coded wall pattern, which would reveal any
  change to the random sequence or the cell order;
- different seeds produce different maps;
- probabilities `0` and `1` produce no walls and only walls, including on
  single-cell and single-row grids;
- the share of walls on a large grid is close to the requested probability;
- raising the probability with the same seed only adds walls;
- every cell has terrain cost `1`;
- invalid dimensions and probabilities are rejected.

`clean verify` passes on JDK 26.
