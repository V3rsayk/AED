import java.util.ArrayList;
import java.util.List;

public class AVL implements SearchTree {

    // ── Nó interno ────────────────────────────────────────────────
    private static class Node {
        Transaction transaction;
        Node left, right;
        int height;

        Node(Transaction t) {
            this.transaction = t;
            this.height = 1;
        }
    }

    // ── Estado ────────────────────────────────────────────────────
    private Node root;
    private int size;
    private long rotations;

    // ── Utilitários de altura e balanço ──────────────────────────
    private int height(Node n) {
        return n == null ? 0 : n.height;
    }

    private void updateHeight(Node n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    // Fator de balanço: positivo = pesado à esquerda, negativo = pesado à direita
    private int balance(Node n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    // ── Rotações ──────────────────────────────────────────────────
    //
    //   Rotação simples à direita (Right Rotation):
    //
    //       y                x
    //      / \              / \
    //     x   T3   →      T1   y
    //    / \                  / \
    //   T1  T2               T2  T3
    //
    private Node rotateRight(Node y) {
        Node x  = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left  = T2;

        updateHeight(y);
        updateHeight(x);
        rotations++;
        return x;
    }

    //   Rotação simples à esquerda (Left Rotation):
    //
    //     x                  y
    //    / \                / \
    //   T1   y     →       x   T3
    //       / \           / \
    //      T2  T3        T1  T2
    //
    private Node rotateLeft(Node x) {
        Node y  = x.right;
        Node T2 = y.left;

        y.left  = x;
        x.right = T2;

        updateHeight(x);
        updateHeight(y);
        rotations++;
        return y;
    }

    // ── Reequilíbrio ──────────────────────────────────────────────
    // Aplica a rotação certa consoante o caso de desequilíbrio
    private Node rebalance(Node n) {
        updateHeight(n);
        int b = balance(n);

        // Caso Left-Left → rotação simples à direita
        if (b > 1 && balance(n.left) >= 0)
            return rotateRight(n);

        // Caso Left-Right → rotação dupla: esquerda depois direita
        if (b > 1 && balance(n.left) < 0) {
            n.left = rotateLeft(n.left);
            return rotateRight(n);
        }

        // Caso Right-Right → rotação simples à esquerda
        if (b < -1 && balance(n.right) <= 0)
            return rotateLeft(n);

        // Caso Right-Left → rotação dupla: direita depois esquerda
        if (b < -1 && balance(n.right) > 0) {
            n.right = rotateRight(n.right);
            return rotateLeft(n);
        }

        return n; // já equilibrado
    }

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

        return rebalance(node);
    }

    // ── Range Query ───────────────────────────────────────────────
    @Override
    public List<Integer> rangeQuery(double v1, double v2, int riskMin) {
        List<Integer> result = new ArrayList<>();
        rangeRec(root, v1, v2, riskMin, result);
        return result;
    }

    private void rangeRec(Node node, double v1, double v2, int riskMin, List<Integer> result) {
        if (node == null) return;

        double val = node.transaction.getValue();

        if (val > v1)
            rangeRec(node.left, v1, v2, riskMin, result);

        if (val >= v1 && val <= v2 && node.transaction.getRisk() >= riskMin)
            result.add(node.transaction.getId());

        if (val < v2)
            rangeRec(node.right, v1, v2, riskMin, result);
    }

    // ── Remove by Value ───────────────────────────────────────────
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
            // Remove duplicados à direita primeiro
            node.right = removeAllRec(node.right, value, removed);
            removed[0]++;

            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            // Substitui pelo sucessor in-order
            Node successor = findMin(node.right);
            node.transaction = successor.transaction;
            node.right = deleteMin(node.right);
        }

        return rebalance(node); // reequilibra após remoção
    }

    private Node findMin(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }

    private Node deleteMin(Node node) {
        if (node.left == null) return node.right;
        node.left = deleteMin(node.left);
        return rebalance(node);
    }

    // ── Métricas ──────────────────────────────────────────────────
    @Override
    public long getRotationCount() { return rotations; }

    @Override
    public void resetMetrics() { rotations = 0; }

    @Override
    public int size() { return size; }
}
