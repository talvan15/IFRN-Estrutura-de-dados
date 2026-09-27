package org.example.algorithm;
import org.example.model.*;
import org.example.heuristic.*;
import java.util.*;
public final class RoutePlanner {
    public SearchResult find(PathFinder finder,Grid grid,Position start,List<Position> goals,Heuristic heuristic,boolean nearest,SearchListener listener) {
        List<Position> remaining=new ArrayList<>(goals),path=new ArrayList<>(),visits=new ArrayList<>();
        int explored=0,generated=0,cost=0,peak=0;
        long time=0;
        Position current=start;
        while(!remaining.isEmpty()) {
            Position origin=current;
            Position goal=nearest?remaining.stream().min(Comparator.comparingDouble(p->new Diagonal().estimate(origin,p))).orElseThrow():remaining.getFirst();
            remaining.remove(goal);
            final int offset=explored;
            final long elapsed=time;
            listener.onSegmentStarted();
            SearchResult part=finder.find(grid,current,goal,heuristic,new SearchListener() {
                public void onNodeOpened(Node n,int size) {
                    listener.onNodeOpened(n,size);
                }
                public void onNodeClosed(Node n,int count,long ns) {
                    listener.onNodeClosed(n,offset+count,elapsed+ns);
                }
            }
            );
            explored+=part.exploredNodes();
            generated+=part.generatedNodes();
            cost+=part.totalCost();
            time+=part.timeNs();
            peak=Math.max(peak,part.maxOpenSize());
            visits.addAll(part.visitOrder());
            if(!part.found())return new SearchResult(List.of(),explored,generated,0,time,peak,visits,false);
            if(!path.isEmpty())path.removeLast();
            path.addAll(part.path());
            current=goal;
        }
        SearchResult result=new SearchResult(path,explored,generated,cost,time,peak,visits,true);
        listener.onPathFound(result);
        return result;
    }
}
