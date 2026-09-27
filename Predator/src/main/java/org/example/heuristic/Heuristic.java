package org.example.heuristic;
import org.example.model.Position;
@FunctionalInterface public interface Heuristic {
    double estimate(Position from,Position goal);
}
