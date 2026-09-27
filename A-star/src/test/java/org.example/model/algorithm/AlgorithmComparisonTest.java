package org.example.model.algorithm;

import org.example.algorithm.AStarSearch;
import org.example.algorithm.GreedySearch;
import org.example.algorithm.SearchResult;
import org.example.model.Grid;
import org.example.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmComparisonTest {

    @Test
    void bothAlgorithmsShouldFindPathOnSimpleMap() {

        Grid grid = new Grid(10, 10);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(9, 9));

        SearchResult aStarResult =
                new AStarSearch().search(grid);

        SearchResult greedyResult =
                new GreedySearch().search(grid);

        assertTrue(aStarResult.isFound());
        assertTrue(greedyResult.isFound());
    }

    @Test
    void aStarShouldFindOptimalPathOnEmptyGrid() {

        Grid grid = new Grid(10, 10);

        grid.setStart(new Position(0, 0));
        grid.setGoal(new Position(9, 9));

        SearchResult result =
                new AStarSearch().search(grid);

        /*
         * Manhattan:
         *
         * |0 - 9| + |0 - 9|
         * = 18
         */
        assertEquals(
                18.0,
                result.getPathCost()
        );
    }

    @Test
    void aStarPathShouldNeverBeMoreExpensiveThanGreedyPath() {

        Grid grid = createComparisonGrid();

        SearchResult aStarResult =
                new AStarSearch().search(grid);

        SearchResult greedyResult =
                new GreedySearch().search(grid);

        assertTrue(aStarResult.isFound());
        assertTrue(greedyResult.isFound());

        assertTrue(
                aStarResult.getPathCost()
                        <= greedyResult.getPathCost(),
                "O A* não deveria produzir um caminho "
                        + "mais caro que o Greedy neste mapa."
        );
    }

    @Test
    void bothPathsShouldBeValid() {

        Grid grid = createComparisonGrid();

        SearchResult aStarResult =
                new AStarSearch().search(grid);

        SearchResult greedyResult =
                new GreedySearch().search(grid);

        assertValidPath(
                grid,
                aStarResult
        );

        assertValidPath(
                grid,
                greedyResult
        );
    }

    private Grid createComparisonGrid() {

        Grid grid = new Grid(10, 12);

        grid.setStart(new Position(5, 1));
        grid.setGoal(new Position(5, 10));

        /*
         * Barreira vertical.
         *
         * Existem passagens em cima e embaixo,
         * forçando os algoritmos a escolherem
         * como contornar a barreira.
         */
        for (int row = 1; row <= 8; row++) {

            if (row != 2 && row != 8) {

                grid.setWall(
                        new Position(row, 6),
                        true
                );
            }
        }

        return grid;
    }

    private void assertValidPath(
            Grid grid,
            SearchResult result
    ) {

        assertTrue(result.isFound());

        for (Position position : result.getPath()) {

            assertTrue(
                    grid.isValid(position),
                    "Posição fora do mapa: "
                            + position
            );

            assertTrue(
                    grid.isWalkable(position),
                    "O caminho passou por um obstáculo: "
                            + position
            );
        }

        for (int i = 1;
             i < result.getPath().size();
             i++) {

            Position previous =
                    result.getPath().get(i - 1);

            Position current =
                    result.getPath().get(i);

            int distance =
                    Math.abs(
                            previous.getRow()
                                    - current.getRow()
                    )
                            +
                            Math.abs(
                                    previous.getColumn()
                                            - current.getColumn()
                            );

            assertEquals(
                    1,
                    distance,
                    "O caminho contém um movimento inválido "
                            + previous
                            + " -> "
                            + current
            );
        }
    }
}