package eco.core;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import eco.mixin.neo.mixin.industry.BaseIndustryAccessor;
import eco.mixin.neo.IBaseIndustryBridge;
import eco.mutable.BridgedMutableCommodityQuantity;
import eco.mutable.BridgedMutableStat;

import java.io.Serializable;
import java.util.*;

import static eco.core.EconomyService.*;

public class IndustryEconomy implements Serializable {
    //<editor-fold desc="Inline Data">
    // ***************
    // * Inline Data *
    // ***************
    private final Industry industry;
    private Map<String, MutableCommodityQuantity> sourceSupply = new HashMap<>();
    private Map<String, MutableCommodityQuantity> sourceDemand = new HashMap<>();
    private float peopleScale;
    private float efficiency = 1f;
    private Map<String, Float> demandEfficiency = new HashMap<>();
    public Industry getIndustry(){
        return industry;
    }
    public float getPeopleScale() { return peopleScale; }
    public float getEfficiency() {
        return efficiency;
    }
    //</editor-fold>


    //<editor-fold desc="Useless Data Stream">
    // ***********************
    // * Useless Data Stream *
    // ***********************

    private Set<String> shortages = new HashSet<>();
    private Set<String> overloadShortages = new HashSet<>();
    private float profit = 0f;
    private float expectedProfit = 0f;
    public Set<String> getShortages() {
        return shortages;
    }
    public Set<String> getOverloadShortages() {
        return overloadShortages;
    }
    public float getProfit() {
        return profit;
    }
    public float getExpectedProfit() { return expectedProfit; }

        //<editor-fold desc="Useless">
        private Map<String, Integer> baseSupply = new HashMap<>();
        private Map<String, Integer> baseDemand = new HashMap<>();
        public int getBaseSupply(String commodityId){
            return baseSupply.getOrDefault(commodityId, 0);
        }
        public Map<String, Integer> getAllBaseSupply(){
            return baseSupply;
        }
        public int getBaseDemand(String commodityId){
            return baseDemand.getOrDefault(commodityId, 0);
        }
        public Map<String, Integer> getAllBaseDemand(){
            return baseDemand;
        }
        //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="UI Data Stream">
    // ***********************
    // * UI Data Stream *
    // ***********************
    private Map<String, BridgedMutableCommodityQuantity> modSupply = new HashMap<>();
    private Map<String, BridgedMutableCommodityQuantity> modDemand = new HashMap<>();
    private BridgedMutableStat modIncome = null;
    private BridgedMutableStat modUpkeep = null;
    private Map<String, Integer> deficit = new HashMap<>();
    public BridgedMutableCommodityQuantity getModSupply(String commodityId) {
        return modSupply.get(commodityId);
    }
    public Map<String, BridgedMutableCommodityQuantity> getAllModSupply() {
        return modSupply;
    }
    public BridgedMutableCommodityQuantity getModDemand(String commodityId) {
        return modDemand.get(commodityId);
    }
    public Map<String, BridgedMutableCommodityQuantity> getAllModDemand() {
        return modDemand;
    }
    public BridgedMutableStat getModIncome() { return modIncome; }
    public BridgedMutableStat getModUpkeep() { return modUpkeep; }
    public Map<String, Integer> getAllDeficit(){
        return deficit;
    }
    public int getDeficit(String commodityId) {
        return deficit.getOrDefault(commodityId, 0);
    }
    //</editor-fold>


    //<editor-fold desc="Logic Data Stream">
    // *********************
    // * Logic Data Stream *
    // *********************
    private Map<String, Integer> supply = new HashMap<>();
    private Map<String, Integer> demand = new HashMap<>();
    private Set<String> commodityIds = new HashSet<>();
    private float income = 0f;
    private float upkeep = 0f;
    private boolean overload = false;
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
    public Set<String> getCommodityIds() {
        return commodityIds;
    }
    public float getIncome() { return income; }
    public float getUpkeep() { return upkeep; }
    public boolean isOverload() {
        return overload;
    }
    //</editor-fold>


    public IndustryEconomy(Industry industry){
        this.industry = industry;
    }
    public void updateSource(float peopleScale){
        sourceSupply.clear();
        sourceDemand.clear();
        //baseSupply.clear();
        //baseDemand.clear();

        Map<String, MutableCommodityQuantity> zeroSupply = ((BaseIndustryAccessor)industry).getSupplySource();
        Map<String, MutableCommodityQuantity> zeroDemand = ((BaseIndustryAccessor)industry).getDemandSource();
        for(Map.Entry<String, MutableCommodityQuantity> zsSM : zeroSupply.entrySet()){
            if(zsSM.getValue().getQuantity().getModifiedInt() > 0){
                sourceSupply.put(zsSM.getValue().getCommodityId(), zsSM.getValue());
                //baseSupply.merge(zsSM.getValue().getCommodityId(), zsSM.getValue().getQuantity().getModifiedInt(), Integer::sum);
            }
        }
        for(Map.Entry<String, MutableCommodityQuantity> zdSM : zeroDemand.entrySet()){
            if(zdSM.getValue().getQuantity().getModifiedInt() > 0){
                sourceDemand.put(zdSM.getValue().getCommodityId(), zdSM.getValue());
                //baseDemand.merge(zdSM.getValue().getCommodityId(), zdSM.getValue().getQuantity().getModifiedInt(), Integer::sum);
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
        demandEfficiency.clear();
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

            demandEfficiency.put(demandSM.getKey(), Float.isFinite(tempEff) ? Math.max(0f, Math.min(1f, tempEff)) : 0f);
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
        deficit.clear();
        modSupply.clear();
        modDemand.clear();

        for(MutableCommodityQuantity supplyMCQ : sourceSupply.values()){
            String commodityId = supplyMCQ.getCommodityId();

            supply.merge(commodityId, (int) (supplyMCQ.getQuantity().getModifiedInt() * peopleScale * efficiency), Integer::sum);

            MutableCommodityQuantity newSupplyMCQ = copyMaskMCQ(supplyMCQ, "cc_econ_population_size", "cc_econ_efficiency");
            newSupplyMCQ.getQuantity().modifyMult("cc_econ_population_size", peopleScale, "人口规模");
            newSupplyMCQ.getQuantity().modifyMult("cc_econ_efficiency", efficiency, "生产效率");

            modSupply.put(commodityId, new BridgedMutableCommodityQuantity(commodityId, supplyMCQ::getQuantity, newSupplyMCQ::getQuantity));
        }
        for(MutableCommodityQuantity demandMCQ : sourceDemand.values()){
            String commodityId = demandMCQ.getCommodityId();

            demand.merge(commodityId, (int) (demandMCQ.getQuantity().getModifiedInt() * peopleScale * efficiency), Integer::sum);

            MutableCommodityQuantity newDemandMCQ = copyMaskMCQ(demandMCQ, "cc_econ_population_size", "cc_econ_efficiency");
            newDemandMCQ.getQuantity().modifyMult("cc_econ_population_size", peopleScale, "人口规模");
            newDemandMCQ.getQuantity().modifyMult("cc_econ_efficiency", efficiency, "生产效率");

            modDemand.put(commodityId, new BridgedMutableCommodityQuantity(commodityId, demandMCQ::getQuantity, newDemandMCQ::getQuantity));

            deficit.merge(commodityId, (int) (demandMCQ.getQuantity().getModifiedInt() * peopleScale * (1 - demandEfficiency.getOrDefault(commodityId,0f))), Integer::sum);
        }
    }
    public void updateProfit() {
        this.income = ((BaseIndustryAccessor) industry).getIncomeSource().getModifiedValue() * peopleScale * efficiency;
        this.upkeep = ((BaseIndustryAccessor) industry).getUpkeepSource().getModifiedValue() * peopleScale * efficiency * 0.75f;
        this.expectedProfit = (((BaseIndustryAccessor) industry).getIncomeSource().getModifiedValue() - ((BaseIndustryAccessor) industry).getUpkeepSource().getModifiedValue() * 0.75f) * peopleScale;
        this.profit = income - upkeep;

        MutableStat msIncome = copyMaskMCQ(((BaseIndustryAccessor) industry).getIncomeSource(), "cc_econ_population_size", "cc_econ_efficiency");
        msIncome.modifyMult("cc_econ_population_size", peopleScale, "人口规模");
        msIncome.modifyMult("cc_econ_efficiency", efficiency, "生产效率");
        modIncome = new BridgedMutableStat(() -> ((BaseIndustryAccessor)industry).getIncomeSource(), () -> msIncome);

        MutableStat msUpkeep = copyMaskMCQ(((BaseIndustryAccessor) industry).getUpkeepSource(), "cc_econ_population_size", "cc_econ_efficiency");
        msUpkeep.modifyMult("cc_econ_population_size", peopleScale * 0.75f, "人口规模");
        msUpkeep.modifyMult("cc_econ_efficiency", efficiency, "生产效率");
        modUpkeep = new BridgedMutableStat(() -> ((BaseIndustryAccessor)industry).getUpkeepSource(), () -> msUpkeep);
    }
    public void updateCollectData() {
        if (industry instanceof IBaseIndustryBridge) {
            ((IBaseIndustryBridge) industry).ECON$dataUpdate(this);
        }
    }
}
