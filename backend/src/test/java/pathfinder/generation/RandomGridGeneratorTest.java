package pathfinder.generation;

import org.junit.jupiter.api.Test;

import pathfinder.grid.Grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
