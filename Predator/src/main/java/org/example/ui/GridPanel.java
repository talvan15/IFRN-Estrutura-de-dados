package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import org.example.model.*;
import org.example.util.MazeLoader;

/** A viewport-sized board. Painting and hit testing share the same transform. */
public final class GridPanel extends JPanel {
    private Grid grid;
    private Position start,goal,hover;
    private List<Position> path=List.of();
    private final Set<Position> closed=new HashSet<>();
    private boolean editing=true,greedy,dragStart,dragGoal,erase,drawing;
    private Runnable onEdit=()->{};

    public GridPanel() {
        setMinimumSize(new Dimension(0,0));
        MouseAdapter mouse=new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(!editing||grid==null)return;
                Position p=at(e);if(!grid.contains(p))return;
                dragStart=p.equals(start);dragGoal=p.equals(goal);drawing=!dragStart&&!dragGoal;
                erase=SwingUtilities.isRightMouseButton(e)||!grid.walkable(p);
                if(drawing)paintWall(p);
            }
            public void mouseDragged(MouseEvent e) {
                if(!editing||grid==null)return;
                Position p=at(e);if(!grid.contains(p))return;
                if(dragStart&&!p.equals(goal)){start=p;grid.setWall(p,false);changed();}
                else if(dragGoal&&!p.equals(start)){goal=p;grid.setWall(p,false);changed();}
                else if(drawing)paintWall(p);
            }
            public void mouseReleased(MouseEvent e){dragStart=false;dragGoal=false;drawing=false;}
            public void mouseMoved(MouseEvent e){if(grid!=null){hover=at(e);repaint();}}
            public void mouseExited(MouseEvent e){hover=null;repaint();}
        };
        addMouseListener(mouse);addMouseMotionListener(mouse);
        setToolTipText("A: predador • B: presa");
    }
    public void setScenario(MazeLoader.Scenario s){grid=s.grid();start=s.start();goal=s.goals().getFirst();clearSearch();}
    public MazeLoader.Scenario scenario(){return new MazeLoader.Scenario(grid,start,List.of(goal));}
    public void setOnEdit(Runnable action){onEdit=action;}
    public void setEditing(boolean enabled){editing=enabled;if(!enabled){drawing=false;dragStart=false;dragGoal=false;}repaint();}
    public void setGreedy(boolean value){greedy=value;repaint();}
    private void changed(){clearSearch();onEdit.run();}
    private void paintWall(Position p){
        if(!p.equals(start)&&!p.equals(goal)&&grid.walkable(p)==!erase){grid.setWall(p,!erase);changed();}
    }
    private double cell(){return Math.max(1,Math.min((getWidth()-16.0)/grid.width(),(getHeight()-16.0)/grid.height()));}
    private double offsetX(){return (getWidth()-grid.width()*cell())/2;}
    private double offsetY(){return (getHeight()-grid.height()*cell())/2;}
    private Position at(MouseEvent e){return new Position((int)Math.floor((e.getX()-offsetX())/cell()),(int)Math.floor((e.getY()-offsetY())/cell()));}
    public void clearSearch(){closed.clear();path=List.of();repaint();}
    public void opened(Node node){} // Only explored nodes are visualized.
    public void closed(Node node){closed.add(node.position());}
    public void path(SearchResult result){path=result.found()?result.path():List.of();repaint();}
    protected void paintComponent(Graphics raw){
        super.paintComponent(raw);if(grid==null)return;
        Graphics2D g=(Graphics2D)raw.create();Theme.quality(g);
        double c=cell();g.translate(offsetX(),offsetY());Color accent=greedy?Theme.PURPLE:Theme.CYAN;
        for(int y=0;y<grid.height();y++)for(int x=0;x<grid.width();x++){
            Position p=new Position(x,y);
            g.setColor(!grid.walkable(p)?new Color(0x59616d):closed.contains(p)?new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),65):Theme.SURFACE);
            g.fill(new Rectangle2D.Double(x*c+.5,y*c+.5,c-1,c-1));
        }
        if(path.size()>1){
            Path2D route=new Path2D.Double();route.moveTo((path.getFirst().x()+.5)*c,(path.getFirst().y()+.5)*c);
            for(Position p:path)route.lineTo((p.x()+.5)*c,(p.y()+.5)*c);
            g.setStroke(new BasicStroke((float)Math.max(2,c*.15),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(accent);g.draw(route);
        }
        marker(g,start,"A",c,false);marker(g,goal,"B",c,true);
        if(editing&&hover!=null&&grid.contains(hover)){
            g.setColor(Theme.TEXT);g.setStroke(new BasicStroke(1));g.draw(new Rectangle2D.Double(hover.x()*c+1,hover.y()*c+1,c-2,c-2));
        }
        g.dispose();
    }
    private void marker(Graphics2D g,Position p,String text,double c,boolean round){
        double x=p.x()*c+c*.08,y=p.y()*c+c*.08,size=c*.84;
        g.setColor(Theme.TEXT);
        g.fill(round?new Ellipse2D.Double(x,y,size,size):new RoundRectangle2D.Double(x,y,size,size,c*.12,c*.12));
        g.setColor(Theme.BACKGROUND);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,(int)Math.max(10,c*.52)));
        FontMetrics fm=g.getFontMetrics();g.drawString(text,(float)((p.x()+.5)*c-fm.stringWidth(text)/2.0),(float)((p.y()+.5)*c+(fm.getAscent()-fm.getDescent())/2.0));
    }
}
