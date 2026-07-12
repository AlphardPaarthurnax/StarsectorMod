package eco.data;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemMarket {
    private final StarSystemAPI system;
    private Map<PlanetAPI, PlanetMarket> planetMarkets = new HashMap<>();
    // 产能/消耗 总和
    private List<Trade> supply = new ArrayList<>();
    private List<Trade> demand = new ArrayList<>();
    // 生产/进口 预订单
    private List<Trade> supplyList = new ArrayList<>();
    private List<Trade> demandList = new ArrayList<>();
    public SystemMarket(StarSystemAPI system){
        this.system = system;
    }
    public void updateSupplyAndDemand(){
        supply.clear();
        demand.clear();
        supplyList.clear();
        demandList.clear();
        for(Map.Entry<PlanetAPI, PlanetMarket> planetMarketPair : planetMarkets.entrySet()){
            PlanetMarket planetMarket = planetMarketPair.getValue();
            for (CommodityOnMarketAPI item : planetMarket.getMarket().getAllCommodities()) {
                if (item.isNonEcon()) continue;

                String itemId = item.getId();
                int supply = planetMarket.getSupply(itemId);
                int demand = planetMarket.getDemand(itemId);
                if (supply > 0) {
                    this.addSupplyList(new Trade(planetMarket, itemId, supply));
                }
                if (demand > 0) {
                    this.addDemandList(new Trade(planetMarket, itemId, demand));
                }
            }
        }
    }
    public void updateTrade() {
        for(Map.Entry<PlanetAPI, PlanetMarket> pm : planetMarkets.entrySet()){
            pm.getValue().updateTrade();
        }
    }
    public void cleanTrade() {
        supplyList.removeIf(trade -> trade.getItemNum() <= 0);
        demandList.removeIf(trade -> trade.getItemNum() <= 0);
    }
    public void matchTrade(){
        Map<String,Map<FactionAPI,List<Trade>>> supplyTrades = new HashMap<>();
        Map<String,Map<FactionAPI,List<Trade>>> demandTrades = new HashMap<>();
        for(Trade trade : supplyList){
            supplyTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
        }
        for(Trade trade : demandList){
            demandTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
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
        cleanTrade();
        for (Trade trade : getSupplyList()) {
            if (trade.getItemNum() > 0) addSupply(trade);
        }
        for (Trade trade : getDemandList()) {
            if (trade.getItemNum() > 0) addDemand(trade);
        }
    }
    private void makeDeal(Trade supplyTrade, Trade demandTrade, String itemID) {
        int itemNum = Math.min(supplyTrade.getItemNum(), demandTrade.getItemNum());
        TradePair tradePair = new TradePair(supplyTrade, demandTrade, itemID,itemNum);
        supplyTrade.addItemNum(-itemNum);
        demandTrade.addItemNum(-itemNum);
        getPlanetMarket(supplyTrade.getPlanet()).addSupplyTrade(tradePair);
        getPlanetMarket(demandTrade.getPlanet()).addDemandTrade(tradePair);
    }
    public List<Trade> getSupply() {
        return supply;
    }
    public List<Trade> getDemand() {
        return demand;
    }
    public List<Trade> getSupplyList() {
        return supplyList;
    }
    public List<Trade> getDemandList() {
        return demandList;
    }
    public Map<PlanetAPI, PlanetMarket> getPlanetMarkets(){
        return planetMarkets;
    }
    public PlanetMarket getPlanetMarket(PlanetAPI planet){
        return planetMarkets.get(planet);
    }
    public void addSupplyList(Trade trade){
        supplyList.add(trade);
    }
    public void addDemandList(Trade trade){
        demandList.add(trade);
    }
    public void addSupply(Trade trade) {
        supply.add(trade);
    }
    public void addDemand(Trade trade) {
        demand.add(trade);
    }
    public void addPlanetMarket(PlanetAPI planetAPI, PlanetMarket planetMarket){
        planetMarkets.put(planetAPI,planetMarket);
    }
}
