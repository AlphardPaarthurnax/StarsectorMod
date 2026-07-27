package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.combat.MutableStat;
import eco.core.GlobalEconomy;

import java.util.Map;

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
        globalEconomy.updateSource();
        globalEconomy.matchTrade();
        globalEconomy.updateSupplyDemand();
        globalEconomy.updateTradeStock();
        globalEconomy.updatePrices();
        EcoDebugDump.dump();
    }
    public static MutableCommodityQuantity scaleMutableCommodityQuantity(MutableCommodityQuantity A, float base, float flat, float mult, float percent){
        MutableCommodityQuantity MCQ = new MutableCommodityQuantity(A.getCommodityId());
        MCQ.getQuantity().setBaseValue(A.getQuantity().getBaseValue() * base);
        for (Map.Entry<String, MutableStat.StatMod> e : A.getQuantity().getFlatMods().entrySet()) {
            MutableStat.StatMod m = e.getValue();
            MCQ.getQuantity().modifyFlat(e.getKey(), m.value * flat, m.desc);
        }
        for (Map.Entry<String, MutableStat.StatMod> e : A.getQuantity().getMultMods().entrySet()) {
            MutableStat.StatMod m = e.getValue();
            MCQ.getQuantity().modifyMult(e.getKey(), m.value * mult, m.desc);
        }
        for (Map.Entry<String, MutableStat.StatMod> e : A.getQuantity().getPercentMods().entrySet()) {
            MutableStat.StatMod m = e.getValue();
            MCQ.getQuantity().modifyPercent(e.getKey(), m.value * percent, m.desc);
        }
        return MCQ;
    }
    public static MutableStat scaleMutableStat(MutableStat A, float base, float flat, float mult, float percent){
        MutableStat MS = new MutableStat(A.getBaseValue() * base);
        for(Map.Entry<String, MutableStat.StatMod> e : A.getFlatMods().entrySet()){
            MutableStat.StatMod m = e.getValue();
            MS.modifyFlat(e.getKey(), m.value * flat, m.desc);
        }
        for (Map.Entry<String, MutableStat.StatMod> e : A.getMultMods().entrySet()) {
            MutableStat.StatMod m = e.getValue();
            MS.modifyMult(e.getKey(), m.value * mult, m.desc);
        }
        for (Map.Entry<String, MutableStat.StatMod> e : A.getPercentMods().entrySet()) {
            MutableStat.StatMod m = e.getValue();
            MS.modifyPercent(e.getKey(), m.value * percent, m.desc);
        }
        return MS;
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
