package br.edu.ifrn.explorer;

import br.edu.ifrn.explorer.model.*;
import br.edu.ifrn.explorer.search.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest {
    @Test void simpleRouteAndParentReconstruction() {
        for (Algorithm algorithm : Algorithm.values()) {
            Grid g = new Grid(5, 3);
            SearchSnapshot s = new SearchEngine(g, Movement.FOUR, algorithm).runToEnd();
            assertEquals(SearchEngine.Status.FOUND, s.status());
            assertEquals(60, s.metrics().cost()); assertEquals(6, s.metrics().steps());
            assertValidPath(g, Movement.FOUR, s);
        }
    }
    @Test void noRouteExhaustsFrontier() {
        Grid g = new Grid(3, 3); g.wall(new Cell(1, 0), true); g.wall(new Cell(0, 1), true);
        for (Algorithm algorithm : Algorithm.values()) for (Movement movement : Movement.values()) {
            SearchSnapshot s = new SearchEngine(g, movement, algorithm).runToEnd();
            assertEquals(SearchEngine.Status.NO_PATH, s.status()); assertTrue(s.path().isEmpty());
            assertNull(s.metrics().cost()); assertEquals(0, s.metrics().frontier()); assertEquals(1, s.metrics().expanded());
        }
    }
    @Test void greedyCanChooseMoreExpensiveRoute() {
        Grid g = fromRows("....#...", "........", ".###...#", "........", "..#..###", ".......#", "..#..#..");
        SearchSnapshot a = new SearchEngine(g, Movement.FOUR, Algorithm.ASTAR).runToEnd();
        SearchSnapshot b = new SearchEngine(g, Movement.FOUR, Algorithm.GREEDY).runToEnd();
        assertEquals(130, a.metrics().cost()); assertEquals(170, b.metrics().cost());
        assertValidPath(g, Movement.FOUR, a); assertValidPath(g, Movement.FOUR, b);
    }
    @Test void diagonalCostsAndHeuristics() {
        Grid g = new Grid(3, 3);
        for (Algorithm algorithm : Algorithm.values()) {
            SearchSnapshot s = new SearchEngine(g, Movement.EIGHT, algorithm).runToEnd();
            assertEquals(28, s.metrics().cost()); assertEquals(2, s.metrics().steps()); assertValidPath(g, Movement.EIGHT, s);
        }
        assertEquals(38, Movement.EIGHT.heuristic(new Cell(0, 0), new Cell(3, 2)));
        assertEquals(50, Movement.FOUR.heuristic(new Cell(0, 0), new Cell(3, 2)));
    }
    @Test void cornersRequireBothAdjacentCellsFree() {
        Grid g = new Grid(2, 2); g.wall(new Cell(1, 0), true);
        assertFalse(g.neighbors(g.start(), Movement.EIGHT).stream().anyMatch(e -> e.cell().equals(g.goal())));
        assertEquals(20, new SearchEngine(g, Movement.EIGHT, Algorithm.ASTAR).runToEnd().metrics().cost());
        g.wall(new Cell(0, 1), true);
        assertEquals(SearchEngine.Status.NO_PATH, new SearchEngine(g, Movement.EIGHT, Algorithm.ASTAR).runToEnd().status());
    }
    @Test void astarMatchesIndependentBellmanFordOnEveryThreeByThreeMap() {
        // 7 non-endpoint cells -> all 128 obstacle patterns, for both movement models.
        for (Movement movement : Movement.values()) for (int mask = 0; mask < 128; mask++) {
            Grid g = new Grid(3, 3);
            for (int index = 1; index < 8; index++) g.wall(new Cell(index % 3, index / 3), (mask & (1 << (index - 1))) != 0);
            SearchSnapshot s = new SearchEngine(g, movement, Algorithm.ASTAR).runToEnd();
            assertEquals(referenceCost(g, movement), s.metrics().cost(), "mask=" + mask + ", movement=" + movement);
            if (s.metrics().cost() != null) assertValidPath(g, movement, s);
        }
    }
    @Test void largerSeededMapsMatchReferenceAndHaveStableTieBreaks() {
        for (int seed = 0; seed < 40; seed++) for (Movement movement : Movement.values()) {
            Grid g = new Grid(9, 8); Random random = new Random(seed);
            for (int y = 0; y < g.height(); y++) for (int x = 0; x < g.width(); x++) g.wall(new Cell(x, y), random.nextDouble() < .25);
            SearchSnapshot s = new SearchEngine(g, movement, Algorithm.ASTAR).runToEnd();
            assertEquals(referenceCost(g, movement), s.metrics().cost(), "seed=" + seed);
            SearchSnapshot again = new SearchEngine(g, movement, Algorithm.ASTAR).runToEnd();
            assertEquals(s.path(), again.path()); assertEquals(s.metrics().expanded(), again.metrics().expanded());
            if (s.metrics().cost() != null) assertValidPath(g, movement, s);
        }
    }
    @Test void oneStepIsOneRealExpansionAndMetricsCountUniqueFrontier() {
        for (Algorithm algorithm : Algorithm.values()) {
            SearchEngine e = new SearchEngine(Maps.random(42), Movement.EIGHT, algorithm);
            int previous = 0;
            while (!e.finished()) {
                e.step(); SearchSnapshot s = e.snapshot();
                assertEquals(previous + 1, s.metrics().expanded()); previous = s.metrics().expanded();
                int open = 0, closed = 0, discovered = 0;
                for (int y = 0; y < Maps.HEIGHT; y++) for (int x = 0; x < Maps.WIDTH; x++) {
                    Cell c = new Cell(x, y); if (s.open(c)) open++; if (s.closed(c)) closed++;
                    if (s.g(c) != Integer.MAX_VALUE) discovered++;
                }
                assertEquals(open, s.metrics().frontier()); assertEquals(discovered, s.metrics().discovered());
                assertEquals(closed, s.metrics().expanded()); // consistent heuristic: no reopen is needed
                assertEquals(discovered, open + closed);
            }
            int end = e.snapshot().metrics().expanded(); e.step(); assertEquals(end, e.snapshot().metrics().expanded());
        }
    }
    @Test void goalIsOnlyAcceptedWhenExpanded() {
        Grid g = new Grid(2, 2); SearchEngine e = new SearchEngine(g, Movement.EIGHT, Algorithm.ASTAR);
        e.step(); SearchSnapshot first = e.snapshot();
        assertEquals(14, first.g(g.goal())); assertTrue(first.open(g.goal())); assertEquals(SearchEngine.Status.SEARCHING, first.status());
        e.step(); assertEquals(SearchEngine.Status.FOUND, e.snapshot().status()); assertEquals(2, e.snapshot().metrics().expanded());
    }
    @Test void editorAndReturnedSnapshotsCannotMutateRunningSearch() {
        Grid g = new Grid(3, 3); SearchEngine a = new SearchEngine(g, Movement.FOUR, Algorithm.ASTAR);
        SearchSnapshot before = a.snapshot(); g.wall(new Cell(0, 1), true); g.wall(new Cell(1, 0), true);
        assertEquals(40, a.runToEnd().metrics().cost()); assertEquals(0, before.metrics().expanded());
        assertEquals(Integer.MAX_VALUE, before.g(new Cell(1, 0)));
        assertThrows(UnsupportedOperationException.class, () -> a.snapshot().path().clear());
    }
    @Test void endpointsCannotOverlapWallsOrEachOther() {
        Grid g = new Grid(3, 3);
        assertFalse(g.wall(g.start(), true)); assertFalse(g.wall(g.goal(), true));
        assertFalse(g.start(g.goal())); assertFalse(g.goal(g.start()));
        g.wall(new Cell(1, 1), true); assertFalse(g.start(new Cell(1, 1))); assertFalse(g.goal(new Cell(1, 1)));
    }
    @Test void seededGenerationIsReproducibleAndPresetsAreSolvable() {
        Grid a = Maps.random(-2026), b = Maps.random(-2026);
        for (int y = 0; y < a.height(); y++) for (int x = 0; x < a.width(); x++) assertEquals(a.blocked(new Cell(x, y)), b.blocked(new Cell(x, y)));
        for (int i = 0; i < 3; i++) for (Movement movement : Movement.values())
            assertEquals(SearchEngine.Status.FOUND, new SearchEngine(Maps.preset(i), movement, Algorithm.ASTAR).runToEnd().status());
    }
    private static Grid fromRows(String... rows) {
        Grid g = new Grid(rows[0].length(), rows.length);
        for (int y = 0; y < rows.length; y++) for (int x = 0; x < rows[y].length(); x++) g.wall(new Cell(x, y), rows[y].charAt(x) == '#');
        return g;
    }
    private static void assertValidPath(Grid grid, Movement movement, SearchSnapshot s) {
        assertEquals(grid.start(), s.path().getFirst()); assertEquals(grid.goal(), s.path().getLast());
        assertEquals(s.path().size(), new HashSet<>(s.path()).size()); int cost = 0;
        for (int i = 1; i < s.path().size(); i++) {
            Cell before = s.path().get(i - 1), after = s.path().get(i);
            Grid.Edge edge = grid.neighbors(before, movement).stream().filter(e -> e.cell().equals(after)).findFirst().orElseThrow(); cost += edge.cost();
        }
        assertEquals(cost, s.metrics().cost());
    }
    /** Independent relaxation oracle: does not call production neighbors or heuristics. */
    private static Integer referenceCost(Grid g, Movement movement) {
        int size = g.width() * g.height(), inf = 1_000_000; int[] d = new int[size]; Arrays.fill(d, inf);
        d[g.start().y() * g.width() + g.start().x()] = 0;
        for (int pass = 0; pass < size - 1; pass++) {
            boolean changed = false;
            for (int from = 0; from < size; from++) for (int to = 0; to < size; to++) {
                int ax = from % g.width(), ay = from / g.width(), bx = to % g.width(), by = to / g.width();
                int dx = Math.abs(ax - bx), dy = Math.abs(ay - by);
                if (dx > 1 || dy > 1 || dx + dy == 0 || g.blocked(new Cell(ax, ay)) || g.blocked(new Cell(bx, by))) continue;
                if (dx + dy == 2 && (movement == Movement.FOUR || g.blocked(new Cell(ax, by)) || g.blocked(new Cell(bx, ay)))) continue;
                int candidate = d[from] + (dx + dy == 2 ? 14 : 10);
                if (candidate < d[to]) { d[to] = candidate; changed = true; }
            }
            if (!changed) break;
        }
        int result = d[g.goal().y() * g.width() + g.goal().x()]; return result == inf ? null : result;
    }
}
