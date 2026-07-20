package eco.neo;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.combat.entities.terrain.Planet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlanetEconomy {
    private final PlanetAPI planet;
    private final MarketAPI market;
    private Map<Industry, IndustryEconomy> industryEconomys = new HashMap<>();
    private Map<String, Long> stock = new HashMap<>();
    public PlanetAPI getPlanet(){
        return planet;
    }
    public MarketAPI getMarket(){
        return market;
    }
    public IndustryEconomy getIndustryEconomy(Industry industry){
        return industryEconomys.getOrDefault(industry, new IndustryEconomy(industry));
    }
    public Map<Industry, IndustryEconomy> getAllIndustryEconomy(){
        return industryEconomys;
    }
    public Long getStock(String commodityId){
        return stock.getOrDefault(commodityId, 0L);
    }
    public Map<String, Long> getAllStock(){
        return stock;
    }

    private Map<String, Integer> netSD = new HashMap<>();


    private Map<String, Integer> baseSupply = new HashMap<>();
    private Map<String, Integer> baseDemand = new HashMap<>();
    public PlanetEconomy(MarketAPI market){
        this.market = market;
        this.planet = market.getPlanetEntity();
    }
    public void updateSource(){
        baseSupply.clear();
        baseDemand.clear();
        netSD.clear();

        for (Industry industry : market.getIndustries()) {
            if (!industryEconomys.containsKey(industry)) {
                industryEconomys.put(industry, new IndustryEconomy(industry));
            }
        }
        Set<Industry> currentSet = new HashSet<>(market.getIndustries());
        industryEconomys.keySet().removeIf(ind -> !currentSet.contains(ind));
        // 同步
        for(Map.Entry<Industry, IndustryEconomy> industryEco : industryEconomys.entrySet()){
            industryEco.getValue().updateSource(getPeopleScale(market.getSize()));

            Map<String, Map<String, Integer>> base = industryEco.getValue().getReferenceSupplyDemand();
            for(Map.Entry<String, Integer> SI : base.get("s").entrySet()){
                baseSupply.merge(SI.getKey(), SI.getValue(), Integer::sum);
            }
            for(Map.Entry<String, Integer> DI : base.get("d").entrySet()){
                baseDemand.merge(DI.getKey(), DI.getValue(), Integer::sum);
            }
        }
        // 更新
        if (stock.isEmpty()){
            for(Map.Entry<Industry, IndustryEconomy> industryEco : industryEconomys.entrySet()){
                Map<String, Map<String, Integer>> reference = industryEco.getValue().getReferenceSupplyDemand();
                initStock(reference);
            }
        }
        // initStock
        Map<String, Float> effList = new HashMap<>();
        for(Map.Entry<String, Long> SL : stock.entrySet()){
            effList.put(SL.getKey(), (float) ((double) SL.getValue() / baseDemand.getOrDefault(SL.getKey(),0)));
        }
        // efflist
        for(Map.Entry<Industry, IndustryEconomy> industryEco : industryEconomys.entrySet()){
            industryEco.getValue().updateEfficiency(effList);
            industryEco.getValue().updateSupplyDemand();
            updateStock(industryEco.getValue());
        }
    }
    private void initStock(Map<String, Map<String, Integer>> reference){
        for(Map.Entry<String, Integer> SI : reference.get("s").entrySet()){
            stock.merge(SI.getKey(), (long) SI.getValue(), Long::sum);
        }
        for(Map.Entry<String, Integer> DI : reference.get("d").entrySet()){
            stock.merge(DI.getKey(), (long) DI.getValue(), Long::sum);
        }
    }
    private void updateStock(IndustryEconomy industryEconomy){
        for(Map.Entry<String, Integer> supplySI : industryEconomy.getAllSupply().entrySet()){
            stock.merge(supplySI.getKey(),(long) supplySI.getValue(),Long::sum);
        }
        for(Map.Entry<String, Integer> demandSI : industryEconomy.getAllDemand().entrySet()){
            stock.merge(demandSI.getKey(),(long) -demandSI.getValue(),Long::sum);
        }
    }
    public void updateSupplyDemand(){

    }

    private static float getPeopleScale(int size) {
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
}
