package org.example.algorithm;

import org.example.model.Position;

import java.util.HashSet;
import java.util.Set;

public class SearchStep {

    private final Set<Position> open;
    private final Set<Position> closed;
    private final Position current;

    public SearchStep(
            Set<Position> open,
            Set<Position> closed,
            Position current
    ) {
        this.open = new HashSet<>(open);
        this.closed = new HashSet<>(closed);
        this.current = current;
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
}