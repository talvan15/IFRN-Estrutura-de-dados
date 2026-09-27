package org.example.ui;
import javax.swing.*;
import java.awt.*;

public final class ControlPanel extends JPanel {
    public final JToggleButton astar=new JToggleButton("A*",true),greedy=new JToggleButton("Gulosa"),compare=new JToggleButton("Comparar");
    public final JButton run=new JButton("Executar"),pause=new JButton("Pausar"),reset=new JButton("Reiniciar"),clear=new JButton("Limpar paredes"),options=new JButton("Opções");
    public final JSlider speed=new JSlider(1,101,15);
    public final JComboBox<String> mazes=new JComboBox<>(new String[]{"Armadilha em U","Corredores","Ilhas"});
    public final JLabel status=new JLabel("Pronto");
    public final JPanel secondary=new JPanel(new FlowLayout(FlowLayout.CENTER,12,8));
    public ControlPanel() {
        setLayout(new BorderLayout(20,0));
        JLabel title=new JLabel("PredatorPath");
        title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,20));
        add(title,BorderLayout.WEST);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        ButtonGroup group=new ButtonGroup();
        for(JToggleButton b:new JToggleButton[]{astar,greedy,compare}) {group.add(b);actions.add(b);}
        for(JButton b:new JButton[]{run,pause,reset,options})actions.add(b);
        add(actions,BorderLayout.CENTER);
        astar.setForeground(Theme.CYAN);greedy.setForeground(Theme.PURPLE);
        secondary.add(new JLabel("Cenário"));secondary.add(mazes);
        secondary.add(new JLabel("Velocidade"));speed.setPreferredSize(new Dimension(150,28));secondary.add(speed);secondary.add(clear);
        secondary.setVisible(false);
        options.addActionListener(e->{secondary.setVisible(!secondary.isVisible());revalidate();});
        reset.setToolTipText("Limpa a busca e preserva o cenário");
        speed.setToolTipText("Velocidade da animação; não altera o tempo medido da busca");
        lock(false);
    }
    public void lock(boolean running) {
        for(Component c:new Component[]{astar,greedy,compare,clear,mazes,run})c.setEnabled(!running);
        pause.setEnabled(running);
    }
}
