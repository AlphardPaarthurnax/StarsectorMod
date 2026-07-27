package eco.core;

public class Profit {
    private float export;
    private float tax;
    private float income;
    private float importCost;
    private float upkeep;
    public void clear() {
        export = 0f;
        tax = 0f;
        income = 0f;
        importCost = 0f;
        upkeep = 0f;
    }
    public void add(Profit other) {
        if (other == null) return;
        export += other.export;
        tax += other.tax;
        income += other.income;
        importCost += other.importCost;
        upkeep += other.upkeep;
    }
    public void addExport(float value) { export += value; }
    public void addTax(float value) { tax += value; }
    public void addIncome(float value) { income += value; }
    public void addImportCost(float value) { importCost += value; }
    public void addUpkeep(float value) { upkeep += value; }
    public float getExport() { return export; }
    public float getTax() { return tax; }
    public float getIncome() { return income; }
    public float getImportCost() { return importCost; }
    public float getUpkeep() { return upkeep; }
    public float getTotalIncome() {
        return export + tax + income;
    }
    public float getTotalCost() {
        return  importCost + upkeep;
    }
    public float getNetProfit() {
        return getTotalIncome() - getTotalCost();
    }
}
