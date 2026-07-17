package eco.mixin;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySourceType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.ui.marketinfo.*;
import com.fs.starfarer.campaign.ui.marketinfo.cdd.CommodityDetailDialogV2;
import com.fs.starfarer.ui.Q;
import com.fs.starfarer.ui.U;
import com.fs.starfarer.ui.n;
import com.fs.starfarer.ui.oo0O;
import com.fs.starfarer.ui.voidsuper;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 创建物品项的混入（目标类：CommodityPanel，即市场中"商品"面板）
 *
 * 功能：
 * 1. 排序：supply（供应）行在前，demand（需求）行在后
 * 2. 可滚动：所有行包裹在滚动容器中，防止溢出
 * 3. 数据传递：从 market memory 读取 persistTradeData() 写入的行数据，
 *    存入 $crack_rowspec 供 ooOoMixin 渲染每行时读取
 * 4. 点击行为：点击某行打开原版 CommodityDetailDialogV2（市场份额弹窗）
 * 5. 行高预计算：根据图标栏内容预估所需行高，避免重复设大小
 */
@Mixin(CommodityPanel.class)
public abstract class CommodityPanelMixin extends voidsuper implements U, oo0O.o {
    public CommodityPanelMixin(String s) {
        super(s);
    }

    @Shadow protected MarketAPI market;
    @Shadow protected Color color;
    @Shadow private CommodityDetailDialogV2 Ö0Ôo00;
    @Shadow private com.fs.starfarer.ui.newui.L Õ0Ôo00;

    // reflect
    @Unique
    private static Field dialogState$super$interface$while;
    static{
        try {
            dialogState$super$interface$while = CommodityPanel.class.getDeclaredField("super.interface$while");
            dialogState$super$interface$while.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("CommodityPanelMixin 反射 属性 错误", e);
        }
    }
    @Inject(method = "sizeChanged", at = @At("HEAD"), cancellable = true)
    public void injectSizeChanged(float width, float height, CallbackInfo ci) {
        this.clearChildren();
        super.sizeChanged(width, height);
        List<CommodityOnMarketAPI> commodityList = CPM$getCommoditys();

        float rowGap = 3.0F;
        float margin = 10.0F;
        float rowHeight;

        float availableHeight = height - this.titleHeight - margin - rowGap;
        rowHeight = (float)(int)(availableHeight / commodityList.size());

        if (rowHeight % 2.0F == 0.0F) --rowHeight;

        rowHeight -= rowGap;
        if (rowHeight > 28.0F) rowHeight = 28.0F;


        n previousButton = null;
        boolean allowAnyColony = Global.getSettings().getBoolean("allowPriceViewAtAnyColony");
        boolean isEnabled = Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay() || allowAnyColony;

        n button;
        for(CommodityOnMarketAPI commodity : commodityList){
            ItemLine rowRenderer = new ItemLine((CommodityOnMarket)commodity);
            button = Q.o00000(rowRenderer, this);
            button.setQuickMode(false);
            button.setSize(width - margin * 2.0F, rowHeight);

            if (previousButton == null) {
                this.add(button).inTL(margin, this.getTitleHeight() + margin);
            } else {
                this.add(button).belowLeft(previousButton, rowGap);
            }

            final StandardTooltipV2Expandable tooltip = CommodityTooltipCreator.createCommodityTooltip(commodity);
            button.setTooltip(0.0F, tooltip);

            final n finalButton = button;
            tooltip.setBeforeShowing(() -> finalButton.setTooltipPositionRelativeToAnchor(-tooltip.getWidth(), -(tooltip.getHeight() - this.getHeight()), this));
            button.setEnabled(isEnabled);

            previousButton = button;
        }
        ci.cancel();
    }
    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    public void injectActionPerformed(Object source, Object target, CallbackInfo ci) {
        if (target instanceof n button) {
            CommodityOnMarket commodity = ((ItemLine)button.getPanel()).getCommodity();
            if (this.Ö0Ôo00 == null) {
                CommodityDetailDialogV2 commodityDetail = new CommodityDetailDialogV2(commodity, this.Õ0Ôo00, this.Õ0Ôo00.getDialogParentForSubDialog(), this);
                commodityDetail.show(0.3F, 0.2F);
                CPM$setDialogState();
            } else {
                this.Ö0Ôo00.switchToCommodity(commodity);
            }
        }
        ci.cancel();
    }
    @Inject(method = "selectRow", at = @At("HEAD"), cancellable = true)
    public void injectSelectRow(CommodityOnMarket commodity, CallbackInfo ci){
        for (Object child : this.getChildrenCopy()) {
            if (child instanceof n button) {
                ItemLine itemLine = (ItemLine) button.getPanel();
                if (itemLine.getCommodity().getId().equals(commodity.getId())) {
                    button.highlight();
                } else {
                    button.unhighlight();
                }
            }
        }
        ci.cancel();
    }
    @Unique
    @NotNull
    private List<CommodityOnMarketAPI> CPM$getCommoditys() {
        List<CommodityOnMarketAPI> commodityList = this.market.getCommoditiesCopy();

        commodityList.sort((commodity1, commodity2) -> {
            boolean isNoLocal1 = commodity1.getCommodityMarketData().getMarketShareData(commodity1.getMarket()).getSource() != CommoditySourceType.LOCAL;
            boolean isNoLocal2 = commodity2.getCommodityMarketData().getMarketShareData(commodity2.getMarket()).getSource() != CommoditySourceType.LOCAL;
            if (isNoLocal1 && !isNoLocal2) {
                return 1;
            } else if (isNoLocal2 && !isNoLocal1) {
                return -1;
            } else if (commodity1.getMaxSupply() == 0 && commodity2.getMaxSupply() != 0) {
                return 1;
            } else {
                return commodity2.getMaxSupply() == 0 && commodity1.getMaxSupply() != 0 ? -1 : (int) Math.signum(commodity1.getCommodity().getEconomyTier() - commodity2.getCommodity().getEconomyTier());
            }
        });

        for (CommodityOnMarketAPI commodity : new ArrayList<>(commodityList)){
            if (!commodity.isNonEcon()) {
                if (commodity.getAvailableStat().getBaseValue() <= 0.0F && commodity.getMaxDemand() <= 0 && commodity.getMaxSupply() <= 0) {
                    commodityList.remove(commodity);
                }
            } else {
                commodityList.remove(commodity);
            }
        }

        return commodityList;
    }
    @Unique
    private void CPM$setDialogState() {
        try {
            dialogState$super$interface$while.set(this, CommodityPanel.o.Ò00000);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
