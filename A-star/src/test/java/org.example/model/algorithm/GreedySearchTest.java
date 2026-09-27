package org.example.model.algorithm;

import org.example.algorithm.GreedySearch;
import org.example.algorithm.SearchResult;
import org.example.model.Grid;
import org.example.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GreedySearchTest {

    private GreedySearch greedy;

    @BeforeEach
    void setUp() {
        greedy = new GreedySearch();
    }

    @Test
    void shouldFindStraightPath() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(0, 4));

        SearchResult result =
                greedy.search(grid);

        assertTrue(result.isFound());

        assertEquals(
                4.0,
                result.getPathCost()
        );
    }

    @Test
    void shouldStartAtStartPosition() {

        Grid grid = new Grid(5, 5);

        Position start = new Position(0, 0);

        grid.setStart(start);
        grid.setGoal(new Position(4, 4));

        SearchResult result =
                greedy.search(grid);

        assertTrue(result.isFound());

        assertEquals(
                start,
                result.getPath().get(0)
        );
    }

    @Test
    void shouldEndAtGoalPosition() {

        Grid grid = new Grid(5, 5);

        Position goal = new Position(4, 4);

        grid.setStart(new Position(0, 0));
        grid.setGoal(goal);

        SearchResult result =
                greedy.search(grid);

        assertTrue(result.isFound());

        Position lastPosition =
                result.getPath().get(
                        result.getPath().size() - 1
                );

        assertEquals(
                goal,
                lastPosition
        );
    }

    @Test
    void shouldAvoidWalls() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(2, 0));
        grid.setGoal(new Position(2, 4));

        Position wall =
                new Position(2, 2);

        grid.setWall(wall, true);

        SearchResult result =
                greedy.search(grid);

        assertTrue(result.isFound());

        assertFalse(
                result.getPath().contains(wall)
        );
    }

    @Test
    void shouldReturnNoPathWhenGoalIsBlocked() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(2, 2));

        grid.setWall(new Position(1, 2), true);
        grid.setWall(new Position(3, 2), true);
        grid.setWall(new Position(2, 1), true);
        grid.setWall(new Position(2, 3), true);

        SearchResult result =
                greedy.search(grid);

        assertFalse(result.isFound());

        assertTrue(
                result.getPath().isEmpty()
        );
    }

    @Test
    void shouldGenerateSearchSteps() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(4, 4));

        SearchResult result =
                greedy.search(grid);

        assertFalse(
                result.getSteps().isEmpty()
        );
    }

    @Test
    void exploredNodesShouldBeGreaterThanZero() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(4, 4));

        SearchResult result =
                greedy.search(grid);

        assertTrue(
                result.getExploredNodes() > 0
        );
    }
}