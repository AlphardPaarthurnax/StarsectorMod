package eco.mixin;

import com.fs.graphics.util.B;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.ui.marketinfo.f;
import com.fs.starfarer.campaign.ui.marketinfo.i;
import com.fs.starfarer.campaign.ui.marketinfo.ooO0;
import com.fs.starfarer.renderers.O;
import com.fs.starfarer.ui.OO0O;
import com.fs.starfarer.ui.U;
import com.fs.starfarer.ui.d;
import com.fs.starfarer.ui.interfacenew;
import com.fs.starfarer.ui.m;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.Map;

/**
 * 物品项混入（目标类：ooOo，即商品列表中每一行的渲染器）
 *
 * 原版 ooOo 从 CommodityOnMarket 读取数据渲染图标栏和来源标记。
 * 我们替换其渲染逻辑，改为使用我们计算的经济数据。
 *
 * 数据流：
 *   persistTradeData() → $corecracking_econ_data_rows（持久化的行数据）
 *   CommodityPanelMixin → $crack_rowspec（存到 market memory 的每行规格）
 *   ooOoMixin → 在 sizeChanged 中读取 $crack_rowspec 来渲染
 */
@Mixin(value = com.fs.starfarer.campaign.ui.marketinfo.ooOo.class)
public abstract class ooOoMixin extends m.Oo {
    @Shadow private CommodityOnMarket øøÓO00;

    @Inject(method = "sizeChanged", at = @At("HEAD"), cancellable = true)
    public void sizeChanged(float width, float height, CallbackInfo ci) {
        //ci.cancel();
    }
}
