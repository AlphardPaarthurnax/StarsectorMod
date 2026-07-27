/*package eco.mixin;

import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.Market;
import eco.SystemEconomyService;
import eco.core.PlanetMarket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommodityOnMarket.class)
public abstract class CommodityOnMarketMixin {
    @Shadow private Market market;
    @Shadow private String commodityId;
    @Inject(method = "getStockpile", at = @At("HEAD"), cancellable = true)
    public void injectGetStockpile(CallbackInfoReturnable<Float> cir) {
        PlanetMarket planetMarket = SystemEconomyService.getPlanetMarket(market);
        if(planetMarket == null) {
            cir.setReturnValue(0f);
            return;
        }
        cir.setReturnValue((float)planetMarket.getStock(commodityId));
    }
    @Inject(method = "setStockpile", at = @At("HEAD"), cancellable = true)
    public void injectSetStockpile(float par1, CallbackInfo ci) {
        ci.cancel();
    }
    @Inject(method = "addToStockpile", at = @At("HEAD"), cancellable = true)
    public void injectAddToStockpile(float par1, CallbackInfo ci) {
        PlanetMarket planetMarket = SystemEconomyService.getPlanetMarket(market);
        if(planetMarket == null) return;
        planetMarket.addStock(commodityId, (long) par1);
        ci.cancel();
    }
    @Inject(method = "removeFromStockpile", at = @At("HEAD"), cancellable = true)
    public void injectRemoveFromStockpile(float par1, CallbackInfo ci) {
        PlanetMarket planetMarket = SystemEconomyService.getPlanetMarket(market);
        if(planetMarket == null) return;
        if(planetMarket.getStock(commodityId) < par1){
            planetMarket.addStock(commodityId, -planetMarket.getStock(commodityId));
        } else {
            planetMarket.addStock(commodityId, (long) -par1);
        }
        ci.cancel();
    }
}
*/