package eco.mixin;

import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.ui.marketinfo.f;
import com.fs.starfarer.campaign.ui.marketinfo.i;
import com.fs.starfarer.campaign.ui.marketinfo.ooO0;
import com.fs.starfarer.renderers.O;
import com.fs.starfarer.ui.*;
import com.fs.graphics.A.ooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO;
import eco.SystemEconomyData;
import eco.SystemEconomyService;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static eco.SystemEconomyService.*;

public class ItemLine extends m.Oo{

    private static final Method set$int;
    static{
        try {
            set$int = ooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO.class.getDeclaredMethod("int", boolean.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    private void outlineRender$int(ooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO obj, boolean v1){
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

        SystemEconomyData systemEconomyData = SystemEconomyService.getSystemEconomyData(commodity.getMarket());
        SystemEconomyData.CommodityEconomyData commodityEconomyData = (systemEconomyData != null) ? systemEconomyData.getCommodityEconomyData(commodity.getId()) : null;

        if(commodityEconomyData != null){
            int factionImports = formatNumberIntIcon(commodityEconomyData.getFactionImports());//本势力进口
            int otherImports = formatNumberIntIcon(commodityEconomyData.getOtherImports());//其他势力进口
            int factionExports = formatNumberIntIcon(commodityEconomyData.getFactionExports());//本势力出口
            int otherExports = formatNumberIntIcon(commodityEconomyData.getOtherExports());//其他势力出口
            int extra = formatNumberIntIcon(commodityEconomyData.getExtra());//过剩
            int deficit = formatNumberIntIcon(commodityEconomyData.getDeficit());//短缺
            int sum = factionImports + otherImports + factionExports + otherExports + extra + deficit;
            //计算图标数量

            byte limit = 20;
            if (sum > limit) {
                factionImports = (int) Math.ceil((float)factionImports / (float)sum * (float)limit);
                otherImports = (int) Math.ceil((float)otherImports / (float)sum * (float)limit);
                factionExports = (int) Math.ceil((float)factionExports / (float)sum * (float)limit);
                otherExports = (int) Math.ceil((float)otherExports / (float)sum * (float)limit);
                extra = (int) Math.ceil((float)extra / (float)sum * (float)limit);
                deficit = (int) Math.ceil((float)deficit / (float)sum * (float)limit);
            } else if (sum == 0){
                factionImports = 1;
            }
            //截断1<=图标<20

            if (factionImports > 0) {
                this.iconGroup.addGroup(this.commodity, factionImports, 1.0F, f.o.values()[0], null);
            }
            if (factionExports > 0) {
                this.iconGroup.addGroup(this.commodity, factionExports, 1.0F, f.o.values()[0], null);
            }
            if (otherImports > 0) {
                this.iconGroup.addGroup(this.commodity, otherImports, 1.0F, f.o.values()[0], null);
            }
            if (otherExports > 0) {
                this.iconGroup.addGroup(this.commodity, otherImports, 1.0F, f.o.values()[0], null);
            }
            if (extra > 0) {
                this.iconGroup.addGroup(this.commodity, extra, 1.0F, f.o.values()[2], null);
            }
            if (deficit > 0) {
                this.iconGroup.addGroup(this.commodity, deficit, 1.0F, f.o.ô00000, null);
            }
            //添加图标组

            float smallGap = 3.0F;
            float paragraphGap = 10.0F;
            float lineHeight = this.getHeight();

            float textWidth = 60.0F;
            Color uiColor = this.commodity.getMarket().getFaction().getBaseUIColor();
            d textLabel = d.createSmallInsigniaLabel("×" + formatNumberString(Math.abs(commodityEconomyData.getNetSupply())), Alignment.MID);
            //文本标签

            boolean isLowR = lineHeight < 24.0F;
            if (isLowR) {
                textLabel = new d("×" + formatNumberString(Math.abs(commodityEconomyData.getNetSupply())), GameSettings.getFont(), uiColor, true, Alignment.MID);
                textWidth = 52.0F;
            }
            //小标签

            this.iconGroup.autoSizeWithAdjust(this.getHeight(), this.getWidth() - lineHeight * 2.0F - smallGap * 2.0F - textWidth - smallGap - paragraphGap, this.getHeight(), this.getHeight());
            //自适应大小

            textLabel.setColor(uiColor);
            outlineRender$int(textLabel.getRenderer(),true);
            textLabel.setSize(textWidth, textLabel.getLineHeight());
            this.add(textLabel).inLMid(lineHeight + smallGap);
            if (isLowR) {
                textLabel.getPosition().setYAlignOffset(1.0F);
            }
            this.add(this.iconGroup).inLMid(lineHeight + smallGap + textLabel.getWidth() + smallGap);
            //添加标签

            boolean isIllegal = false;//var8.isSourceIsIllegal();
            i icon = null;
            interfacenew iconIfn = null;
            String source = commodityEconomyData.getSourceType();
            switch (source) {
                case "IN_FACTION_AND_GLOBAL":
                    String crest = this.commodity.getMarket().getFaction().getCrest();
                    String importSprite = GameSettings.getSpritePath("commodity_markers", "imports");
                    Color baseColor = this.commodity.getMarket().getFaction().getBaseUIColor();

                    iconIfn = new interfacenew();
                    iconIfn.setSize(lineHeight, lineHeight);

                    i bg = new i(crest, null, isIllegal);
                    bg.setSize(lineHeight, lineHeight);
                    iconIfn.add(bg).inTL(0f, 0f);

                    i ol = new i(importSprite, baseColor, isIllegal);
                    ol.setSize(lineHeight * 0.75f, lineHeight * 0.75f);
                    iconIfn.add(ol).inBR(0f, 0f);
                    break;
                case "GLOBAL":
                    icon = new i(GameSettings.getSpritePath("commodity_markers", "imports"), this.commodity.getMarket().getFaction().getBaseUIColor(), isIllegal);
                    break;
                case "IN_FACTION":
                    icon = new i(this.commodity.getMarket().getFaction().getCrest(), (Color)null, isIllegal);
                    break;
                case "LOCAL":
                case "NONE":
                    icon = new i(GameSettings.getSpritePath("commodity_markers", "production"), this.commodity.getMarket().getFaction().getBaseUIColor(), isIllegal);
            }
            //来源标记

            if (iconIfn != null) {
                this.add(iconIfn).setSize(lineHeight, lineHeight).inBL(0.0F, 0.0F);
            } else if (icon != null) {
                this.add(icon).setSize(lineHeight, lineHeight).inBL(0.0F, 0.0F);
            }

            if (factionExports > 0 || otherExports > 0) {
                this.exportIcon = new i(GameSettings.getSpritePath("commodity_markers", "exports"), this.commodity.getMarket().getFaction().getBaseUIColor(), false);
                this.add(this.exportIcon).setSize(lineHeight, lineHeight).inBR(3.0F, 0.0F);
            }

            this.bringToTop(textLabel);
        }else{
            d textLabel = d.createSmallInsigniaLabel("无数据", Alignment.MID);textLabel.setColor(Color.red);
            outlineRender$int(textLabel.getRenderer(),true);
            textLabel.setSize(width, textLabel.getLineHeight());
            this.add(textLabel).inLMid(height + 3.0F);
        }
    }

    public CommodityOnMarket getCommodity() {
        return this.commodity;
    }

    protected void renderImpl(float var1) {
        Color uiColor = this.commodity.getMarket().getFaction().getBaseUIColor();
        if (this.glowAmount > 0.0F) {
            OO0O position = this.getPosition();
            float x = position.getX();
            float y = position.getY();
            float width = position.getWidth();
            float height = position.getHeight();
            O.o00000(x, y, width, height, uiColor, var1 * this.glowAmount * 1.0F * 0.5F, true);
        }
        super.renderImpl(var1);
    }
}
