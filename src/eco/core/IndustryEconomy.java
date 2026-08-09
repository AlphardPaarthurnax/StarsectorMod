package eco.core;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import eco.ui.mixin.industry.BaseIndustryAccessor;
import eco.ui.IBaseIndustryBridge;

import java.util.*;

public class IndustryEconomy {
    //<editor-fold desc="Inline Data">
    // ***************
    // * Inline Data *
    // ***************
    private final Industry industry;
    private Map<String, MutableCommodityQuantity> supply = new HashMap<>();
    private Map<String, MutableCommodityQuantity> demand = new HashMap<>();
    private float peopleScale;
    private float efficiency = 1f;
    private Map<String, Float> demandEfficiency = new HashMap<>();

    public Industry getIndustry() {
        return industry;
    }

    public float getPeopleScale() {
        return peopleScale;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public Map<String, MutableCommodityQuantity> getSourceSupply() {
        return supply;
    }

    public Map<String, MutableCommodityQuantity> getSourceDemand() {
        return demand;
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

    public float getExpectedProfit() {
        return expectedProfit;
    }
    //</editor-fold>

    //<editor-fold desc="UI Data Stream">
    // ***********************
    // * UI Data Stream *
    // ***********************
    private Map<String, Integer> deficit = new HashMap<>();

    public Map<String, Integer> getAllDeficit() {
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
    private Map<String, Integer> effSupply = new HashMap<>();
    private Map<String, Integer> effDemand = new HashMap<>();
    private Map<String, Integer> refSupply = new HashMap<>();
    private Map<String, Integer> refDemand = new HashMap<>();
    private Set<String> commodityIds = new HashSet<>();
    private float income = 0f;
    private float upkeep = 0f;
    private boolean overload = false;

    public int getSupply(String commodityId) {
        return effSupply.getOrDefault(commodityId, 0);
    }

    public Map<String, Integer> getAllSupply() {
        return effSupply;
    }

    public int getDemand(String commodityId) {
        return effDemand.getOrDefault(commodityId, 0);
    }

    public Map<String, Integer> getAllDemand() {
        return effDemand;
    }

    public Set<String> getCommodityIds() {
        return commodityIds;
    }

    public float getIncome() {
        return income;
    }

    public float getUpkeep() {
        return upkeep;
    }

    public boolean isOverload() {
        return overload;
    }

    public Map<String, Integer> getAllRefSupply() {
        return refSupply;
    }

    public Map<String, Integer> getAllRefDemand() {
        return refDemand;
    }

    public int getRefSupply(String commodityId) {
        return refSupply.getOrDefault(commodityId, 0);
    }

    public int getRefDemand(String commodityId) {
        return refDemand.getOrDefault(commodityId, 0);
    }
    //</editor-fold>

    public IndustryEconomy(Industry industry) {
        this.industry = industry;
    }

    /** Vanilla -> [peopleScale supply demand refSupply refDemand] */
    public void preUpdate(float peopleScale) {
        this.peopleScale = peopleScale;
        supply = ((BaseIndustryAccessor) industry).getSupplySource();
        demand = ((BaseIndustryAccessor) industry).getDemandSource();

        refSupply.clear();
        refDemand.clear();

        for (MutableCommodityQuantity mcq : supply.values()) {
            if (mcq.getQuantity().getFlatStatMod("cc_debug_monthtimer") == null) {
                mcq.getQuantity().modifyFlat("cc_debug_monthtimer", -1);
            }
            if (mcq.getQuantity().getFlatStatMod("cc_debug_monthtimer").getValue() != GlobalEconomy.getInstance().getMonth() && mcq.getQuantity().getFlatStatMod("cc_debug_inupdate") == null) {
                mcq.getQuantity().unmodify("cc_econ_population_size");
                mcq.getQuantity().unmodify("cc_econ_efficiency");
                mcq.getQuantity().unmodify("cc_debug_monthtimer");
                mcq.getQuantity().unmodify("cc_debug_monthtimer_sub");

                mcq.getQuantity().modifyFlat("cc_debug_inupdate", 1);
                mcq.getQuantity().modifyFlat("cc_debug_inupdate_sub", -1);
                mcq.getQuantity().modifyFlat("cc_debug_monthtimer", GlobalEconomy.getInstance().getMonth());
                mcq.getQuantity().modifyFlat("cc_debug_monthtimer_sub", -GlobalEconomy.getInstance().getMonth());

                mcq.getQuantity().modifyMult("cc_econ_population_size", peopleScale, "人口规模");
            }

            refSupply.merge(mcq.getCommodityId(), mcq.getQuantity().getModifiedInt(), Integer::sum);
        }
        for (MutableCommodityQuantity mcq : demand.values()) {
            if (mcq.getQuantity().getFlatStatMod("cc_debug_monthtimer") == null) {
                mcq.getQuantity().modifyFlat("cc_debug_monthtimer", -1);
            }
            if (mcq.getQuantity().getFlatStatMod("cc_debug_monthtimer").getValue() != GlobalEconomy.getInstance().getMonth() && mcq.getQuantity().getFlatStatMod("cc_debug_inupdate") == null) {
                mcq.getQuantity().unmodify("cc_econ_population_size");
                mcq.getQuantity().unmodify("cc_econ_efficiency");
                mcq.getQuantity().unmodify("cc_debug_monthtimer");
                mcq.getQuantity().unmodify("cc_debug_monthtimer_sub");

                mcq.getQuantity().modifyFlat("cc_debug_inupdate", 1);
                mcq.getQuantity().modifyFlat("cc_debug_inupdate_sub", -1);
                mcq.getQuantity().modifyFlat("cc_debug_monthtimer", GlobalEconomy.getInstance().getMonth());
                mcq.getQuantity().modifyFlat("cc_debug_monthtimer_sub", -GlobalEconomy.getInstance().getMonth());

                mcq.getQuantity().modifyMult("cc_econ_population_size", peopleScale, "人口规模");
            }

            refDemand.merge(mcq.getCommodityId(), mcq.getQuantity().getModifiedInt(), Integer::sum);
        }
    }
    /**<p>planetEconomy -> [efficiency overload shortages overloadShortages]
     * <p>[efficiency supply demand] -> [supply demand effSupply effDemand deficit]*/
    public void Update(PlanetEconomy planetEconomy) {
        efficiency = 1.0f;
        overload = false;
        shortages.clear();
        overloadShortages.clear();

        effSupply.clear();
        effDemand.clear();
        deficit.clear();

        // efficiency
        for (MutableCommodityQuantity demandMCQ : demand.values()) {
            if (demandMCQ.getQuantity().getModifiedInt() <= 0) continue;

            float commodityEff = Math.max(0f, Math.min(1f, planetEconomy.getEfficiency(demandMCQ.getCommodityId())));

            efficiency = Math.min(efficiency, commodityEff);

            if (commodityEff < 0.1f) {
                overloadShortages.add(demandMCQ.getCommodityId());
            } else if (commodityEff < 1.0f) {
                shortages.add(demandMCQ.getCommodityId());
            }
        }
        if (efficiency < 0.1f) {
            efficiency = 0.1f;
            overload = true;
        }

        // supply & demand
        for (MutableCommodityQuantity mcq : supply.values()) {
            if (mcq.getQuantity().getFlatStatMod("cc_debug_inupdate").getValue() == 1 && mcq.getQuantity().getModifiedInt() > 0) {
                mcq.getQuantity().modifyMult("cc_econ_efficiency", efficiency, "生产效率");

                effSupply.merge(mcq.getCommodityId(), mcq.getQuantity().getModifiedInt(), Integer::sum);

                mcq.getQuantity().unmodify("cc_debug_inupdate");
                mcq.getQuantity().unmodify("cc_debug_inupdate_sub");
            }
        }
        for (MutableCommodityQuantity mcq : demand.values()) {
            if (mcq.getQuantity().getFlatStatMod("cc_debug_inupdate").getValue() == 1 && mcq.getQuantity().getModifiedInt() > 0) {
                mcq.getQuantity().modifyMult("cc_econ_efficiency", efficiency, "生产效率");

                effDemand.merge(mcq.getCommodityId(), mcq.getQuantity().getModifiedInt(), Integer::sum);

                mcq.getQuantity().unmodify("cc_debug_inupdate");
                mcq.getQuantity().unmodify("cc_debug_inupdate_sub");
            }
        }
        for (String commodity : effDemand.keySet()) {
            deficit.merge(commodity, refDemand.getOrDefault(commodity, 0) - effDemand.getOrDefault(commodity, 0), Integer::sum);
        }
    }
    /**<p> Vanilla -> Vanilla
     * <p> Vanilla -> [income upkeep profit expectedProfit]*/
    public void proUpdate() {
        MutableStat incomeSource = ((BaseIndustryAccessor) industry).getIncomeSource();
        MutableStat upkeepSource = ((BaseIndustryAccessor) industry).getUpkeepSource();

        if (incomeSource.getFlatStatMod("cc_debug_monthtimer") == null)
            incomeSource.modifyFlat("cc_debug_monthtimer", -1);
        if (upkeepSource.getFlatStatMod("cc_debug_monthtimer") == null)
            upkeepSource.modifyFlat("cc_debug_monthtimer", -1);

        if (incomeSource.getFlatStatMod("cc_debug_monthtimer").getValue() != GlobalEconomy.getInstance().getMonth()) {
            incomeSource.unmodify("cc_econ_population_size");
            incomeSource.unmodify("cc_econ_efficiency");
            incomeSource.unmodify("cc_debug_monthtimer");
            incomeSource.unmodify("cc_debug_monthtimer_sub");

            incomeSource.modifyFlat("cc_debug_monthtimer", GlobalEconomy.getInstance().getMonth());
            incomeSource.modifyFlat("cc_debug_monthtimer_sub", -GlobalEconomy.getInstance().getMonth());

            incomeSource.modifyMult("cc_econ_population_size", peopleScale, "人口规模");
            incomeSource.modifyMult("cc_econ_efficiency", efficiency, "生产效率");
        }
        if (upkeepSource.getFlatStatMod("cc_debug_monthtimer").getValue() != GlobalEconomy.getInstance().getMonth()) {
            upkeepSource.unmodify("cc_econ_population_size");
            upkeepSource.unmodify("cc_econ_efficiency");
            upkeepSource.unmodify("cc_debug_monthtimer");
            upkeepSource.unmodify("cc_debug_monthtimer_sub");

            upkeepSource.modifyFlat("cc_debug_monthtimer", GlobalEconomy.getInstance().getMonth());
            upkeepSource.modifyFlat("cc_debug_monthtimer_sub", -GlobalEconomy.getInstance().getMonth());

            upkeepSource.modifyMult("cc_econ_population_size", peopleScale * 0.75f, "人口规模");
            upkeepSource.modifyMult("cc_econ_efficiency", efficiency, "生产效率");
        }

        this.income = incomeSource.getModifiedValue();
        this.upkeep = upkeepSource.getModifiedValue();
        this.profit = income - upkeep;
        this.expectedProfit = profit / efficiency;

        if (industry instanceof IBaseIndustryBridge) {
            ((IBaseIndustryBridge) industry).ECON$dataUpdate(this);
        }
    }
}
