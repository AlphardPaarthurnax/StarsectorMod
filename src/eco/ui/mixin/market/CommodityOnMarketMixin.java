package eco.ui.mixin.market;

import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import eco.core.PlanetEconomy;
import eco.ui.IMarketBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommodityOnMarket.class)
public abstract class CommodityOnMarketMixin implements IMarketBridge {
    @Shadow private String commodityId;
    @Unique private PlanetEconomy ECON$economy;

    @Override
    public void ECON$dataUpdate(PlanetEconomy economy) {
        this.ECON$economy = economy;
    }

    // **********
    // * getter *
    // **********
    @Inject(method = "getAvailable", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetAvailable(CallbackInfoReturnable<Integer> cir) {
        if (ECON$economy == null) return;
        MutableStatWithTempMods stat = ECON$economy.getAvailable(commodityId);
        if (stat == null) return;
        cir.setReturnValue(stat.getModifiedInt());
    }
    @Inject(method = "getAvailableStat", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetAvailableStat(CallbackInfoReturnable<MutableStatWithTempMods> cir) {
        if (ECON$economy == null) return;
        MutableStatWithTempMods stat = ECON$economy.getAvailable(commodityId);
        if (stat == null) return;
        cir.setReturnValue(stat);
    }
    @Inject(method = "getStockpile", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetStockpile(CallbackInfoReturnable<Float> cir) {
        if (ECON$economy == null) return;
        cir.setReturnValue(ECON$economy.getStock(commodityId).floatValue());
    }
    @Inject(method = "getMaxSupply", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetMaxSupply(CallbackInfoReturnable<Integer> cir) {
        if (ECON$economy == null) return;
        cir.setReturnValue(ECON$economy.getActualSupply(commodityId));
    }
    @Inject(method = "getMaxDemand", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetMaxDemand(CallbackInfoReturnable<Integer> cir) {
        if (ECON$economy == null) return;
        cir.setReturnValue(ECON$economy.getBaseDemand(commodityId));
    }
    // **********
    // * setter *
    // **********
    @Inject(method = "addTradeMod", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddTradeMod(String source, float quantity, float days, CallbackInfo ci) {
        if (ECON$economy == null) return;
        ECON$economy.addStock(commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "addTradeModPlus", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddTradeModPlus(String source, float quantity, float days, CallbackInfo ci) {
        if (ECON$economy == null) return;
        ECON$economy.addStock(commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "addTradeModMinus", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddTradeModMinus(String source, float quantity, float days, CallbackInfo ci) {
        if (ECON$economy == null) return;
        ECON$economy.addStock(commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "addToStockpile", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddToStockpile(float quantity, CallbackInfo ci) {
        if (ECON$economy == null) return;
        ECON$economy.addStock(commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "removeFromStockpile", at = @At("HEAD"), cancellable = true)
    private void ECON$injectRemoveFromStockpile(float quantity, CallbackInfo ci) {
        if (ECON$economy == null) return;
        ECON$economy.addStock(commodityId, -Math.round(quantity));
        ci.cancel();
    }
}
