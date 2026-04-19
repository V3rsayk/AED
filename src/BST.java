import java.util.ArrayList;
import java.util.List;

public class BST implements SearchTree {

    // ── Nó interno ────────────────────────────────────────────────
    protected static class Node {
        Transaction transaction;
        Node left, right;

        Node(Transaction t) {
            this.transaction = t;
        }
    }

    // ── Estado ────────────────────────────────────────────────────
    protected Node root;
    private int size;
    private long rotations; // BST não roda, mas mantém contador para interface uniforme

    // ── Insert ────────────────────────────────────────────────────
    @Override
    public void insert(Transaction t) {
        root = insertRec(root, t);
        size++;
    }

    private Node insertRec(Node node, Transaction t) {
        if (node == null) return new Node(t);
        if (t.getValue() < node.transaction.getValue())
            node.left  = insertRec(node.left,  t);
        else
            node.right = insertRec(node.right, t);
        return node;
    }

    // ── Range Query ───────────────────────────────────────────────
    // Percorre apenas os ramos relevantes usando os limites do intervalo.
    // k = número de resultados; custo médio O(log n + k)
    @Override
    public List<Integer> rangeQuery(double v1, double v2, int riskMin) {
        List<Integer> result = new ArrayList<>();
        rangeRec(root, v1, v2, riskMin, result);
        return result;
    }

    private void rangeRec(Node node, double v1, double v2, int riskMin, List<Integer> result) {
        if (node == null) return;

        double val = node.transaction.getValue();

        // Só desce à esquerda se puder haver valores >= v1 lá
        if (val > v1)
            rangeRec(node.left, v1, v2, riskMin, result);

        // Visita este nó se estiver no intervalo e tiver risco suficiente
        if (val >= v1 && val <= v2 && node.transaction.getRisk() >= riskMin)
            result.add(node.transaction.getId());

        // Só desce à direita se puder haver valores <= v2 lá
        if (val < v2)
            rangeRec(node.right, v1, v2, riskMin, result);
    }

    // ── Remove by Value ───────────────────────────────────────────
    // Remove TODOS os nós com o valor dado (podem existir duplicados)
    @Override
    public void removeByValue(double value) {
        int[] removed = {0};
        root = removeAllRec(root, value, removed);
        size -= removed[0];
    }

    private Node removeAllRec(Node node, double value, int[] removed) {
        if (node == null) return null;

        if (value < node.transaction.getValue()) {
            node.left  = removeAllRec(node.left,  value, removed);
        } else if (value > node.transaction.getValue()) {
            node.right = removeAllRec(node.right, value, removed);
        } else {
            // Valor encontrado — pode haver mais à esquerda E à direita
            // (duplicados vão sempre para a direita na inserção)
            // Primeiro elimina recursivamente duplicados à direita
            node.right = removeAllRec(node.right, value, removed);

            // Agora remove este nó
            removed[0]++;
            if (node.left == null)  return node.right;
            if (node.right == null) return node.left;

            // Nó com dois filhos: substitui pelo menor da subárvore direita
            Node successor = findMin(node.right);
            node.transaction = successor.transaction;
            node.right = deleteMin(node.right);
        }
        return node;
    }

    // Encontra o nó com menor valor numa subárvore
    protected Node findMin(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }

    // Remove o nó mínimo de uma subárvore
    private Node deleteMin(Node node) {
        if (node.left == null) return node.right;
        node.left = deleteMin(node.left);
        return node;
    }

    // ── Métricas ──────────────────────────────────────────────────
    @Override
    public long getRotationCount() { return rotations; }

    @Override
    public void resetMetrics() { rotations = 0; }

    @Override
    public int size() { return size; }
}
