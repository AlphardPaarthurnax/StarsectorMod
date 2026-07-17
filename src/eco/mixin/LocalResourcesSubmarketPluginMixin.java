package eco.mixin;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.LocalResourcesSubmarketPlugin;
import eco.SystemEconomyService;
import eco.data.PlanetMarket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalResourcesSubmarketPlugin.class)
public class LocalResourcesSubmarketPluginMixin extends BaseSubmarketPlugin {
    @Inject(method = "advance", at = @At("HEAD"), cancellable = true)
    public void injectAdvance(float amount, CallbackInfo ci) {
        ci.cancel();
    }
    @Inject(method = "updateCargoPrePlayerInteraction", at = @At("HEAD"), cancellable = true)
    public void injectUpdateCargoPrePlayerInteraction(CallbackInfo ci) {
        PlanetMarket pm = SystemEconomyService.getPlanetMarket(market);
        if (pm == null) return;

        CargoAPI cargo = getCargo();
        cargo.clear();
        for (String commodityId : pm.getCommodityIds()) {
            long amt = pm.getStock(commodityId);
            if (amt > 0) {
                cargo.addCommodity(commodityId, amt);
            }
        }
        ci.cancel();
    }
    @Inject(method = "reportPlayerMarketTransaction", at = @At("HEAD"), cancellable = true)
    public void injectReportPlayerMarketTransaction(PlayerMarketTransaction transaction, CallbackInfo ci) {
        PlanetMarket pm = SystemEconomyService.getPlanetMarket(market);
        if (pm == null) return;

        for (CargoStackAPI stack : transaction.getBought().getStacksCopy()) {
            if (stack.isCommodityStack()) {
                pm.addStock(stack.getCommodityId(), -(long) stack.getSize());
            }
        }
        for (CargoStackAPI stack : transaction.getSold().getStacksCopy()) {
            if (stack.isCommodityStack()) {
                pm.addStock(stack.getCommodityId(), (long) stack.getSize());
            }
        }
        ci.cancel();
    }
}