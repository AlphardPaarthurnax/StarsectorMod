package eco.mixin.neo.mixin.market;

import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.Market;
import com.fs.starfarer.campaign.econ.MarketDemand;
import com.fs.starfarer.campaign.econ.MarketDemandData;
import eco.core.PlanetEconomy;
import eco.mixin.neo.market.EconDataBridge;
import eco.mixin.neo.market.MarketBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(Market.class)
public abstract class MarketMixin implements MarketBridge {
    @Unique
    private PlanetEconomy ECON$planetEconomy;
    @Override
    public void ECON$dataUpdate(PlanetEconomy economy) {
        this.ECON$planetEconomy = economy;
        MarketAccessor accessor = (MarketAccessor) this;

        // 分发到本市场的每个商品（id = commodityId）
        if (accessor.getCommoditiesSource() != null) {
            for (CommodityOnMarket commodity : accessor.getCommoditiesSource()) {
                if (commodity instanceof EconDataBridge) {
                    ((EconDataBridge) commodity).ECON$dataUpdate(economy, commodity.getId());
                }
            }
        }
        // 分发到本市场的每个需求类（id = demandClass）
        MarketDemandData demandData = accessor.getDemandDataSource();
        if (demandData != null && demandData.getDemands() != null) {
            for (Map.Entry<String, MarketDemand> entry : demandData.getDemands().entrySet()) {
                if (entry.getValue() instanceof EconDataBridge) {
                    ((EconDataBridge) entry.getValue()).ECON$dataUpdate(economy, entry.getKey());
                }
            }
        }
    }
}
