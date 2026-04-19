import java.util.ArrayList;
import java.util.List;

public class SplayTree implements SearchTree {

    // ── Nó interno ────────────────────────────────────────────────
    private static class Node {
        Transaction transaction;
        Node left, right, parent;

        Node(Transaction t) {
            this.transaction = t;
        }
    }

    // ── Estado ────────────────────────────────────────────────────
    private Node root;
    private int size;
    private long rotations;

    // ── Rotações base ─────────────────────────────────────────────
    private void rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        if (x.right != null) x.right.parent = y;
        x.parent = y.parent;
        if (y.parent == null)          root = x;
        else if (y == y.parent.left)   y.parent.left  = x;
        else                            y.parent.right = x;
        x.right  = y;
        y.parent = x;
        rotations++;
    }

    private void rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (y.left != null) y.left.parent = x;
        y.parent = x.parent;
        if (x.parent == null)          root = y;
        else if (x == x.parent.left)   x.parent.left  = y;
        else                            x.parent.right = y;
        y.left   = x;
        x.parent = y;
        rotations++;
    }

    // ── Splay ─────────────────────────────────────────────────────
    // Move o nó x até à raiz usando rotações zig, zig-zig e zig-zag.
    //
    //  Zig       (pai é raiz):           rotação simples
    //  Zig-Zig   (x e pai mesmo lado):   roda pai primeiro, depois x
    //  Zig-Zag   (x e pai lados opostos):roda x duas vezes
    //
    private void splay(Node x) {
        while (x.parent != null) {
            Node p = x.parent;
            Node g = p.parent;

            if (g == null) {
                // Zig: pai é a raiz
                if (x == p.left) rotateRight(p);
                else             rotateLeft(p);
            } else if (x == p.left && p == g.left) {
                // Zig-Zig esquerda
                rotateRight(g);
                rotateRight(p);
            } else if (x == p.right && p == g.right) {
                // Zig-Zig direita
                rotateLeft(g);
                rotateLeft(p);
            } else if (x == p.right && p == g.left) {
                // Zig-Zag esquerda-direita
                rotateLeft(p);
                rotateRight(g);
            } else {
                // Zig-Zag direita-esquerda
                rotateRight(p);
                rotateLeft(g);
            }
        }
    }

    // ── Insert ────────────────────────────────────────────────────
    // Insere como BST normal e faz splay do novo nó até à raiz.
    @Override
    public void insert(Transaction t) {
        Node z = new Node(t);
        Node y = null;
        Node x = root;

        while (x != null) {
            y = x;
            if (t.getValue() < x.transaction.getValue()) x = x.left;
            else                                          x = x.right;
        }
        z.parent = y;
        if (y == null)                                    root = z;
        else if (t.getValue() < y.transaction.getValue()) y.left  = z;
        else                                               y.right = z;

        splay(z); // novo nó sobe à raiz
        size++;
    }

    // ── Range Query ───────────────────────────────────────────────
    // Faz splay do limite inferior para aproveitar localidade,
    // depois percorre a subárvore direita dentro do intervalo.
    @Override
    public List<Integer> rangeQuery(double v1, double v2, int riskMin) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        // Splay do valor mais próximo de v1 para a raiz
        splayClosest(v1);
        rangeRec(root, v1, v2, riskMin, result);
        return result;
    }

    // Faz splay do nó com valor mais próximo de target
    private void splayClosest(double target) {
        Node x = root, last = null;
        while (x != null) {
            last = x;
            if (target < x.transaction.getValue())      x = x.left;
            else if (target > x.transaction.getValue()) x = x.right;
            else break;
        }
        if (last != null) splay(last);
    }

    private void rangeRec(Node node, double v1, double v2, int riskMin, List<Integer> result) {
        if (node == null) return;
        double val = node.transaction.getValue();
        if (val > v1) rangeRec(node.left,  v1, v2, riskMin, result);
        if (val >= v1 && val <= v2 && node.transaction.getRisk() >= riskMin)
            result.add(node.transaction.getId());
        if (val < v2) rangeRec(node.right, v1, v2, riskMin, result);
    }

    // ── Remove by Value ───────────────────────────────────────────
    // Faz splay de um nó com o valor alvo para a raiz, depois une
    // as subárvores esquerda e direita. Repete para duplicados.
    @Override
    public void removeByValue(double value) {
        while (true) {
            Node target = findNode(root, value);
            if (target == null) break;
            splay(target);
            // Junta subárvore esquerda e direita
            if (root.left == null) {
                root = root.right;
                if (root != null) root.parent = null;
            } else if (root.right == null) {
                root = root.left;
                root.parent = null;
            } else {
                Node leftTree  = root.left;
                Node rightTree = root.right;
                leftTree.parent  = null;
                rightTree.parent = null;
                // Faz splay do máximo da subárvore esquerda
                root = leftTree;
                splayMax(root);
                root.right = rightTree;
                rightTree.parent = root;
            }
            size--;
        }
    }

    private Node findNode(Node node, double value) {
        if (node == null) return null;
        if (value < node.transaction.getValue()) return findNode(node.left,  value);
        if (value > node.transaction.getValue()) return findNode(node.right, value);
        return node;
    }

    // Faz splay do nó máximo (mais à direita) para a raiz
    private void splayMax(Node node) {
        while (node.right != null) node = node.right;
        splay(node);
    }

    // ── Métricas ──────────────────────────────────────────────────
    @Override
    public long getRotationCount() { return rotations; }

    @Override
    public void resetMetrics() { rotations = 0; }

    @Override
    public int size() { return size; }
}
