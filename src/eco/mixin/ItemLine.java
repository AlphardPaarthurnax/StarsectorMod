package eco.mixin;

import com.fs.graphics.util.B;
import com.fs.starfarer.api.impl.campaign.econ.CommodityIconCounts;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.reach.CommodityMarketData;
import com.fs.starfarer.campaign.econ.reach.MarketShareData;
import com.fs.starfarer.campaign.ui.marketinfo.f;
import com.fs.starfarer.campaign.ui.marketinfo.i;
import com.fs.starfarer.campaign.ui.marketinfo.ooO0;
import com.fs.starfarer.renderers.O;
import com.fs.starfarer.ui.*;
import com.fs.graphics.A.ooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemLine extends m.Oo{

    private static final Method set$int;
    static{
        try {
            set$int = ooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO.class.getDeclaredMethod("int", boolean.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    private void setSet$int(ooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO obj, boolean v1){
        try{
            set$int.invoke(obj, v1);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private final CommodityOnMarket commodity;
    private ooO0 iconGroup;
    private i exportIcon;

    public ItemLine(CommodityOnMarket commodity) {
        this.commodity = commodity;
    }

    public void sizeChanged(float width, float height) {
        this.clearChildren();
        super.sizeChanged(width, height);
        
        this.iconGroup = new ooO0(null);
        this.iconGroup.setWideSpacing(true);
        this.iconGroup.setMediumSpacing(true);

        f.o var3 = f.o.values()[0];
        int var4 = (int)this.commodity.getAvailableStat().getModifiedValue();//库存
        int var5 = this.commodity.getMaxDemand();//需求
        Math.min(var4, this.commodity.getMaxSupply());
        CommodityMarketData var7 = this.commodity.getCommodityMarketData();//全局数据
        MarketShareData var8 = var7.getMarketShareData(this.commodity.getMarket());//本地数据

        CommodityIconCounts var9 = new CommodityIconCounts(this.commodity);
        int var10 = var9.demandMetWithLocal;//满足本地
        int var11 = var9.nonDemandExport;//纯出口
        int var12 = var9.imports;//进口
        int var13 = var9.extra;//过剩
        int var14 = var9.deficit;//短缺
        //计算图标数量

        byte var15 = 20;
        if (var10 > var15) { var10 = var15; }
        if (var11 > var15) { var11 = var15; }
        if (var12 > var15) { var12 = var15; }
        if (var13 > var15) { var13 = var15; }
        if (var14 > var15) { var14 = var15; }
        //截断图标<20

        if (var10 > 0) {
            this.iconGroup.addGroup(this.commodity, var10, 1.0F, f.o.values()[0], (Object)null);
        }
        if (var11 > 0) {
            this.iconGroup.addGroup(this.commodity, var11, 1.0F, f.o.values()[0], (Object)null);
        }
        if (var12 > 0) {
            this.iconGroup.addGroup(this.commodity, var12, 1.0F, f.o.Ò00000, (Object)null);
        }
        if (var13 > 0) {
            this.iconGroup.addGroup(this.commodity, var13, 1.0F, f.o.values()[2], (Object)null);
        }
        if (var14 > 0) {
            this.iconGroup.addGroup(this.commodity, var14, 1.0F, f.o.ô00000, (Object)null);
        }
        //添加图标组

        float var16 = 3.0F;
        float var17 = 10.0F;
        float var18 = this.getHeight();
        this.iconGroup.autoSizeWithAdjust(this.getHeight(), this.getWidth() - var18 * 2.0F - var16 * 2.0F - var18 - var16 - var17, this.getHeight(), this.getHeight());
        //自适应大小

        float var19 = 32.0F;
        Color var20 = this.commodity.getMarket().getFaction().getBaseUIColor();
        d var21 = d.createSmallInsigniaLabel(this.commodity.getAvailable() + "×", Alignment.MID);
        //文本标签

        boolean var22 = var18 < 24.0F;
        if (var22) {
            var21 = new d(this.commodity.getAvailable() + "×", GameSettings.getFont(), var20, true, Alignment.MID);
            var19 = 24.0F;
        }
        //小标签

        var21.setColor(var20);
        setSet$int(var21.getRenderer(),true);
        var21.setSize(var19, var21.getLineHeight());
        float var23 = var21.getWidth() + var16;
        this.add(var21).inLMid(var18 + var16);
        if (var22) {
            var21.getPosition().setYAlignOffset(1.0F);
        }
        this.add(this.iconGroup).inLMid(var18 + var16 + var23);
        //添加标签

        var23 = 0.0F;
        float var24 = 3.0F;

        boolean var25 = var8.isSourceIsIllegal();
        i var26 = null;
        switch (var8.getSource()) {
            case GLOBAL:
                var26 = new i(GameSettings.getSpritePath("commodity_markers", "imports"), this.commodity.getMarket().getFaction().getBaseUIColor(), var25);
                break;
            case IN_FACTION:
                var26 = new i(this.commodity.getMarket().getFaction().getCrest(), (Color)null, var25);
                break;
            case LOCAL:
            case NONE:
                var26 = new i(GameSettings.getSpritePath("commodity_markers", "production"), this.commodity.getMarket().getFaction().getBaseUIColor(), var25);
        }
        //来源标记

        if (var26 != null) {
            this.add(var26).setSize(var18, var18).inBL(var23, 0.0F);
        }

        if (var7.getExportIncome(this.commodity) > 0) {
            this.exportIcon = new i(GameSettings.getSpritePath("commodity_markers", "exports"), this.commodity.getMarket().getFaction().getBaseUIColor(), false);
            this.add(this.exportIcon).setSize(var18, var18).inBR(var24, 0.0F);
        }

        this.bringToTop(var21);
    }

    public CommodityOnMarket getCommodity() {
        return this.commodity;
    }

    protected void renderImpl(float var1) {
        Color var2;
        if (this.glowAmount > 0.0F) {
            var2 = B.Ô00000(Color.white, this.glowAmount * 0.33F);
        }

        var2 = this.commodity.getMarket().getFaction().getDarkUIColor();
        Color var3 = this.commodity.getMarket().getFaction().getBaseUIColor();
        if (this.isDisabled) {
            ;
        }

        if (this.glowAmount > 0.0F) {
            OO0O var5 = this.getPosition();
            float var6 = var5.getX();
            float var7 = var5.getY();
            float var8 = var5.getWidth();
            float var9 = var5.getHeight();
            O.o00000(var6, var7, var8, var9, var3, var1 * this.glowAmount * 1.0F * 0.5F, true);
        }

        super.renderImpl(var1);
    }
}
