package eco.core;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.combat.MutableStat;
import eco.EcoDebugDump;
import eco.core.GlobalEconomy;

import java.util.Map;
import java.util.Objects;

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
        globalEconomy.updateMarketInjection();
        EcoDebugDump.dump();
    }
    private static boolean matchMask(String source, String[] mask) {
        if (mask == null) return false;
        for (String m : mask) {
            if (Objects.equals(source, m)) {
                return true;
            }
        }
        return false;
    }
    public static MutableCommodityQuantity copyMaskMCQ(MutableCommodityQuantity A, String... Mask){
        MutableCommodityQuantity MaskMCQ = new MutableCommodityQuantity(A.getCommodityId());
        MaskMCQ.getQuantity().setBaseValue(A.getQuantity().getBaseValue());

        for(MutableStat.StatMod s : A.getQuantity().getFlatMods().values()){
            if (matchMask(s.source, Mask)) {
                continue;
            }
            MaskMCQ.getQuantity().modifyFlat(s.source, s.value, s.desc);
        }
        for(MutableStat.StatMod s : A.getQuantity().getMultMods().values()){
            if (matchMask(s.source, Mask)) {
                continue;
            }
            MaskMCQ.getQuantity().modifyMult(s.source, s.value, s.desc);
        }
        for(MutableStat.StatMod s : A.getQuantity().getPercentMods().values()){
            if (matchMask(s.source, Mask)) {
                continue;
            }
            MaskMCQ.getQuantity().modifyPercent(s.source, s.value, s.desc);
        }
        return MaskMCQ;
    }
    public static MutableStat copyMaskMCQ(MutableStat A, String... Mask){
        MutableStat MaskMS = new MutableStat(A.base);

        for(MutableStat.StatMod s : A.getFlatMods().values()){
            if (matchMask(s.source, Mask)) {
                continue;
            }
            MaskMS.modifyFlat(s.source, s.value, s.desc);
        }
        for(MutableStat.StatMod s : A.getMultMods().values()){
            if (matchMask(s.source, Mask)) {
                continue;
            }
            MaskMS.modifyMult(s.source, s.value, s.desc);
        }
        for(MutableStat.StatMod s : A.getPercentMods().values()){
            if (matchMask(s.source, Mask)) {
                continue;
            }
            MaskMS.modifyPercent(s.source, s.value, s.desc);
        }
        return MaskMS;
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
