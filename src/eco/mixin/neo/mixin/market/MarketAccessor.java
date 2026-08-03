package eco.mixin.neo.mixin.market;

import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.Market;
import com.fs.starfarer.campaign.econ.MarketDemandData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * 暴露 {@code Market} 的私有字段与方法给经济系统（数据分发、刷新入口）。
 * 字段名以反编译源码（DoNotObfuscate 类，未混淆）为准。
 */
@Mixin(Market.class)
public interface MarketAccessor {
    @Accessor("commodities")
    List<CommodityOnMarket> getCommoditiesSource();

    @Accessor("demandData")
    MarketDemandData getDemandDataSource();

    /**
     * 原版 {@code Market.updatePrices()}：遍历商品刷新 PriceCalculator。
     * 经济系统月度注回后调用，让价格链底层与注入值对齐。
     */
    @Invoker("updatePrices")
    void invokeUpdatePrices();
}
