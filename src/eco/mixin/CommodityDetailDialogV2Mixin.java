package eco.mixin;/*
package eco;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.campaign.ui.marketinfo.cdd.A;
import eco.data.PlanetMarket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(value = A.class)
public abstract class CommodityDetailDialogV2Mixin {


    @Overwrite
    public int getSortQuantity() {
        // 将 this 强转为 A，以便调用 A 的公开方法（getMarket、getCommodity、getMode）
        A self = (A)(Object)this;
        try {
            // 查找此市场对应的 PlanetMarket
            PlanetMarket planetMarket = SystemEconomyService.getPlanetMarket(self.getMarket());
            if (planetMarket != null) {
                String commodityId = self.getCommodity().getId();
                // 生产商行用我们的 supply 值，消费方行用我们的 demand 值
                int supply = planetMarket.getSupply(commodityId);
                int demand = planetMarket.getDemand(commodityId);
                if (supply > 0 || demand > 0) {
                    return supply > 0 ? supply : demand;
                }
            }
        } catch (Exception ignored) {}

        // 回退到原版行为（当我们的数据不可用时）
        CommodityOnMarketAPI commodity = self.getCommodity();
        // A.o.Ó00000 = 生产商模式，其余 = 消费方模式
        if (self.getMode() == A.o.Ó00000) {
            return commodity.getMaxSupply();
        } else {
            return commodity.getMaxDemand();
        }
    }
}*/