public class Transaction {
    private int id;
    private double value;
    private int risk;

    public Transaction(int id, double value, int risk) {
        this.id = id;
        this.value = value;
        this.risk = risk;
    }

    public int getId()      { return id; }
    public double getValue(){ return value; }
    public int getRisk()    { return risk; }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", value=" + value + ", risk=" + risk + "}";
    }
}
