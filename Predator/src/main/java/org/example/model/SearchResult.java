package org.example.model;
import java.util.List;
public record SearchResult(List<Position> path,int exploredNodes,int generatedNodes,int totalCost,long timeNs,int maxOpenSize,List<Position> visitOrder,boolean found) {
    public SearchResult {
        path=List.copyOf(path);
        visitOrder=List.copyOf(visitOrder);
    }
    public int steps() {
        return Math.max(0,path.size()-1);
    }
}
