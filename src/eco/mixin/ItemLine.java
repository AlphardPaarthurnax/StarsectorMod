package eco.mixin;

import com.fs.graphics.util.B;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.CommodityIconCounts;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.reach.CommodityMarketData;
import com.fs.starfarer.campaign.econ.reach.MarketShareData;
import com.fs.starfarer.campaign.ui.marketinfo.CommodityTooltipFactory;
import com.fs.starfarer.campaign.ui.marketinfo.f;
import com.fs.starfarer.campaign.ui.marketinfo.i;
import com.fs.starfarer.campaign.ui.marketinfo.ooO0;
import com.fs.starfarer.renderers.O;
import com.fs.starfarer.settings.StarfarerSettings;
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
    private CommodityOnMarket øøÓO00;
    private ooO0 field_0001;
    private i O0ÔO00;

    public ItemLine(CommodityOnMarket var1) {
        this.øøÓO00 = var1;
    }

    public void sizeChanged(float var1, float var2) {
        this.clearChildren();
        super.sizeChanged(var1, var2);
        this.field_0001 = new ooO0((U)null);
        this.field_0001.setWideSpacing(true);
        this.field_0001.setMediumSpacing(true);
        f.o var3 = f.o.values()[0];
        int var4 = (int)this.øøÓO00.getAvailableStat().getModifiedValue();
        int var5 = this.øøÓO00.getMaxDemand();
        Math.min(var4, this.øøÓO00.getMaxSupply());
        CommodityMarketData var7 = this.øøÓO00.getCommodityMarketData();
        MarketShareData var8 = var7.getMarketShareData(this.øøÓO00.getMarket());
        CommodityIconCounts var9 = new CommodityIconCounts(this.øøÓO00);
        int var10 = var9.demandMetWithLocal;
        int var11 = var9.nonDemandExport;
        int var12 = var9.imports;
        int var13 = var9.extra;
        int var14 = var9.deficit;
        byte var15 = 20;
        if (var10 > var15) {
            var10 = var15;
        }

        if (var11 > var15) {
            var11 = var15;
        }

        if (var12 > var15) {
            var12 = var15;
        }

        if (var13 > var15) {
            var13 = var15;
        }

        if (var14 > var15) {
            var14 = var15;
        }

        if (var10 > 0) {
            this.field_0001.addGroup(this.øøÓO00, var10, 1.0F, f.o.values()[0], (Object)null);
        }

        if (var11 > 0) {
            this.field_0001.addGroup(this.øøÓO00, var11, 1.0F, f.o.values()[0], (Object)null);
        }

        if (var12 > 0) {
            this.field_0001.addGroup(this.øøÓO00, var12, 1.0F, f.o.Ò00000, (Object)null);
        }

        if (var13 > 0) {
            this.field_0001.addGroup(this.øøÓO00, var13, 1.0F, f.o.values()[2], (Object)null);
        }

        if (var14 > 0) {
            this.field_0001.addGroup(this.øøÓO00, var14, 1.0F, f.o.ô00000, (Object)null);
        }

        float var16 = 3.0F;
        float var17 = 10.0F;
        float var18 = this.getHeight();
        this.field_0001.autoSizeWithAdjust(this.getHeight(), this.getWidth() - var18 * 2.0F - var16 * 2.0F - var18 - var16 - var17, this.getHeight(), this.getHeight());
        float var19 = 32.0F;
        Color var20 = this.øøÓO00.getMarket().getFaction().getBaseUIColor();
        d var21 = d.createSmallInsigniaLabel(this.øøÓO00.getAvailable() + "×", Alignment.MID);
        boolean var22 = var18 < 24.0F;
        if (var22) {
            var21 = new d(this.øøÓO00.getAvailable() + "×", GameSettings.getFont(), var20, true, Alignment.MID);
            var19 = 24.0F;
        }

        var21.setColor(var20);
        setSet$int(var21.getRenderer(),true);
        var21.setSize(var19, var21.getLineHeight());
        float var23 = var21.getWidth() + var16;
        this.add(var21).inLMid(var18 + var16);
        if (var22) {
            var21.getPosition().setYAlignOffset(1.0F);
        }

        this.add(this.field_0001).inLMid(var18 + var16 + var23);
        var23 = 0.0F;
        float var24 = 3.0F;
        boolean var25 = var8.isSourceIsIllegal();
        i var26 = null;
        switch (var8.getSource()) {
            case GLOBAL:
                var26 = new i(GameSettings.getSpritePath("commodity_markers", "imports"), this.øøÓO00.getMarket().getFaction().getBaseUIColor(), var25);
                break;
            case IN_FACTION:
                var26 = new i(this.øøÓO00.getMarket().getFaction().getCrest(), (Color)null, var25);
                break;
            case LOCAL:
            case NONE:
                var26 = new i(GameSettings.getSpritePath("commodity_markers", "production"), this.øøÓO00.getMarket().getFaction().getBaseUIColor(), var25);
        }

        if (var26 != null) {
            this.add(var26).setSize(var18, var18).inBL(var23, 0.0F);
        }

        if (var7.getExportIncome(this.øøÓO00) > 0) {
            this.O0ÔO00 = new i(GameSettings.getSpritePath("commodity_markers", "exports"), this.øøÓO00.getMarket().getFaction().getBaseUIColor(), false);
            this.add(this.O0ÔO00).setSize(var18, var18).inBR(var24, 0.0F);
        }

        this.bringToTop(var21);
    }

    public CommodityOnMarket getCommodity() {
        return this.øøÓO00;
    }

    protected void renderImpl(float var1) {
        Color var2;
        if (this.glowAmount > 0.0F) {
            var2 = B.Ô00000(Color.white, this.glowAmount * 0.33F);
        }

        var2 = this.øøÓO00.getMarket().getFaction().getDarkUIColor();
        Color var3 = this.øøÓO00.getMarket().getFaction().getBaseUIColor();
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
