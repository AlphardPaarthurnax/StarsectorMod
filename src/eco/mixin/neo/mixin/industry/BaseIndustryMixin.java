package eco.mixin.neo.mixin.industry;

import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import eco.core.IndustryEconomy;
import eco.mixin.neo.industry.BaseIndustryBridge;
import eco.mutable.BridgedMutableCommodityQuantity;
import eco.mutable.BridgedMutableStat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BaseIndustry.class)
public abstract class BaseIndustryMixin implements BaseIndustryBridge {

    @Shadow protected Map<String, MutableCommodityQuantity> supply;
    @Shadow protected Map<String, MutableCommodityQuantity> demand;
    @Shadow protected MutableStat income;
    @Shadow protected MutableStat upkeep;
    @Shadow protected MarketAPI market;
    @Shadow public abstract List<Pair<String, Integer>> getAllDeficit(String ... commodityIds);
    @Unique private IndustryEconomy ECON$industryEconomy;
    @Unique private BridgedMutableStat ECON$modIncome;
    @Unique private BridgedMutableStat ECON$modUpkeep;
    @Override
    public void ECON$dataUpdate(IndustryEconomy industryEconomy) {
        this.ECON$industryEconomy = industryEconomy;
    }
    @Inject(method = "getAllSupply", at = @At("HEAD"), cancellable = true)
    public void injectGetAllSupply(CallbackInfoReturnable<List<MutableCommodityQuantity>> cir){
        if(ECON$industryEconomy == null) return;
        List<MutableCommodityQuantity> result = new ArrayList<>();
        for (BridgedMutableCommodityQuantity MCQ : ECON$industryEconomy.getAllModSupply().values()){
            if (MCQ.getQuantity().getModifiedValue() > 0f){
                result.add(MCQ);
            }
        }
        cir.setReturnValue(result);
    }
    @Inject(method = "getAllDemand", at = @At("HEAD"), cancellable = true)
    public void injectGetAllDemand(CallbackInfoReturnable<List<MutableCommodityQuantity>> cir){
        if(ECON$industryEconomy == null) return;
        List<MutableCommodityQuantity> result = new ArrayList<>();
        for (BridgedMutableCommodityQuantity MCQ : ECON$industryEconomy.getAllModDemand().values()){
            if (MCQ.getQuantity().getModifiedValue() > 0f){
                result.add(MCQ);
            }
        }
        cir.setReturnValue(result);
    }
    @Inject(method = "getSupply", at = @At("HEAD"), cancellable = true)
    public void injectGetSupply(String id, CallbackInfoReturnable<MutableCommodityQuantity> cir){
        if(ECON$industryEconomy == null) return;
        cir.setReturnValue(ECON$industryEconomy.getModSupply(id));
    }
    @Inject(method = "getDemand", at = @At("HEAD"), cancellable = true)
    public void injectGetDemand(String id, CallbackInfoReturnable<MutableCommodityQuantity> cir){
        if(ECON$industryEconomy == null) return;
        cir.setReturnValue(ECON$industryEconomy.getModDemand(id));
    }
    @Inject(method = "getIncome", at = @At("HEAD"), cancellable = true)
    public void injectGetIncome(CallbackInfoReturnable<MutableStat> cir){
        if (ECON$modIncome == null) {
            ECON$modIncome = new BridgedMutableStat(
                () -> income,
                () -> ECON$industryEconomy == null ? null : ECON$industryEconomy.getModIncome()
            );
        }
        cir.setReturnValue(ECON$modIncome);
    }
    @Inject(method = "getUpkeep", at = @At("HEAD"), cancellable = true)
    public void injectGetUpkeep(CallbackInfoReturnable<MutableStat> cir){
        if (ECON$modUpkeep == null) {
            ECON$modUpkeep = new BridgedMutableStat(
                    () -> upkeep,
                    () -> ECON$industryEconomy == null ? null : ECON$industryEconomy.getModUpkeep()
            );
        }
        cir.setReturnValue(ECON$modUpkeep);
    }
    // ***********
    // * mod fix *
    // ***********
    @Inject(method = "apply", at = @At("RETURN"), cancellable = true)
    private void injectApply(boolean withIncomeUpdate, CallbackInfo ci){
        for(MutableCommodityQuantity MCQ : supply.values()){
            MCQ.getQuantity().unmodify("deficit");
        }
        for(MutableCommodityQuantity MCQ : demand.values()){
            MCQ.getQuantity().unmodify("deficit");
        }
        income.unmodify("deficit");
        upkeep.unmodify("deficit");
        ci.cancel();
    }
    @Inject(method = "getMaxDeficit", at = @At("HEAD"), cancellable = true)
    private void injectGetMaxDeficit(String[] commodityIds, CallbackInfoReturnable<Pair<String, Integer>> cir) {
        if (ECON$industryEconomy == null || commodityIds == null) return;

        List<Pair<String, Integer>> allDeficit = getAllDeficit(commodityIds);
        Pair<String, Integer> maxDeficit = new Pair<>(null, 0);
        for (Pair<String, Integer> deficit : allDeficit) {
            if (deficit.two > maxDeficit.two) {
                maxDeficit.one = deficit.one;
                maxDeficit.two = deficit.two;
            }
        }
        cir.setReturnValue(maxDeficit);
    }
    @Inject(method = "getAllDeficit([Ljava/lang/String;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void injectGetAllDeficit(String[] commodityIds, CallbackInfoReturnable<List<Pair<String, Integer>>> cir) {
        if (ECON$industryEconomy == null || commodityIds == null) return;

        List<Pair<String, Integer>> result = new ArrayList<>();
        Map<String, Integer> fullDemand = ECON$industryEconomy.getReferenceSupplyDemand().get("d");

        for (String commodityId : commodityIds) {
            int industryFullDemand = Math.max(0, fullDemand.getOrDefault(commodityId, 0));
            float fulfillment = ECON$industryEconomy.getDemandFulfillment(commodityId);
            int deficit = (int) Math.ceil(industryFullDemand * (1f - fulfillment));

            if (deficit > 0) {
                result.add(new Pair<>(commodityId, deficit));
            }
        }

        cir.setReturnValue(result);
    }
}
