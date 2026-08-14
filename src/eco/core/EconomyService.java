package eco.core;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import eco.EcoDebugDump;

public class EconomyService implements EconomyTickListener {
    private GlobalEconomy globalEconomy = new GlobalEconomy();
    private int lastProcessedMonth = Integer.MIN_VALUE;

    public void activate() {
        GlobalEconomy.setInstance(globalEconomy);
    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }
    @Override
    public void reportEconomyMonthEnd() {
        int month = Global.getSector().getClock().getCycle() * 12
                + Global.getSector().getClock().getMonth();
        if (month == lastProcessedMonth) return;
        lastProcessedMonth = month;

        activate();
        globalEconomy.updateSource(month);
        globalEconomy.matchTrade();
        globalEconomy.updateSupplyDemand();
        globalEconomy.updateTradeStock();
        globalEconomy.updatePrices();

        EcoDebugDump.dump();
    }
    public static float getPeopleScale(int size) {
        if (size <= 1) return 0.01f;
        if (size == 2) return 0.10f;
        if (size == 3) return 1.0f;

        float result = 1.0f;
        final float r = 0.772f;
        for (int s = 4; s <= size; s++) {
            result *= (1.0f + 9.0f * (float) Math.pow(r, s - 4));
        }
        return result;
    }
    public static float computePriceMultiplier(int supply, int demand, long stock) {
        float targetStock = demand * 3f;
        float stockRatio = demand > 0 ? (float) stock / Math.max(targetStock, 1f) : 1f;
        float stockPressure = 1f - clamp(stockRatio, 0f, 1f);

        float shortage = demand - supply;
        float demandPressure = clamp(shortage / Math.max(demand, 1f), -1f, 1f);

        float pressure = clamp(demandPressure * 0.6f + stockPressure * 0.4f, -1f, 1f);
        float smooth = pressure * pressure * (3f - 2f * Math.abs(pressure)) * Math.signum(pressure);

        if (smooth >= 0f) return 1f + smooth * 3f;     // max 4x (+300%)
        else               return 1f + smooth * 0.75f;  // min 0.25x (-75%)
    }
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
