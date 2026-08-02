package eco.mixin.neo.mixin.market;

import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import eco.core.PlanetEconomy;
import eco.mixin.neo.market.EconDataBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <p> 商品级桥接（挂在 {@code CommodityOnMarket} 上）。
 * <p> 数据由 {@link MarketMixin#ECON$dataUpdate} 分发（经 {@link EconDataBridge}）；
 * 未分发（ECON$economy 为 null，含读档后）时各 getter 回落原版实现（不 cancel 注入）。
 * <p>
 * <p> 接管语义（读取注回 / 写入接管，见开发计划）：
 * <ul>
 *   <li>读：{@code getAvailable()/getAvailableStat()/getStockpile()/getMaxSupply()/getMaxDemand()}
 *       返回 eco 计算值（maxSupply/maxDemand 为 eco 总量 sum，见 sum 化决策）；</li>
 *   <li>写：{@code addTradeMod*} / {@code addToStockpile} / {@code removeFromStockpile} 直接路由到
 *       {@code PlanetEconomy.modifyStock()}（其他 mod 的修改立即进 eco，不等月度）；</li>
 *   <li>{@code setStockpile} 刻意不接管：原版经济匹配用它写原版字段（无人读），
 *       接管会与 {@code modifyStock} 冲突。</li>
 * </ul>
 */
@Mixin(CommodityOnMarket.class)
public abstract class CommodityOnMarketMixin implements EconDataBridge {

    @Unique
    private PlanetEconomy ECON$economy;
    @Unique
    private String ECON$commodityId;

    @Override
    public void ECON$dataUpdate(PlanetEconomy economy, String commodityId) {
        this.ECON$economy = economy;
        this.ECON$commodityId = commodityId;
    }

    // -------------------- //
    // 读取：eco 计算值注回  //
    // -------------------- //
    @Inject(method = "getAvailable", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetAvailable(CallbackInfoReturnable<Integer> cir) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        cir.setReturnValue(economy.getAvailable(ECON$commodityId));
    }

    @Inject(method = "getAvailableStat", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetAvailableStat(CallbackInfoReturnable<MutableStatWithTempMods> cir) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        cir.setReturnValue(economy.getAvailableStat(ECON$commodityId));
    }

    @Inject(method = "getStockpile", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetStockpile(CallbackInfoReturnable<Float> cir) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        cir.setReturnValue(economy.getStock(ECON$commodityId).floatValue());
    }

    @Inject(method = "getMaxSupply", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetMaxSupply(CallbackInfoReturnable<Integer> cir) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        // sum 语义：eco 该商品的市场总供给（原版为单工业最大值）
        cir.setReturnValue(economy.getAllActualSupply().getOrDefault(ECON$commodityId, 0));
    }

    @Inject(method = "getMaxDemand", at = @At("HEAD"), cancellable = true)
    private void ECON$injectGetMaxDemand(CallbackInfoReturnable<Integer> cir) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        // sum 语义：eco 该商品的市场总需求
        cir.setReturnValue(economy.getAllActualDemand().getOrDefault(ECON$commodityId, 0));
    }

    // -------------------- //
    // 写入：直接进 eco     //
    // -------------------- //
    @Inject(method = "addTradeMod", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddTradeMod(String source, float quantity, float days, CallbackInfo ci) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        economy.modifyStock(ECON$commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "addTradeModPlus", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddTradeModPlus(String source, float quantity, float days, CallbackInfo ci) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        economy.modifyStock(ECON$commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "addTradeModMinus", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddTradeModMinus(String source, float quantity, float days, CallbackInfo ci) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        economy.modifyStock(ECON$commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "addToStockpile", at = @At("HEAD"), cancellable = true)
    private void ECON$injectAddToStockpile(float quantity, CallbackInfo ci) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        economy.modifyStock(ECON$commodityId, Math.round(quantity));
        ci.cancel();
    }

    @Inject(method = "removeFromStockpile", at = @At("HEAD"), cancellable = true)
    private void ECON$injectRemoveFromStockpile(float quantity, CallbackInfo ci) {
        PlanetEconomy economy = ECON$economy;
        if (economy == null) return; // 未注回 → 走原版
        economy.modifyStock(ECON$commodityId, -Math.round(quantity));
        ci.cancel();
    }
}
