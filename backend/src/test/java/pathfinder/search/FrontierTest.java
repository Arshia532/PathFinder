package pathfinder.search;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
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
    void priorityFrontierReturnsLowestCostFirstAcrossGrowth() {
        Frontier priority = new PriorityFrontier(1);
        priority.push(10, 40);
        priority.push(20, 10);
        priority.push(30, 30);
        priority.push(40, 20);
        assertEquals(20, priority.pop());
        assertEquals(40, priority.pop());
        assertEquals(30, priority.pop());
        assertEquals(10, priority.pop());
        assertTrue(priority.isEmpty());
    }

    @Test
    void priorityFrontierBreaksEqualPriorityTiesByHigherCostThenInsertionOrder() {
        // Heuristic equals the cell index: priority = cost + cell.
        Frontier priority = new PriorityFrontier(2, cell -> cell);
        priority.push(4, 1); // priority 5, cost 1 (order 0)
        priority.push(2, 3); // priority 5, cost 3 (order 1: closer to goal)
        priority.push(2, 3); // priority 5, cost 3 (order 2: later duplicate)
        priority.push(1, 3); // priority 4, cost 3 (lowest priority overall)

        assertEquals(1, priority.pop(), "Lowest total priority comes first");
        assertEquals(2, priority.pop(), "Among equal priorities, higher path cost comes first");
        assertEquals(2, priority.pop(), "Equal priority and cost break in insertion order");
        assertEquals(4, priority.pop());
        assertTrue(priority.isEmpty());
    }

    @Test
    void frontiersMatchJavaCollectionsUnderRandomUse() {
        record HeapEntry(int cell, long priority, long cost, long order) {
        }
        Comparator<HeapEntry> comparator = Comparator
                .comparingLong(HeapEntry::priority)
                .thenComparing(Comparator.comparingLong(HeapEntry::cost).reversed())
                .thenComparingLong(HeapEntry::order);

        Random random = new Random(11L);
        Frontier queue = new QueueFrontier(1);
        Frontier stack = new StackFrontier(1);
        Frontier priority = new PriorityFrontier(1, cell -> cell % 7);
        ArrayDeque<Integer> expectedQueue = new ArrayDeque<>();
        ArrayDeque<Integer> expectedStack = new ArrayDeque<>();
        PriorityQueue<HeapEntry> expectedPriority = new PriorityQueue<>(comparator);
        long nextOrder = 0;

        for (int step = 0; step < 20_000; step++) {
            // Push slightly more often than pop so all frontiers grow repeatedly.
            if (expectedQueue.isEmpty() || random.nextInt(100) < 55) {
                int cell = random.nextInt(1_000);
                long cost = random.nextInt(50);
                queue.push(cell, cost);
                stack.push(cell, cost);
                priority.push(cell, cost);
                expectedQueue.addLast(cell);
                expectedStack.push(cell);
                expectedPriority.add(new HeapEntry(cell, cost + (cell % 7), cost, nextOrder++));
            } else {
                assertEquals(expectedQueue.removeFirst(), queue.pop(), "Queue order at step " + step);
                assertEquals(expectedStack.pop(), stack.pop(), "Stack order at step " + step);
                assertEquals(expectedPriority.remove().cell(), priority.pop(),
                        "Priority order at step " + step);
            }
            assertEquals(expectedQueue.isEmpty(), queue.isEmpty());
            assertEquals(expectedStack.isEmpty(), stack.isEmpty());
            assertEquals(expectedPriority.isEmpty(), priority.isEmpty());
        }
    }

    @Test
    void emptyFrontiersRejectPopEvenWithZeroInitialCapacity() {
        for (Frontier frontier : new Frontier[] {
                new QueueFrontier(0), new StackFrontier(0), new PriorityFrontier(0)}) {
            assertThrows(NoSuchElementException.class, frontier::pop);
            frontier.push(5, 0);
            assertEquals(5, frontier.pop());
            assertThrows(NoSuchElementException.class, frontier::pop);
        }
        assertThrows(NullPointerException.class, () -> new PriorityFrontier(4, null));
    }
}
