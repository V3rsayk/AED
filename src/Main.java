import java.io.*;
import java.util.*;

/**
 * Benchmark — corre cada experiência 3 vezes e reporta médias.
 * Testa múltiplos valores de N: 100k, 500k, 1000k transações.
 *
 * Compilar:  javac src/*.java -d out/
 * Executar:  java -Xmx1g -cp out Main
 */
public class Main {

    private static final String[] INPUT_TYPES  = {
        "random", "sorted_asc", "sorted_desc", "nearly_sorted", "repeated"
    };
    private static final String[] TREE_NAMES   = {
        "BST", "AVL", "RedBlack", "Splay"
    };
    private static final int[]    N_VALUES     = {100_000, 500_000, 1_000_000};
    private static final int      RUNS         = 3;

    public static void main(String[] args) throws IOException {
        new File("results").mkdirs();

        try (PrintWriter out = new PrintWriter("results/results.csv")) {
            out.println("tree,input_type,n," +
                        "insert_ms_avg,insert_rotations_avg," +
                        "query_ms_avg," +
                        "remove_ms_avg,remove_rotations_avg");

            for (int N : N_VALUES) {
                System.out.println("\n══════════════════════════════════════");
                System.out.println("  N = " + N);
                System.out.println("══════════════════════════════════════");

                for (String type : INPUT_TYPES) {
                    System.out.println("\n  Input: " + type);

                    // Gera dados para este N e tipo
                    DataGenerator.generateForN(type, N);

                    List<Transaction> insertData = loadInsert("data/insert_" + type + ".csv");
                    List<double[]>    queryData  = loadQuery ("data/query_"  + type + ".csv");
                    List<Double>      removeData = loadRemove("data/remove_" + type + ".csv");

                    for (String treeName : TREE_NAMES) {
                        long[] insertMs  = new long[RUNS];
                        long[] insertRot = new long[RUNS];
                        long[] queryMs   = new long[RUNS];
                        long[] removeMs  = new long[RUNS];
                        long[] removeRot = new long[RUNS];

                        for (int run = 0; run < RUNS; run++) {
                            SearchTree tree = createTree(treeName);

                            // Fase 1: Inserção
                            tree.resetMetrics();
                            long t0 = System.currentTimeMillis();
                            for (Transaction tx : insertData) tree.insert(tx);
                            insertMs[run]  = System.currentTimeMillis() - t0;
                            insertRot[run] = tree.getRotationCount();

                            // Fase 2: Query
                            tree.resetMetrics();
                            t0 = System.currentTimeMillis();
                            for (double[] q : queryData)
                                tree.rangeQuery(q[0], q[1], (int) q[2]);
                            queryMs[run] = System.currentTimeMillis() - t0;

                            // Fase 3: Remoção
                            tree.resetMetrics();
                            t0 = System.currentTimeMillis();
                            for (double v : removeData) tree.removeByValue(v);
                            removeMs[run]  = System.currentTimeMillis() - t0;
                            removeRot[run] = tree.getRotationCount();
                        }

                        // Médias
                        double avgInsertMs  = avg(insertMs);
                        double avgInsertRot = avg(insertRot);
                        double avgQueryMs   = avg(queryMs);
                        double avgRemoveMs  = avg(removeMs);
                        double avgRemoveRot = avg(removeRot);

                        System.out.printf("    %-10s ins=%.0fms rot=%.0f | qry=%.0fms | rem=%.0fms rot=%.0f%n",
                            treeName, avgInsertMs, avgInsertRot,
                            avgQueryMs, avgRemoveMs, avgRemoveRot);

                        out.printf("%s,%s,%d,%.1f,%.1f,%.1f,%.1f,%.1f%n",
                            treeName, type, N,
                            avgInsertMs, avgInsertRot,
                            avgQueryMs,
                            avgRemoveMs, avgRemoveRot);
                    }
                }
            }
        }
        System.out.println("\nResultados em results/results.csv");
    }

    // ── Fábrica ───────────────────────────────────────────────────
    private static SearchTree createTree(String name) {
        switch (name) {
            case "BST":      return new BST();
            case "AVL":      return new AVL();
            case "RedBlack": return new RedBlackTree();
            case "Splay":    return new SplayTree();
            default: throw new IllegalArgumentException(name);
        }
    }

    // ── Leitores CSV ──────────────────────────────────────────────
    private static List<Transaction> loadInsert(String path) throws IOException {
        List<Transaction> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                list.add(new Transaction(
                    Integer.parseInt(p[0].trim()),
                    Double.parseDouble(p[1].trim()),
                    Integer.parseInt(p[2].trim())
                ));
            }
        }
        return list;
    }

    private static List<double[]> loadQuery(String path) throws IOException {
        List<double[]> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                list.add(new double[]{
                    Double.parseDouble(p[0].trim()),
                    Double.parseDouble(p[1].trim()),
                    Double.parseDouble(p[2].trim())
                });
            }
        }
        return list;
    }

    private static List<Double> loadRemove(String path) throws IOException {
        List<Double> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null)
                list.add(Double.parseDouble(line.trim()));
        }
        return list;
    }

    private static double avg(long[] arr) {
        long sum = 0;
        for (long v : arr) sum += v;
        return (double) sum / arr.length;
    }
}
