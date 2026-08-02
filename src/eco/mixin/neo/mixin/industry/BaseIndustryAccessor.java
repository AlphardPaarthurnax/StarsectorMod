package eco.mixin.neo.mixin.industry;


import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(BaseIndustry.class)
public interface BaseIndustryAccessor {
    @Accessor("supply")
    Map<String, MutableCommodityQuantity> getSupplySource();
    @Accessor("demand")
    Map<String, MutableCommodityQuantity> getDemandSource();
    @Accessor("income")
    MutableStat getIncomeSource();
    @Accessor("upkeep")
    MutableStat getUpkeepSource();
}
