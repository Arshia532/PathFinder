package pathfinder.search;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontierTest {
    @Test
    void queueIsFirstInFirstOutAcrossWrapAroundAndGrowth() {
        Frontier queue = new QueueFrontier(2);
        assertTrue(queue.isEmpty());
        queue.push(1, 0);
        queue.push(2, 0);
        assertEquals(1, queue.pop());
        queue.push(3, 0); // Wraps to the start of the array.
        queue.push(4, 0); // Full, so the queue grows.
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.pop());
        assertEquals(3, queue.pop());
        assertEquals(4, queue.pop());
        assertTrue(queue.isEmpty());
    }

    @Test
    void stackIsLastInFirstOutAcrossGrowth() {
        Frontier stack = new StackFrontier(1);
        stack.push(1, 0);
        stack.push(2, 0);
        stack.push(3, 0);
        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    void frontiersMatchJavaCollectionsUnderRandomUse() {
        Random random = new Random(11L);
        Frontier queue = new QueueFrontier(1);
        Frontier stack = new StackFrontier(1);
        ArrayDeque<Integer> expectedQueue = new ArrayDeque<>();
        ArrayDeque<Integer> expectedStack = new ArrayDeque<>();
        for (int step = 0; step < 20_000; step++) {
            // Push slightly more often than pop so both frontiers grow and wrap repeatedly.
            if (expectedQueue.isEmpty() || random.nextInt(100) < 55) {
                int cell = random.nextInt(1_000);
                queue.push(cell, 0);
                stack.push(cell, 0);
                expectedQueue.addLast(cell);
                expectedStack.push(cell);
            } else {
                assertEquals(expectedQueue.removeFirst(), queue.pop(), "Queue order at step " + step);
                assertEquals(expectedStack.pop(), stack.pop(), "Stack order at step " + step);
            }
            assertEquals(expectedQueue.isEmpty(), queue.isEmpty());
            assertEquals(expectedStack.isEmpty(), stack.isEmpty());
        }
    }

    @Test
    void emptyFrontiersRejectPopEvenWithZeroInitialCapacity() {
        for (Frontier frontier : new Frontier[] {new QueueFrontier(0), new StackFrontier(0)}) {
            assertThrows(NoSuchElementException.class, frontier::pop);
            frontier.push(5, 0);
            assertEquals(5, frontier.pop());
            assertThrows(NoSuchElementException.class, frontier::pop);
        }
    }
}
