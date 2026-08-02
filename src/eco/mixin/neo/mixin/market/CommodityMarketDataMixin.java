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
 * <p> 屏蔽原版经济 step 对 available stat 的 4 个原版 key 写入
 * （KEY_LOCAL 本地产量 / KEY_IMPORTS 进口 / KEY_SHORTAGE 短缺 / KEY_LOWACCESS 低可达）。
 * <p>
 * <p> {@code CommodityMarketData.<init>} 每轮原版经济迭代都会 new 并重写 available 的 4 key；
 * 阶段 2 后 {@code getAvailableStat()} 返回 eco 持有的 stat，若不禁写入会被原版值污染。
 * 屏蔽后 eco 月度自己按 eco 数据重填这 4 个 key（{@code PlanetEconomy.updateAvailableStats()}），
 * 玩家交易 / 其他 mod 的写入（eMod、自定义 key）不受影响。
 * <p>
 * <p> 仅拦截 {@code MutableStatWithTempMods} 上的 4 key 写入（javap 确认原版字节码 owner）；
 * {@code accessibilityMod}（StatBonus 的 core_base/core_hostile）与其它 stat 不受影响。
 */
@Mixin(CommodityMarketData.class)
public class CommodityMarketDataMixin {

    @Unique
    private static final Set<String> ECON$VANILLA_AVAILABLE_KEYS = new HashSet<>(Arrays.asList(
            CommodityMarketData.KEY_LOCAL, CommodityMarketData.KEY_IMPORTS,
            CommodityMarketData.KEY_SHORTAGE, CommodityMarketData.KEY_LOWACCESS));

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lcom/fs/starfarer/api/combat/MutableStatWithTempMods;unmodifyFlat(Ljava/lang/String;)V"))
    private static void coreCracking$blockAvailableKeyUnmodify(MutableStatWithTempMods stat, String key) {
        if (!ECON$VANILLA_AVAILABLE_KEYS.contains(key)) {
            stat.unmodifyFlat(key);
        }
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lcom/fs/starfarer/api/combat/MutableStatWithTempMods;modifyFlat(Ljava/lang/String;FLjava/lang/String;)V"))
    private static void coreCracking$blockAvailableKeyModify(MutableStatWithTempMods stat, String key, float value, String desc) {
        if (!ECON$VANILLA_AVAILABLE_KEYS.contains(key)) {
            stat.modifyFlat(key, value, desc);
        }
    }
}
