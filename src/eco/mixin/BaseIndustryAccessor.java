package eco.mixin;


import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
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
    @Invoker("getDescriptionOverride")
    String invokeGetDescriptionOverride();
    @Invoker("addRightAfterDescriptionSection")
    void invokeAddRightAfterDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode);
    @Invoker("addPostDescriptionSection")
    void invokeAddPostDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode);
    @Invoker("addPostUpkeepSection")
    void invokeAddPostUpkeepSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode);
    @Invoker("addPostSupplySection")
    void invokeAddPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, Industry.IndustryTooltipMode mode);
    @Invoker("hasPostDemandSection")
    boolean invokeHasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode);
    @Invoker("addPostDemandSection")
    void invokeAddPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode);
    @Invoker("addInstalledItemsSection")
    void invokeAddInstalledItemsSection(Industry.IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded);
    @Invoker("addImprovedSection")
    void invokeAddImprovedSection(Industry.IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded);
}
