package eco.ui.mixin.industry;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import eco.core.IndustryEconomy;
import eco.ui.IBaseIndustryBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BaseIndustry.class)
public abstract class BaseIndustryMixin implements IBaseIndustryBridge {

    @Shadow public abstract List<Pair<String, Integer>> getAllDeficit(String ... commodityIds);
    @Unique private IndustryEconomy ECON$industryEconomy;
    @Override
    public void ECON$dataUpdate(IndustryEconomy industryEconomy) {
        this.ECON$industryEconomy = industryEconomy;
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
