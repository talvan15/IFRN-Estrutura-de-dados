package org.example.model;
public record Node(int x, int y, int g, double h, double f, Position parent, boolean walkable) {
    public Position position() {
        return new Position(x,y);
    }
}
