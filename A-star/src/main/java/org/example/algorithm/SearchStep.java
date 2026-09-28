package org.example.algorithm;

import org.example.model.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SearchStep {

    private final Set<Position> open;
    private final Set<Position> closed;
    private final Position current;

    private final Map<Position, Double> gValues;
    private final Map<Position, Double> hValues;

    public SearchStep(
            Set<Position> open,
            Set<Position> closed,
            Position current,
            Map<Position, Double> gValues,
            Map<Position, Double> hValues
    ) {
        this.open = new HashSet<>(open);
        this.closed = new HashSet<>(closed);
        this.current = current;

        this.gValues = new HashMap<>(gValues);
        this.hValues = new HashMap<>(hValues);
    }

    public Set<Position> getOpen() {
        return open;
    }

    public Set<Position> getClosed() {
        return closed;
    }

    public Position getCurrent() {
        return current;
    }

    public Double getG(Position position) {
        return gValues.get(position);
    }

    public Double getH(Position position) {
        return hValues.get(position);
    }

    public Double getF(Position position) {

        Double g = getG(position);
        Double h = getH(position);

        if (g == null || h == null) {
            return null;
        }

        return g + h;
    }
}