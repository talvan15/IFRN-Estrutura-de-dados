package org.example.util;
import org.example.model.*;
import java.util.*;
public final class MazeGenerator {
    public static MazeLoader.Scenario generate(int size,double density,long seed) {
        Random random=new Random(seed);
        Grid grid=new Grid(size,size);
        for(int y=0;y<size;y++)for(int x=0;x<size;x++)grid.setWall(new Position(x,y),true);
        Position start=new Position(1,1);
        grid.setWall(start,false);
        Deque<Position> stack=new ArrayDeque<>();
        stack.push(start);
        while(!stack.isEmpty()) {
            Position p=stack.peek();
            List<Position> choices=new ArrayList<>();
            for(int[] d:new int[][] {
                {
                    2,0
                }
                , {
                    -2,0
                }
                , {
                    0,2
                }
                , {
                    0,-2
                }
            }
            ) {
                Position n=new Position(p.x()+d[0],p.y()+d[1]);
                if(n.x()>0&&n.y()>0&&n.x()<size-1&&n.y()<size-1&&!grid.walkable(n))choices.add(n);
            }
            if(choices.isEmpty()) {
                stack.pop();
                continue;
            }
            Position n=choices.get(random.nextInt(choices.size()));
            grid.setWall(new Position((p.x()+n.x())/2,(p.y()+n.y())/2),false);
            grid.setWall(n,false);
            stack.push(n);
        }
        for(int y=1;y<size-1;y++)for(int x=1;x<size-1;x++)if(random.nextDouble()>density)grid.setWall(new Position(x,y),false);
        int end=size%2==0?size-3:size-2;
        return new MazeLoader.Scenario(grid,start,List.of(new Position(end,end)));
    }
}
