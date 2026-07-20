package eco.neo;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import eco.mixin.BaseIndustryAccessor;

import java.util.*;

public class IndustryEconomy {
    private final Industry industry;
    private Map<String, Integer> supply = new HashMap<>();
    private Map<String, Integer> demand = new HashMap<>();
    private Map<String, MutableCommodityQuantity> modSupply = new HashMap<>();
    private Map<String, MutableCommodityQuantity> modDemand = new HashMap<>();
    private float efficiency = 1f;
    private Set<String> commodityIds = new HashSet<>();
    private Set<String> shortages = new HashSet<>();
    private Set<String> overloadShortages = new HashSet<>();
    private int profit = 0;
    private boolean overload = false;
    public Industry getIndustry(){
        return industry;
    }
    public int getSupply(String commodityId) {
        return supply.getOrDefault(commodityId, 0);
    }
    public Map<String, Integer> getAllSupply() {
        return supply;
    }
    public int getDemand(String commodityId) {
        return demand.getOrDefault(commodityId, 0);
    }
    public Map<String, Integer> getAllDemand() {
        return demand;
    }
    public MutableCommodityQuantity getModSupply(String commodityId) {
        return modSupply.getOrDefault(commodityId, new MutableCommodityQuantity(commodityId));
    }
    public Map<String, MutableCommodityQuantity> getAllModSupply() {
        return modSupply;
    }
    public MutableCommodityQuantity getModDemand(String commodityId) {
        return modDemand.getOrDefault(commodityId, new MutableCommodityQuantity(commodityId));
    }
    public Map<String, MutableCommodityQuantity> getAllModDemand() {
        return modDemand;
    }
    public float getEfficiency() {
        return efficiency;
    }
    public Set<String> getCommodityIds() {
        return commodityIds;
    }
    public Set<String> getShortages() {
        return shortages;
    }
    public Set<String> getOverloadShortages() {
        return overloadShortages;
    }
    public int getProfit() {
        return profit;
    }
    public boolean isOverload() {
        return overload;
    }


    private Map<String, Integer> baseSupply = new HashMap<>();
    private Map<String, Integer> baseDemand = new HashMap<>();
    protected int getBaseSupply(String commodityId){
        return baseSupply.getOrDefault(commodityId, 0);
    }
    protected Map<String, Integer> getAllBaseSupply(){
        return baseSupply;
    }
    protected int getBaseDemand(String commodityId){
        return baseDemand.getOrDefault(commodityId, 0);
    }
    protected Map<String, Integer> getAllBaseDemand(){
        return baseDemand;
    }


    private Map<String, MutableCommodityQuantity> sourceSupply = new HashMap<>();
    private Map<String, MutableCommodityQuantity> sourceDemand = new HashMap<>();
    private float peopleScale;
    public IndustryEconomy(Industry industry){
        this.industry = industry;
    }
    public void updateSource(float peopleScale){
        sourceSupply.clear();
        sourceDemand.clear();
        baseSupply.clear();
        baseDemand.clear();

        Map<String, MutableCommodityQuantity> zeroSupply = ((BaseIndustryAccessor)industry).getSupplySource();
        Map<String, MutableCommodityQuantity> zeroDemand = ((BaseIndustryAccessor)industry).getDemandSource();
        for(Map.Entry<String, MutableCommodityQuantity> zsSM : zeroSupply.entrySet()){
            if(zsSM.getValue().getQuantity().getModifiedInt() > 0){
                sourceSupply.put(zsSM.getValue().getCommodityId(), zsSM.getValue());
                baseSupply.merge(zsSM.getValue().getCommodityId(), zsSM.getValue().getQuantity().getModifiedInt(), Integer::sum);
            }
        }
        for(Map.Entry<String, MutableCommodityQuantity> zdSM : zeroDemand.entrySet()){
            if(zdSM.getValue().getQuantity().getModifiedInt() > 0){
                sourceDemand.put(zdSM.getValue().getCommodityId(), zdSM.getValue());
                baseDemand.merge(zdSM.getValue().getCommodityId(), zdSM.getValue().getQuantity().getModifiedInt(), Integer::sum);
            }
        }
        this.peopleScale = peopleScale;
    }
    public Map<String, Map<String, Integer>> getReferenceSupplyDemand(){
        Map<String, Integer> RS = new HashMap<>();
        Map<String, Integer> RD = new HashMap<>();
        for(Map.Entry<String, MutableCommodityQuantity> supplySM : sourceSupply.entrySet()){
            RS.merge(supplySM.getKey(), (int) (supplySM.getValue().getQuantity().getModifiedInt() * peopleScale), Integer::sum);
        }
        for(Map.Entry<String, MutableCommodityQuantity> demandSM : sourceDemand.entrySet()){
            RD.merge(demandSM.getKey(), (int) (demandSM.getValue().getQuantity().getModifiedInt() * peopleScale), Integer::sum);
        }
        Map<String, Map<String, Integer>> ret = new HashMap<>();
        ret.put("s",RS);
        ret.put("d",RD);
        return ret;
    }
    public void updateEfficiency(Map<String, Float> efficiencyList){
        efficiency = 1.0f;
        overload = false;
        commodityIds.clear();
        shortages.clear();
        overloadShortages.clear();
        for(Map.Entry<String, MutableCommodityQuantity> supplySM : sourceSupply.entrySet()){
            commodityIds.add(supplySM.getKey());
        }
        for(Map.Entry<String, MutableCommodityQuantity> demandSM : sourceDemand.entrySet()){
            commodityIds.add(demandSM.getKey());
            float tempEff = efficiencyList.getOrDefault(demandSM.getKey(), 0f);
            efficiency = Math.min(efficiency, tempEff);
            if(tempEff < 0.1f){
                overloadShortages.add(demandSM.getKey());
            } else if(tempEff < 1.0f){
                shortages.add(demandSM.getKey());
            }
        }
        if(efficiency < 0.1f){
            efficiency = 0.1f;
            overload = true;
        }
    }
    public void updateSupplyDemand(){
        supply.clear();
        demand.clear();
        modSupply.clear();
        modDemand.clear();

        for(Map.Entry<String, MutableCommodityQuantity> supplySM : sourceSupply.entrySet()){
            supply.merge(supplySM.getKey(), (int) (supplySM.getValue().getQuantity().getModifiedInt() * peopleScale * efficiency), Integer::sum);

            MutableCommodityQuantity supplyMCQ = supplySM.getValue();
            MutableCommodityQuantity newSupplyMCQ = new MutableCommodityQuantity(supplyMCQ.getCommodityId());
            newSupplyMCQ.getQuantity().setBaseValue(supplyMCQ.getQuantity().getBaseValue() * peopleScale * efficiency);
            for (Map.Entry<String, MutableStat.StatMod> e : supplyMCQ.getQuantity().getFlatMods().entrySet()) {
                MutableStat.StatMod m = e.getValue();
                newSupplyMCQ.getQuantity().modifyFlat(e.getKey(), m.value * peopleScale * efficiency, m.desc);
            }
            for (Map.Entry<String, MutableStat.StatMod> e : supplyMCQ.getQuantity().getMultMods().entrySet()) {
                MutableStat.StatMod m = e.getValue();
                newSupplyMCQ.getQuantity().modifyMult(e.getKey(), m.value, m.desc);
            }
            for (Map.Entry<String, MutableStat.StatMod> e : supplyMCQ.getQuantity().getPercentMods().entrySet()) {
                MutableStat.StatMod m = e.getValue();
                newSupplyMCQ.getQuantity().modifyPercent(e.getKey(), m.value, m.desc);
            }
            modSupply.put(supplyMCQ.getCommodityId(), newSupplyMCQ);
        }
        for(Map.Entry<String, MutableCommodityQuantity> demandSM : sourceDemand.entrySet()){
            demand.merge(demandSM.getKey(), (int) (demandSM.getValue().getQuantity().getModifiedInt() * peopleScale * efficiency), Integer::sum);

            MutableCommodityQuantity demandMCQ = demandSM.getValue();
            MutableCommodityQuantity newDemandMCQ = new MutableCommodityQuantity(demandMCQ.getCommodityId());
            newDemandMCQ.getQuantity().setBaseValue(demandMCQ.getQuantity().getBaseValue() * peopleScale * efficiency);
            for (Map.Entry<String, MutableStat.StatMod> e : demandMCQ.getQuantity().getFlatMods().entrySet()) {
                MutableStat.StatMod m = e.getValue();
                newDemandMCQ.getQuantity().modifyFlat(e.getKey(), m.value * peopleScale * efficiency, m.desc);
            }
            for (Map.Entry<String, MutableStat.StatMod> e : demandMCQ.getQuantity().getMultMods().entrySet()) {
                MutableStat.StatMod m = e.getValue();
                newDemandMCQ.getQuantity().modifyMult(e.getKey(), m.value, m.desc);
            }
            for (Map.Entry<String, MutableStat.StatMod> e : demandMCQ.getQuantity().getPercentMods().entrySet()) {
                MutableStat.StatMod m = e.getValue();
                newDemandMCQ.getQuantity().modifyPercent(e.getKey(), m.value, m.desc);
            }
            modDemand.put(demandMCQ.getCommodityId(), newDemandMCQ);
        }
    }
    public void updateProfit(){
        profit = 0;
    }
}
