package eco.neo.trade;

import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class TradeMatcher {
    public static void matchTrade(
            Map<String, List<TradeOffer>> supplyByCommodity,
            Map<String, List<TradeOffer>> demandByCommodity) {

        for (Map.Entry<String, List<TradeOffer>> entry : supplyByCommodity.entrySet()) {
            String commodityId = entry.getKey();
            List<TradeOffer> supplies = entry.getValue();
            List<TradeOffer> demands  = demandByCommodity.get(commodityId);
            if (demands == null) continue;

            for (TradeOffer supply : supplies) {
                while (supply.getItemNum() > 0) {
                    TradeOffer best = findBestDemand(supply, demands);
                    if (best == null) break;
                    new TradeDeal(supply, best);
                }
            }
        }
    }

    private static TradeOffer findBestDemand(TradeOffer supply, List<TradeOffer> demands) {
        TradeOffer best = null;
        List<TradeStrategy> chain = TradeConfig.getChain(supply.getFaction().getId());
        for (TradeOffer demand : demands) {
            if (demand.getItemNum() >= 0) continue;
            if (best == null) {
                best = demand;
                continue;
            }
            if (compareOffer(supply, demand, best, chain) < 0) {
                best = demand;
            }
        }
        return best;
    }

    private static int compareOffer(TradeOffer supply, TradeOffer a, TradeOffer b, List<TradeStrategy> chain) {
        for (TradeStrategy s : chain) {
            if (s == TradeStrategy.FACTION) {
                boolean aSame = a.getFaction().getId().equals(supply.getFaction().getId());
                boolean bSame = b.getFaction().getId().equals(supply.getFaction().getId());
                if (aSame != bSame) return bSame ? 1 : -1;
            } else if (s == TradeStrategy.PRICE) {
                float pa = a.getItemPrice() - supply.getItemPrice();
                float pb = b.getItemPrice() - supply.getItemPrice();
                if (pa != pb) return Float.compare(pb, pa);
            } else {
                float da = getDistance(supply, a);
                float db = getDistance(supply, b);
                if (da != db) return Float.compare(da, db);
            }
        }
        return 0;
    }

    private static float getDistance(TradeOffer supply, TradeOffer demand) {
        if (supply.getStarSystem() == demand.getStarSystem()) return 0f;
        return Misc.getDistance(supply.getStarSystem().getLocation(), demand.getStarSystem().getLocation());
    }
}
