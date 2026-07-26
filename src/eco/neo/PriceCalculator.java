package eco.neo;


public class PriceCalculator {
    public static float computePriceMultiplier(int supply, int demand, long stock) {
        float targetStock = demand * 3f;
        float stockRatio = demand > 0 ? (float) stock / Math.max(targetStock, 1f) : 1f;
        float stockPressure = 1f - clamp(stockRatio, 0f, 1f);

        float shortage = demand - supply;
        float demandPressure = clamp(shortage / Math.max(demand, 1f), -1f, 1f);

        float pressure = clamp(demandPressure * 0.6f + stockPressure * 0.4f, -1f, 1f);
        float smooth = pressure * pressure * (3f - 2f * Math.abs(pressure)) * Math.signum(pressure);

        if (smooth >= 0f) return 1f + smooth * 4f;     // max 5x (+400%)
        else               return 1f + smooth * 0.75f;  // min 0.25x (-75%)
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
