import java.util.ArrayList;
import java.util.List;

public class RedBlackTree implements SearchTree {

    private static final boolean RED   = true;
    private static final boolean BLACK = false;

    // ── Nó interno ────────────────────────────────────────────────
    private static class Node {
        Transaction transaction;
        Node left, right, parent;
        boolean color;

        Node(Transaction t, boolean color, Node parent) {
            this.transaction = t;
            this.color  = color;
            this.parent = parent;
        }
    }

    // ── NIL sentinel ──────────────────────────────────────────────
    // Um único nó NIL preto partilhado por todas as folhas.
    // Simplifica os casos nulos nas rotações e recoloração.
    private final Node NIL;
    private Node root;
    private int size;
    private long rotations;

    public RedBlackTree() {
        NIL  = new Node(null, BLACK, null);
        root = NIL;
    }

    // ── Utilitários ───────────────────────────────────────────────
    private boolean isRed(Node n)   { return n != NIL && n.color == RED; }
    private boolean isBlack(Node n) { return n == NIL || n.color == BLACK; }

    // ── Rotações ──────────────────────────────────────────────────
    //
    //   Left Rotation:          Right Rotation:
    //
    //     x                          y
    //    / \          →             / \
    //   T1   y                     x   T3
    //       / \                   / \
    //      T2  T3                T1  T2
    //
    private void rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (y.left != NIL) y.left.parent = x;

        y.parent = x.parent;
        if (x.parent == null)       root = y;
        else if (x == x.parent.left) x.parent.left  = y;
        else                          x.parent.right = y;

        y.left   = x;
        x.parent = y;
        rotations++;
    }

    private void rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        if (x.right != NIL) x.right.parent = y;

        x.parent = y.parent;
        if (y.parent == null)       root = x;
        else if (y == y.parent.left) y.parent.left  = x;
        else                          y.parent.right = x;

        x.right  = y;
        y.parent = x;
        rotations++;
    }

    // ── Insert ────────────────────────────────────────────────────
    @Override
    public void insert(Transaction t) {
        Node z = new Node(t, RED, null);
        z.left  = NIL;
        z.right = NIL;

        // Inserção BST normal
        Node y = null;
        Node x = root;
        while (x != NIL) {
            y = x;
            if (t.getValue() < x.transaction.getValue()) x = x.left;
            else                                          x = x.right;
        }
        z.parent = y;
        if (y == null)                              root = z;
        else if (t.getValue() < y.transaction.getValue()) y.left  = z;
        else                                               y.right = z;

        size++;
        insertFixup(z); // corrige violações das propriedades RB
    }

    // Corrige as propriedades Red-Black após inserção.
    // Existem 3 casos principais (e os seus simétricos):
    private void insertFixup(Node z) {
        while (isRed(z.parent)) {
            if (z.parent == z.parent.parent.left) {
                Node uncle = z.parent.parent.right;

                if (isRed(uncle)) {
                    // Caso 1: tio é vermelho → recoloração
                    z.parent.color         = BLACK;
                    uncle.color            = BLACK;
                    z.parent.parent.color  = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        // Caso 2: z é filho direito → rotação esquerda
                        z = z.parent;
                        rotateLeft(z);
                    }
                    // Caso 3: z é filho esquerdo → rotação direita
                    z.parent.color        = BLACK;
                    z.parent.parent.color = RED;
                    rotateRight(z.parent.parent);
                }
            } else {
                // Simétrico: pai é filho direito do avô
                Node uncle = z.parent.parent.left;

                if (isRed(uncle)) {
                    z.parent.color        = BLACK;
                    uncle.color           = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rotateRight(z);
                    }
                    z.parent.color        = BLACK;
                    z.parent.parent.color = RED;
                    rotateLeft(z.parent.parent);
                }
            }
        }
        root.color = BLACK; // raiz é sempre preta
    }

    // ── Range Query ───────────────────────────────────────────────
    @Override
    public List<Integer> rangeQuery(double v1, double v2, int riskMin) {
        List<Integer> result = new ArrayList<>();
        rangeRec(root, v1, v2, riskMin, result);
        return result;
    }

    private void rangeRec(Node node, double v1, double v2, int riskMin, List<Integer> result) {
        if (node == NIL) return;

        double val = node.transaction.getValue();

        if (val > v1) rangeRec(node.left,  v1, v2, riskMin, result);

        if (val >= v1 && val <= v2 && node.transaction.getRisk() >= riskMin)
            result.add(node.transaction.getId());

        if (val < v2) rangeRec(node.right, v1, v2, riskMin, result);
    }

    // ── Remove by Value ───────────────────────────────────────────
    @Override
    public void removeByValue(double value) {
        // Recolhe todos os nós com esse valor e remove-os um a um
        List<Node> toRemove = new ArrayList<>();
        collectByValue(root, value, toRemove);
        for (Node n : toRemove) {
            rbDelete(n);
            size--;
        }
    }

    private void collectByValue(Node node, double value, List<Node> list) {
        if (node == NIL) return;
        if (value < node.transaction.getValue())
            collectByValue(node.left, value, list);
        else if (value > node.transaction.getValue())
            collectByValue(node.right, value, list);
        else {
            collectByValue(node.left,  value, list);
            list.add(node);
            collectByValue(node.right, value, list);
        }
    }

    // Remoção RB clássica (Cormen et al.)
    private void rbDelete(Node z) {
        Node y = z;
        Node x;
        boolean yOriginalColor = y.color;

        if (z.left == NIL) {
            x = z.right;
            rbTransplant(z, z.right);
        } else if (z.right == NIL) {
            x = z.left;
            rbTransplant(z, z.left);
        } else {
            y = minimum(z.right);
            yOriginalColor = y.color;
            x = y.right;
            if (y.parent == z) {
                x.parent = y;
            } else {
                rbTransplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }
            rbTransplant(z, y);
            y.left   = z.left;
            y.left.parent = y;
            y.color  = z.color;
        }
        if (yOriginalColor == BLACK)
            deleteFixup(x);
    }

    private void rbTransplant(Node u, Node v) {
        if (u.parent == null)           root = v;
        else if (u == u.parent.left)    u.parent.left  = v;
        else                            u.parent.right = v;
        v.parent = u.parent;
    }

    // Corrige propriedades RB após remoção — 4 casos
    private void deleteFixup(Node x) {
        while (x != root && isBlack(x)) {
            if (x == x.parent.left) {
                Node w = x.parent.right;
                if (isRed(w)) {
                    // Caso 1
                    w.color        = BLACK;
                    x.parent.color = RED;
                    rotateLeft(x.parent);
                    w = x.parent.right;
                }
                if (isBlack(w.left) && isBlack(w.right)) {
                    // Caso 2
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (isBlack(w.right)) {
                        // Caso 3
                        w.left.color = BLACK;
                        w.color      = RED;
                        rotateRight(w);
                        w = x.parent.right;
                    }
                    // Caso 4
                    w.color        = x.parent.color;
                    x.parent.color = BLACK;
                    w.right.color  = BLACK;
                    rotateLeft(x.parent);
                    x = root;
                }
            } else {
                // Simétrico
                Node w = x.parent.left;
                if (isRed(w)) {
                    w.color        = BLACK;
                    x.parent.color = RED;
                    rotateRight(x.parent);
                    w = x.parent.left;
                }
                if (isBlack(w.right) && isBlack(w.left)) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (isBlack(w.left)) {
                        w.right.color = BLACK;
                        w.color       = RED;
                        rotateLeft(w);
                        w = x.parent.left;
                    }
                    w.color        = x.parent.color;
                    x.parent.color = BLACK;
                    w.left.color   = BLACK;
                    rotateRight(x.parent);
                    x = root;
                }
            }
        }
        x.color = BLACK;
    }

    private Node minimum(Node node) {
        while (node.left != NIL) node = node.left;
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
