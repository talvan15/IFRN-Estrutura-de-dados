package org.example.algorithm;
import org.example.model.*;
import org.example.heuristic.Heuristic;
public interface PathFinder {
    SearchResult find(Grid grid,Position start,Position goal,Heuristic heuristic,SearchListener listener);
}
