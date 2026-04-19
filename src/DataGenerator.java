import java.io.*;
import java.util.*;

/**
 * Gera ficheiros CSV de input para as 3 fases do projeto.
 *
 * Tipos de input:
 *   random        – valores aleatórios (caso médio)
 *   sorted_asc    – ordenado crescente (pior caso BST)
 *   sorted_desc   – ordenado decrescente (pior caso BST)
 *   nearly_sorted – 95% ordenado, 5% trocas (stresses AVL/RB)
 *   repeated      – baixa cardinalidade (stresses remoção)
 */
public class DataGenerator {

    private static final int    N_QUERY             = 10_000;
    private static final double REMOVE_FRACTION     = 0.20;
    private static final int    MAX_RISK            = 10;
    private static final double MAX_VALUE           = 10_000.0;
    private static final int    REPEATED_CARDINALITY = 500;

    // Chamado pelo Main para cada combinação (type, N)
    public static void generateForN(String type, int n) throws IOException {
        new File("data").mkdirs();
        Random rng = new Random(42);
        double[] values = generateValues(type, n, rng);
        int[]    risks  = generateRisks(n, rng);
        writeInsertFile(type, values, risks);
        writeQueryFile(type, values, rng);
        writeRemoveFile(type, values, rng);
    }

    // Ponto de entrada standalone (gera N=100k para todos os tipos)
    public static void main(String[] args) throws IOException {
        String[] types = {"random", "sorted_asc", "sorted_desc", "nearly_sorted", "repeated"};
        for (String type : types) {
            System.out.println("A gerar: " + type);
            generateForN(type, 100_000);
        }
        System.out.println("Concluído. Ficheiros em data/");
    }

    // ── Geração de valores ────────────────────────────────────────
    private static double[] generateValues(String type, int n, Random rng) {
        double[] v = new double[n];
        switch (type) {
            case "random":
                for (int i = 0; i < n; i++)
                    v[i] = Math.round(rng.nextDouble() * MAX_VALUE * 100.0) / 100.0;
                break;

            case "sorted_asc":
                for (int i = 0; i < n; i++)
                    v[i] = Math.round((i * MAX_VALUE / n) * 100.0) / 100.0;
                break;

            case "sorted_desc":
                for (int i = 0; i < n; i++)
                    v[i] = Math.round(((n - i) * MAX_VALUE / n) * 100.0) / 100.0;
                break;

            case "nearly_sorted":
                for (int i = 0; i < n; i++)
                    v[i] = Math.round((i * MAX_VALUE / n) * 100.0) / 100.0;
                int swaps = n / 20;
                for (int i = 0; i < swaps; i++) {
                    int a = rng.nextInt(n), b = rng.nextInt(n);
                    double tmp = v[a]; v[a] = v[b]; v[b] = tmp;
                }
                break;

            case "repeated":
                double[] pool = new double[REPEATED_CARDINALITY];
                for (int i = 0; i < REPEATED_CARDINALITY; i++)
                    pool[i] = Math.round((i * MAX_VALUE / REPEATED_CARDINALITY) * 100.0) / 100.0;
                for (int i = 0; i < n; i++)
                    v[i] = pool[rng.nextInt(REPEATED_CARDINALITY)];
                break;
        }
        return v;
    }

    private static int[] generateRisks(int n, Random rng) {
        int[] r = new int[n];
        for (int i = 0; i < n; i++) r[i] = rng.nextInt(MAX_RISK) + 1;
        return r;
    }

    // ── Escrita de ficheiros ───────────────────────────────────────
    private static void writeInsertFile(String type, double[] values, int[] risks)
            throws IOException {
        try (PrintWriter pw = new PrintWriter("data/insert_" + type + ".csv")) {
            pw.println("id,value,risk");
            for (int i = 0; i < values.length; i++)
                pw.printf("%d,%.2f,%d%n", i + 1, values[i], risks[i]);
        }
    }

    private static void writeQueryFile(String type, double[] values, Random rng)
            throws IOException {
        double rangeWidth = MAX_VALUE * 0.10;
        try (PrintWriter pw = new PrintWriter("data/query_" + type + ".csv")) {
            pw.println("value1,value2,riskMin");
            for (int i = 0; i < N_QUERY; i++) {
                double v1 = Math.round(rng.nextDouble() * (MAX_VALUE - rangeWidth) * 100.0) / 100.0;
                double v2 = Math.round((v1 + rangeWidth) * 100.0) / 100.0;
                int riskMin = rng.nextInt(MAX_RISK) + 1;
                pw.printf("%.2f,%.2f,%d%n", v1, v2, riskMin);
            }
        }
    }

    private static void writeRemoveFile(String type, double[] values, Random rng)
            throws IOException {
        int nRemove = (int)(values.length * REMOVE_FRACTION);
        Set<Double> chosen = new LinkedHashSet<>();
        List<Double> pool  = new ArrayList<>();
        for (double val : values) pool.add(val);
        Collections.shuffle(pool, rng);
        for (double val : pool) {
            chosen.add(val);
            if (chosen.size() >= nRemove) break;
        }
        try (PrintWriter pw = new PrintWriter("data/remove_" + type + ".csv")) {
            pw.println("value");
            for (double val : chosen) pw.printf("%.2f%n", val);
        }
    }
}
