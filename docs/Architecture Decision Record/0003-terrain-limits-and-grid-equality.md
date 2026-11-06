# 0003: Terrain limits and grid equality

Status: Implemented and confirmed. Terrain-cost limits are 1-100, inclusive,
and equality includes all stored grid state.

## Context

Maps need a configurable maximum terrain cost with a hard cap of 100. Grid
snapshots also need value equality so callers can compare their contents rather
than their object identities.

## Decision

- Allow a configured `maxTerrainCost` from 1 to 100, inclusive. Reject values
  outside that range rather than silently clamp them.
- Add constructors for open and array-backed grids with an explicit limit.
  Existing constructor signatures remain available and default to 100.
- Validate every stored cost against the configured limit, including terrain
  under walls. Open grids still start with cost 1, regardless of their limit.
- Expose the configured limit through `maxTerrainCost()`. This is not the
  largest cost currently present in the arrays.
- Compare row count, column count, configured limit, wall positions, and all
  terrain costs in `equals`. Use array content equality rather than reference
  equality. Include terrain under walls because it is retained map data.
- Override `hashCode` with exactly the same value components. Equal grids must
  have equal hashes; different grids may still have hash collisions.

All stored grid state participates in equality, including the configured limit
and terrain beneath walls. Separate grid instances with matching values are
equal; object identity remains a separate comparison using `==`.

The minimum is 1 so every move adds a positive cost. A cost of zero
would represent free movement and require revisiting the current minimum-cost
assumption used in planning A* heuristics. A configurable maximum of zero would
also conflict with the current default terrain cost of 1. Zero-cost movement is
not inherently invalid for pathfinding, but is outside the current grid contract.

## Consequences

The new limits intentionally reject costs above 100 that older versions accepted.
Invalid input causes `IllegalArgumentException`; existing grid snapshots remain
immutable. No random map generation or search behavior is added by this change.

Comparing distinct grids can scan the arrays; hash calculation also scans them.
Both take O(cellCount) time in the worst case. Reference equality and mismatching
dimensions or limits are checked before array contents. Hash caching can be
considered if actual usage demonstrates a need; it is not needed for correctness.

`hashCode` enables correct use in hash-based collections. This does not introduce
a cache or make a hash a unique grid identifier. A custom `toString` is optional
for debugging; it is not implemented because no logging format is required yet.
It should not be used as a persistence or API format if introduced later.

## Verification

Tests cover configured/default bounds, inclusive costs at the limit, invalid
limits, excessive costs including walls, content-based equality and its basic
contract, unequal value components, equal hashes, hash-set behavior, and immunity
to later mutation of constructor inputs. Existing grid and position tests remain
part of Maven verification.

Run `clean verify` with the Maven wrapper on JDK 26 to execute the 14-test suite,
package the domain JAR, generate coverage, and validate JavaDocs. A change only
to the column count or to terrain beneath a wall is tested as an unequal value.

Verified on Eclipse Temurin 26.0.2.1 and Maven 3.9.9: all 14 tests passed with no
failures, errors, or skips. Packaging, coverage generation, and strict JavaDoc
validation also passed.
