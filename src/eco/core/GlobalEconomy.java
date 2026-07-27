package eco.core;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import eco.trade.TradeMatcher;
import eco.trade.TradeOffer;

import java.util.*;

import static eco.core.EconomyService.computePriceMultiplier;

public class GlobalEconomy{
    private Map<StarSystemAPI, SystemEconomy> systemEconomys = new HashMap<>();
    private Map<String, List<TradeOffer>> supply = new HashMap<>();
    private Map<String, List<TradeOffer>> demand = new HashMap<>();
    private Map<String, Float> globalPrices = new HashMap<>();
    private Map<FactionAPI, Float> factionProfits = new HashMap<>();
    private Map<FactionAPI, Profit> factionProfitBreakdowns = new HashMap<>();
    public Map<StarSystemAPI, SystemEconomy> getAllSystemEconomys() { return systemEconomys; }
    public SystemEconomy getSystemEconomy(StarSystemAPI s) { return systemEconomys.get(s); }
    public Map<String, List<TradeOffer>> getAllSupply() { return supply; }
    public Map<String, List<TradeOffer>> getAllDemand() { return demand; }
    public Map<String, Float> getGlobalPrices() { return globalPrices; }
    public Map<FactionAPI, Float> getFactionProfits() { return factionProfits; }
    public Map<FactionAPI, Profit> getFactionProfitBreakdowns() {
        if (factionProfitBreakdowns == null) factionProfitBreakdowns = new HashMap<>();
        return factionProfitBreakdowns;
    }

    private static GlobalEconomy instance = new GlobalEconomy();
    GlobalEconomy() {}
    public static GlobalEconomy getInstance() { return instance; }
    static void setInstance(GlobalEconomy globalEconomy) {
        if (globalEconomy != null) instance = globalEconomy;
    }

    public void updateSource(){
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        allMarkets.removeIf(m -> !m.isInEconomy() || m.getStarSystem() == null);

        Map<StarSystemAPI, List<MarketAPI>> systemGroups = new LinkedHashMap<>();
        for (MarketAPI market : allMarkets) {
            systemGroups.computeIfAbsent(market.getStarSystem(), k -> new ArrayList<>()).add(market);
        }

        systemEconomys.keySet().removeIf(sys -> !systemGroups.containsKey(sys));
        for (Map.Entry<StarSystemAPI, List<MarketAPI>> entry : systemGroups.entrySet()) {
            if (!systemEconomys.containsKey(entry.getKey())) {
                systemEconomys.put(entry.getKey(), new SystemEconomy(entry.getKey()));
            }
            systemEconomys.get(entry.getKey()).updateSource(entry.getValue());
        }
        //创建 SystemEconomy
        for (Map.Entry<StarSystemAPI, SystemEconomy> systemEconomy : systemEconomys.entrySet()){
            systemEconomy.getValue().updateSovereigntyFaction();
            systemEconomy.getValue().updateSupplyDemand();
        }
    }
    public void matchTrade() {
        Map<String, List<TradeOffer>> allSupply = new HashMap<>();
        Map<String, List<TradeOffer>> allDemand = new HashMap<>();

        for (SystemEconomy se : systemEconomys.values()) {
            for (Map.Entry<String, List<TradeOffer>> e : se.getAllSupply().entrySet())
                allSupply.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue());
            for (Map.Entry<String, List<TradeOffer>> e : se.getAllDemand().entrySet())
                allDemand.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue());
        }

        TradeMatcher.matchTrade(allSupply, allDemand);

        for (SystemEconomy se : systemEconomys.values()) {
            se.clearSatisfied();
            se.updateSupplyDemand();
        }
    }
    public void updateSupplyDemand() {
        supply.clear();
        demand.clear();

        for (SystemEconomy se : systemEconomys.values()) {
            for (PlanetEconomy pe : se.getAllPlanetEconomys().values()) {
                for (TradeOffer o : pe.getSupplyOffers().values()) {
                    if (o.getItemNum() > 0)
                        supply.computeIfAbsent(o.getItemId(), k -> new ArrayList<>()).add(o);
                }
                for (TradeOffer o : pe.getDemandOffers().values()) {
                    if (o.getItemNum() < 0)
                        demand.computeIfAbsent(o.getItemId(), k -> new ArrayList<>()).add(o);
                }
            }
        }
    }
    public void updateTradeStock() {
        for (SystemEconomy se : systemEconomys.values()) {
            for (PlanetEconomy pe : se.getAllPlanetEconomys().values()) {
                pe.updateTradeStock();
            }
        }
    }
    public void updatePrices() {
        // 1. 聚合全银河 supply/demand/stock
        Map<String, Integer> allSupply = new HashMap<>();
        Map<String, Integer> allDemand = new HashMap<>();
        Map<String, Long> allStock = new HashMap<>();
        Set<String> allIds = new HashSet<>();

        for (SystemEconomy se : systemEconomys.values()) {
            for (PlanetEconomy pe : se.getAllPlanetEconomys().values()) {
                for (Map.Entry<String, Integer> e : pe.getAllActualSupply().entrySet())
                    allSupply.merge(e.getKey(), e.getValue(), Integer::sum);
                for (Map.Entry<String, Integer> e : pe.getAllActualDemand().entrySet())
                    allDemand.merge(e.getKey(), e.getValue(), Integer::sum);
                for (Map.Entry<String, Long> e : pe.getAllStock().entrySet())
                    allStock.merge(e.getKey(), e.getValue(), Long::sum);
                allIds.addAll(pe.getAllActualSupply().keySet());
                allIds.addAll(pe.getAllActualDemand().keySet());
            }
        }

        globalPrices.clear();
        for (String cid : allIds) {
            float base = Global.getSettings().getCommoditySpec(cid).getBasePrice();
            int s = allSupply.getOrDefault(cid, 0);
            int d = allDemand.getOrDefault(cid, 0);
            long st = allStock.getOrDefault(cid, 0L);
            globalPrices.put(cid, base * computePriceMultiplier(s, d, st));
        }

        // 3. 下推到各星系 + 汇总全局势力利润
        factionProfits.clear();
        getFactionProfitBreakdowns().clear();
        for (SystemEconomy se : systemEconomys.values()) {
            se.updatePrices(globalPrices);
            for (Map.Entry<FactionAPI, Profit> e : se.getFactionProfitBreakdowns().entrySet()) {
                getFactionProfitBreakdowns().computeIfAbsent(e.getKey(), key -> new Profit())
                        .add(e.getValue());
            }
        }
        for (Map.Entry<FactionAPI, Profit> entry : getFactionProfitBreakdowns().entrySet()) {
            factionProfits.put(entry.getKey(), entry.getValue().getNetProfit());
        }
    }
}
