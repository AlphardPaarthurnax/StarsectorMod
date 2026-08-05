package eco.mixin.neo.mixin.market;

import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.campaign.econ.reach.CommodityMarketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p> 屏蔽原版经济对 available stat 的写入
 */
@Mixin(CommodityMarketData.class)
public class CommodityMarketDataMixin {

    @Unique
    private static final Set<String> ECON$VANILLA_AVAILABLE_KEYS = new HashSet<>(Arrays.asList(CommodityMarketData.KEY_LOCAL, CommodityMarketData.KEY_IMPORTS, CommodityMarketData.KEY_SHORTAGE, CommodityMarketData.KEY_LOWACCESS));

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lcom/fs/starfarer/api/combat/MutableStatWithTempMods;unmodifyFlat(Ljava/lang/String;)V"))
    private void coreCracking$blockAvailableKeyUnmodify(MutableStatWithTempMods stat, String key) {
        if (!ECON$VANILLA_AVAILABLE_KEYS.contains(key)) {
            stat.unmodifyFlat(key);
        }
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lcom/fs/starfarer/api/combat/MutableStatWithTempMods;modifyFlat(Ljava/lang/String;FLjava/lang/String;)V"))
    private void coreCracking$blockAvailableKeyModify(MutableStatWithTempMods stat, String key, float value, String desc) {
        if (!ECON$VANILLA_AVAILABLE_KEYS.contains(key)) {
            stat.modifyFlat(key, value, desc);
        }
    }
}
