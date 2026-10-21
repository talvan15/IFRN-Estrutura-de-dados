# RotaMinima

Aplicação desktop acadêmica da disciplina **Estrutura de Dados Não Lineares**, do curso de Análise e Desenvolvimento de Sistemas do IFRN. Demonstra caminhos mínimos com Dijkstra e um percurso fechado mínimo de visitas, permitindo repetições, com Dijkstra combinado à força bruta.

## Pré-requisitos e execução no Linux

- JDK 21 para compilar e Java 21 ou superior para executar o JAR. O projeto usa `maven.compiler.release=21`, conforme o requisito de entrega.
- Maven 3.9 ou superior para compilar.
- Sessão gráfica para executar Swing. Não há dependências de execução além do Java; JUnit 5 é usado apenas nos testes. O primeiro build precisa de internet para baixar plugins Maven e JUnit.

Na raiz do projeto:

```bash
mvn clean verify
java -jar target/rotaminima-1.0.0.jar
```

Para executar apenas os testes:

```bash
mvn test
```

O manifesto do JAR aponta para `br.edu.ifrn.rotaminima.app.Main`. Não é necessário fornecer classpath ou instalar bibliotecas visuais. Confira com `mvn -version` se o Maven está usando o JDK 21 antes de compilar.

## Controles da interface

1. **Adicionar cidade:** clique no espaço vazio do desenho. As cidades recebem nomes A, B, C etc. O limite visual é 20 cidades. Cliques muito próximos de outra cidade são ignorados para evitar sobreposição na criação.
2. **Conectar / atualizar peso:** clique em duas cidades distintas e informe o peso. Aceita ponto ou vírgula decimal, inclusive zero. Uma aresta existente terá seu peso substituído. Cancelar o diálogo não altera o grafo.
3. **Remover aresta:** selecione o modo e clique nas duas cidades. Um clique vazio cancela a primeira seleção.
4. **Carregar exemplo:** escolha um dos quatro exemplos e pressione o botão. Isso substitui o grafo atual. **Limpar** começa um grafo vazio.
5. **Dijkstra:** selecione origem e destino, calcule e navegue com **Anterior** e **Próximo**. A tabela mostra os vetores após finalizar e relaxar uma cidade. O caminho final aparece ao chegar ao último snapshot. Origem igual ao destino tem custo zero; destino inacessível é informado explicitamente.
6. **Rota de visitas:** selecione a cidade inicial e calcule. O botão fica disponível para 3 a 9 cidades. São mostrados a ordem planejada com retorno, o trajeto expandido, o custo, a quantidade de ordens e o tempo total.

Origem verde, destino laranja e arestas do resultado azuis. Os rótulos mostram sempre os pesos **originais**, inclusive quando um caminho indireto custa menos. A primeira cidade selecionada na edição recebe um contorno amarelo. Não há arraste ou remoção individual de cidades. Ao editar, os resultados, etapas e destaques anteriores são invalidados; trocar seletores também limpa os resultados para evitar informações referentes a outra seleção.

A janela é redimensionável. Coordenadas do modelo usam um espaço lógico fixo de 1000 × 800 e não dependem do tamanho da janela; desenho e detecção de cliques compartilham a mesma transformação. Aproximar cidades não muda os pesos. O destaque de uma aresta indica sua participação no trajeto; a sequência textual informa a direção e as repetições.

## Modelo e arquitetura

Pacote base: `br.edu.ifrn.rotaminima`.

| Pacote | Responsabilidade |
| --- | --- |
| `model` | `City`, matriz em `Graph`, resultados e snapshots defensivos |
| `algorithm` | `DijkstraSolver` e `BruteForceTspSolver`, sem Swing |
| `service` | `RoutePlanningService` integra os cálculos; `GraphExamples` fornece os exemplos |
| `ui` | `MainFrame` controla ações e resultados; `GraphPanel` desenha e detecta cliques |
| `app` | `Main` inicia a interface na Event Dispatch Thread |

O grafo é simples, não direcionado e ponderado, representado por `double[][]`. A diagonal é zero; ausência de aresta é `Double.POSITIVE_INFINITY`. Zero é um peso válido. As mutações de arestas mantêm a simetria, rejeitam laços, pesos negativos, NaN e infinitos fornecidos como peso. A construção por matriz valida dimensões, diagonal, simetria e valores. Infinito positivo é aceito nessa matriz exclusivamente como ausência de aresta. Os IDs são índices estáveis; não há remoção individual de cidades.

Listas imutáveis e cópias defensivas de vetores preservam resultados e snapshots. Cada cálculo usa uma cópia do grafo em `SwingWorker`. A edição e ações conflitantes ficam desabilitadas durante o cálculo; os controles são restaurados mesmo após erro. Atualizações Swing ocorrem na thread de eventos. Pintar o desenho não altera o resultado.

## Dijkstra didático

A origem começa com distância zero, demais distâncias com infinito e todos os predecessores com -1. Em cada iteração, uma seleção linear escolhe o vértice alcançável não finalizado de menor distância; empates mantêm o menor índice. O vértice é finalizado e suas arestas são relaxadas. Só uma melhora estrita troca distância e predecessor.

A execução termina quando não há mais vértices alcançáveis não finalizados. Nunca se soma a partir de distância infinita. O caminho é reconstruído pelos predecessores, do destino à origem, e invertido. Um destino inacessível produz lista vazia e estado explícito de inacessibilidade. Um snapshot independente é registrado após cada finalização e relaxamento, contendo cidade processada, distâncias, predecessores e finalizados.

Com matriz e seleção linear, o tempo é **O(V²)** por origem. A matriz ocupa O(V²); os vetores do algoritmo ocupam O(V). Os snapshots didáticos ocupam O(V²) no total. Não se geram snapshots nos cálculos internos da rota de visitas.

## Dijkstra combinado à força bruta

Dijkstra sozinho **não resolve o caixeiro viajante**: calcula caminhos mínimos a partir de uma origem, mas não escolhe a melhor ordem de visita de todas as cidades nem otimiza o retorno.

No segundo modo:

1. Executa-se Dijkstra a partir de cada cidade, guardando distâncias e predecessores.
2. Constrói-se a matriz de menores distâncias de todos os pares. Se o grafo estiver desconectado, não existe percurso fechado que visite todas as cidades.
3. Fixa-se a origem e enumeram-se, por backtracking em ordem crescente dos índices, todas as **(n-1)!** permutações restantes. Circuitos inversos também são avaliados.
4. Para cada ordem, somam-se os n trechos na matriz, incluindo o retorno à origem. Apenas a melhor ordem e seu custo são guardados; as permutações não são armazenadas.
5. Reconstruem-se os menores caminhos de cada trecho pelos predecessores. Ao concatenar, descarta-se o primeiro vértice de cada novo trecho, evitando duplicar artificialmente a junção.
6. Soma-se o custo das arestas reais do trajeto expandido e confere-se sua equivalência com o custo planejado, com tolerância numérica.

**A ordem planejada contém cada cidade uma vez, mas os caminhos entre elas podem atravessar outras cidades e repetir vértices ou arestas.**

Na exibição da ordem, a origem aparece novamente no final para indicar o retorno. Isso resolve o **percurso fechado mínimo que visita todas as cidades, permitindo repetições no trajeto real**, para pesos não negativos. Não garante um ciclo hamiltoniano no grafo original. Um ciclo hamiltoniano exige visitar cada vértice exatamente uma vez antes de retornar, sempre por arestas originais. A enumeração ocorre sobre o fecho de menores distâncias, cujas ligações podem representar vários passos no grafo original. Qualquer percurso de visitas pode ser dividido em trechos entre visitas; substituir esses trechos por caminhos mínimos não aumenta o custo, justificando o uso dessa matriz para o objetivo com repetições.

Executar esta versão de Dijkstra a partir de todos os vértices custa **O(V³)**. A enumeração custa **O(n × (n-1)!)**, pois cada ordem tem n trechos, incluindo o retorno. O backtracking ocupa O(n) além dos resultados de Dijkstra, que ocupam O(n²). A força bruta é limitada a nove cidades para manter o uso interativo.

## Exemplos e a correção do valor 11

| Exemplo | Arestas originais | Resultado esperado |
| --- | --- | --- |
| Quatro cidades | AB=1, AC=4, AD=6, BC=2, BD=3, CD=3 | Percurso com repetições: **10**, seis ordens |
| Dijkstra — caminho indireto | AB=2, BC=3, AC=10, CD=1, BD=8 | A → B → C → D, custo **6** |
| Grafo desconectado | AB=2, CD=3 | De A a D: inacessível; rota de todas: impossível |
| Repetição no trajeto | AB=1, BC=1 | Ordem A → B → C → A; expansão A → B → C → B → A, custo **4** |

O enunciado chama o primeiro exemplo de “Quatro cidades — rota ótima 11” e solicita 11 também sobre as menores distâncias. Esses dados são incompatíveis: o ciclo direto A → B → D → C → A custa 11, mas a expansão A → B → C → D → B → A custa **1+2+3+3+1 = 10**. Na matriz de menores distâncias, A-C custa 3 e A-D custa 4. As seis ordens com origem A têm custos 10, 10, 10, 10, 12 e 12. A implementação preserva todos os pesos fornecidos e informa corretamente o ótimo **10**, sem alterar o algoritmo para produzir 11. Essa distinção também aparece na interface e nos testes.

## Tempo, testes e limitações

O tempo usa `System.nanoTime`, dentro do trabalho em segundo plano, excluindo cópia prévia do grafo, desenho, atualização de componentes e navegação de snapshots. No modo Dijkstra, inclui a criação dos snapshots; na rota, inclui Dijkstra por cidade, enumeração, reconstrução e conferência. É **medição de uma execução**, não benchmark rigoroso.

Os testes JUnit 5 cobrem caminhos e predecessores, todas as distâncias alcançáveis, origem igual ao destino, desconexão, peso zero, validação e simetria da matriz, cópias defensivas, snapshots, desempates, quantidade fatorial, limites, retorno, ordem de visitas e custo e validade da expansão. O caso de nove cidades avalia 40.320 ordens.

Limitações: dados em memória, sem salvar/abrir arquivos; máximo visual de 20 cidades e força bruta de 3 a 9; sem mover ou remover cidades individualmente; precisão finita de `double`. Pesos finitos muito grandes cuja soma exceda a capacidade de `double` geram uma mensagem de erro, em vez de um resultado incorreto. Em janelas muito pequenas ou grafos densos pode haver sobreposição de arestas e rótulos; maximize para apresentar. A aplicação não resolve a versão que exige ciclo hamiltoniano no grafo original.

O arquivo `src/main/java/org/example/Main.java`, já existente na pasta, foi preservado e não é o ponto de entrada do JAR.

## Verificação desta entrega

- `clean verify` executado com JDK 21 e Maven 3.9.11: **BUILD SUCCESS**, 14 testes, nenhuma falha, erro ou teste ignorado.
- JAR gerado com `Main-Class` correto e bytecode de versão 65 (Java 21).
- Conferência gráfica executada: caminho de custo 6, navegação até a última etapa, tabela de quatro cidades, bloqueio/restauração de controles, rota expandida de custo 4, desconexão, Limpar e redimensionamento para 1000 × 650. As imagens da janela foram inspecionadas.
- O JAR final também foi iniciado com `java -jar` em sessão gráfica e encerrado automaticamente após cinco segundos; a inicialização não produziu erro. O modo sem ambiente gráfico foi conferido e informa a necessidade de sessão gráfica.

O pacote `target/rotaminima-entrega.zip` reúne código, testes, documentação, relatório dos testes e o JAR em `target/rotaminima-1.0.0.jar`. Após extrair, entre na pasta `rotaminima` e use o mesmo comando de execução deste README. O ZIP é um artefato de entrega gerado à parte; um novo `mvn clean` remove os arquivos em `target`.
