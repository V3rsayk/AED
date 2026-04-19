# Projeto #2 — Algoritmos e Estruturas de Dados
## Universidade de Coimbra — FCTUC 2025/2026

---

## Estrutura do projeto

```
projeto2/
├── src/
│   ├── Transaction.java       # Classe base
│   ├── SearchTree.java        # Interface comum
│   ├── BST.java               # Árvore Binária de Pesquisa
│   ├── AVL.java               # Árvore AVL
│   ├── RedBlackTree.java      # Árvore Vermelho-Preta
│   ├── SplayTree.java         # Árvore Splay
│   ├── DataGenerator.java     # Geração de inputs
│   └── Main.java              # Benchmark
├── graphs/
│   └── plot_results.py        # Gráficos Python
├── data/                      # CSV de input (gerado)
├── results/                   # CSV de resultados (gerado)
└── run.sh                     # Script completo
```

---

## Requisitos

- Java 11+
- Python 3.8+ com: `pip install pandas matplotlib seaborn`

---

## Como correr

### Tudo de uma vez:
```bash
bash run.sh
```

### Passo a passo:
```bash
# Compilar
javac src/*.java -d out/

# Gerar dados
java -cp out DataGenerator

# Benchmark
java -Xmx512m -cp out Main

# Gráficos
python3 graphs/plot_results.py
```

---

## Tipos de input e justificação

| Tipo           | Caso difícil para       | Razão                                      |
|----------------|-------------------------|--------------------------------------------|
| random         | —                       | Caso médio, linha de base                  |
| sorted_asc     | BST                     | Degenera em lista ligada, altura = n       |
| sorted_desc    | BST                     | Igual ao anterior                          |
| nearly_sorted  | AVL, Red-Black          | Muitas rotações para manter equilíbrio     |
| repeated       | Todas (remoção)         | Muitos duplicados tornam remoção cara      |

---

## Resultados gerados

- `results/results.csv` — tempos e rotações por estrutura × input
- `graphs/output/*.png` — 8 gráficos para o relatório

---

## Estruturas implementadas

| Estrutura    | Altura      | Rotações inserção | Rotações remoção | Ideal para         |
|--------------|-------------|-------------------|------------------|--------------------|
| BST          | O(n) pior   | 0                 | 0                | —                  |
| AVL          | ~1.44 log n | ≤ 2               | O(log n)         | Muitas queries     |
| Red-Black    | ~2 log n    | ≤ 2               | ≤ 3              | Muitas inserções   |
| Splay        | O(n) pior   | amortizado        | amortizado       | Acessos repetidos  |
