package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/** Fixed-width dashboard controls; simulation state is owned by MainFrame. */
public final class ControlPanel extends JPanel {
    public final JToggleButton astar=new JToggleButton("A* Search",true),greedy=new JToggleButton("Guloso BFS"),compare=new JToggleButton("Comparar Mapas");
    public final JButton run=new JButton("Executar Simulação"),pause=new JButton("Pausar"),reset=new JButton("Reiniciar Simulação"),clear=new JButton("Limpar Caminho");
    public final JSlider speed=new JSlider(1,101,15);
    public final JComboBox<String> mazes=new JComboBox<>(new String[]{"Mapa 24 × 16","Armadilha em U","Corredores","Ilhas"});
    public final JLabel status=new JLabel("Pronto");

    public ControlPanel() {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBackground(Theme.SURFACE);
        JLabel title=new JLabel("PredatorPath — Painel de Controle");
        title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,17));
        row(title);gap(24);heading("SELEÇÃO DE ALGORITMO");
        ButtonGroup group=new ButtonGroup();group.add(astar);group.add(greedy);
        JPanel algorithms=grid(1,2);algorithms.add(greedy);algorithms.add(astar);row(algorithms);
        for(JToggleButton button:new JToggleButton[]{astar,greedy,compare}) {
            style(button,Theme.CARD);
            button.addItemListener(e->button.setBackground(button.isSelected()?Theme.BLUE:Theme.CARD));
        }
        astar.setBackground(Theme.BLUE);
        gap(22);heading("MAPA E SIMULAÇÃO");row(mazes);gap(10);
        JPanel actions=grid(2,2);
        for(AbstractButton button:new AbstractButton[]{compare,clear,reset,pause}) {
            if(button instanceof JButton)style(button,button==reset?Theme.ORANGE:Theme.CARD);
            actions.add(button);
        }
        row(actions);gap(18);
        JLabel speedTitle=new JLabel("Velocidade da Sim");row(speedTitle);
        speed.setBackground(Theme.SURFACE);speed.setForeground(Theme.MUTED);
        Hashtable<Integer,JLabel> labels=new Hashtable<>();
        labels.put(1,new JLabel("Lento"));labels.put(51,new JLabel("Médio"));labels.put(101,new JLabel("Rápido"));
        labels.values().forEach(label->{label.setForeground(Theme.MUTED);label.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));});
        speed.setLabelTable(labels);speed.setPaintLabels(true);row(speed);gap(14);
        style(run,Theme.GREEN);run.setForeground(Theme.BACKGROUND);row(run);gap(12);
        status.setForeground(Theme.MUTED);row(status);
        reset.setToolTipText("Cancela a execução e limpa resultados, preservando o mapa editado");
        clear.setToolTipText("Remove a exploração e a rota, preservando obstáculos e personagens");
        compare.setToolTipText("Executa A* e Guloso no mesmo cenário");
        speed.setToolTipText("Controla a animação; não altera o tempo medido da busca");
        lock(false);
    }
    private void heading(String text){JLabel label=new JLabel(text);label.setForeground(Theme.MUTED);label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));row(label);gap(10);}
    private void gap(int height){add(Box.createVerticalStrut(height));}
    private void row(JComponent component){component.setAlignmentX(LEFT_ALIGNMENT);component.setMaximumSize(new Dimension(Integer.MAX_VALUE,component.getPreferredSize().height));add(component);}
    private JPanel grid(int rows,int columns){JPanel panel=new JPanel(new GridLayout(rows,columns,8,8));panel.setOpaque(false);return panel;}
    private void style(AbstractButton button,Color color){
        button.setBackground(color);button.setForeground(Theme.TEXT);button.setFocusPainted(false);button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.GRID),BorderFactory.createEmptyBorder(11,8,11,8)));
        button.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    public void lock(boolean running) {
        for(Component c:new Component[]{astar,greedy,compare,mazes,run})c.setEnabled(!running);
        pause.setEnabled(running);
    }
}
