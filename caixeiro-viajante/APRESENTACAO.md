# Roteiro de apresentação — RotaMinima

Duração sugerida: 5 a 7 minutos. Abra com `java -jar target/rotaminima-1.0.0.jar`.

## 1. Modelagem do grafo

“Cada cidade tem identificador estável, nome e coordenadas. O grafo é simples, ponderado e não direcionado. A matriz é simétrica, com zero na diagonal e infinito onde não há aresta. Peso zero é permitido. A posição no desenho não determina os custos.”

Adicione uma cidade, conecte duas com um peso e mostre o modo de remoção. Explique que Limpar reinicia tudo e que não há remoção individual de cidades.

## 2. Distâncias, predecessores e relaxamento

“Distâncias começam em infinito, exceto a origem, que recebe zero. Predecessores começam em -1. Relaxar significa verificar se chegar por uma cidade já alcançada melhora o custo conhecido. Só uma melhora estrita atualiza o predecessor.”

Explique os três vetores e os snapshots independentes. Empates da seleção são resolvidos pelo índice.

## 3. Dijkstra entre duas cidades

Carregue **Dijkstra — caminho indireto**, selecione A e D e calcule. Navegue até a última etapa.

“O resultado é A → B → C → D, custo 6. A aresta A-C continua mostrando peso original 10. O desenho não substitui os pesos pela distância mínima. A seleção linear com matriz custa O(V²).”

## 4. Matriz de menores distâncias

“Para o modo de visitas, executo Dijkstra a partir de todas as cidades, sem snapshots. Isso custa O(V³) e produz distâncias mínimas de todos os pares e os predecessores para reconstruir cada trecho.”

A matriz é interna ao algoritmo; a interface apresenta o resultado e a expansão. A interface não implementa os algoritmos.

## 5. Força bruta para escolher a ordem

“Dijkstra sozinho não escolhe a ordem de visita. Fixo a origem e uso backtracking para avaliar todas as (n-1)! ordens, incluindo as inversas e o retorno. Não guardo as permutações: só a melhor ordem e seu custo. Cada ordem leva O(n), total O(n × (n-1)!).”

“A ordem planejada contém cada cidade uma vez, mas os caminhos entre elas podem atravessar outras cidades e repetir vértices ou arestas.”

Mostre a separação entre ordem e trajeto. Não chame o resultado de ciclo hamiltoniano garantido no grafo original.

## 6. Demonstração dos exemplos

- **Quatro cidades:** calcule a rota a partir de A. Há seis ordens. **Atenção ao erro numérico do enunciado:** com os pesos fornecidos, o percurso mínimo com repetições custa **10**, pela expansão A → B → C → D → B → A. O valor 11 é o melhor ciclo direto sem repetir vértices. Os pesos foram preservados.
- **Repetição no trajeto:** a ordem é A → B → C → A; a expansão é A → B → C → B → A, custo 4. B precisa ser atravessada novamente para voltar. Aqui nem existe ciclo hamiltoniano no grafo original.
- **Grafo desconectado:** de A a D o destino é inacessível; não existe percurso fechado que visite todas as cidades.
- Se houver tempo, calcule de A para A: caminho de uma cidade e custo zero. Atualize uma aresta para zero e mostre que isso não significa ausência de ligação.

## 7. Limitações e conclusão da demonstração

“São até 20 cidades na edição e 3 a 9 para força bruta. Nove cidades geram 40.320 ordens. Não há persistência, arraste ou remoção individual. Uso double com validação. O cálculo ocorre em SwingWorker sobre cópia do grafo, mantendo a interface responsiva. O tempo inclui o algoritmo e a reconstrução, mas não o desenho; uma execução não é benchmark.”

Para demonstrar a validação automatizada: `mvn clean verify`. Mostre a separação entre modelo, algoritmos, serviço e interface.
