package eco.mixin.neo.mixin.industry;

import com.fs.starfarer.campaign.ui.marketinfo.intnew;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(intnew.class)
public abstract class intnewMixin {

    @ModifyArg(method = "sizeChanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/fs/starfarer/campaign/ui/marketinfo/ooO0;addGroup(Lcom/fs/starfarer/api/campaign/econ/CommodityOnMarketAPI;IFLcom/fs/starfarer/campaign/ui/marketinfo/f$o;Ljava/lang/Object;)V",
                    ordinal = 0
            ),
            index = 1
    )
    private int modifyArgAddGroup(int amount) {
        return amount > 0 ? 1 : 0;
    }
}
