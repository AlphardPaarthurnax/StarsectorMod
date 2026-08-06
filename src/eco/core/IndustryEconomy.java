package eco.core;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import eco.ui.mixin.industry.BaseIndustryAccessor;
import eco.ui.IBaseIndustryBridge;

import java.util.*;

import static eco.core.EconomyService.*;

public class IndustryEconomy {
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
    private Map<String, Integer> deficit = new HashMap<>();
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
    private Map<String, Integer> refSupply = new HashMap<>();
    private Map<String, Integer> refDemand = new HashMap<>();
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
    public Map<String, Integer> getAllRefSupply(){
        return refSupply;
    }
    public Map<String, Integer> getAllRefDemand(){
        return refDemand;
    }
    public int getRefSupply(String commodityId){
        return refSupply.getOrDefault(commodityId, 0);
    }
    public int getRefDemand(String commodityId){
        return refDemand.getOrDefault(commodityId, 0);
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

        // 经济堆清理（direct-write 前提）：cc_econ_* 修饰直接写在 vanilla 共享堆上，
        // 而 vanilla 从不清除未知 key，跨月会残留上月的 cc_econ_*。
        // 若不先 unmodify，本月 getModifiedInt() 读到的是“纯 base × 上月 peopleScale × 上月 efficiency”，
        // 再乘一次即双重缩放。因此必须在读取前剥离，拿到干净的 vanilla base。
        for (MutableCommodityQuantity mcq : zeroSupply.values()) {
            mcq.getQuantity().unmodify("cc_econ_population_size");
            mcq.getQuantity().unmodify("cc_econ_efficiency");
        }
        for (MutableCommodityQuantity mcq : zeroDemand.values()) {
            mcq.getQuantity().unmodify("cc_econ_population_size");
            mcq.getQuantity().unmodify("cc_econ_efficiency");
        }

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
    public void updateReferenceSupplyDemand(){
        refSupply.clear();
        refDemand.clear();
        for(Map.Entry<String, MutableCommodityQuantity> supplySM : sourceSupply.entrySet()){
            refSupply.merge(supplySM.getKey(), (int) (supplySM.getValue().getQuantity().getModifiedInt() * peopleScale), Integer::sum);
        }
        for(Map.Entry<String, MutableCommodityQuantity> demandSM : sourceDemand.entrySet()){
            refDemand.merge(demandSM.getKey(), (int) (demandSM.getValue().getQuantity().getModifiedInt() * peopleScale), Integer::sum);
        }
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

        for(MutableCommodityQuantity supplyMCQ : sourceSupply.values()){
            String commodityId = supplyMCQ.getCommodityId();

            // 读取前剥离 cc_econ_*（同 updateSource 的理由：防止堆上残留导致双重缩放）
            supplyMCQ.getQuantity().unmodify("cc_econ_population_size");
            supplyMCQ.getQuantity().unmodify("cc_econ_efficiency");

            supply.merge(commodityId, (int) (supplyMCQ.getQuantity().getModifiedInt() * peopleScale * efficiency), Integer::sum);

            // 直写 vanilla 堆：modifyMult 同 key 就地替换，原版 getAllSupply()/UI 直接看到缩放后的产量
            supplyMCQ.getQuantity().modifyMult("cc_econ_population_size", peopleScale, "人口规模");
            supplyMCQ.getQuantity().modifyMult("cc_econ_efficiency", efficiency, "生产效率");
        }
        for(MutableCommodityQuantity demandMCQ : sourceDemand.values()){
            String commodityId = demandMCQ.getCommodityId();

            demandMCQ.getQuantity().unmodify("cc_econ_population_size");
            demandMCQ.getQuantity().unmodify("cc_econ_efficiency");

            demand.merge(commodityId, (int) (demandMCQ.getQuantity().getModifiedInt() * peopleScale * efficiency), Integer::sum);

            demandMCQ.getQuantity().modifyMult("cc_econ_population_size", peopleScale, "人口规模");
            demandMCQ.getQuantity().modifyMult("cc_econ_efficiency", efficiency, "生产效率");

            deficit.merge(commodityId, (int) (demandMCQ.getQuantity().getModifiedInt() * peopleScale * (1 - demandEfficiency.getOrDefault(commodityId,0f))), Integer::sum);
        }
    }
    public void updateProfit() {
        MutableStat incomeSource = ((BaseIndustryAccessor) industry).getIncomeSource();
        MutableStat upkeepSource = ((BaseIndustryAccessor) industry).getUpkeepSource();

        // cc_econ_* 修饰直接写在 vanilla income/upkeep 堆上，vanilla 从不清除未知 key，
        // 读取前必须 unmodify，否则读到的 modifiedValue 含上月残留缩放，造成双重缩放
        incomeSource.unmodify("cc_econ_population_size");
        incomeSource.unmodify("cc_econ_efficiency");
        upkeepSource.unmodify("cc_econ_population_size");
        upkeepSource.unmodify("cc_econ_efficiency");

        this.income = incomeSource.getModifiedValue() * peopleScale * efficiency;
        this.upkeep = upkeepSource.getModifiedValue() * peopleScale * efficiency * 0.75f;
        this.expectedProfit = (incomeSource.getModifiedValue() - upkeepSource.getModifiedValue() * 0.75f) * peopleScale;
        this.profit = income - upkeep;

        // 直写 vanilla 堆：modifyMult 同 key 就地替换，原版 getIncome()/getUpkeep() 直接返回缩放后的数值
        incomeSource.modifyMult("cc_econ_population_size", peopleScale, "人口规模");
        incomeSource.modifyMult("cc_econ_efficiency", efficiency, "生产效率");
        upkeepSource.modifyMult("cc_econ_population_size", peopleScale * 0.75f, "人口规模");
        upkeepSource.modifyMult("cc_econ_efficiency", efficiency, "生产效率");
    }
    public void updateCollectData() {
        if (industry instanceof IBaseIndustryBridge) {
            ((IBaseIndustryBridge) industry).ECON$dataUpdate(this);
        }
    }
}
