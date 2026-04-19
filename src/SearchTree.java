import java.util.List;

public interface SearchTree {

    // Fase 1: inserir transação
    void insert(Transaction t);

    // Fase 2: consulta por intervalo [v1, v2] com risco >= riskMin
    List<Integer> rangeQuery(double v1, double v2, int riskMin);

    // Fase 3: remover todas as transações com o valor dado
    void removeByValue(double value);

    // Métricas
    long getRotationCount();
    void resetMetrics();
    int size();
}
