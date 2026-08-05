package eco.mixin.neo.mixin.market;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.campaign.econ.MarketDemand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MarketDemand.class)
public interface MarketDemandAccessor {
    @Accessor("demand")
    MutableStat getDemandSource();
}
