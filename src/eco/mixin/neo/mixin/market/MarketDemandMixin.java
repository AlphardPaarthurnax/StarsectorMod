package eco.mixin.neo.mixin.market;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.campaign.econ.MarketDemand;
import eco.core.PlanetEconomy;
import eco.mixin.neo.IMarketBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MarketDemand.class)
public abstract class MarketDemandMixin implements IMarketBridge {
    @Shadow private String demandClass;
    @Unique private PlanetEconomy ECON$planeteconomy;

    @Override
    public void ECON$dataUpdate(PlanetEconomy economy) {
        this.ECON$planeteconomy = economy;
    }
    @Inject(method = "getDemand", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetDemand(CallbackInfoReturnable<MutableStat> cir) {
        if (ECON$planeteconomy == null) return;
        cir.setReturnValue(ECON$planeteconomy.getDemandMS(demandClass));
    }
    @Inject(method = "getDemandValue", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetDemandValue(CallbackInfoReturnable<Float> cir) {
        if (ECON$planeteconomy == null) return;
        cir.setReturnValue(ECON$planeteconomy.getDemandMS(demandClass).getModifiedValue());
    }
}
