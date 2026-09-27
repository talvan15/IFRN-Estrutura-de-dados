package org.example.algorithm;
import java.util.*;
import org.example.model.*;
import org.example.heuristic.Heuristic;
abstract class BestFirstFinder implements PathFinder {
    private record Entry(Node node,long sequence) {
    }
    protected abstract double priority(Node node);
    public SearchResult find(Grid grid,Position start,Position goal,Heuristic heuristic,SearchListener listener) {
        Objects.requireNonNull(listener);
        long begun=System.nanoTime(),excluded=0,sequence=0;
        int generated=0,maxOpen=0;
        Comparator<Entry> order=Comparator.comparingDouble((Entry e)->priority(e.node)).thenComparingDouble(e->e.node.h()).thenComparingLong(Entry::sequence);
        PriorityQueue<Entry> open=new PriorityQueue<>(order);
        Map<Position,Entry> entries=new HashMap<>();
        Set<Position> closed=new HashSet<>();
        List<Position> visited=new ArrayList<>();
        if(!grid.walkable(start)||!grid.walkable(goal)) return new SearchResult(List.of(),0,0,0,System.nanoTime()-begun,0,List.of(),false);
        double initialH=heuristic.estimate(start,goal);
        Node first=new Node(start.x(),start.y(),0,initialH,initialH,null,true);
        Entry initial=new Entry(first,sequence++);
        open.add(initial);
        entries.put(start,initial);
        generated++;
        maxOpen=1;
        // O tempo dos listeners inclui esperas de animação e fica fora da métrica.
        long before=System.nanoTime();
        listener.onNodeOpened(first,1);
        excluded+=System.nanoTime()-before;
        while(!open.isEmpty()) {
            if(Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException();
            Entry current=open.remove();
            Node node=current.node;
            Position p=node.position();
            closed.add(p);
            visited.add(p);
            before=System.nanoTime();
            listener.onNodeClosed(node,visited.size(),before-begun-excluded);
            excluded+=System.nanoTime()-before;
            if(p.equals(goal)) {
                List<Position> path=new ArrayList<>();
                for(Position at=p;at!=null;at=entries.get(at).node.parent()) path.add(at);
                Collections.reverse(path);
                SearchResult result=new SearchResult(path,visited.size(),generated,node.g(),System.nanoTime()-begun-excluded,maxOpen,visited,true);
                listener.onPathFound(result);
                return result;
            }
            for(Position next:grid.neighbors(p)) {
                if(closed.contains(next)) continue;
                int g=node.g()+Grid.cost(p,next);
                Entry old=entries.get(next);
                if(old!=null&&g>=old.node.g()) continue;
                double h=heuristic.estimate(next,goal);
                Node neighbor=new Node(next.x(),next.y(),g,h,g+h,p,true);
                if(old!=null) open.remove(old);
                else generated++;
                // Uma melhoria de G preserva o desempate original por inserção.
                Entry entry=new Entry(neighbor,old==null?sequence++:old.sequence);
                entries.put(next,entry);
                open.add(entry);
                maxOpen=Math.max(maxOpen,open.size());
                before=System.nanoTime();
                listener.onNodeOpened(neighbor,open.size());
                excluded+=System.nanoTime()-before;
            }
        }
        return new SearchResult(List.of(),visited.size(),generated,0,System.nanoTime()-begun-excluded,maxOpen,visited,false);
    }
}
