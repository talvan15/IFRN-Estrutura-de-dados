package org.example.ui;
import javax.swing.*;
import java.awt.*;
import org.example.model.SearchResult;

public final class MetricsPanel extends JPanel {
    private final JLabel title=new JLabel(),time=new JLabel("—"),explored=new JLabel("0"),cost=new JLabel("—"),status=new JLabel("Pronto");
    private boolean greedy;
    public MetricsPanel(String name) {
        setLayout(new BorderLayout(0,8));
        setBackground(Theme.CARD);
        setBorder(BorderFactory.createEmptyBorder(12,16,12,16));
        setTitle(name);
        JPanel values=new JPanel(new GridLayout(4,1,0,12));
        values.setOpaque(false);
        card(values,"Custo do Caminho",cost);card(values,"Nós explorados",explored);card(values,"Tempo",time);card(values,"Status",status);
        JPanel body=new JPanel(new BorderLayout(0,18));body.setOpaque(false);body.add(title,BorderLayout.NORTH);body.add(values);add(body,BorderLayout.NORTH);
        status.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        time.setToolTipText("Tempo de cálculo acumulado; exclui callbacks, espera na fila e animação. Ao concluir, mostra o cálculo total.");
    }
    private void card(JPanel parent,String caption,JLabel value) {
        JPanel card=new JPanel(new BorderLayout(0,5));
        card.setOpaque(false);
        JLabel label=new JLabel(caption);label.setForeground(Theme.MUTED);
        value.setFont(new Font(Font.SANS_SERIF,Font.BOLD,20));
        card.add(label,BorderLayout.NORTH);card.add(value);parent.add(card);
    }
    public void live(int closed,int opened,int peak,long ns) {
        setStatus("Buscando…");explored.setText(Integer.toString(closed));time.setText(formatTime(ns));
    }
    public void finish(SearchResult r) {
        live(r.exploredNodes(),0,0,r.timeNs());
        setStatus(!r.found()?"Sem caminho":greedy?"Rota encontrada":"Rota ótima");
        cost.setFont(cost.getFont().deriveFont(r.found()?20f:16f));cost.setText(r.found()?Integer.toString(r.totalCost()):"Sem caminho");
    }
    public void highlight(SearchResult own,SearchResult other) {
        time.setForeground(own.timeNs()<other.timeNs()?title.getForeground():Theme.TEXT);
        explored.setForeground(own.exploredNodes()<other.exploredNodes()?title.getForeground():Theme.TEXT);
        cost.setForeground(own.found()&&other.found()&&own.totalCost()<other.totalCost()?title.getForeground():Theme.TEXT);
    }
    public void reset() {
        setStatus("Pronto");time.setText("—");explored.setText("0");cost.setText("—");
        for(JLabel l:new JLabel[]{time,explored,cost})l.setForeground(Theme.TEXT);
    }
    public void setStatus(String text){status.setText(text);status.setForeground(text.startsWith("Rota")?Theme.GREEN:Theme.MUTED);}
    public void setTitle(String name) {greedy=name.equals("Gulosa");title.setText(greedy?"Guloso BFS":"A* Search");title.setForeground(greedy?Theme.PURPLE:Theme.CYAN);}
    public static String formatTime(long ns) {return ns>=1_000_000_000?String.format("%.2f s",ns/1_000_000_000.0):String.format("%.2f ms",ns/1_000_000.0);}
}
