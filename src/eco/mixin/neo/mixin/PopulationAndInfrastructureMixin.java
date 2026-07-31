package eco.mixin.neo.mixin;

import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PopulationAndInfrastructure.class)
public class PopulationAndInfrastructureMixin {

    @Redirect(method = "modifyStability", at = @At(value = "INVOKE", target = "Lcom/fs/starfarer/api/impl/campaign/econ/impl/PopulationAndInfrastructure;getIncomeStabilityMult(F)F"))
    private static float redirectIncomeStabilityMultiplier(float stability){
        return 1f;
    }
}
