# Pathfinding Lab — A* e Greedy

Aplicação desktop em **Java 21**, com interface gráfica **Swing**, para visualizar e comparar dois algoritmos de busca de caminhos: **A\*** e **Greedy Best-First Search**.

O usuário pode desenhar obstáculos, escolher a origem e o destino e acompanhar a exploração do mapa por uma animação. Ao final, a aplicação apresenta o caminho encontrado, seu custo, a quantidade de nós explorados e o tempo de execução.

## Como executar

### Requisitos

- JDK 21 ou superior, com `java` e `javac` disponíveis no terminal.
- Maven para compilar pelo `pom.xml` e executar os testes.
- Ambiente gráfico para abrir a janela Swing.

Na pasta raiz do projeto, execute:

```bash
mvn compile
java -cp target/classes org.example.Main
```

Também é possível importar o `pom.xml` em uma IDE, selecionar o JDK 21 e executar a classe `org.example.Main`.

### Compilação sem Maven

A aplicação usa apenas bibliotecas do próprio Java em tempo de execução. No Linux ou macOS, é possível compilar e iniciar sem Maven:

```bash
mkdir -p out
find src/main/java -name '*.java' > /tmp/a-star-sources.txt
javac --release 21 -encoding UTF-8 -d out @/tmp/a-star-sources.txt
java -cp out org.example.Main
```

Essa alternativa compila somente a aplicação; os testes dependem do JUnit.

## Como usar

1. Ao abrir, a aplicação exibe um mapa padrão de **20 linhas por 25 colunas**, com origem, destino e obstáculos.
2. Use as ferramentas do editor para alterar o mapa.
3. Selecione **A*** ou **Greedy Best-First** e clique em **Executar**.
4. Acompanhe a exploração e o caminho final, caso exista.
5. Clique em **Comparar A* × Greedy** para executar os dois algoritmos sobre o mesmo mapa e atualizar a tabela de métricas, sem animação.

| Controle | Função |
| --- | --- |
| Parede | Clique ou arraste para adicionar obstáculos. |
| Borracha | Clique ou arraste para remover obstáculos. |
| Origem A | Selecione a ferramenta e clique em uma célula para definir a origem. |
| Destino B | Selecione a ferramenta e clique em uma célula para definir o destino. |
| Restaurar mapa padrão | Recria o cenário inicial. |
| Limpar mapa | Remove tudo, inclusive origem e destino. |
| Gerar obstáculos | Substitui as paredes por uma distribuição aleatória com probabilidade fixa de 25% por célula, preservando origem e destino e criando esses pontos se estiverem ausentes. |
| Velocidade da animação | Define o intervalo entre etapas, de 20 a 500 ms; valores menores deixam a animação mais rápida. Ajuste antes de executar. |

A origem e o destino devem ocupar células diferentes. Posicionar um sobre o outro remove a definição anterior daquele outro ponto. Paredes e borracha não apagam origem ou destino.

A geração aleatória não garante a existência de um caminho. Se o destino estiver inacessível, o resultado será **Nenhum caminho encontrado**. A edição do mapa fica bloqueada durante a animação.

### Cores e informações das células

| Cor | Significado |
| --- | --- |
| Verde | Origem A. |
| Vermelho | Destino B. |
| Amarelo | OPEN: posições descobertas que aguardam exploração. |
| Laranja | CLOSED: posições já processadas. |
| Roxo | Posição atual da busca. |
| Azul | Caminho final encontrado. |

Ao passar o mouse sobre uma célula, a interface mostra sua posição e seu tipo. Durante a visualização da busca, também mostra os valores disponíveis de `g(n)`, `h(n)` e `f(n)`, além do estado da célula.

## Como a busca funciona

O mapa é uma matriz de células vazias, paredes, origem e destino. Cada posição é identificada por linha e coluna, com índices começando em zero.

Os movimentos permitidos são **cima, baixo, esquerda e direita**, sempre com custo **1**. Não há movimentos diagonais nem passagem por paredes.

Os dois algoritmos usam a distância de Manhattan para estimar a distância até o destino:

```text
h(n) = |linha atual - linha do destino|
     + |coluna atual - coluna do destino|
```

Por exemplo, entre `(0, 0)` e `(4, 5)`, a estimativa é `4 + 5 = 9`. Em um mapa vazio, esse também é o custo do menor caminho. Obstáculos podem exigir desvios.

### A*

O A* escolhe a próxima posição pelo menor valor de:

```text
f(n) = g(n) + h(n)
```

- `g(n)`: custo acumulado da origem até a posição.
- `h(n)`: estimativa da distância restante.
- `f(n)`: soma usada para priorizar a exploração.

A implementação utiliza uma `PriorityQueue`, ordenada por `f(n)` e, em caso de empate, por `h(n)`. Quando encontra um percurso mais barato até uma posição ainda não fechada, atualiza seu custo e seu antecessor e a reinsere na fila para corrigir a prioridade.

Com os movimentos e custos deste projeto, a heurística de Manhattan permite que o A* encontre um caminho de menor custo quando existe uma solução.

### Greedy Best-First Search

O Greedy prioriza o menor `h(n)`: explora primeiro a posição que parece mais próxima do destino. Em caso de empate, esta implementação usa o menor `g(n)`.

O custo percorrido é registrado, mas não é o critério principal de escolha. Posições já visitadas ou presentes na fila não são inseridas novamente. Isso pode reduzir a exploração em alguns mapas, mas **não garante o caminho de menor custo**.

| Característica | A* | Greedy |
| --- | --- | --- |
| Prioridade principal | `g(n) + h(n)` | `h(n)` |
| Desempate | Menor `h(n)` | Menor `g(n)` |
| Atualiza um percurso já descoberto | Sim, se ficar mais barato e a posição não estiver fechada | Não |
| Garante menor custo neste modelo | Sim | Não |

### Reconstrução e animação

Cada nó guarda uma referência ao seu antecessor (`parent`). Ao alcançar o destino, o algoritmo percorre essas referências até a origem e inverte a lista para obter o caminho na ordem correta.

Durante a busca, são criados objetos `SearchStep` com cópias dos conjuntos OPEN e CLOSED, da posição atual e dos valores de custo. O resultado completo é retornado em um `SearchResult`.

A busca termina **antes de a animação começar**. A interface usa um `javax.swing.Timer` para reproduzir os passos registrados e, ao final, desenhar o caminho. Assim, a velocidade da animação não altera as decisões do algoritmo nem o tempo de busca registrado.

## Como interpretar as métricas

- **Custo:** quantidade de movimentos do caminho. Uma lista com 10 posições tem custo 9, pois inclui origem e destino. Quando não há solução, a interface exibe um traço.
- **Nós explorados:** quantidade de posições no conjunto de visitados/fechados ao terminar a busca.
- **Tempo:** duração da busca em milissegundos, medida com `System.nanoTime()`. Inclui o registro dos passos e a reconstrução do caminho, mas não a animação.

Há uma diferença na contagem atual: o **A*** verifica se chegou ao destino antes de adicioná-lo ao conjunto fechado; o **Greedy** adiciona o destino ao conjunto de visitados antes dessa verificação. Portanto, quando encontram um caminho, o Greedy inclui o destino na contagem e o A* não.

Os tempos são medições de uma execução e podem variar. Para comparar o mesmo cenário após editar o mapa, use novamente **Comparar A* × Greedy**, pois resultados de execuções individuais anteriores podem continuar no painel.

## Organização do código

```text
pom.xml                              Configuração Maven e dependência de testes
src/main/java/org/example/
├── Main.java                        Ponto de entrada da aplicação
├── algorithm/
│   ├── SearchAlgorithm.java         Interface comum dos algoritmos
│   ├── AStarSearch.java             Implementação do A*
│   ├── GreedySearch.java            Implementação do Greedy
│   ├── SearchResult.java            Caminho, passos e métricas da busca
│   └── SearchStep.java              Estado de uma etapa para visualização
├── model/
│   ├── Grid.java                    Matriz, obstáculos, vizinhos e mapas
│   ├── Position.java                Coordenadas, igualdade e hash
│   ├── Node.java                    Posição, custos e antecessor
│   └── CellType.java                Tipos de célula
├── ui/
│   ├── MainWindow.java              Janela, controles, execução e comparação
│   ├── GridPanel.java               Desenho do mapa, mouse e dicas das células
│   ├── ScrollablePanel.java         Suporte à rolagem do painel lateral
│   └── ControlPanel.java            Classe vazia, sem uso no fluxo atual
└── metrics/
    └── SearchMetrics.java           Classe vazia; métricas ficam em SearchResult
```

O fluxo principal é `Main → MainWindow → SearchAlgorithm.search(grid) → SearchResult → GridPanel`. A classe `Main` inicializa a interface na thread de eventos do Swing com `SwingUtilities.invokeLater`.

## Testes

Os testes usam **JUnit Jupiter 5.11.4** e estão em `src/test/java/org.example/model/`, incluindo a subpasta `algorithm/`.

Para executá-los:

```bash
mvn test
```

As verificações cobrem coordenadas e igualdade de posições, edição e limites do mapa, vizinhos válidos, caminhos com e sem obstáculos, destino inacessível, custos, geração de passos e comparação entre os algoritmos.

O `pom.xml` não fixa uma versão do Maven Surefire. Se a instalação do Maven usar uma versão antiga desse plugin e informar que nenhum teste foi executado, use explicitamente uma versão compatível com JUnit 5:

```bash
mvn org.apache.maven.plugins:maven-surefire-plugin:3.2.5:test
```
