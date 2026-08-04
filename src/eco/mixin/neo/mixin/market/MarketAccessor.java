package eco.mixin.neo.mixin.market;

import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.Market;
import com.fs.starfarer.campaign.econ.MarketDemandData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Market.class)
public interface MarketAccessor {
    @Accessor("commodities")
    List<CommodityOnMarket> getCommoditiesSource();
    @Accessor("demandData")
    MarketDemandData getDemandDataSource();
}
