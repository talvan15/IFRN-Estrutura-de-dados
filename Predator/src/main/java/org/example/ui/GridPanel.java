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
    private final Set<Position> closed=new HashSet<>(),frontier=new HashSet<>();
    private boolean editing=true,greedy,dragStart,dragGoal,erase,drawing;
    private Runnable onEdit=()->{};
    private final Image coyoteImage = loadImage("/images/coyote.png");
    private final Image roadRunnerImage = loadImage("/images/road-runner.png");

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
        setToolTipText("Coiote: início • Papa-Léguas: destino");
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
    private double cell(){return Math.max(1,Math.min((getWidth()-64.0)/grid.width(),(getHeight()-210.0)/grid.height()));}
    private double offsetX(){return (getWidth()-grid.width()*cell())/2;}
    private double offsetY(){return 48+(getHeight()-210-grid.height()*cell())/2;}
    private Position at(MouseEvent e){return new Position((int)Math.floor((e.getX()-offsetX())/cell()),(int)Math.floor((e.getY()-offsetY())/cell()));}
    public void clearSearch(){closed.clear();frontier.clear();path=List.of();repaint();}
    public void opened(Node node){frontier.add(node.position());}
    public void closed(Node node){frontier.remove(node.position());closed.add(node.position());}
    public void path(SearchResult result){path=result.found()?result.path():List.of();repaint();}
    protected void paintComponent(Graphics raw){
        super.paintComponent(raw);if(grid==null)return;
        Graphics2D g=(Graphics2D)raw.create();Theme.quality(g);
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,15));g.setColor(greedy?Theme.PURPLE:Theme.CYAN);
        g.drawString(greedy?"Guloso BFS":"A* Search",32,22);
        double c=cell();g.translate(offsetX(),offsetY());
        g.setColor(Theme.GRID);g.fill(new Rectangle2D.Double(0,0,grid.width()*c,grid.height()*c));
        for(int y=0;y<grid.height();y++)for(int x=0;x<grid.width();x++){
            Position p=new Position(x,y);
            if(!grid.walkable(p))g.setColor(Theme.WALL);
            else if(closed.contains(p))g.setPaint(new GradientPaint(0,0,new Color(0xFFB800),(float)(grid.width()*c),(float)(grid.height()*c),new Color(0xFF5C00)));
            else g.setColor(frontier.contains(p)?new Color(0x78521F):Theme.SURFACE);
            g.fill(new Rectangle2D.Double(x*c+.5,y*c+.5,c-1,c-1));
        }
        if(path.size()>1){
            Path2D route=new Path2D.Double();route.moveTo((path.getFirst().x()+.5)*c,(path.getFirst().y()+.5)*c);
            for(Position p:path)route.lineTo((p.x()+.5)*c,(p.y()+.5)*c);
            g.setStroke(new BasicStroke((float)Math.max(2,c*.15),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(Theme.YELLOW);g.draw(route);
        }
        marker(g, start, coyoteImage, "A", c, false);
        marker(g, goal, roadRunnerImage, "B", c, true);
        if(editing&&hover!=null&&grid.contains(hover)){
            g.setColor(Theme.TEXT);g.setStroke(new BasicStroke(1));g.draw(new Rectangle2D.Double(hover.x()*c+1,hover.y()*c+1,c-2,c-2));
        }
        g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));g.setColor(Theme.MUTED);
        FontMetrics labels=g.getFontMetrics();
        for(int x=0;x<grid.width();x++){
            String text=Integer.toString(x+1);float tx=(float)((x+.5)*c-labels.stringWidth(text)/2.0);
            g.drawString(text,tx,-9);g.drawString(text,tx,(float)(grid.height()*c+18));
        }
        for(int y=0;y<grid.height();y++){
            String text=Integer.toString(y+1);g.drawString(text,-10-labels.stringWidth(text),(float)((y+.5)*c+4));
        }
        g.translate(-offsetX(),-offsetY());
        legend(g);
        g.dispose();
    }
    /** Floating legend in the lower-left viewport gutter, leaving all cells editable. */
    private void legend(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        int x=32,y=getHeight()-134,w=Math.min(300,getWidth()-48);
        g.setColor(new Color(0,0,0,65));g.fillRoundRect(x+3,y+4,w,122,16,16);
        g.setColor(Theme.SURFACE);g.fillRoundRect(x,y,w,122,16,16);
        g.setColor(Theme.GRID);g.drawRoundRect(x,y,w,122,16,16);
        String[] labels={"Início","Destino","Obstáculo","Caminho Explorado / Rota Final"};
        for(int i=0;i<labels.length;i++){
            int yy=y+10+i*27;
            if(i<2){
                Graphics2D icon=(Graphics2D)g.create();icon.translate(x+10,yy);
                marker(icon,new Position(0,0),i==0?coyoteImage:roadRunnerImage,i==0?"A":"B",24,i==1);icon.dispose();
            }else{
                if(i==2)g.setColor(Theme.WALL);
                else g.setPaint(new GradientPaint(x+13,0,Theme.YELLOW,x+31,0,new Color(0xFF5C00)));
                g.fillRoundRect(x+13,yy+3,18,18,4,4);
            }
            g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));g.setColor(Theme.TEXT);g.drawString(labels[i],x+44,yy+17);
        }
    }
    private Image loadImage(String path) {
        java.net.URL url = getClass().getResource(path);
        return url == null ? null : new ImageIcon(url).getImage();
    }

    private void marker(
            Graphics2D g, Position p, Image image,
            String fallbackText, double c, boolean round
    ) {
        if (image != null && image.getWidth(this) > 0 && image.getHeight(this) > 0) {
            double maxSize = c * .88;
            double scale = Math.min(
                    maxSize / image.getWidth(this),
                    maxSize / image.getHeight(this)
            );

            int width = Math.max(1, (int) Math.round(image.getWidth(this) * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight(this) * scale));
            int x = (int) Math.round((p.x() + .5) * c - width / 2.0);
            int y = (int) Math.round((p.y() + .5) * c - height / 2.0);

            g.drawImage(image, x, y, width, height, this);
            return;
        }

        // Marcador original, usado enquanto a imagem não estiver disponível.
        double x = p.x() * c + c * .08;
        double y = p.y() * c + c * .08;
        double size = c * .84;

        g.setColor(Theme.TEXT);
        g.fill(round
                ? new Ellipse2D.Double(x, y, size, size)
                : new RoundRectangle2D.Double(x, y, size, size, c * .12, c * .12));

        g.setColor(Theme.BACKGROUND);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) Math.max(10, c * .52)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                fallbackText,
                (float) ((p.x() + .5) * c - fm.stringWidth(fallbackText) / 2.0),
                (float) ((p.y() + .5) * c + (fm.getAscent() - fm.getDescent()) / 2.0)
        );
    }
}
