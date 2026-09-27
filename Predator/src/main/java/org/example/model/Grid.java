package org.example.model;
import java.util.*;
public final class Grid {
    public static final int COST_STRAIGHT=10, COST_DIAGONAL=14;
    private final boolean[][] walls;
    private boolean diagonals=true, preventCornerCutting=true;
    public Grid(int width,int height) {
        if(width<1||height<1) throw new IllegalArgumentException("Dimensões inválidas");
        walls=new boolean[height][width];
    }
    public int width() {
        return walls[0].length;
    }
    public int height() {
        return walls.length;
    }
    public boolean contains(Position p) {
        return p.x()>=0&&p.y()>=0&&p.x()<width()&&p.y()<height();
    }
    public boolean walkable(Position p) {
        return contains(p)&&!walls[p.y()][p.x()];
    }
    public void setWall(Position p,boolean wall) {
        if(contains(p)) walls[p.y()][p.x()]=wall;
    }
    public boolean diagonals() {
        return diagonals;
    }
    public void setDiagonals(boolean v) {
        diagonals=v;
    }
    public boolean preventCornerCutting() {
        return preventCornerCutting;
    }
    public void setPreventCornerCutting(boolean v) {
        preventCornerCutting=v;
    }
    public List<Position> neighbors(Position p) {
        List<Position> result=new ArrayList<>(8);
        for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++) {
            if(dx==0&&dy==0||!diagonals&&dx!=0&&dy!=0) continue;
            Position n=new Position(p.x()+dx,p.y()+dy);
            if(!walkable(n)) continue;
            if(dx!=0&&dy!=0&&preventCornerCutting&&(!walkable(new Position(p.x()+dx,p.y()))||!walkable(new Position(p.x(),p.y()+dy)))) continue;
            result.add(n);
        }
        return result;
    }
    public static int cost(Position a,Position b) {
        return a.x()!=b.x()&&a.y()!=b.y()?COST_DIAGONAL:COST_STRAIGHT;
    }
    public Grid copy() {
        Grid g=new Grid(width(),height());
        g.diagonals=diagonals;
        g.preventCornerCutting=preventCornerCutting;
        for(int y=0;y<height();y++) g.walls[y]=walls[y].clone();
        return g;
    }
}
