package eco.data;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;

import java.util.*;

public class PlanetMarket{
    private final StarSystemAPI system;
    private final PlanetAPI planet;
    private final MarketAPI market;
    private final FactionAPI faction;
    private int updateTime = 0;
    //库存
    private Map<String, Long> stock = new HashMap<>();
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

        updateTime++;

        float peopleScale = getPeopleScale(market.getSize());

        if(stock.isEmpty()){
            for (Industry ind : market.getIndustries()) {
                for (MutableCommodityQuantity supplyMCQ : ind.getAllSupply()) {
                    stock.merge(supplyMCQ.getCommodityId(), (long) (supplyMCQ.getQuantity().getModifiedInt() * peopleScale), Long::sum);
                }
                for (MutableCommodityQuantity demandMCQ : ind.getAllDemand()) {
                    stock.merge(demandMCQ.getCommodityId(), (long) (demandMCQ.getQuantity().getModifiedInt() * peopleScale), Long::sum);
                }
            }
        }
        //初始化库存

        Map<String, Integer> demandPre = new HashMap<>();
        Map<String, Float> ratio = new HashMap<>();

        for (Industry ind : market.getIndustries()) {
            for (MutableCommodityQuantity demandMCQ : ind.getAllDemand()) {
                demandPre.merge(demandMCQ.getCommodityId(), (int) Math.floor(demandMCQ.getQuantity().getModifiedInt() * peopleScale), Integer::sum);
            }
        }
        for(Map.Entry<String, Integer> pre : demandPre.entrySet()){
            ratio.put(pre.getKey(), Math.min(1.0f, (float) stock.getOrDefault(pre.getKey(), 0L) / pre.getValue()));
        }
        //产率

        for (Industry ind : market.getIndustries()) {
            float industryRatio = 1f;
            for (MutableCommodityQuantity demandMCQ : ind.getAllDemand()) {
                industryRatio = Math.min(industryRatio, ratio.get(demandMCQ.getCommodityId()));
            }
            industryRatio = Math.max(0.1f, industryRatio);

            for (MutableCommodityQuantity supplyMCQ : ind.getAllSupply()) {
                String commodityId = supplyMCQ.getCommodityId();
                int supplyR = (int) Math.floor(supplyMCQ.getQuantity().getModifiedInt() * peopleScale * industryRatio);

                stock.merge(commodityId, (long) supplyR, Long::sum);
                supplyRaw.merge(commodityId, supplyR, Integer::sum);
                demandRaw.merge(commodityId, 0, Integer::sum);
                supplyFactory.computeIfAbsent(commodityId, k -> new LinkedHashMap<>()).put(ind, supplyR);
            }
            for (MutableCommodityQuantity demandMCQ : ind.getAllDemand()) {
                String commodityId = demandMCQ.getCommodityId();
                int demandR = (int) Math.floor(demandMCQ.getQuantity().getModifiedInt() * peopleScale * industryRatio);

                stock.merge(demandMCQ.getCommodityId(), (long) -demandR, Long::sum);
                supplyRaw.merge(commodityId, 0, Integer::sum);
                demandRaw.merge(commodityId, demandR, Integer::sum);
                demandFactory.computeIfAbsent(commodityId, k -> new LinkedHashMap<>()).put(ind, demandR);
            }
        }
        for(Map.Entry<String, Integer> raw : supplyRaw.entrySet()){
            int net = supplyRaw.get(raw.getKey()) - demandRaw.get(raw.getKey());
            if (net > 0) supply.put(raw.getKey(), net);
            if (net < 0) demand.put(raw.getKey(), -net);
        }
        //核心
    }
    public void updateTrade(){
        for(TradePair trade : supplyTrade){
            stock.merge(trade.getItemId(), (long) -trade.getItemNum(),Long::sum);
        }
        for(TradePair trade : demandTrade){
            stock.merge(trade.getItemId(), (long) trade.getItemNum(),Long::sum);
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
    public int getUpdateTime(){
        return updateTime;
    }
    public long getStock(String commodityId){
        return stock.getOrDefault(commodityId, 0L);
    }
}
