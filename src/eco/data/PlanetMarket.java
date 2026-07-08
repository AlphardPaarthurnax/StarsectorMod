package eco.data;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.*;

public class PlanetMarket{
    private final StarSystemAPI system;
    private final PlanetAPI planet;
    private final MarketAPI market;
    private final FactionAPI faction;
    // 逐工业 产能/消耗
    private Map<String, Map<Industry, Integer>> supplyFactory = new LinkedHashMap<>();
    private Map<String, Map<Industry, Integer>> demandFactory = new LinkedHashMap<>();
    // 原始总产量/总消耗(未互相抵消)
    private Map<String, Integer> supplyRaw = new HashMap<>();
    private Map<String, Integer> demandRaw = new HashMap<>();
    // 产能/消耗 总和
    private Map<String, Integer> supply = new HashMap<>();
    private Map<String, Integer> demand = new HashMap<>();
    // 生产/进口 订单
    private List<TradePair> supplyTrade = new ArrayList<>();
    private List<TradePair> demandTrade = new ArrayList<>();
    public PlanetMarket(StarSystemAPI system, PlanetAPI planet, MarketAPI market, FactionAPI faction){
        this.system = system;
        this.planet = planet;
        this.market = market;
        this.faction = faction;
    }
    public void updateSupplyAndDemand() {
        supplyFactory.clear();
        demandFactory.clear();
        supplyRaw.clear();
        demandRaw.clear();
        supply.clear();
        demand.clear();
        for (CommodityOnMarketAPI item : market.getAllCommodities()) {
            if (item.isNonEcon()) continue;
            int totalSupply = 0;
            int totalDemand = 0;
            for (Industry ind : market.getIndustries()) {
                int supplyNum = (int) (ind.getSupply(item.getId()).getQuantity().getModifiedInt() * getPeopleScale(market.getSize()));
                totalSupply += supplyNum;
                if(supplyNum > 0){
                    supplyFactory.computeIfAbsent(item.getId(), k -> new LinkedHashMap<>()).put(ind, supplyNum);
                }
                int demandNum = (int) (ind.getDemand(item.getId()).getQuantity().getModifiedInt() * getPeopleScale(market.getSize()));
                totalDemand += demandNum;
                if(demandNum > 0){
                    demandFactory.computeIfAbsent(item.getId(), k -> new LinkedHashMap<>()).put(ind, demandNum);
                }
            }
            supplyRaw.put(item.getId(), totalSupply);
            demandRaw.put(item.getId(), totalDemand);

            int netMargin = totalSupply - totalDemand;
            if (netMargin > 0) supply.put(item.getId(), netMargin);
            if (netMargin < 0) demand.put(item.getId(), -netMargin);
        }
    }
    private float getPeopleScale(int size) {
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
    public MarketAPI getMarket() { return market; }
    public StarSystemAPI getSystem() { return system; }
    public PlanetAPI getPlanet() { return planet; }
    public FactionAPI getFaction() { return faction; }
    public int getSupply(String commodityId)    { return supply.getOrDefault(commodityId, 0); }
    public int getDemand(String commodityId)    { return demand.getOrDefault(commodityId, 0); }
    public int getSupplyRaw(String commodityId) { return supplyRaw.getOrDefault(commodityId, 0); }
    public int getDemandRaw(String commodityId) { return demandRaw.getOrDefault(commodityId, 0); }
    public Map<Industry, Integer> getSupplyFactory(String commodityId) { return supplyFactory.getOrDefault(commodityId, Collections.emptyMap()); }
    public Map<Industry, Integer> getDemandFactory(String commodityId) { return demandFactory.getOrDefault(commodityId, Collections.emptyMap()); }
    public List<TradePair> getSupplyTrade() { return supplyTrade; }
    public List<TradePair> getDemandTrade() { return demandTrade; }
    public void addSupplyTrade(TradePair supplyTradePair) { this.supplyTrade.add(supplyTradePair); }
    public void addDemandTrade(TradePair demandTradePair) { this.demandTrade.add(demandTradePair); }
    public Set<String> getCommodityIds() {
        Set<String> ids = new HashSet<>();
        ids.addAll(supplyRaw.keySet());
        ids.addAll(demandRaw.keySet());
        return ids;
    }
}
