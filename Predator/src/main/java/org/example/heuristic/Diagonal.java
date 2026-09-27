package org.example.heuristic;
import org.example.model.Position;
import static org.example.model.Grid.*;
public final class Diagonal implements Heuristic {
    private static final int D=COST_STRAIGHT;
    public double estimate(Position a,Position b) {
        int dx=Math.abs(a.x()-b.x()),dy=Math.abs(a.y()-b.y());
        return D*(dx+dy)+(COST_DIAGONAL-2*D)*Math.min(dx,dy);
    }
    public String toString() {
        return "Diagonal (octile)";
    }
}
