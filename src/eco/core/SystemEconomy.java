package eco.core;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import eco.trade.*;

import java.util.*;

import static eco.EconomyService.computePriceMultiplier;

public class SystemEconomy {
    private final StarSystemAPI system;
    private Map<MarketAPI, PlanetEconomy> planetEconomys = new HashMap<>();
    private Set<String> commodityIds = new HashSet<>();
    private FactionAPI sovereigntyFaction = null;
    private boolean inSovereigntyFight = false;
    private Map<String, List<TradeOffer>> supply = new HashMap<>();
    private Map<String, List<TradeOffer>> demand = new HashMap<>();
    private Map<FactionAPI, Float> factionProfits = new HashMap<>();
    private Map<FactionAPI, Profit> factionProfitBreakdowns = new HashMap<>();
    public StarSystemAPI getSystem() { return system; }
    public Map<MarketAPI, PlanetEconomy> getAllPlanetEconomys() { return planetEconomys; }
    public PlanetEconomy getPlanetEconomy(MarketAPI m) { return planetEconomys.get(m); }
    public Set<String> getCommodityIds() {
        return commodityIds;
    }
    public FactionAPI getSovereigntyFaction() {
        return sovereigntyFaction;
    }
    public boolean isInSovereigntyFight() {
        return inSovereigntyFight;
    }
    public Map<String, List<TradeOffer>> getAllSupply() {
        return supply;
    }
    public Map<String, List<TradeOffer>> getAllDemand() {
        return demand;
    }
    public Map<FactionAPI, Float> getFactionProfits() {
        return factionProfits;
    }
    public Map<FactionAPI, Profit> getFactionProfitBreakdowns() {
        if (factionProfitBreakdowns == null) factionProfitBreakdowns = new HashMap<>();
        return factionProfitBreakdowns;
    }

    public SystemEconomy(StarSystemAPI system){
        this.system = system;
    }
    public void updateSource(List<MarketAPI> markets) {
        commodityIds.clear();

        for (MarketAPI market : markets) {
            if (!planetEconomys.containsKey(market)) {
                planetEconomys.put(market, new PlanetEconomy(market));
            }
        }
        planetEconomys.keySet().removeIf(m -> !markets.contains(m));

        for (PlanetEconomy pe : planetEconomys.values()) {
            pe.updateSource();
            pe.updateSupplyDemand();
            commodityIds.addAll(pe.getCommodityIds());
        }

        matchTrade();
    }

    public void matchTrade() {
        Map<String, List<TradeOffer>> supplyByCommodity = new HashMap<>();
        Map<String, List<TradeOffer>> demandByCommodity = new HashMap<>();

        for (PlanetEconomy pe : planetEconomys.values()) {
            for (TradeOffer o : pe.getSupplyOffers().values())
                supplyByCommodity.computeIfAbsent(o.getItemId(), k -> new ArrayList<>()).add(o);
            for (TradeOffer o : pe.getDemandOffers().values())
                demandByCommodity.computeIfAbsent(o.getItemId(), k -> new ArrayList<>()).add(o);
        }

        TradeMatcher.matchTrade(supplyByCommodity, demandByCommodity);
        clearSatisfied();
    }
    public void clearSatisfied() {
        for (PlanetEconomy pe : planetEconomys.values()) {
            pe.getSupplyOffers().values().removeIf(o -> o.getItemNum() <= 0);
            pe.getDemandOffers().values().removeIf(o -> o.getItemNum() >= 0);
        }
    }
    public void updateSovereigntyFaction() {
        if (planetEconomys.isEmpty()) {
            sovereigntyFaction = null;
            inSovereigntyFight = false;
            return;
        }

        Map<FactionAPI, Integer> factionTotalSize = new LinkedHashMap<>();

        for (PlanetEconomy pe : planetEconomys.values()) {
            factionTotalSize.merge(pe.getMarket().getFaction(), pe.getMarketSize(), Integer::sum);
        }

        if (factionTotalSize.size() == 1) {
            sovereigntyFaction = factionTotalSize.keySet().iterator().next();
            inSovereigntyFight = false;
        } else {
            sovereigntyFaction = factionTotalSize.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
            inSovereigntyFight = true;
        }
    }
    public void updateSupplyDemand() {
        supply.clear();
        demand.clear();

        for (PlanetEconomy pe : planetEconomys.values()) {
            for (TradeOffer o : pe.getSupplyOffers().values()) {
                supply.computeIfAbsent(o.getItemId(), k -> new ArrayList<>()).add(o);
            }
            for (TradeOffer o : pe.getDemandOffers().values()) {
                demand.computeIfAbsent(o.getItemId(), k -> new ArrayList<>()).add(o);
            }
        }
    }
    public void updatePrices(Map<String, Float> globalPrices) {
        Map<String, Float> systemPrices = new HashMap<>();

        for (String cid : commodityIds) {
            float gPrice = globalPrices.getOrDefault(cid, Global.getSettings().getCommoditySpec(cid).getBasePrice());

            int s = 0, d = 0;
            long st = 0;
            for (PlanetEconomy pe : planetEconomys.values()) {
                s += pe.getAllActualSupply().getOrDefault(cid, 0);
                d += pe.getAllActualDemand().getOrDefault(cid, 0);
                st += pe.getAllStock().getOrDefault(cid, 0L);
            }

            float mult = computePriceMultiplier(s, d, st);
            systemPrices.put(cid, gPrice * mult);
        }

        factionProfits.clear();
        getFactionProfitBreakdowns().clear();
        for (PlanetEconomy pe : planetEconomys.values()) {
            pe.updatePlanetPrices(systemPrices);
            pe.updatePlanetProfit();
            FactionAPI faction = pe.getMarket().getFaction();
            getFactionProfitBreakdowns().computeIfAbsent(faction, key -> new Profit())
                    .add(pe.getProfit());
        }
        for (Map.Entry<FactionAPI, Profit> entry : getFactionProfitBreakdowns().entrySet()) {
            factionProfits.put(entry.getKey(), entry.getValue().getNetProfit());
        }
    }
}
