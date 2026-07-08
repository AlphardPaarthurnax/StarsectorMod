package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import eco.data.PlanetMarket;
import eco.data.SystemMarket;
import eco.data.Trade;
import eco.data.TradePair;

import java.util.*;

public class SystemEconomyService implements EconomyTickListener {
    /**{@code <systemId, SystemMarket> }*/
    private static Map<StarSystemAPI, SystemMarket> systemMarkets = new HashMap<>();
    private static Map<MarketAPI,SystemEconomyData> allData = new HashMap<>();
    /** 从allMarkets到systemMarkets*/
    private static Map<StarSystemAPI, SystemMarket> getMarkets(List<MarketAPI> allMarkets) {
        Map<StarSystemAPI, SystemMarket> result = new HashMap<>();
        for (MarketAPI market : allMarkets) {
            if (!market.isInEconomy()) continue;

            StarSystemAPI system = market.getStarSystem();
            PlanetAPI planet = market.getPlanetEntity();
            FactionAPI faction = market.getFaction();
            if (system == null || planet == null) continue;

            PlanetMarket planetMarket = new PlanetMarket(system, planet, market, faction);
            planetMarket.updateSupplyAndDemand();


            result.computeIfAbsent(system, k -> new SystemMarket(system)).addPlanetMarket(planetMarket.getPlanet(), planetMarket);
        }
        for(Map.Entry<StarSystemAPI, SystemMarket> sm : result.entrySet()){
            sm.getValue().updateSupplyAndDemand();
        }
        return result;
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
    private static void collateData(){
        for (Map.Entry<StarSystemAPI, SystemMarket> smP : systemMarkets.entrySet()){
            for (Map.Entry<PlanetAPI, PlanetMarket> pmP : smP.getValue().getPlanetMarkets().entrySet()){
                allData.put(pmP.getValue().getMarket(), new SystemEconomyData(getPlanetMarket(pmP.getValue().getMarket())));
            }
        }
    }
    @Override
    public void reportEconomyTick(int iterIndex) {}
    @Override
    public void reportEconomyMonthEnd() {
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        systemMarkets = getMarkets(allMarkets);
        for (Map.Entry<StarSystemAPI, SystemMarket> systemMarketPair : systemMarkets.entrySet()) {
            systemMarketPair.getValue().matchTrade();
        }
        matchInterSystemTrade();
        collateData();
    }
    public static PlanetMarket getPlanetMarket(MarketAPI market) {
        if (market == null) return null;
        if (systemMarkets == null) return null;
        if (systemMarkets.get(market.getStarSystem()) == null) return null;
        if (systemMarkets.get(market.getStarSystem()).getPlanetMarkets() == null) return null;
        return systemMarkets.get(market.getStarSystem()).getPlanetMarkets().get(market.getPlanetEntity());
    }
    public static SystemEconomyData getSystemEconomyData(MarketAPI market){
        return allData.get(market);
    }
    public static Map<StarSystemAPI, SystemMarket> getSystemMarkets() {
        return systemMarkets;
    }
}
