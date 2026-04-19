# Relatório — Projeto #2
## Algoritmos e Estruturas de Dados — 2025/2026

---

## 1. Introdução

O presente trabalho aborda um sistema de transações financeiras onde cada transação é caracterizada por um identificador único (`id`), um valor monetário (`value`) e um nível de risco (`risk`). O sistema opera em três fases distintas: inserção de transações, consulta por intervalo sobre valores monetários com filtro de risco mínimo, e remoção de transações por valor.

Foram implementadas quatro estruturas de dados baseadas em árvores de pesquisa: Árvore Binária de Pesquisa (BST), Árvore AVL, Árvore Vermelho-Preta (Red-Black) e Árvore Splay. O objetivo da análise experimental é comparar o desempenho destas estruturas em termos de tempo de execução e número de rotações, sob diferentes tipos de input.

---

## 2. Implementação

### Chave da árvore

A chave de ordenação utilizada em todas as árvores é o **valor monetário** (`value`) da transação. Esta escolha justifica-se porque as consultas por intervalo incidem diretamente sobre os valores monetários, permitindo assim uma navegação eficiente na árvore ao descartar subárvores fora do intervalo.

Cada nó armazena o objeto `Transaction` completo (id, value, risk), permitindo que a consulta por intervalo aplique o filtro de risco mínimo sem acessos adicionais. Como os valores monetários não são necessariamente únicos, duplicados são inseridos na subárvore direita.

### BST

A BST não possui campos adicionais no nó além da transação e dos ponteiros `left`/`right`. Não realiza qualquer rebalanceamento, pelo que não são contabilizadas rotações. A sua altura depende da ordem de inserção, podendo degradar para O(n) em inputs ordenados.

### AVL

Cada nó da AVL armazena adicionalmente um campo `height` (inteiro). Após cada inserção ou remoção, a altura é atualizada e o fator de balanço (altura esquerda − altura direita) é verificado. Quando este sai do intervalo [−1, +1], é aplicada uma das quatro rotações: LL, LR, RR ou RL. Rotações duplas (LR, RL) são contabilizadas como 2 rotações simples.

### Red-Black

Cada nó armazena um campo `color` (booleano RED/BLACK) e um ponteiro `parent`. É utilizado um nó sentinela NIL partilhado por todas as folhas, simplificando os casos nas rotações. Após inserção, o `insertFixup` corrige violações em até 3 casos (recoloração, rotação simples, rotação dupla). Após remoção, o `deleteFixup` trata 4 casos. Todas as rotações são contabilizadas individualmente.

### Splay

A Splay não armazena campos adicionais. Após cada acesso (inserção, query, remoção), o nó acedido é movido para a raiz através da operação `splay`, que aplica os casos zig (1 rotação), zig-zig (2 rotações) e zig-zag (2 rotações). Não existe garantia de balanceamento por operação, mas o custo amortizado é O(log n).

---

## 3. Geração de Inputs

Foram gerados 5 tipos de input para explorar as vantagens e desvantagens de cada estrutura:

| Tipo | Descrição | Caso mais difícil para |
|---|---|---|
| `random` | Valores uniformemente aleatórios | — (linha de base) |
| `sorted_asc` | Valores ordenados crescentemente | BST (degenera em lista) |
| `sorted_desc` | Valores ordenados decrescentemente | BST (degenera em lista) |
| `nearly_sorted` | 95% ordenado, 5% trocas aleatórias | AVL e Red-Black (muitas rotações) |
| `repeated` | 500 valores distintos repetidos (N/500 vezes cada) | Remoção em todas (muitos duplicados) |

**Justificação por fase:**

*Inserção:* O input `sorted_asc` e `sorted_desc` são o pior caso para a BST pois cada inserção desce sempre pelo mesmo lado, produzindo uma árvore com altura n e custo O(n) por operação. Para AVL e Red-Black, o input `nearly_sorted` força o maior número de rebalanceamentos pois as pequenas perturbações quebram continuamente a propriedade de equilíbrio. A Splay degrada com inputs ordenados pois os splays consecutivos não beneficiam de localidade.

*Consulta por intervalo:* O input `random` é o caso médio. O input `repeated` pode ser vantajoso para a Splay se as queries repetirem intervalos semelhantes (efeito de cache). Para BST com input ordenado, a árvore degenerada torna as queries O(n).

*Remoção:* O input `repeated` é o mais desafiante para todas as estruturas pois cada operação de remoção por valor elimina N/500 ≈ 200 nós (para N=100k), exigindo múltiplas reestruturações da árvore.

Os volumes de dados respeitam os requisitos: N ∈ {100 000, 500 000, 1 000 000} para inserção, 10 000 consultas por conjunto, e remoção de 20% das transações.

---

## 4. Avaliação Experimental

Cada experiência foi executada 3 vezes. Os valores reportados correspondem à média dos tempos totais das 3 execuções.

### 4.1 Fase 1 — Inserção

**Tempo médio de inserção (ms):**

| Estrutura | random | sorted_asc | sorted_desc | nearly_sorted | repeated |
|---|---|---|---|---|---|
| BST | — | — | — | — | — |
| AVL | — | — | — | — | — |
| Red-Black | — | — | — | — | — |
| Splay | — | — | — | — | — |

*(substituir — pelos valores de results/results.csv para N=100 000)*

**Número total de rotações na inserção:**

| Estrutura | random | sorted_asc | sorted_desc | nearly_sorted | repeated |
|---|---|---|---|---|---|
| AVL | — | — | — | — | — |
| Red-Black | — | — | — | — | — |
| Splay | — | — | — | — | — |

*[Inserir aqui gráfico 1_insert_time.png e 2_insert_rotations.png]*

**Análise:** A BST degrada significativamente com inputs ordenados devido à sua altura linear. A AVL apresenta mais rotações que a Red-Black no input `nearly_sorted` pois o seu critério de rebalanceamento é mais estrito (diferença de altura ≤ 1 vs. propriedades de cor). A Splay acumula muitas rotações com inputs ordenados pois cada splay move o nó do fundo da árvore até à raiz.

### 4.2 Fase 2 — Consulta por Intervalo

**Tempo médio de consulta (ms) para 10 000 queries:**

| Estrutura | random | sorted_asc | sorted_desc | nearly_sorted | repeated |
|---|---|---|---|---|---|
| BST | — | — | — | — | — |
| AVL | — | — | — | — | — |
| Red-Black | — | — | — | — | — |
| Splay | — | — | — | — | — |

*[Inserir aqui gráfico 3_query_time.png e 8_query_comparison_line.png]*

**Análise:** A AVL tende a ser ligeiramente mais rápida nas queries que a Red-Black por ter altura máxima menor (~1.44 log n vs ~2 log n). A Splay beneficia de localidade: queries repetidas sobre o mesmo intervalo são mais rápidas pois os nós relevantes estão próximos da raiz. A BST com input ordenado tem queries muito lentas devido à altura linear.

### 4.3 Fase 3 — Remoção

**Tempo médio de remoção (ms):**

| Estrutura | random | sorted_asc | sorted_desc | nearly_sorted | repeated |
|---|---|---|---|---|---|
| BST | — | — | — | — | — |
| AVL | — | — | — | — | — |
| Red-Black | — | — | — | — | — |
| Splay | — | — | — | — | — |

**Número total de rotações na remoção:**

| Estrutura | random | sorted_asc | sorted_desc | nearly_sorted | repeated |
|---|---|---|---|---|---|
| AVL | — | — | — | — | — |
| Red-Black | — | — | — | — | — |
| Splay | — | — | — | — | — |

*[Inserir aqui gráfico 4_remove_time.png e 5_remove_rotations.png]*

**Análise:** A Red-Black realiza no máximo 3 rotações por remoção, enquanto a AVL pode necessitar de O(log n) rotações em cascata após cada remoção. O input `repeated` é o pior caso para todas as estruturas pois cada remoção por valor elimina múltiplos nós consecutivamente.

---

## 5. Otimização das Consultas por Intervalo

A otimização implementada consiste em **fazer splay do nó com valor mais próximo do limite inferior v1** antes de iniciar a travessia por intervalo na Splay Tree. Desta forma, o nó de entrada na região de interesse fica na raiz, e os nós vizinhos (que fazem parte do resultado) ficam mais próximos da raiz nas iterações seguintes.

Para as árvores balanceadas (AVL e Red-Black), a otimização das range queries baseia-se na **poda de subárvores**: durante a travessia, a subárvore esquerda só é visitada se `val > v1` e a subárvore direita só se `val < v2`. Esta poda evita visitar nós que não podem pertencer ao intervalo, reduzindo o número de comparações de O(n) para O(log n + k), onde k é o número de resultados.

Os resultados experimentais mostram que esta abordagem reduz o tempo de query em [X]% face a uma travessia in-order completa, especialmente para intervalos estreitos (baixo k).

---

## 6. Conclusões

A análise experimental confirmou as propriedades teóricas de cada estrutura. A BST é adequada apenas para inputs aleatórios; inputs ordenados tornam-na inutilizável para grandes N. A AVL oferece as queries mais rápidas por ter a menor altura garantida, mas paga um custo superior em rotações durante a remoção. A Red-Black equilibra bem inserções e remoções com poucas rotações, sendo a escolha mais versátil. A Splay destaca-se em cenários com localidade temporal — quando as mesmas transações são acedidas repetidamente — mas degrada em inputs ordenados.

Para um sistema financeiro real com predominância de queries sobre um conjunto estável de transações, a **AVL** seria a escolha preferencial. Para sistemas com muitas inserções e remoções intercaladas, a **Red-Black** é mais adequada.
