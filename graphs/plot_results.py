"""
plot_results.py
Gera todos os gráficos do relatório a partir de results/results.csv
Executar: python3 graphs/plot_results.py
Requer:   pip install pandas matplotlib seaborn
"""

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ── Config ────────────────────────────────────────────────────────
sns.set_theme(style="whitegrid", palette="Set2")
plt.rcParams.update({"figure.dpi": 150, "font.size": 10})

OUTPUT_DIR  = "graphs/output"
os.makedirs(OUTPUT_DIR, exist_ok=True)

TREE_ORDER  = ["BST", "AVL", "RedBlack", "Splay"]
INPUT_ORDER = ["random", "sorted_asc", "sorted_desc", "nearly_sorted", "repeated"]
COLORS      = sns.color_palette("Set2", len(TREE_ORDER))
TREE_COLOR  = dict(zip(TREE_ORDER, COLORS))

# ── Carregar dados ────────────────────────────────────────────────
df = pd.read_csv("results/results.csv")
df["tree"]       = pd.Categorical(df["tree"],       categories=TREE_ORDER,  ordered=True)
df["input_type"] = pd.Categorical(df["input_type"], categories=INPUT_ORDER, ordered=True)

N_VALUES = sorted(df["n"].unique())

# ─────────────────────────────────────────────────────────────────
# Aux: gráfico de barras agrupadas para uma métrica, por input_type
# ─────────────────────────────────────────────────────────────────
def grouped_bar(ax, data, metric, title, ylabel, trees=None):
    if trees is None:
        trees = TREE_ORDER
    x = range(len(INPUT_ORDER))
    width = 0.8 / len(trees)
    for i, tree in enumerate(trees):
        subset = data[data["tree"] == tree].sort_values("input_type")
        offset = (i - len(trees) / 2 + 0.5) * width
        ax.bar([xi + offset for xi in x], subset[metric],
               width=width, label=tree, color=TREE_COLOR[tree])
    ax.set_xticks(list(x))
    ax.set_xticklabels(INPUT_ORDER, rotation=20, ha="right", fontsize=9)
    ax.set_ylabel(ylabel)
    ax.set_title(title, fontsize=11)
    ax.legend(title="Estrutura", fontsize=8)

# ─────────────────────────────────────────────────────────────────
# Gráfico 1 — Tempo Inserção (um subplot por N)
# ─────────────────────────────────────────────────────────────────
def plot_insert_time():
    fig, axes = plt.subplots(1, len(N_VALUES), figsize=(5 * len(N_VALUES), 5), sharey=False)
    if len(N_VALUES) == 1: axes = [axes]
    for ax, n in zip(axes, N_VALUES):
        grouped_bar(ax, df[df["n"] == n], "insert_ms_avg",
                    f"Inserção — N={n:,}", "Tempo médio (ms)")
    fig.suptitle("Fase 1 — Tempo de Inserção", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/1_insert_time.png")
    plt.close()
    print("Gerado: 1_insert_time.png")

# ─────────────────────────────────────────────────────────────────
# Gráfico 2 — Rotações Inserção (sem BST)
# ─────────────────────────────────────────────────────────────────
def plot_insert_rotations():
    trees = ["AVL", "RedBlack", "Splay"]
    fig, axes = plt.subplots(1, len(N_VALUES), figsize=(5 * len(N_VALUES), 5), sharey=False)
    if len(N_VALUES) == 1: axes = [axes]
    for ax, n in zip(axes, N_VALUES):
        grouped_bar(ax, df[df["n"] == n], "insert_rotations_avg",
                    f"Rotações Inserção — N={n:,}", "Rotações", trees=trees)
    fig.suptitle("Fase 1 — Rotações na Inserção (sem BST)", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/2_insert_rotations.png")
    plt.close()
    print("Gerado: 2_insert_rotations.png")

# ─────────────────────────────────────────────────────────────────
# Gráfico 3 — Tempo Query
# ─────────────────────────────────────────────────────────────────
def plot_query_time():
    fig, axes = plt.subplots(1, len(N_VALUES), figsize=(5 * len(N_VALUES), 5), sharey=False)
    if len(N_VALUES) == 1: axes = [axes]
    for ax, n in zip(axes, N_VALUES):
        grouped_bar(ax, df[df["n"] == n], "query_ms_avg",
                    f"Query — N={n:,}", "Tempo médio (ms)")
    fig.suptitle("Fase 2 — Tempo de Consulta por Intervalo", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/3_query_time.png")
    plt.close()
    print("Gerado: 3_query_time.png")

# ─────────────────────────────────────────────────────────────────
# Gráfico 4 — Tempo Remoção
# ─────────────────────────────────────────────────────────────────
def plot_remove_time():
    fig, axes = plt.subplots(1, len(N_VALUES), figsize=(5 * len(N_VALUES), 5), sharey=False)
    if len(N_VALUES) == 1: axes = [axes]
    for ax, n in zip(axes, N_VALUES):
        grouped_bar(ax, df[df["n"] == n], "remove_ms_avg",
                    f"Remoção — N={n:,}", "Tempo médio (ms)")
    fig.suptitle("Fase 3 — Tempo de Remoção", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/4_remove_time.png")
    plt.close()
    print("Gerado: 4_remove_time.png")

# ─────────────────────────────────────────────────────────────────
# Gráfico 5 — Rotações Remoção (sem BST)
# ─────────────────────────────────────────────────────────────────
def plot_remove_rotations():
    trees = ["AVL", "RedBlack", "Splay"]
    fig, axes = plt.subplots(1, len(N_VALUES), figsize=(5 * len(N_VALUES), 5), sharey=False)
    if len(N_VALUES) == 1: axes = [axes]
    for ax, n in zip(axes, N_VALUES):
        grouped_bar(ax, df[df["n"] == n], "remove_rotations_avg",
                    f"Rotações Remoção — N={n:,}", "Rotações", trees=trees)
    fig.suptitle("Fase 3 — Rotações na Remoção (sem BST)", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/5_remove_rotations.png")
    plt.close()
    print("Gerado: 5_remove_rotations.png")

# ─────────────────────────────────────────────────────────────────
# Gráfico 6 — Heatmap tempo total (para N máximo)
# ─────────────────────────────────────────────────────────────────
def plot_heatmap_total():
    n_max = max(N_VALUES)
    d = df[df["n"] == n_max].copy()
    d["total_ms"] = d["insert_ms_avg"] + d["query_ms_avg"] + d["remove_ms_avg"]
    pivot = d.pivot(index="tree", columns="input_type", values="total_ms")
    pivot = pivot.reindex(index=TREE_ORDER, columns=INPUT_ORDER)

    fig, ax = plt.subplots(figsize=(10, 4))
    sns.heatmap(pivot, annot=True, fmt=".0f", cmap="YlOrRd",
                linewidths=0.5, ax=ax, cbar_kws={"label": "ms"})
    ax.set_title(f"Tempo Total (Insert+Query+Remove) em ms — N={n_max:,}")
    ax.set_xlabel("Tipo de Input")
    ax.set_ylabel("Estrutura")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/6_heatmap_total.png")
    plt.close()
    print("Gerado: 6_heatmap_total.png")

# ─────────────────────────────────────────────────────────────────
# Gráfico 7 — AVL vs RedBlack rotações (linha por N)
# ─────────────────────────────────────────────────────────────────
def plot_avl_vs_rb():
    fig, axes = plt.subplots(1, 2, figsize=(12, 5))
    for ax, metric, title in zip(axes,
        ["insert_rotations_avg", "remove_rotations_avg"],
        ["Inserção", "Remoção"]):
        for tree in ["AVL", "RedBlack"]:
            for n in N_VALUES:
                subset = df[(df["tree"] == tree) & (df["n"] == n)].sort_values("input_type")
                ax.plot(INPUT_ORDER, subset[metric].values,
                        marker="o", label=f"{tree} N={n:,}",
                        color=TREE_COLOR[tree],
                        linestyle="--" if n != N_VALUES[0] else "-")
        ax.set_title(f"Rotações — {title}")
        ax.set_xticklabels(INPUT_ORDER, rotation=20, ha="right", fontsize=9)
        ax.set_ylabel("Rotações")
        ax.legend(fontsize=7)
    fig.suptitle("AVL vs Red-Black: Rotações", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/7_avl_vs_rb_rotations.png")
    plt.close()
    print("Gerado: 7_avl_vs_rb_rotations.png")

# ─────────────────────────────────────────────────────────────────
# Gráfico 8 — Escalabilidade: tempo vs N (por estrutura, input random)
# ─────────────────────────────────────────────────────────────────
def plot_scalability():
    d = df[df["input_type"] == "random"]
    fig, axes = plt.subplots(1, 3, figsize=(14, 5))
    for ax, metric, title in zip(axes,
        ["insert_ms_avg", "query_ms_avg", "remove_ms_avg"],
        ["Inserção", "Query", "Remoção"]):
        for tree in TREE_ORDER:
            subset = d[d["tree"] == tree].sort_values("n")
            ax.plot(subset["n"], subset[metric],
                    marker="o", label=tree, color=TREE_COLOR[tree])
        ax.set_xlabel("N (transações)")
        ax.set_ylabel("Tempo médio (ms)")
        ax.set_title(title)
        ax.legend(fontsize=8)
    fig.suptitle("Escalabilidade — Input Random", fontsize=13, fontweight="bold")
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/8_scalability.png")
    plt.close()
    print("Gerado: 8_scalability.png")

# ── Executar todos ────────────────────────────────────────────────
if __name__ == "__main__":
    print("A gerar gráficos...\n")
    plot_insert_time()
    plot_insert_rotations()
    plot_query_time()
    plot_remove_time()
    plot_remove_rotations()
    plot_heatmap_total()
    plot_avl_vs_rb()
    plot_scalability()
    print(f"\nTodos os gráficos em {OUTPUT_DIR}/")
