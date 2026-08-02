package eco.mixin.neo.mixin.market;

import com.fs.starfarer.campaign.econ.MarketDemand;
import eco.core.PlanetEconomy;
import eco.mixin.neo.market.EconDataBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <p> 需求类桥接（挂在 {@code MarketDemand} 上）。
 * <p> 数据由 {@link MarketMixin#ECON$dataUpdate} 分发（经 {@link EconDataBridge}）；
 * 未分发时回落原版实现（不 cancel 注入）。
 * <p>
 * <p> 目标：{@code getDemandValue()} 返回 eco 按需求类聚合的 actualDemand。
 */
@Mixin(MarketDemand.class)
public abstract class MarketDemandMixin implements EconDataBridge {

    @Unique
    private transient PlanetEconomy ECON$economy;
    @Unique
    private transient String ECON$demandClass;

    @Override
    public void ECON$dataUpdate(PlanetEconomy economy, String demandClass) {
        this.ECON$economy = economy;
        this.ECON$demandClass = demandClass;
    }

    @Inject(method = "getDemandValue", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetDemandValue(CallbackInfoReturnable<Float> cir) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        cir.setReturnValue(economy.getDemandValue(ECON$demandClass));
    }
}
