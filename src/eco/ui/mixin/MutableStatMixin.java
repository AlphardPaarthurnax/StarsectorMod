package eco.ui.mixin;

import com.fs.starfarer.api.combat.MutableStat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MutableStat.class)
public abstract class MutableStatMixin {
    @Inject(method = {
            "modifyFlat(Ljava/lang/String;F)V",
            "modifyPercent(Ljava/lang/String;F)V",
            "modifyMult(Ljava/lang/String;F)V"
    }, at = @At("HEAD"), cancellable = true)
    private void injectModify(String source, float value, CallbackInfo ci) {
        if ("deficit".equals(source)) {
            ci.cancel();
        }
    }
    @Inject(method = {
            "modifyFlat(Ljava/lang/String;FLjava/lang/String;)V",
            "modifyPercent(Ljava/lang/String;FLjava/lang/String;)V",
            "modifyMult(Ljava/lang/String;FLjava/lang/String;)V",
            "modifyFlatAlways",
            "modifyPercentAlways",
            "modifyMultAlways"
    }, at = @At("HEAD"), cancellable = true)
    private void injectModify(String source, float value, String description, CallbackInfo ci) {
        if ("deficit".equals(source)) {
            ci.cancel();
        }
    }
}
