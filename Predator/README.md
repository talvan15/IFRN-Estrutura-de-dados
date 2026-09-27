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

- **A** é o predador (marca quadrada); **B** é a presa (marca circular). Arraste as marcas para mudar início e alvo.
- Clique para criar/remover paredes; arraste para pintar ou apagar várias. O botão direito também apaga.
- Selecione **A***, **Gulosa** ou **Comparar**, depois **Executar**. Ciano identifica A*; lilás identifica Gulosa. Células suaves mostram a exploração e a linha destaca a rota completa.
- **Comparar** executa as duas buscas no mesmo mapa, início, alvo e regras. Editar qualquer grid atualiza o outro. As métricas ficam alinhadas sob cada grid; o menor valor recebe a cor do algoritmo (empates não são destacados).
- **Pausar/Continuar** controla a reprodução. **Reiniciar** cancela a busca e limpa os resultados, preservando as paredes e A/B.
- **Opções** reúne velocidade da animação, três cenários e **Limpar paredes**, que preserva A/B.
- Atalhos: espaço executa/pausa, R reinicia, 1 seleciona A*, 2 seleciona Gulosa.

O cenário inicial, **Armadilha em U**, demonstra a diferença: A* encontra custo 304 e Gulosa 326. A interface usa um único alvo, oito direções, custo ortogonal 10 e diagonal 14, sem corte de cantos. A heurística octile é admissível e consistente nessas regras. A* encontra o caminho de menor custo; Gulosa prioriza a estimativa até o alvo, sem garantir menor custo.

## Métricas

São exibidos somente **Tempo**, **Nós explorados** e **Custo**. Durante a animação, tempo e contagem correspondem às expansões já reproduzidas. Ao terminar, o tempo corresponde ao cálculo total informado pelo algoritmo, incluindo a reconstrução da rota.

A medição existente usa `System.nanoTime()` e exclui callbacks, espera na fila de eventos e animação. É tempo decorrido de cálculo, não tempo de CPU puro; aquecimento da JVM e escalonamento podem afetar comparações. Custo aparece somente com uma rota completa; uma busca impossível mostra **Sem caminho**.

As buscas permanecem fora da EDT, em `SwingWorker`, com filas limitadas a 8.192 eventos. Um timer reproduz os eventos com orçamento por ciclo para manter a interface responsiva. Pausar afeta a reprodução, e o cálculo pode continuar em segundo plano até o limite da fila. Reiniciar cancela os workers e descarta eventos anteriores.

## Estrutura

- `model`, `algorithm`, `heuristic`: modelo e algoritmos originais preservados.
- `ui`: barra de controles, grids responsivos desenhados em Java2D e faixa de métricas.
- `util`: utilitários originais de mapas e gravação preservados.
- `src/test`: testes existentes de busca, heurística, cancelamento e persistência.

Histórico, CSV, gráficos, mapa de calor, G/H/F, setas, múltiplos alvos e geração por densidade não fazem parte da interface principal. As capacidades existentes do modelo e utilitários continuam disponíveis no código.
