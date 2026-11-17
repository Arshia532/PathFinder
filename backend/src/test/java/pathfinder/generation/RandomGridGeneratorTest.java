package pathfinder.generation;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomGridGeneratorTest {
    @Test
    void sameInputsReproduceWallsRegardlessOfEarlierCalls() {
        RandomGridGenerator generator = new RandomGridGenerator();
        Grid first = generator.generate(7, 11, 42L, 0.30);
        generator.generate(4, 5, 999L, 0.80);
        Grid repeated = generator.generate(7, 11, 42L, 0.30);
        Grid independent = new RandomGridGenerator().generate(7, 11, 42L, 0.30);
        for (int index = 0; index < first.size(); index++) {
            assertEquals(first.isBlocked(index), repeated.isBlocked(index));
            assertEquals(first.isBlocked(index), independent.isBlocked(index));
        }
    }

    @Test
    void sameInputsProduceEqualGrids() {
        RandomGridGenerator generator = new RandomGridGenerator();
        assertEquals(generator.generate(9, 13, 2026L, 0.35), generator.generate(9, 13, 2026L, 0.35));
        assertEquals(Grid.MAX_TERRAIN_COST, generator.generate(2, 2, 1L, 0.5).maxTerrainCost(),
                "Generated grids use the default terrain limit");
    }

    @Test
    void differentSeedsProduceDifferentMaps() {
        RandomGridGenerator generator = new RandomGridGenerator();
        Grid base = generator.generate(10, 10, 1L, 0.50);
        for (long seed = 2; seed <= 20; seed++) {
            assertNotEquals(base, generator.generate(10, 10, seed, 0.50), "Seed " + seed);
        }
    }

    @Test
    void wallShareIsCloseToTheProbability() {
        RandomGridGenerator generator = new RandomGridGenerator();
        for (double probability : new double[] {0.10, 0.30, 0.50, 0.90}) {
            Grid grid = generator.generate(100, 100, 7L, probability);
            double share = (double) wallCount(grid) / grid.size();
            // With 10,000 cells the share varies by about 0.005, so 0.03 is a generous margin.
            assertEquals(probability, share, 0.03, "Wall share for probability " + probability);
        }
    }

    @Test
    void raisingTheProbabilityOnlyAddsWalls() {
        RandomGridGenerator generator = new RandomGridGenerator();
        Grid sparse = generator.generate(20, 25, 99L, 0.25);
        Grid dense = generator.generate(20, 25, 99L, 0.60);
        for (int index = 0; index < sparse.size(); index++) {
            if (sparse.isBlocked(index)) {
                assertTrue(dense.isBlocked(index), "Wall " + index + " must remain a wall");
            }
        }
        assertTrue(wallCount(dense) > wallCount(sparse));
    }

    @Test
    void wallDecisionsFollowRowMajorOrderWhateverTheShape() {
        RandomGridGenerator generator = new RandomGridGenerator();
        Grid wide = generator.generate(3, 8, 5L, 0.40);
        Grid tall = generator.generate(6, 4, 5L, 0.40);
        for (int index = 0; index < wide.size(); index++) {
            assertEquals(wide.isBlocked(index), tall.isBlocked(index), "Cell " + index);
        }
    }

    private static int wallCount(Grid grid) {
        int count = 0;
        for (int index = 0; index < grid.size(); index++) {
            if (grid.isBlocked(index)) {
                count++;
            }
        }
        return count;
    }

    @Test
    void fixedSeedProducesExpectedRowMajorWalls() {
        Grid grid = new RandomGridGenerator().generate(2, 3, 42L, 0.50);
        boolean[] expected = {false, false, true, true, false, false};
        assertEquals(2, grid.rows());
        assertEquals(3, grid.columns());
        assertEquals(expected.length, grid.size());
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], grid.isBlocked(index), "Cell " + index);
            assertEquals(1, grid.terrainCost(index));
        }
    }

    @Test
    void extremeProbabilitiesWorkForNarrowAndSingleCellGrids() {
        RandomGridGenerator generator = new RandomGridGenerator();
        for (int[] dimensions : new int[][] {{1, 1}, {1, 5}, {5, 1}, {3, 7}}) {
            for (double probability : new double[] {0.0, 1.0}) {
                Grid grid = generator.generate(dimensions[0], dimensions[1], -12L, probability);
                assertEquals(dimensions[0], grid.rows());
                assertEquals(dimensions[1], grid.columns());
                for (int index = 0; index < grid.size(); index++) {
                    assertEquals(probability == 1.0, grid.isBlocked(index));
                    assertEquals(1, grid.terrainCost(index));
                }
            }
        }
    }

    @Test
    void rejectsInvalidDimensionsAndProbabilities() {
        RandomGridGenerator generator = new RandomGridGenerator();
        for (int[] dimensions : new int[][] {{0, 1}, {-1, 1}, {1, 0}, {1, -1}, {50_000, 50_000}}) {
            assertThrows(IllegalArgumentException.class,
                    () -> generator.generate(dimensions[0], dimensions[1], 0L, 0.30));
        }
        for (double probability : new double[] {-0.01, 1.01, Double.NaN,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> generator.generate(2, 3, 0L, probability));
        }
    }
}
