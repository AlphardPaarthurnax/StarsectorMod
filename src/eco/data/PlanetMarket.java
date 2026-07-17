package eco.data;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import eco.SystemEconomyService;
import eco.mixin.BaseIndustryAccessor;

import java.util.*;

import static eco.SystemEconomyService.clamp;

public class PlanetMarket{
    private final StarSystemAPI system;
    private final PlanetAPI planet;
    private final MarketAPI market;
    private final FactionAPI faction;
    private int updateTime = 0;
    //库存
    private Map<String, Long> stock = new HashMap<>();
    //价格
    private Map<String, Float> prices = new HashMap<>();
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
    // 送回 Industry 的修正值
    Map<Industry,Map<String, MutableCommodityQuantity>> allSupplyS = new HashMap<>();
    Map<Industry,Map<String, MutableCommodityQuantity>> allDemandS = new HashMap<>();
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

        Map<Industry,List<MutableCommodityQuantity>> allSupply = new HashMap<>();
        Map<Industry,List<MutableCommodityQuantity>> allDemand = new HashMap<>();

        for (Industry ind : market.getIndustries()) {
            for (MutableCommodityQuantity mcq : ((BaseIndustryAccessor)ind).getSupplySource().values()) {
                if (mcq.getQuantity().getModifiedValue() > 0) {
                    allSupply.computeIfAbsent(ind, k -> new ArrayList<>()).add(mcq);
                }
            }
            for (MutableCommodityQuantity mcq : ((BaseIndustryAccessor)ind).getDemandSource().values()) {
                if (mcq.getQuantity().getModifiedValue() > 0) {
                    allDemand.computeIfAbsent(ind, k -> new ArrayList<>()).add(mcq);
                }
            }
        }

        if(stock.isEmpty()){
            for (Industry ind : market.getIndustries()) {
                for (MutableCommodityQuantity supplyMCQ : allSupply.getOrDefault(ind, Collections.emptyList())) {
                    stock.merge(supplyMCQ.getCommodityId(), (long) (supplyMCQ.getQuantity().getModifiedInt() * peopleScale), Long::sum);
                }
                for (MutableCommodityQuantity demandMCQ : allDemand.getOrDefault(ind, Collections.emptyList())) {
                    stock.merge(demandMCQ.getCommodityId(), (long) (demandMCQ.getQuantity().getModifiedInt() * peopleScale), Long::sum);
                }
            }
        }
        //初始化库存

        Map<String, Integer> demandPre = new HashMap<>();
        Map<String, Float> ratio = new HashMap<>();

        for (Industry ind : market.getIndustries()) {
            for (MutableCommodityQuantity demandMCQ : allDemand.getOrDefault(ind, Collections.emptyList())) {
                demandPre.merge(demandMCQ.getCommodityId(), (int) Math.floor(demandMCQ.getQuantity().getModifiedInt() * peopleScale), Integer::sum);
            }
        }
        for(Map.Entry<String, Integer> pre : demandPre.entrySet()){
            ratio.put(pre.getKey(), Math.min(1.0f, (float) stock.getOrDefault(pre.getKey(), 0L) / pre.getValue()));
        }
        //产率

        for (Industry ind : market.getIndustries()) {
            float industryRatio = 1f;
            for (MutableCommodityQuantity demandMCQ : allDemand.getOrDefault(ind, Collections.emptyList())) {
                industryRatio = Math.min(industryRatio, ratio.get(demandMCQ.getCommodityId()));
            }
            industryRatio = Math.max(0.1f, industryRatio);

            for (MutableCommodityQuantity supplyMCQ : allSupply.getOrDefault(ind, Collections.emptyList())) {
                String commodityId = supplyMCQ.getCommodityId();
                int supplyR = (int) Math.floor(supplyMCQ.getQuantity().getModifiedInt() * peopleScale * industryRatio);

                MutableCommodityQuantity newSupplyMCQ = new MutableCommodityQuantity(commodityId);
                newSupplyMCQ.getQuantity().setBaseValue(supplyMCQ.getQuantity().getBaseValue() * peopleScale * industryRatio);
                for (Map.Entry<String, MutableStat.StatMod> e : supplyMCQ.getQuantity().getFlatMods().entrySet()) {
                    MutableStat.StatMod m = e.getValue();
                    newSupplyMCQ.getQuantity().modifyFlat(e.getKey(), m.value * peopleScale * industryRatio, m.desc);
                }
                for (Map.Entry<String, MutableStat.StatMod> e : supplyMCQ.getQuantity().getMultMods().entrySet()) {
                    MutableStat.StatMod m = e.getValue();
                    newSupplyMCQ.getQuantity().modifyMult(e.getKey(), m.value, m.desc);
                }
                for (Map.Entry<String, MutableStat.StatMod> e : supplyMCQ.getQuantity().getPercentMods().entrySet()) {
                    MutableStat.StatMod m = e.getValue();
                    newSupplyMCQ.getQuantity().modifyPercent(e.getKey(), m.value, m.desc);
                }
                allSupplyS.computeIfAbsent(ind, k -> new HashMap<>()).put(supplyMCQ.getCommodityId(), newSupplyMCQ);

                stock.merge(commodityId, (long) supplyR, Long::sum);
                supplyRaw.merge(commodityId, supplyR, Integer::sum);
                demandRaw.merge(commodityId, 0, Integer::sum);
                supplyFactory.computeIfAbsent(commodityId, k -> new LinkedHashMap<>()).put(ind, supplyR);
            }
            for (MutableCommodityQuantity demandMCQ : allDemand.getOrDefault(ind, Collections.emptyList())) {
                String commodityId = demandMCQ.getCommodityId();
                int demandR = (int) Math.floor(demandMCQ.getQuantity().getModifiedInt() * peopleScale * industryRatio);

                MutableCommodityQuantity newDemandMCQ = new MutableCommodityQuantity(commodityId);
                newDemandMCQ.getQuantity().setBaseValue(demandMCQ.getQuantity().getBaseValue() * peopleScale * industryRatio);
                for (Map.Entry<String, MutableStat.StatMod> e : demandMCQ.getQuantity().getFlatMods().entrySet()) {
                    MutableStat.StatMod m = e.getValue();
                    newDemandMCQ.getQuantity().modifyFlat(e.getKey(), m.value * peopleScale * industryRatio, m.desc);
                }
                for (Map.Entry<String, MutableStat.StatMod> e : demandMCQ.getQuantity().getMultMods().entrySet()) {
                    MutableStat.StatMod m = e.getValue();
                    newDemandMCQ.getQuantity().modifyMult(e.getKey(), m.value, m.desc);
                }
                for (Map.Entry<String, MutableStat.StatMod> e : demandMCQ.getQuantity().getPercentMods().entrySet()) {
                    MutableStat.StatMod m = e.getValue();
                    newDemandMCQ.getQuantity().modifyPercent(e.getKey(), m.value, m.desc);
                }
                allDemandS.computeIfAbsent(ind, k -> new HashMap<>()).put(demandMCQ.getCommodityId(), newDemandMCQ);

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
    public void updatePrices(){
        for(String commodityId : getCommodityIds()){
            float targetStock = demandRaw.getOrDefault(commodityId, 0) * 3f;

            float stockRatio = stock.getOrDefault(commodityId, 0L) / Math.max(targetStock, 1f);
            float stockPressure = 1f - clamp(stockRatio, 0f, 1f);

            float shortage = demandRaw.getOrDefault(commodityId, 0) - supplyRaw.getOrDefault(commodityId, 0);
            float demandPressure = shortage / Math.max(demandRaw.getOrDefault(commodityId, 0), 1f);
            demandPressure = clamp(demandPressure, -1f, 1f);

            float pressure = demandPressure * 0.6f + stockPressure * 0.4f;
            pressure = clamp(pressure, -1f, 1f);

            float smooth = pressure * pressure * (3f - 2f * Math.abs(pressure));
            smooth *= Math.signum(pressure);

            float multiplier;

            if (smooth >= 0f) {
                multiplier = 1f + smooth * 10f;
            } else {
                multiplier = 1f + smooth * 0.9f;
            }

            prices.put(commodityId, SystemEconomyService.getGlobalPrice(commodityId) * multiplier);
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
    public void addStock(String commodityId, long num){
        stock.merge(commodityId,num,Long::sum);
    }
    public float getPrice(String commodityId) {
        return prices.getOrDefault(commodityId, 0f);
    }
    public Map<String, MutableCommodityQuantity> getAllSupply(Industry industry) {
        return allSupplyS.get(industry);
    }
    public Map<String, MutableCommodityQuantity> getAllDemand(Industry industry) {
        return allDemandS.get(industry);
    }
}
