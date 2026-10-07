package org.example.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import org.example.model.*;
import org.example.heuristic.*;
import org.example.algorithm.*;
import org.example.util.*;
public final class MainFrame extends JFrame {
    private final ControlPanel controls=new ControlPanel();
    private final GridPanel left=new GridPanel(),right=new GridPanel();
    private final MetricsPanel leftMetrics=new MetricsPanel("A*"),rightMetrics=new MetricsPanel("Gulosa");
    private final JPanel grids=new JPanel(new GridLayout(1,1,16,0)),metrics=new JPanel(new GridLayout(1,1,16,0));
    private final List<Lane> lanes=new ArrayList<>();
    private boolean active,paused;
    private final javax.swing.Timer playback;
    private long lastTick;
    private double credit;
    private int epoch;
    private record Event(Node node,boolean closed,int count,long ns,SearchResult result,String error) {
    }
    private static final class Lane {
        final boolean greedy;
        final GridPanel panel;
        final MetricsPanel metrics;
        // A fila limitada aplica contrapressão sem bloquear a EDT.
        final BlockingQueue<Event> events=new ArrayBlockingQueue<>(8192);
        final Set<Position> opened=new HashSet<>();
        int generated,peak;
        SearchResult result;
        SwingWorker<Void,Void> worker;
        Lane(boolean greedy,GridPanel panel,MetricsPanel metrics) {
            this.greedy=greedy;
            this.panel=panel;
            this.metrics=metrics;
        }
    }
    public MainFrame() {
        super("PredatorPath — Laboratório de rotas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080,760));setSize(1440,900);setLocationRelativeTo(null);
        JPanel content=new JPanel(new BorderLayout(0,12));
        content.setBorder(BorderFactory.createEmptyBorder(16,20,12,20));setContentPane(content);
        grids.add(left);content.add(grids,BorderLayout.CENTER);
        JPanel sidebar=new JPanel(new BorderLayout(0,22));
        sidebar.setBackground(Theme.SURFACE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20,18,20,18));
        sidebar.add(controls,BorderLayout.NORTH);
        JPanel metricBlock=new JPanel(new BorderLayout(0,12));metricBlock.setOpaque(false);
        JLabel metricsTitle=new JLabel("MÉTRICAS DA SIMULAÇÃO");metricsTitle.setForeground(Theme.MUTED);
        metricBlock.add(metricsTitle,BorderLayout.NORTH);
        metrics.setOpaque(false);metrics.add(leftMetrics);metricBlock.add(metrics,BorderLayout.CENTER);
        sidebar.add(metricBlock,BorderLayout.CENTER);
        JScrollPane scroll=new JScrollPane(sidebar);
        scroll.setBorder(BorderFactory.createEmptyBorder());scroll.setPreferredSize(new Dimension(390,0));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);content.add(scroll,BorderLayout.EAST);
        JLabel instruction=new JLabel("Arraste o Coiote e o Papa-Léguas • Clique para editar obstáculos • Espaço: executar/pausar");
        instruction.setForeground(Theme.MUTED);content.add(instruction,BorderLayout.SOUTH);
        loadScenario();
        left.setOnEdit(()->edited(left,right));right.setOnEdit(()->edited(right,left));
        controls.run.addActionListener(e->start());controls.pause.addActionListener(e->togglePause());controls.reset.addActionListener(e->reset());
        controls.clear.addActionListener(e->reset());
        controls.mazes.addActionListener(e->loadScenario());
        for(AbstractButton b:new AbstractButton[]{controls.astar,controls.greedy,controls.compare})b.addActionListener(e->{
            grids.removeAll();metrics.removeAll();boolean compare=controls.compare.isSelected();
            grids.setLayout(new GridLayout(1,compare?2:1,16,0));metrics.setLayout(new GridLayout(1,compare?2:1,16,0));
            grids.add(left);metrics.add(leftMetrics);if(compare){grids.add(right);metrics.add(rightMetrics);}
            reset();display();content.revalidate();content.repaint();
        });
        bindings();display();playback=new javax.swing.Timer(16,e->tick());playback.start();
        addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){cancel();playback.stop();}});
    }
    private void edited(GridPanel source,GridPanel target) {
        cancel();target.setScenario(copy(source.scenario()));leftMetrics.reset();rightMetrics.reset();
    }
    private void loadScenario() {
        try {setScenario(MazeLoader.read(getClass().getResourceAsStream("/mazes/"+new String[]{"dashboard","u-trap","corridors","islands"}[controls.mazes.getSelectedIndex()]+".txt")));}
        catch(Exception ex){error(ex.getMessage());}
    }
    private MazeLoader.Scenario copy(MazeLoader.Scenario s) {
        return new MazeLoader.Scenario(s.grid().copy(),s.start(),s.goals());
    }
    private void setScenario(MazeLoader.Scenario s) {
        cancel();
        left.setScenario(s);
        right.setScenario(copy(s));
        leftMetrics.reset();
        rightMetrics.reset();
    }
    private void display() {
        left.setGreedy(!controls.compare.isSelected()&&controls.greedy.isSelected());right.setGreedy(true);
        leftMetrics.setTitle(!controls.compare.isSelected()&&controls.greedy.isSelected()?"Gulosa":"A*");
    }
    private void start() {
        if(active)return;
        reset();
        active=true;
        paused=false;
        controls.lock(true);
        left.setEditing(false);right.setEditing(false);
        controls.pause.setText("Pausar");
        controls.status.setText("Explorando…");
        display();
        MazeLoader.Scenario scenario=copy(left.scenario());
        scenario.grid().setDiagonals(true);
        scenario.grid().setPreventCornerCutting(true);
        Heuristic heuristic=new Diagonal();
        boolean nearest=false;
        lanes.add(new Lane(!controls.compare.isSelected()&&controls.greedy.isSelected(),left,leftMetrics));
        if(controls.compare.isSelected())lanes.add(new Lane(true,right,rightMetrics));
        for(Lane lane:lanes)lane.metrics.setStatus("Buscando…");
        int runEpoch=epoch;
        for(Lane lane:lanes) {
            lane.worker=new SwingWorker<>() {
                private void emit(Event event) {
                    try {
                        lane.events.put(event);
                    }
                    catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new CancellationException();
                    }
                }
                protected Void doInBackground() {
                    try {
                        SearchResult result=new RoutePlanner().find(lane.greedy?new GreedyFinder():new AStarFinder(),scenario.grid(),scenario.start(),scenario.goals(),heuristic,nearest,new SearchListener() {
                            public void onSegmentStarted() {
                                emit(new Event(null,false,-1,0,null,null));
                            }
                            public void onNodeOpened(Node node,int size) {
                                emit(new Event(node,false,size,0,null,null));
                            }
                            public void onNodeClosed(Node node,int count,long ns) {
                                emit(new Event(node,true,count,ns,null,null));
                            }
                        }
                        );
                        emit(new Event(null,false,0,0,result,null));
                    }
                    catch(CancellationException ignored) {
                    }
                    catch(Exception ex) {
                        emit(new Event(null,false,0,0,null,ex.getMessage()==null?ex.toString():ex.getMessage()));
                    }
                    return null;
                }
                protected void done() {
                    if(runEpoch==epoch&&isCancelled())controls.status.setText("Busca cancelada");
                }
            }
            ;
            lane.worker.execute();
        }
        credit=0;
        lastTick=System.nanoTime();
    }
    private void tick() {
        long now=System.nanoTime();
        if(!active||paused) {
            lastTick=now;
            return;
        }
        int speed=controls.speed.getValue();
        credit+=((now-lastTick)/1e9)*(speed==101?100000:Math.pow(speed,1.7));
        lastTick=now;
        int steps=(int)Math.min(credit,1000);
        credit-=steps;
        long deadline=System.nanoTime()+8_000_000;
        for(int i=0;i<steps&&active&&System.nanoTime()<deadline;i++)advance();
    }
    private void advance() {
        for(Lane lane:lanes) {
            if(lane.result!=null)continue;
            Event event;
            int budget=100;
            while(budget-->0&&(event=lane.events.poll())!=null) {
                if(event.error!=null) {
                    String message=event.error;
                    cancel();
                    error(message);
                    return;
                }
                if(event.result!=null) {
                    lane.result=event.result;
                    lane.panel.path(event.result);
                    lane.metrics.finish(event.result);
                    break;
                }
                if(event.count==-1) {
                    lane.opened.clear();
                    lane.panel.clearSearch();
                    continue;
                }
                if(event.closed) {
                    lane.panel.closed(event.node);
                    lane.metrics.live(event.count,lane.generated,lane.peak,event.ns);
                    break;
                }
                if(lane.opened.add(event.node.position()))lane.generated++;
                lane.peak=Math.max(lane.peak,event.count);
                lane.panel.opened(event.node);
            }
            lane.panel.repaint();
        }
        if(!lanes.isEmpty()&&lanes.stream().allMatch(l->l.result!=null)) {
            active=false;
            controls.lock(false);
            left.setEditing(true);right.setEditing(true);
            controls.status.setText(lanes.stream().allMatch(l->l.result.found())?"Concluído":"Sem caminho");
            if(lanes.size()==2) {
                Lane a=lanes.get(0),g=lanes.get(1);
                a.metrics.highlight(a.result,g.result);
                g.metrics.highlight(g.result,a.result);
            }
            controls.pause.setText("Pausar");
        }
    }
    private void togglePause() {
        if(!active)return;
        paused=!paused;
        lastTick=System.nanoTime();
        controls.pause.setText(paused?"Continuar":"Pausar");
        controls.status.setText(paused?"Busca pausada":"Busca em andamento…");
        for(Lane lane:lanes)if(lane.result==null)lane.metrics.setStatus(paused?"Pausado":"Buscando…");
    }
    private void cancel() {
        epoch++;
        for(Lane lane:lanes)if(lane.worker!=null)lane.worker.cancel(true);
        lanes.clear();
        active=false;
        paused=false;
        controls.lock(false);
        left.setEditing(true);right.setEditing(true);
        controls.pause.setText("Pausar");
        controls.status.setText("Pronto");
    }
    private void reset() {
        cancel();
        left.clearSearch();
        right.clearSearch();
        leftMetrics.reset();
        rightMetrics.reset();
    }
    private void error(String message) {
        JOptionPane.showMessageDialog(this,message,"Não foi possível concluir",JOptionPane.ERROR_MESSAGE);
    }
    private void bindings() {
        bind("SPACE",()-> {
            if(active)togglePause();else start();
        }
        );
        bind("R",this::reset);
        bind("1",()-> {
            if(!active)controls.astar.doClick();
        }
        );
        bind("2",()-> {
            if(!active)controls.greedy.doClick();
        }
        );
    }
    private void bind(String key,Runnable action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key),key);
        getRootPane().getActionMap().put(key,new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        }
        );
    }
}
