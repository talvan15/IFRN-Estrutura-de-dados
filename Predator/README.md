# PredatorPath — Laboratório de rotas

Aplicação desktop em Java 21 e Swing, sem dependências externas de execução.

## Executar

Com JDK 21 e Maven instalados, na raiz do projeto:

```bash
mvn test
mvn clean package
java -jar target/Predator-1.0-SNAPSHOT.jar
```

A aplicação precisa de uma sessão gráfica. O primeiro build baixa os plugins Maven e o JUnit.

## Usar

- O **Coiote** representa o início e o **Papa-Léguas**, o destino. Arraste os personagens para mudar suas posições.
- Clique para criar/remover obstáculos; arraste para pintar ou apagar vários. O botão direito também apaga.
- No painel lateral, selecione **Guloso BFS** ou **A* Search** e clique em **Executar Simulação**. A seleção aparece em azul; os nós explorados usam um gradiente amarelo/laranja e a rota final recebe uma linha amarela.
- **Comparar Mapas** ativa dois grids e duas colunas de métricas, executando A* e Guloso no mesmo cenário. Editar qualquer grid atualiza o outro. Desative o botão para retornar ao algoritmo selecionado.
- **Limpar Caminho** e **Reiniciar Simulação** cancelam a execução e limpam resultados, preservando obstáculos e posições. **Pausar/Continuar** controla a reprodução.
- **Velocidade da Sim** controla a animação. O seletor de cenários inclui um mapa inicial de **24 × 16** e os três mapas anteriores. As coordenadas acompanham as dimensões do cenário selecionado.
- A legenda flutuante fica no canto inferior esquerdo da área de simulação, em uma faixa reservada para não cobrir células editáveis. O painel lateral tem rolagem em janelas menores.
- Atalhos: espaço executa/pausa, R reinicia, 1 seleciona A*, 2 seleciona Guloso.

O cenário **Armadilha em U** demonstra a diferença: A* encontra custo 304 e Gulosa 326. A interface usa um único alvo, oito direções, custo ortogonal 10 e diagonal 14, sem corte de cantos. A heurística octile é admissível e consistente nessas regras. A* encontra o caminho de menor custo; Gulosa prioriza a estimativa até o alvo, sem garantir menor custo.

## Métricas

São exibidos **Custo do Caminho**, **Nós explorados**, **Tempo (ms/s)** e **Status** para cada algoritmo em execução. A* indica **Rota ótima** ao concluir com sucesso; Guloso indica **Rota encontrada**, pois não garante optimalidade. Durante a animação, tempo e contagem correspondem às expansões já reproduzidas. Ao terminar, o tempo corresponde ao cálculo total informado pelo algoritmo, incluindo a reconstrução da rota.

A medição existente usa `System.nanoTime()` e exclui callbacks, espera na fila de eventos e animação. É tempo decorrido de cálculo, não tempo de CPU puro; aquecimento da JVM e escalonamento podem afetar comparações. Custo aparece somente com uma rota completa; uma busca impossível mostra **Sem caminho**.

As buscas permanecem fora da EDT, em `SwingWorker`, com filas limitadas a 8.192 eventos. Um timer reproduz os eventos com orçamento por ciclo para manter a interface responsiva. Pausar afeta a reprodução, e o cálculo pode continuar em segundo plano até o limite da fila. Reiniciar cancela os workers e descarta eventos anteriores.

## Estrutura

- `model`, `algorithm`, `heuristic`: modelo e algoritmos originais preservados.
- `ui`: painel lateral de controles e métricas, tema escuro, grids responsivos com coordenadas e legenda desenhados em Java2D.
- `util`: utilitários originais de mapas e gravação preservados.
- `src/test`: testes existentes de busca, heurística, cancelamento e persistência.

Histórico, CSV, gráficos, mapa de calor, G/H/F, setas, múltiplos alvos e geração por densidade não fazem parte da interface principal. As capacidades existentes do modelo e utilitários continuam disponíveis no código.
