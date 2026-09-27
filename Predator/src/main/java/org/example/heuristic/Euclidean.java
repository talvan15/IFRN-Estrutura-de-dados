package org.example.heuristic;
import org.example.model.Position;
import static org.example.model.Grid.*;
public final class Euclidean implements Heuristic {
    private static final int D=COST_STRAIGHT;
    public double estimate(Position a,Position b) {
        int dx=Math.abs(a.x()-b.x()),dy=Math.abs(a.y()-b.y());
        return D*Math.hypot(dx,dy);
    }
    public String toString() {
        return "Euclidiana";
    }
}
