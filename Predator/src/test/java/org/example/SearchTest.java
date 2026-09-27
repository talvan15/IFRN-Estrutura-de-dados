package org.example;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.example.model.*;
import org.example.algorithm.*;
import org.example.heuristic.*;
import org.example.util.*;
class SearchTest {
    private SearchResult run(PathFinder f,Grid g,Position a,Position b) {
        return f.find(g,a,b,new Diagonal(),SearchListener.NONE);
    }
    @Test void optimalOpenGrid() {
        assertEquals(126,run(new AStarFinder(),new Grid(10,10),new Position(0,0),new Position(9,9)).totalCost());
    }
    @Test void enclosedGoal() {
        Grid g=new Grid(10,10);
        Position b=new Position(5,5);
        for(int y=4;y<=6;y++)for(int x=4;x<=6;x++)if(x!=5||y!=5)g.setWall(new Position(x,y),true);
        assertFalse(run(new AStarFinder(),g,new Position(0,0),b).found());
    }
    @Test void sameStart() {
        SearchResult r=run(new AStarFinder(),new Grid(10,10),new Position(2,2),new Position(2,2));
        assertTrue(r.found());
        assertEquals(0,r.totalCost());
        assertEquals(1,r.path().size());
    }
    @Test void cornerRule() {
        Grid g=new Grid(2,2);
        g.setWall(new Position(1,0),true);
        g.setWall(new Position(0,1),true);
        assertFalse(run(new AStarFinder(),g,new Position(0,0),new Position(1,1)).found());
        g.setPreventCornerCutting(false);
        assertEquals(14,run(new AStarFinder(),g,new Position(0,0),new Position(1,1)).totalCost());
    }
    @Test void admissibleAndGreedyBound() {
        for(int seed=0;seed<50;seed++) {
            var s=MazeGenerator.generate(21,.8,seed);
            SearchResult a=run(new AStarFinder(),s.grid(),s.start(),s.goals().getFirst());
            SearchResult d=new AStarFinder().find(s.grid(),s.start(),s.goals().getFirst(),(p,q)->0,SearchListener.NONE);
            SearchResult greedy=run(new GreedyFinder(),s.grid(),s.start(),s.goals().getFirst());
            assertTrue(a.found());
            assertEquals(d.totalCost(),a.totalCost());
            assertTrue(new Diagonal().estimate(s.start(),s.goals().getFirst())<=a.totalCost());
            assertTrue(greedy.totalCost()>=a.totalCost());
        }
    }
    @Test void fourDirections() {
        Grid g=new Grid(10,10);
        g.setDiagonals(false);
        assertEquals(180,new AStarFinder().find(g,new Position(0,0),new Position(9,9),new Manhattan(),SearchListener.NONE).totalCost());
    }
    @Test void uTrapMakesGreedyMoreExpensive()throws Exception {
        var s=MazeLoader.read(getClass().getResourceAsStream("/mazes/u-trap.txt"));
        var a=run(new AStarFinder(),s.grid(),s.start(),s.goals().getFirst());
        var g=run(new GreedyFinder(),s.grid(),s.start(),s.goals().getFirst());
        assertEquals(304,a.totalCost());
        assertEquals(326,g.totalCost());
        assertTrue(g.totalCost()>a.totalCost());
    }
    @Test void waypointsAccumulate() {
        Grid g=new Grid(10,10);
        var goals=java.util.List.of(new Position(9,0),new Position(9,9));
        var r=new RoutePlanner().find(new AStarFinder(),g,new Position(0,0),goals,new Diagonal(),false,SearchListener.NONE);
        assertTrue(r.found());
        assertEquals(180,r.totalCost());
        assertEquals(18,r.steps());
        assertEquals(20,r.exploredNodes());
    }
    @Test void loaderPreservesWaypointOrder()throws Exception {
        var file=java.nio.file.Files.createTempFile("predator-map-",".txt");
        try {
            var s=new MazeLoader.Scenario(new Grid(10,10),new Position(1,1),java.util.List.of(new Position(8,8),new Position(2,2)));
            MazeLoader.save(file,s);
            assertEquals(s.goals(),MazeLoader.load(file).goals());
        }
        finally {
            java.nio.file.Files.deleteIfExists(file);
        }
    }
    @Test void emptyMapRejected() {
        assertThrows(java.io.IOException.class,()->MazeLoader.read(new java.io.ByteArrayInputStream(new byte[0])));
    }
    @Test void deterministicVisitOrder() {
        Grid g=new Grid(20,20);
        for(PathFinder f:java.util.List.of(new AStarFinder(),new GreedyFinder()))assertEquals(run(f,g,new Position(0,0),new Position(19,13)).visitOrder(),run(f,g,new Position(0,0),new Position(19,13)).visitOrder());
    }
    @Test void cancellationStopsSearch() {
        Thread.currentThread().interrupt();
        try {
            assertThrows(java.util.concurrent.CancellationException.class,()->run(new AStarFinder(),new Grid(80,80),new Position(0,0),new Position(79,79)));
        }
        finally {
            Thread.interrupted();
        }
    }
}
