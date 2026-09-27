package org.example.model.algorithm;

import org.example.algorithm.AStarSearch;
import org.example.algorithm.SearchResult;
import org.example.model.Grid;
import org.example.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AStarSearchTest {

    private AStarSearch aStar;

    @BeforeEach
    void setUp() {
        aStar = new AStarSearch();
    }

    @Test
    void shouldFindStraightPath() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(0, 4));

        SearchResult result =
                aStar.search(grid);

        assertTrue(result.isFound());

        assertEquals(
                4.0,
                result.getPathCost()
        );

        assertEquals(
                new Position(0, 0),
                result.getPath().getFirst()
        );

        assertEquals(
                new Position(0, 4),
                result.getPath().getLast()
        );
    }

    @Test
    void shouldFindOptimalPathOnEmptyGrid() {

        Grid grid = new Grid(10, 10);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(4, 5));

        SearchResult result =
                aStar.search(grid);

        /*
         * Distância Manhattan:
         *
         * |0 - 4| + |0 - 5|
         * = 4 + 5
         * = 9
         */
        assertTrue(result.isFound());

        assertEquals(
                9.0,
                result.getPathCost()
        );
    }

    @Test
    void pathShouldContainStartAndGoal() {

        Grid grid = new Grid(5, 5);

        Position start = new Position(0, 0);
        Position goal = new Position(4, 4);

        grid.setStart(start);
        grid.setGoal(goal);

        SearchResult result =
                aStar.search(grid);

        assertTrue(result.isFound());

        assertEquals(
                start,
                result.getPath().getFirst()
        );

        assertEquals(
                goal,
                result.getPath().getLast()
        );
    }

    @Test
    void shouldAvoidWalls() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(2, 0));
        grid.setGoal(new Position(2, 4));

        Position wall = new Position(2, 2);

        grid.setWall(wall, true);

        SearchResult result =
                aStar.search(grid);

        assertTrue(result.isFound());

        assertFalse(
                result.getPath().contains(wall)
        );

        /*
         * Sem obstáculo:
         * custo seria 4.
         *
         * Com obstáculo:
         * precisa fazer um desvio.
         */
        assertEquals(
                6.0,
                result.getPathCost()
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
                aStar.search(grid);

        assertFalse(result.isFound());

        assertTrue(result.getPath().isEmpty());

        assertEquals(
                0.0,
                result.getPathCost()
        );
    }

    @Test
    void shouldFailWhenStartDoesNotExist() {

        Grid grid = new Grid(5, 5);

        grid.setGoal(new Position(4, 4));

        SearchResult result =
                aStar.search(grid);

        assertFalse(result.isFound());
        assertTrue(result.getPath().isEmpty());
    }

    @Test
    void shouldFailWhenGoalDoesNotExist() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));

        SearchResult result =
                aStar.search(grid);

        assertFalse(result.isFound());
        assertTrue(result.getPath().isEmpty());
    }

    @Test
    void pathCostShouldMatchNumberOfMovements() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(4, 4));

        SearchResult result =
                aStar.search(grid);

        /*
         * Se o caminho contém 9 posições:
         *
         * A -> 1 -> 2 -> ... -> B
         *
         * existem 8 movimentos.
         */
        assertEquals(
                result.getPath().size() - 1,
                (int) result.getPathCost()
        );
    }

    @Test
    void shouldGenerateSearchSteps() {

        Grid grid = new Grid(5, 5);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(4, 4));

        SearchResult result =
                aStar.search(grid);

        assertFalse(
                result.getSteps().isEmpty()
        );
    }
}