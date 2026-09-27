package org.example.ui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import org.example.model.SearchResult;
import org.example.util.MetricsRecorder;
public final class ChartPanel extends JPanel {
    private SearchResult astar,greedy;
    private final MetricsRecorder recorder;
    private final DefaultTableModel table=new DefaultTableModel(new String[] {
        "Algoritmo","Heurística","Tempo","Nós","Custo"
    }
    ,0) {
        public boolean isCellEditable(int r,int c) {
            return false;
        }
    }
    ;
    public ChartPanel(MetricsRecorder recorder) {
        this.recorder=recorder;
        setLayout(new BorderLayout());
        JPanel bars=new JPanel() {
            protected void paintComponent(Graphics raw) {
                super.paintComponent(raw);
                Graphics2D g=(Graphics2D)raw.create();
                Theme.quality(g);
                g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));
                if(astar==null||greedy==null) {
                    g.setColor(Theme.MUTED);
                    g.drawString("Execute Comparar para visualizar as barras · ciano A* / roxo Guloso",16,30);
                    g.dispose();
                    return;
                }
                String[] names= {
                    "Tempo","Explorados","Gerados","Custo","Passos","Pico aberta"
                }
                ;
                double[] a= {
                    astar.timeNs(),astar.exploredNodes(),astar.generatedNodes(),astar.totalCost(),astar.steps(),astar.maxOpenSize()
                }
                ,b= {
                    greedy.timeNs(),greedy.exploredNodes(),greedy.generatedNodes(),greedy.totalCost(),greedy.steps(),greedy.maxOpenSize()
                }
                ;
                int width=Math.max(70,getWidth()/6);
                for(int i=0;i<6;i++) {
                    int x=i*width+12;
                    double max=Math.max(1,Math.max(a[i],b[i]));
                    g.setColor(Theme.MUTED);
                    g.drawString(names[i],x,16);
                    g.setColor(Theme.CYAN);
                    if(i!=3&&i!=4||astar.found())g.fillRoundRect(x,25,(int)((width-25)*a[i]/max),12,6,6);
                    g.setColor(Theme.PURPLE);
                    if(i!=3&&i!=4||greedy.found())g.fillRoundRect(x,43,(int)((width-25)*b[i]/max),12,6,6);
                    g.setColor(Theme.TEXT);
                    String label=i==0?MetricsPanel.formatTime((long)a[i])+" / "+MetricsPanel.formatTime((long)b[i]):(long)a[i]+" / "+(long)b[i];
                    if(i==3||i==4)label=(astar.found()?""+(long)a[i]:"—")+" / "+(greedy.found()?""+(long)b[i]:"—");
                    g.drawString(label,x,73);
                }
                g.dispose();
            }
        }
        ;
        bars.setPreferredSize(new Dimension(700,85));
        add(bars,BorderLayout.NORTH);
        JTable history=new JTable(table);
        history.setRowHeight(23);
        add(new JScrollPane(history));
        JButton export=new JButton("Exportar histórico CSV");
        export.addActionListener(e-> {
            JFileChooser chooser=new JFileChooser();if(chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION)try {
                recorder.export(chooser.getSelectedFile().toPath());
            }
            catch(Exception ex) {
                JOptionPane.showMessageDialog(this,ex.getMessage(),"Erro ao exportar",JOptionPane.ERROR_MESSAGE);
            }
        }
        );
        add(export,BorderLayout.SOUTH);
        setPreferredSize(new Dimension(700,210));
    }
    public void refresh() {
        table.setRowCount(0);
        for(var run:recorder.runs()) {
            var r=run.result();
            table.addRow(new Object[] {
                run.algorithm(),run.heuristic(),MetricsPanel.formatTime(r.timeNs()),r.exploredNodes(),r.found()?r.totalCost():"Sem rota"
            }
            );
        }
    }
    public void compare(SearchResult a,SearchResult g) {
        astar=a;
        greedy=g;
        repaint();
    }
    public void clearComparison() {
        astar=null;
        greedy=null;
        repaint();
    }
}
