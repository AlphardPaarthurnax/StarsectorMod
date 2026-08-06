package eco.ui.mixin.market;

import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.Market;
import com.fs.starfarer.campaign.econ.MarketDemand;
import com.fs.starfarer.campaign.econ.MarketDemandData;
import eco.core.PlanetEconomy;
import eco.ui.IMarketBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;


@Mixin(Market.class)
public abstract class MarketMixin implements IMarketBridge {
    @Unique private PlanetEconomy ECON$planetEconomy;
    @Override
    public void ECON$dataUpdate(PlanetEconomy economy) {
        this.ECON$planetEconomy = economy;

        // CommodityOnMarket
        if (((MarketAccessor) this).getCommoditiesSource() != null) {
            for (CommodityOnMarket commodity : ((MarketAccessor) this).getCommoditiesSource()) {
                if (commodity instanceof IMarketBridge commodityBri) {
                    commodityBri.ECON$dataUpdate(economy);
                }
            }
        }
        // MarketDemand
        MarketDemandData marketDemandData = ((MarketAccessor) this).getDemandDataSource();
        if (marketDemandData != null && marketDemandData.getDemands() != null) {
            for (MarketDemand marketDemand : marketDemandData.getDemands().values()) {
                if (marketDemand instanceof IMarketBridge marketDemandBri) {
                    marketDemandBri.ECON$dataUpdate(economy);
                }
            }
        }
    }
}
