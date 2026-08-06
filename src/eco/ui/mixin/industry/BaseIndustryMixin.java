package eco.ui.mixin.industry;

import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import eco.core.IndustryEconomy;
import eco.ui.IBaseIndustryBridge;
import eco.ui.mutable.BridgedMutableCommodityQuantity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BaseIndustry.class)
public abstract class BaseIndustryMixin implements IBaseIndustryBridge {

    @Shadow protected Map<String, MutableCommodityQuantity> supply;
    @Shadow protected Map<String, MutableCommodityQuantity> demand;
    @Shadow protected MutableStat income;
    @Shadow protected MutableStat upkeep;
    @Shadow public abstract List<Pair<String, Integer>> getAllDeficit(String ... commodityIds);
    @Unique private IndustryEconomy ECON$industryEconomy;
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
        if(ECON$industryEconomy.getModSupply(id) == null) {
            cir.setReturnValue(new MutableCommodityQuantity(id));
        } else {
            cir.setReturnValue(ECON$industryEconomy.getModSupply(id));
        }
    }
    @Inject(method = "getDemand", at = @At("HEAD"), cancellable = true)
    public void injectGetDemand(String id, CallbackInfoReturnable<MutableCommodityQuantity> cir){
        if(ECON$industryEconomy == null) return;
        if(ECON$industryEconomy.getModDemand(id) == null){
            cir.setReturnValue(new MutableCommodityQuantity(id));
        } else {
            cir.setReturnValue(ECON$industryEconomy.getModDemand(id));
        }
    }
    @Inject(method = "getIncome", at = @At("HEAD"), cancellable = true)
    public void injectGetIncome(CallbackInfoReturnable<MutableStat> cir){
        if(ECON$industryEconomy == null) return;
        if(ECON$industryEconomy.getModIncome() == null) return;
        cir.setReturnValue(ECON$industryEconomy.getModIncome());
    }
    @Inject(method = "getUpkeep", at = @At("HEAD"), cancellable = true)
    public void injectGetUpkeep(CallbackInfoReturnable<MutableStat> cir){
        if(ECON$industryEconomy == null) return;
        if(ECON$industryEconomy.getModUpkeep() == null) return;
        cir.setReturnValue(ECON$industryEconomy.getModUpkeep());
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
        for(String commodityId : commodityIds) {
            result.add(new Pair<>(commodityId,ECON$industryEconomy.getDeficit(commodityId)));
        }

        cir.setReturnValue(result);
    }
}
