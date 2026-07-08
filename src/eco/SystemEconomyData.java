package eco;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import eco.data.PlanetMarket;
import eco.data.SystemMarket;
import eco.data.Trade;
import eco.data.TradePair;

import java.util.*;

public class SystemEconomyData {
    PlanetMarket market;
    Map<String, CommodityEconomyData> commoditys = new HashMap<>();
    public SystemEconomyData(PlanetMarket market){
        this.market = market;
        update();
    }
    public void update(){
        commoditys.clear();
        for(String commodityId : market.getCommodityIds()){
            CommodityEconomyData commodityEconomyData = new CommodityEconomyData(market,commodityId);
            commodityEconomyData.update();
            commoditys.put(commodityId, commodityEconomyData);
        }
    }
    public CommodityEconomyData getCommodityEconomyData(String commodityId){
        return commoditys.get(commodityId);
    }

    public class CommodityEconomyData{
        String commodityId;
        PlanetMarket market;
        int netSupply, supply, demand;
        List<Pair<String,Integer>> supplyList = new ArrayList<>();
        List<Pair<String,Integer>> demandList = new ArrayList<>();
        int imports, exports;
        List<Pair<String,Integer>> importsList = new ArrayList<>();
        List<Pair<String,Integer>> exportsList = new ArrayList<>();
        int[][][] unmetData = new int[FlowType.values().length][Scope.values().length][Relation.values().length];
        public CommodityEconomyData(PlanetMarket market, String commodityId){
            this.market = market;
            this.commodityId = commodityId;
            update();
        }
        public void update(){
            supplyList.clear();
            demandList.clear();
            importsList.clear();
            exportsList.clear();
            for (int[][] unmetDatum : unmetData) {
                for (int[] ints : unmetDatum) {
                    Arrays.fill(ints, 0);
                }
            }
            this.supply = market.getSupplyRaw(commodityId);
            this.demand = market.getDemandRaw(commodityId);
            this.netSupply = supply - demand;
            for (Map.Entry<Industry, Integer> indint : market.getSupplyFactory(commodityId).entrySet()) {
                supplyList.add(new Pair<>("生产方 " + indint.getKey().getCurrentName(), indint.getValue()));
            }
            for (Map.Entry<Industry, Integer> indint : market.getDemandFactory(commodityId).entrySet()) {
                demandList.add(new Pair<>("需求方 " + indint.getKey().getCurrentName(), indint.getValue()));
            }

            List<TradePair> importTradePair = new ArrayList<>();
            List<TradePair> exportTradePair = new ArrayList<>();
            for (TradePair tradePair : market.getDemandTrade()) {
                if (Objects.equals(tradePair.getItemId(), commodityId)) {
                    importTradePair.add(tradePair);
                    imports += tradePair.getItemNum();
                }
            }
            for (TradePair tradePair : market.getSupplyTrade()) {
                if (Objects.equals(tradePair.getItemId(), commodityId)) {
                    exportTradePair.add(tradePair);
                    exports += tradePair.getItemNum();
                }
            }
            sortTradePairs(importTradePair, market.getFaction(), market.getSystem(), true);
            sortTradePairs(exportTradePair, market.getFaction(), market.getSystem(), false);
            for (TradePair tradePair : importTradePair) {
                importsList.add(new Pair<>(
                        "从 " + ((tradePair.getFromSystem() == market.getSystem()) ? "本星系" : tradePair.getFromSystem().getName()) + " "
                                + tradePair.getFromPlanet().getName()
                                + "(" + ((tradePair.getFromFaction() == market.getFaction()) ? "本势力" : tradePair.getFromFaction().getDisplayName()) + ") 购买",
                        tradePair.getItemNum()
                ));
            }
            for (TradePair tradePair : exportTradePair) {
                exportsList.add(new Pair<>(
                        "向 " + ((tradePair.getFromSystem() == market.getSystem()) ? "本星系" : tradePair.getFromSystem().getName()) + " "
                                + tradePair.getFromPlanet().getName()
                                + "(" + ((tradePair.getFromFaction() == market.getFaction()) ? "本势力" : tradePair.getFromFaction().getDisplayName()) + ") 出售",
                        tradePair.getItemNum()
                ));
            }

            for (Map.Entry<StarSystemAPI, SystemMarket> entry : SystemEconomyService.getSystemMarkets().entrySet()) {
                StarSystemAPI system = entry.getKey();
                for (Trade unmet : entry.getValue().getSupplyList()) {
                    if(Objects.equals(unmet.getItemId(), commodityId)) {
                        Scope scope;
                        Relation relation;
                        if(unmet.getSystem() == system){
                            scope = Scope.SYSTEM;
                        } else {
                            scope = Scope.GLOBAL;
                        }
                        if(unmet.getFaction() == market.getFaction()){
                            relation = Relation.FACTION;
                        } else if(!market.getFaction().isHostileTo(unmet.getFaction())){
                            relation = Relation.NON_HOSTILE;
                        } else {
                            relation = Relation.HOSTILE;
                        }
                        unmetData[FlowType.SUPPLY.ordinal()][scope.ordinal()][relation.ordinal()] += unmet.getItemNum();
                    }
                }
                for (Trade unmet : entry.getValue().getDemandList()) {
                    if(Objects.equals(unmet.getItemId(), commodityId)) {
                        Scope scope;
                        Relation relation;
                        if(unmet.getSystem() == system){
                            scope = Scope.SYSTEM;
                        } else {
                            scope = Scope.GLOBAL;
                        }
                        if(unmet.getFaction() == market.getFaction()){
                            relation = Relation.FACTION;
                        } else if(!market.getFaction().isHostileTo(unmet.getFaction())){
                            relation = Relation.NON_HOSTILE;
                        } else {
                            relation = Relation.HOSTILE;
                        }
                        unmetData[FlowType.DEMAND.ordinal()][scope.ordinal()][relation.ordinal()] += unmet.getItemNum();
                    }
                }
            }
        }

        public int getUnmetData(FlowType flow, Scope scope, Relation relation) {
            return unmetData[flow.ordinal()][scope.ordinal()][relation.ordinal()];
        }
        public int getNetSupply() {
            return netSupply;
        }
        public int getSupply() {
            return supply;
        }
        public int getDemand() {
            return demand;
        }
        public List<Pair<String, Integer>> getSupplyList() {
            return supplyList;
        }
        public List<Pair<String, Integer>> getDemandList() {
            return demandList;
        }
        public int getImports() {
            return imports;
        }
        public int getExports() {
            return exports;
        }
        public List<Pair<String, Integer>> getImportsList() {
            return importsList;
        }
        public List<Pair<String, Integer>> getExportsList() {
            return exportsList;
        }
        private static void sortTradePairs(List<TradePair> pairs, FactionAPI localFaction, StarSystemAPI localSystem, boolean isImport) {
            pairs.sort((a, b) -> {
                FactionAPI facA = isImport ? a.getFromFaction() : a.getToFaction();
                FactionAPI facB = isImport ? b.getFromFaction() : b.getToFaction();
                StarSystemAPI sysA = isImport ? a.getFromSystem() : a.getToSystem();
                StarSystemAPI sysB = isImport ? b.getFromSystem() : b.getToSystem();

                // 同势力优先
                boolean sameFacA = Objects.equals(facA, localFaction);
                boolean sameFacB = Objects.equals(facB, localFaction);
                if (sameFacA != sameFacB) return sameFacA ? -1 : 1;

                // 同星系优先
                boolean sameSysA = Objects.equals(sysA, localSystem);
                boolean sameSysB = Objects.equals(sysB, localSystem);
                if (sameSysA != sameSysB) return sameSysA ? -1 : 1;

                // 数量降序
                return Integer.compare(b.getItemNum(), a.getItemNum());
            });
        }
    }

    public static class Pair<K,V>{
        K key;
        V value;
        public Pair(K key, V value){
            this.key = key;
            this.value = value;
        }
        public K getKey() {
            return key;
        }
        public V getValue() {
            return value;
        }
        public void setKey(K key) {
            this.key = key;
        }
        public void setValue(V value) {
            this.value = value;
        }
    }
    public enum Scope {
        SYSTEM, GLOBAL
    }
    public enum Relation {
        FACTION, NON_HOSTILE, HOSTILE
    }
    public enum FlowType {
        SUPPLY, DEMAND
    }
}
