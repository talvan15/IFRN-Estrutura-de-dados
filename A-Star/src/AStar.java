import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class AStar extends JFrame {
    private static final int SELECIONAR_INICIO = 0;
    private static final int SELECIONAR_FIM = 1;
    private static final int DESENHAR_PAREDE = 2;

    private int modoAtual = SELECIONAR_INICIO;
    private final int LINHAS = 20;
    private final int COLUNAS = 20;
    private final Node[][] grade = new Node[LINHAS][COLUNAS];

    private Node nodoInicio = null;
    private Node nodoFim = null;
    private final JButton[][] botoesGrade = new JButton[LINHAS][COLUNAS];

    // Classe interna que representa cada célula (Nodo) do grafo
    private static class Node implements Comparable<Node> {
        int linha, coluna;
        double gCost, hCost, fCost; // Custos do A*
        boolean ehParede = false;
        Node pai = null;

        Node(int linha, int coluna) {
            this.linha = linha;
            this.coluna = coluna;
        }

        @Override
        public int compareTo(Node outro) {
            return Double.compare(this.fCost, outro.fCost);
        }
    }

    public AStar() {
        setTitle("Visualizador do Algoritmo A*");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel de Controle (Topo)
        JPanel painelControle = new JPanel();
        JButton btnInicio = new JButton("Definir Início (Verde)");
        JButton btnFim = new JButton("Definir Fim (Vermelho)");
        JButton btnParede = new JButton("Desenhar Obstáculos (Cinza)");
        JButton btnCalcular = new JButton("Buscar Caminho");
        JButton btnLimpar = new JButton("Limpar Tudo");

        painelControle.add(btnInicio);
        painelControle.add(btnFim);
        painelControle.add(btnParede);
        painelControle.add(btnCalcular);
        painelControle.add(btnLimpar);
        add(painelControle, BorderLayout.NORTH);

        // Painel do Grid do Mapa
        JPanel painelGrade = new JPanel(new GridLayout(LINHAS, COLUNAS));
        inicializarGrade(painelGrade);
        add(painelGrade, BorderLayout.CENTER);

        // Ouvintes de Ação dos Botões de Controle
        btnInicio.addActionListener(e -> modoAtual = SELECIONAR_INICIO);
        btnFim.addActionListener(e -> modoAtual = SELECIONAR_FIM);
        btnParede.addActionListener(e -> modoAtual = DESENHAR_PAREDE);
        btnCalcular.addActionListener(e -> executarAStar());
        btnLimpar.addActionListener(e -> resetarGrade());
    }

    private void inicializarGrade(JPanel painelGrade) {
        for (int r = 0; r < LINHAS; r++) {
            for (int c = 0; c < COLUNAS; c++) {
                grade[r][c] = new Node(r, c);
                JButton botao = new JButton();
                botao.setBackground(Color.WHITE);
                botoesGrade[r][c] = botao;

                final int linhaFinal = r;
                final int colunaFinal = c;

                // Evento ao clicar no quadrado
                botao.addActionListener(e -> interagirComCelula(linhaFinal, colunaFinal));

                // Evento para arrastar o mouse e desenhar paredes de forma fluida
                botao.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if (modoAtual == DESENHAR_PAREDE && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                            interagirComCelula(linhaFinal, colunaFinal);
                        }
                    }
                });

                painelGrade.add(botao);
            }
        }
    }

    private void interagirComCelula(int r, int c) {
        Node nodo = grade[r][c];
        if (modoAtual == SELECIONAR_INICIO) {
            if (nodoInicio != null) botoesGrade[nodoInicio.linha][nodoInicio.coluna].setBackground(Color.WHITE);
            nodoInicio = nodo;
            nodo.ehParede = false;
            botoesGrade[r][c].setBackground(Color.GREEN);
        } else if (modoAtual == SELECIONAR_FIM) {
            if (nodoFim != null) botoesGrade[nodoFim.linha][nodoFim.coluna].setBackground(Color.WHITE);
            nodoFim = nodo;
            nodo.ehParede = false;
            botoesGrade[r][c].setBackground(Color.RED);
        } else if (modoAtual == DESENHAR_PAREDE) {
            if (nodo != nodoInicio && nodo != nodoFim) {
                nodo.ehParede = !nodo.ehParede;
                botoesGrade[r][c].setBackground(nodo.ehParede ? Color.DARK_GRAY : Color.WHITE);
            }
        }
    }

    // Heurística de distância Euclidiana (pode ser alterada para Manhattan se não usar diagonais)
    private double calcularHeuristica(Node a, Node b) {
        return Math.hypot(a.linha - b.linha, a.coluna - b.coluna);
    }

    private void executarAStar() {
        if (nodoInicio == null || nodoFim == null) {
            JOptionPane.showMessageDialog(this, "Selecione o ponto de Início e o Fim primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limpa caminhos anteriores sem apagar as paredes
        limparCaminhoAnterior();

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[LINHAS][COLUNAS];

        nodoInicio.gCost = 0;
        nodoInicio.hCost = calcularHeuristica(nodoInicio, nodoFim);
        nodoInicio.fCost = nodoInicio.gCost + nodoInicio.hCost;
        openSet.add(nodoInicio);

        boolean caminhoEncontrado = false;

        while (!openSet.isEmpty()) {
            Node atual = openSet.poll();

            if (atual == nodoFim) {
                caminhoEncontrado = true;
                break;
            }

            closedSet[atual.linha][atual.coluna] = true;
            if (atual != nodoInicio && atual != nodoFim) {
                botoesGrade[atual.linha][atual.coluna].setBackground(new Color(175, 238, 238)); // Células avaliadas (Azul claro)
            }

            // Explora os 8 vizinhos possíveis (incluindo diagonais)
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;

                    int novoR = atual.linha + dr;
                    int novoC = atual.coluna + dc;

                    if (novoR >= 0 && novoR < LINHAS && novoC >= 0 && novoC < COLUNAS) {
                        Node vizinho = grade[novoR][novoC];

                        if (vizinho.ehParede || closedSet[novoR][novoC]) continue;

                        // Custo de movimento: 1.414 para diagonais, 1.0 para retas
                        double custoMovimento = (dr != 0 && dc != 0) ? Math.sqrt(2) : 1.0;
                        double tentativeGCost = atual.gCost + custoMovimento;

                        if (tentativeGCost < vizinho.gCost || !openSet.contains(vizinho)) {
                            vizinho.pai = atual;
                            vizinho.gCost = tentativeGCost;
                            vizinho.hCost = calcularHeuristica(vizinho, nodoFim);
                            vizinho.fCost = vizinho.gCost + vizinho.hCost;

                            if (!openSet.contains(vizinho)) {
                                openSet.add(vizinho);
                                if (vizinho != nodoFim) {
                                    botoesGrade[novoR][novoC].setBackground(new Color(255, 218, 185)); // Na fila de análise (Laranja claro)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (caminhoEncontrado) {
            // Reconstrói e pinta o caminho final em Amarelo
            Node temp = nodoFim.pai;
            while (temp != null && temp != nodoInicio) {
                botoesGrade[temp.linha][temp.coluna].setBackground(Color.YELLOW);
                temp = temp.pai;
            }
            JOptionPane.showMessageDialog(this, "Caminho encontrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Não foi possível encontrar um caminho até o destino.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCaminhoAnterior() {
        for (int r = 0; r < LINHAS; r++) {
            for (int c = 0; c < COLUNAS; c++) {
                Node nodo = grade[r][c];
                nodo.gCost = Double.MAX_VALUE;
                nodo.fCost = Double.MAX_VALUE;
                nodo.pai = null;
                if (!nodo.ehParede && nodo != nodoInicio && nodo != nodoFim) {
                    botoesGrade[r][c].setBackground(Color.WHITE);
                }
            }
        }
    }

    private void resetarGrade() {
        nodoInicio = null;
        nodoFim = null;
        modoAtual = SELECIONAR_INICIO;
        for (int r = 0; r < LINHAS; r++) {
            for (int c = 0; c < COLUNAS; c++) {
                grade[r][c] = new Node(r, c);
                botoesGrade[r][c].setBackground(Color.WHITE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AStar app = new AStar();
            app.setVisible(true);
        });
    }
}
