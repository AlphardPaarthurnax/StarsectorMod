package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import eco.data.PlanetMarket;
import eco.data.SystemMarket;
import eco.data.Trade;
import eco.data.TradePair;

import java.util.*;
import java.util.stream.Collectors;

public class SystemEconomyService implements EconomyTickListener {
    private static int lastProcessedMonth = -1;
    private static Map<StarSystemAPI, SystemMarket> systemMarkets = new HashMap<>();
    private static Map<MarketAPI, PlanteEconomyData> allData = new HashMap<>();
    /** 存在的交易 */
    private static Map<String,Integer> globalData = new HashMap<>();
    private static Map<String, Float> globalPrices = new HashMap<>();
    /** 存在的某势力交易 */
    private static Map<String, Map<FactionAPI, Integer>> factionGlobalData = new HashMap<>();
    private static Map<String, Float> GDP = new HashMap<>();
    private static Map<String, Float> GDT = new HashMap<>();
    private static Map<String, Float> EDP = new HashMap<>();
    /** 从allMarkets到systemMarkets*/
    private static void getMarkets(List<MarketAPI> allMarkets) {
        Set<MarketAPI> marketSet = allMarkets.stream().filter(MarketAPI::isInEconomy).collect(Collectors.toSet());;
        systemMarkets.entrySet().removeIf(systemEntry -> {
            SystemMarket systemMarket = systemEntry.getValue();

            systemMarket.getPlanetMarkets().entrySet().removeIf(planetEntry ->
                    !marketSet.contains(planetEntry.getValue().getMarket())
            );

            return systemMarket.getPlanetMarkets().isEmpty();
        });
        //清除

        for (MarketAPI market : allMarkets) {
            if (!market.isInEconomy()) continue;

            StarSystemAPI system = market.getStarSystem();
            PlanetAPI planet = market.getPlanetEntity();
            FactionAPI faction = market.getFaction();
            if (system == null || planet == null) continue;

            systemMarkets.computeIfAbsent(system, i -> new SystemMarket(system))
                    .getPlanetMarkets().computeIfAbsent(planet, j -> new PlanetMarket(system, planet, market, faction))
                    .updateSupplyAndDemand();
        }
        //创建
        for(Map.Entry<StarSystemAPI, SystemMarket> sm : systemMarkets.entrySet()){
            sm.getValue().updateSupplyAndDemand();
        }
        //更新
    }
    /** 匹配跨systemMarkets订单*/
    private static void matchInterSystemTrade(){
        Map<String,Map<FactionAPI,List<Trade>>> supplyTrades = new HashMap<>();
        Map<String,Map<FactionAPI,List<Trade>>> demandTrades = new HashMap<>();
        for(Map.Entry<StarSystemAPI,SystemMarket> systemMarketPair : systemMarkets.entrySet()){
            for(Trade trade : systemMarketPair.getValue().getSupply()){
                supplyTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
            }
            for(Trade trade : systemMarketPair.getValue().getDemand()){
                demandTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
            }
        }
        //遍历每个物品
        for(Map.Entry<String,Map<FactionAPI,List<Trade>>> sftPair : supplyTrades.entrySet()){
            String itemID = sftPair.getKey();
            if (demandTrades.get(itemID) == null) continue;
            //遍历每个物品的每个势力
            for(Map.Entry<FactionAPI,List<Trade>> ftPair : sftPair.getValue().entrySet()){
                FactionAPI faction = ftPair.getKey();
                if (demandTrades.get(itemID).get(faction) == null) continue;
                //遍历每个物品的每个势力的每个供应单
                for(Trade supplyTrade : ftPair.getValue()){
                    if (supplyTrade.getItemNum() <= 0) continue;
                    //遍历该物品的同势力的每个供应单
                    for(Trade demandTrade : demandTrades.get(itemID).get(faction)){
                        if (demandTrade.getItemNum() <= 0) continue;
                        //达成交易
                        makeDeal(supplyTrade, demandTrade, itemID);
                        if (supplyTrade.getItemNum() <= 0) break;
                    }
                }
            }
        }
        //遍历每个物品
        for(Map.Entry<String,Map<FactionAPI,List<Trade>>> sftPair : supplyTrades.entrySet()){
            String itemID = sftPair.getKey();
            if (demandTrades.get(itemID) == null) continue;
            //遍历每个物品的每个势力
            for(Map.Entry<FactionAPI,List<Trade>> ftPair : sftPair.getValue().entrySet()){
                //遍历每个物品的每个势力的每个供应单
                for(Trade supplyTrade : ftPair.getValue()){
                    if (supplyTrade.getItemNum() <= 0) continue;
                    //遍历该物品的每个势力
                    for(Map.Entry<FactionAPI,List<Trade>> dftPair : demandTrades.get(itemID).entrySet()){
                        for(Trade demandTrade : dftPair.getValue()){
                            if (demandTrade.getItemNum() <= 0) continue;
                            //达成交易
                            makeDeal(supplyTrade, demandTrade, itemID);
                            if (supplyTrade.getItemNum() <= 0) break;
                        }
                        if (supplyTrade.getItemNum() <= 0) break;
                    }
                }
            }
        }
        for(Map.Entry<StarSystemAPI,SystemMarket> systemMarketPair : systemMarkets.entrySet()){
            systemMarketPair.getValue().cleanTrade();
        }
    }
    private static void makeDeal(Trade supplyTrade, Trade demandTrade, String itemID) {
        int itemNum = Math.min(supplyTrade.getItemNum(), demandTrade.getItemNum());
        TradePair tradePair = new TradePair(supplyTrade, demandTrade, itemID,itemNum);
        tradePair.setIntraSystem(false);
        supplyTrade.addItemNum(-itemNum);
        demandTrade.addItemNum(-itemNum);
        systemMarkets.get(supplyTrade.getSystem()).getPlanetMarkets().get(supplyTrade.getPlanet()).addSupplyTrade(tradePair);
        systemMarkets.get(demandTrade.getSystem()).getPlanetMarkets().get(demandTrade.getPlanet()).addDemandTrade(tradePair);
    }
    private static void calculatePrices(){
        Set<String> allCommodityIds = new HashSet<>();
        Map<String, Integer> supply = new HashMap<>();
        Map<String, Integer> demand = new HashMap<>();
        Map<String, Long> stock = new HashMap<>();
        for(Map.Entry<StarSystemAPI, SystemMarket> sm : systemMarkets.entrySet()){
            for(Map.Entry<PlanetAPI, PlanetMarket> pm : sm.getValue().getPlanetMarkets().entrySet()){
                PlanetMarket planetMarket = pm.getValue();
                allCommodityIds.addAll(planetMarket.getCommodityIds());
                for (String commodityId : planetMarket.getCommodityIds()) {
                    supply.merge(commodityId, planetMarket.getSupplyRaw(commodityId), Integer::sum);
                    demand.merge(commodityId, planetMarket.getDemandRaw(commodityId), Integer::sum);
                    stock.merge(commodityId, planetMarket.getStock(commodityId), Long::sum);
                }
            }
        }
        for(String commodityId : allCommodityIds){
            float targetStock = demand.getOrDefault(commodityId, 0) * 3f;

            float stockRatio = stock.getOrDefault(commodityId, 0L) / Math.max(targetStock, 1f);
            float stockPressure = 1f - clamp(stockRatio, 0f, 1f);

            float shortage = demand.getOrDefault(commodityId, 0) - supply.getOrDefault(commodityId, 0);
            float demandPressure = shortage / Math.max(demand.getOrDefault(commodityId, 0), 1f);
            demandPressure = clamp(demandPressure, -1f, 1f);

            float pressure = demandPressure * 0.6f + stockPressure * 0.4f;
            pressure = clamp(pressure, -1f, 1f);

            float smooth = pressure * pressure * (3f - 2f * Math.abs(pressure));
            smooth *= Math.signum(pressure);

            float multiplier;

            if (smooth >= 0f) {
                multiplier = 1f + smooth * 5f;
            } else {
                multiplier = 1f + smooth * 0.9f;
            }

            globalPrices.put(commodityId, Global.getSettings().getCommoditySpec(commodityId).getBasePrice() * multiplier);
        }
        for(Map.Entry<StarSystemAPI, SystemMarket> sm : systemMarkets.entrySet()){
            for(Map.Entry<PlanetAPI, PlanetMarket> pm : sm.getValue().getPlanetMarkets().entrySet()){
                pm.getValue().updatePrices();
            }
        }
    }
    private static void collateData(){
        allData.clear();
        globalData.clear();
        factionGlobalData.clear();
        for (Map.Entry<StarSystemAPI, SystemMarket> smP : systemMarkets.entrySet()){
            for (Map.Entry<PlanetAPI, PlanetMarket> pmP : smP.getValue().getPlanetMarkets().entrySet()){
                allData.put(pmP.getValue().getMarket(), new PlanteEconomyData(getPlanetMarket(pmP.getValue().getMarket())));
                for(String commodityId : pmP.getValue().getCommodityIds()){
                    GDP.merge(commodityId, pmP.getValue().getSupplyRaw(commodityId) * globalPrices.getOrDefault(commodityId,0f), Float::sum);
                }
                for(TradePair trade : pmP.getValue().getSupplyTrade()){
                    globalData.merge(trade.getItemId(),trade.getItemNum(),Integer::sum);
                    factionGlobalData.computeIfAbsent(trade.getItemId(), k -> new HashMap<>()).merge(trade.getFromFaction(), trade.getItemNum(), Integer::sum);
                    GDT.merge(trade.getItemId(), trade.getItemNum() * globalPrices.getOrDefault(trade.getItemId(),0f), Float::sum);
                }
            }
        }
        for(Map.Entry<String, Float> sf : GDP.entrySet()){
            EDP.put(sf.getKey(), sf.getValue() - GDT.getOrDefault(sf.getKey(), 0f));
        }
    }
    @Override
    public void reportEconomyTick(int iterIndex) {}
    @Override
    public void reportEconomyMonthEnd() {
        int currentMonth = Global.getSector().getClock().getMonth();
        if (currentMonth == lastProcessedMonth) return;
        lastProcessedMonth = currentMonth;

        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        getMarkets(allMarkets);
        for (Map.Entry<StarSystemAPI, SystemMarket> systemMarketPair : systemMarkets.entrySet()) {
            systemMarketPair.getValue().matchTrade();
        }
        matchInterSystemTrade();
        for (Map.Entry<StarSystemAPI, SystemMarket> systemMarketPair : systemMarkets.entrySet()) {
            systemMarketPair.getValue().updateTrade();
        }
        calculatePrices();
        collateData();
    }
    public static PlanetMarket getPlanetMarket(MarketAPI market) {
        if (market == null) return null;
        if (systemMarkets == null) return null;
        if (systemMarkets.get(market.getStarSystem()) == null) return null;
        if (systemMarkets.get(market.getStarSystem()).getPlanetMarkets() == null) return null;
        return systemMarkets.get(market.getStarSystem()).getPlanetMarkets().get(market.getPlanetEntity());
    }
    public static PlanteEconomyData getSystemEconomyData(MarketAPI market){
        return allData.get(market);
    }
    public static Map<StarSystemAPI, SystemMarket> getSystemMarkets() {
        return systemMarkets;
    }
    public static int getGlobalData(String commodityId) {
        return globalData.getOrDefault(commodityId,0);
    }
    public static int getFactionGlobalData(String commodityId,FactionAPI faction) {
        return factionGlobalData.getOrDefault(commodityId, Collections.emptyMap()).getOrDefault(faction,0);
    }
    public static float getGlobalPrice(String commodityId) {
        return globalPrices.getOrDefault(commodityId, 0f);
    }
    public static Float getGDP(String commodityId) {
        return GDP.getOrDefault(commodityId, 0f);
    }
    public static Float getGDT(String commodityId) {
        return GDT.getOrDefault(commodityId, 0f);
    }
    public static Float getEDP(String commodityId) {
        return EDP.getOrDefault(commodityId, 0f);
    }
    public static String formatNumberString(int n) {
        if (n >= 1E8) return String.format("%.1fB", n / 1E9f);
        if (n >= 1E5) return String.format("%.1fM", n / 1E6f);
        if (n > 1E2)     return String.format("%.1fK", n / 1E3f);
        return  String.format("%.1f", (float) n);
    }
    public static String formatNumberString(float n) {
        if (n >= 1E8) return String.format("%.2fB", n / 1E9f);
        if (n >= 1E5) return String.format("%.2fM", n / 1E6f);
        if (n > 1E2)     return String.format("%.2fK", n / 1E3f);
        return String.format("%.2f", n);
    }
    public static int formatNumberIntIcon(int n) {
        if (n >= 1E9) return (int) Math.ceil(n / 1E9f);
        if (n >= 1E6) return (int) Math.ceil(n / 1E6f);
        if (n > 1E3)     return (int) Math.ceil(n / 1E3f);
        return n;
    }
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
