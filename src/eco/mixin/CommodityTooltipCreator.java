package eco.mixin;

import com.fs.starfarer.O0OO;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.ui.marketinfo.CommodityTooltipFactory;
import com.fs.starfarer.campaign.ui.marketinfo.i;
import com.fs.starfarer.loading.SpecStore;
import com.fs.starfarer.ui.d;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import com.fs.starfarer.ui.interfacenew;
import eco.SystemEconomyService;
import eco.data.PlanetMarket;
import eco.data.TradePair;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommodityTooltipCreator {
    private static final Method createIconLine$super1;
    private static final Method createIconLine$super2;
    static{
        try {
            createIconLine$super1 = CommodityTooltipFactory.class.getDeclaredMethod("super", String.class, Color.class, boolean.class, String.class, float.class, float.class);
            createIconLine$super2 = CommodityTooltipFactory.class.getDeclaredMethod("super", CommodityOnMarketAPI.class, com.fs.starfarer.campaign.ui.marketinfo.f.o.class, String.class, float.class, float.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    private static interfacenew createIconLine(String iconPath, Color iconColor, boolean isSimple, String description, float width, float height){
        try{
            return (interfacenew) createIconLine$super1.invoke(null, iconPath,iconColor,isSimple,description,width,height);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private static interfacenew createIconLine(CommodityOnMarketAPI commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o state, String description, float width, float height){
        try{
            return (interfacenew) createIconLine$super2.invoke(null,commodity, state, description, width, height);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static interfacenew createIconLine(String backgroundIconPath, String overlayIconPath, Color iconColor, boolean isSimple, String description, float width, float height) {
        interfacenew iconLine = new interfacenew();
        iconLine.setSize(width, height);

        interfacenew iconGroup = new interfacenew();
        iconGroup.setSize(height, height);
        i bgSprite = new i(backgroundIconPath, iconColor, isSimple);
        bgSprite.setSize(height, height);
        i olSprite = new i(overlayIconPath, iconColor, isSimple);
        olSprite.setSize(height * 0.75f, height * 0.75f);
        iconGroup.add(bgSprite).inLMid(0.0F);
        iconGroup.add(olSprite).inBR(0f, 0f);


        float var8 = 10.0F;
        d text = d.create(description, GameSettings.getFont());
        text.autoSizeToWidth(width - height - var8);

        iconLine.add(iconGroup).inLMid(0.0F);

        iconLine.add(text).rightOfMid(iconGroup, var8);

        float var10 = -height / 2.0F + text.getLineHeight() / 2.0F;
        iconLine.setSize(width, Math.max(height, text.getHeight() - var10 * 2.0F) + var8);
        return iconLine;
    }
    private static void sortTradePairs(ArrayList<TradePair> pairs, FactionAPI localFaction, StarSystemAPI localSystem, boolean isImport) {
        pairs.sort((a, b) -> {
            FactionAPI facA = isImport ? a.getFromFaction() : a.getToFaction();
            FactionAPI facB = isImport ? b.getFromFaction() : b.getToFaction();
            StarSystemAPI sysA = isImport ? a.getFromSystem() : a.getToSystem();
            StarSystemAPI sysB = isImport ? b.getFromSystem() : b.getToSystem();

            // 同势力优先
            boolean sameFacA = Objects.equals(facA, localFaction);
            boolean sameFacB = Objects.equals(facB, localFaction);
            if (sameFacA != sameFacB) return sameFacA ? -1 : 1;

            // 同星系优先
            boolean sameSysA = Objects.equals(sysA, localSystem);
            boolean sameSysB = Objects.equals(sysB, localSystem);
            if (sameSysA != sameSysB) return sameSysA ? -1 : 1;

            // 数量降序
            return Integer.compare(b.getItemNum(), a.getItemNum());
        });
    }
    private static String formatDemandNumber(int n) {
        if (n >= 100000000) return String.format("%.1f B", n / 1000000000f);
        if (n >= 100000) return String.format("%.1f M", n / 1000000f);
        if (n > 100)     return String.format("%.1f K", n / 1000f);
        return String.valueOf(n);
    }
    public static StandardTooltipV2Expandable createCommodityTooltip(CommodityOnMarketAPI commodity) {
        return new StandardTooltipV2Expandable(500.0F, true) {
            public void createImpl(boolean isExpanded) {
                MarketAPI market = commodity.getMarket();
                CommoditySpecAPI commoditySpec = commodity.getCommodity();
                if (commoditySpec.hasTag("codex_unlockable")) {
                    SharedUnlockData.get().reportPlayerAwareOfCommodity(commoditySpec.getId(), true);
                }

                this.setCodexEntryId(CodexDataV2.getCommodityEntryId(commoditySpec.getId()));
                this.setBgAlpha(0.9F);

                FactionAPI faction = market.getFaction();
                Color baseColor = faction.getBaseUIColor();
                Color darkColor = faction.getDarkUIColor();
                Color grayColor = Misc.getGrayColor();
                Color highlightColor = Misc.getHighlightColor();

                float smallGap = 3.0F;
                float paragraphGap = 10.0F;

                this.addTitle(commodity.getCommodity().getName(), baseColor);

                Description description = SpecStore.o00000(commodity.getId(), Description.Type.RESOURCE);
                this.addPara(description.getText1(), paragraphGap);

                Color hintColor = O0OO.õo0000;

                boolean allowAnyColony = Global.getSettings().getBoolean("allowPriceViewAtAnyColony");
                boolean canViewMarket = Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay() || allowAnyColony;
                if (canViewMarket) {
                    this.addPara("点击查看全星域市场信息", hintColor, paragraphGap);
                } else {
                    this.addPara("必须在通讯中继站有效范围内才可查看全星域市场信息", Misc.getNegativeHighlightColor(), paragraphGap);
                }

                String commodityId = commodity.getId();
                this.expandString = "按 {%s} 显示图例";
                this.unexpandString = "按 {%s} 返回";
                if (isExpanded) {
                    float height = 24.0F;
                    float width = this.width;
                    float negaGap = -6.0F;
                    this.addSectionHeading("图例", baseColor, darkColor, Alignment.MID, paragraphGap);
                    this.addCustom(createIconLine(GameSettings.getSpritePath("commodity_markers", "production"), null, false, "可通过当地生产来满足需求。", width, height), smallGap);
                    this.addCustom(createIconLine(market.getFaction().getCrest(), null, false, "可通过势力之内的进口量来满足需求。", width, height), negaGap);
                    this.addCustom(createIconLine(GameSettings.getSpritePath("commodity_markers", "imports"), null, false, "可通过势力之外的进口量来满足需求。", width, height), negaGap);
                    this.addCustom(createIconLine(market.getFaction().getCrest(), GameSettings.getSpritePath("commodity_markers", "imports"), null, false, "可通过势力内进口来满足部分需求，不足由势力外进口满足。", width, height), negaGap);
                    this.addCustom(createIconLine(null, null, true, "走私或出售由非法企业生产的违禁品，将没有出口收益。", width, height), negaGap);
                    this.addCustom(createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[0], "用于本地需求或用于出口的物资。", width, height), negaGap);
                    this.addCustom(createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[1], "通过一次贸易，进口或事件获得的物资。", width, height), negaGap);
                    this.addCustom(createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[2], "过剩，可自由获取但缺乏需求和出口。价格降低。", width, height), negaGap);
                    this.addCustom(createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[5], "短缺，有需求但无法获取。价格升高。", width, height), negaGap);
                } else {
                    PlanetMarket pm = SystemEconomyService.getPlanetMarket(market);
                    if (pm != null) {
                        this.addSectionHeading("生产 与 需求", baseColor, darkColor, Alignment.MID, paragraphGap);
                        ArrayList<Map.Entry<String, Integer>> supplyItemLines = new ArrayList<>();
                        ArrayList<Map.Entry<String, Integer>> demandItemLines = new ArrayList<>();

                        int supplyItemNum = 0;
                        int demandItemNum = 0;


                        supplyItemNum = pm.getSupplyRaw(commodityId);
                        demandItemNum = pm.getDemandRaw(commodityId);
                        for (Map.Entry<Industry, Integer> e : pm.getSupplyFactory(commodityId).entrySet()) {
                            supplyItemLines.add(new AbstractMap.SimpleEntry<>(e.getKey().getCurrentName(), e.getValue()));
                        }
                        for (Map.Entry<Industry, Integer> e : pm.getDemandFactory(commodityId).entrySet()) {
                            demandItemLines.add(new AbstractMap.SimpleEntry<>(e.getKey().getCurrentName(), e.getValue()));
                        }


                        int availableItemNum = supplyItemNum - demandItemNum;

                        supplyItemLines.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                        demandItemLines.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

                        Color demandColor = availableItemNum > 0 ? O0OO.ÕO0000 : O0OO.ÒÓ0000;
                        this.addPara("净产值：{%s}", paragraphGap, demandColor, "" + availableItemNum);

                        if (supplyItemLines.isEmpty()) {
                            this.addPara("无本地产出。", paragraphGap);
                        } else {
                            this.addPara("产量：{%s}", paragraphGap, highlightColor, "" + supplyItemNum);
                            this.beginGridFlipped(450.0F, 1, 40.0F, paragraphGap);
                            int gridLineIndex = 0;
                            for (Map.Entry<String, Integer> itemLine : supplyItemLines) {
                                this.addToGrid(0, gridLineIndex++, "生产方 " + itemLine.getKey(), formatDemandNumber(itemLine.getValue()), O0OO.ÕO0000);
                            }
                            this.addGrid(smallGap);
                        }
                        if (demandItemLines.isEmpty()) {
                            this.addPara("无本地需求。", paragraphGap);
                        } else {
                            this.addPara("需求：{%s}", paragraphGap, highlightColor, "" + demandItemNum);
                            this.beginGridFlipped(450.0F, 1, 40.0F, paragraphGap);
                            int gridLineIndex = 0;
                            for (Map.Entry<String, Integer> itemLine : demandItemLines) {
                                this.addToGrid(0, gridLineIndex++, "需求方 " + itemLine.getKey(), formatDemandNumber(itemLine.getValue()), O0OO.ÒÓ0000);
                            }
                            this.addGrid(smallGap);
                        }

                        this.addPara("在本殖民地的供给关系中，只计算当地产量与消耗做差得到的净产出。", grayColor, paragraphGap);

                        //进出口

                        this.addSectionHeading("进口 与 出口", baseColor, darkColor, Alignment.MID, paragraphGap);
                        ArrayList<TradePair> importTradePair = new ArrayList<>();
                        ArrayList<TradePair> exportTradePair = new ArrayList<>();

                        int importItemNum = 0;
                        int exportItemNum = 0;

                        //进口
                        for (TradePair tradePair : pm.getDemandTrade()) {
                            if (Objects.equals(tradePair.getItemId(), commodityId)) {
                                importTradePair.add(tradePair);
                                importItemNum += tradePair.getItemNum();
                            }
                        }
                        //出口
                        for (TradePair tradePair : pm.getSupplyTrade()) {
                            if (Objects.equals(tradePair.getItemId(), commodityId)) {
                                exportTradePair.add(tradePair);
                                exportItemNum += tradePair.getItemNum();
                            }
                        }

                        sortTradePairs(importTradePair, faction, market.getStarSystem(), true);
                        sortTradePairs(exportTradePair, faction, market.getStarSystem(), false);

                        if (importTradePair.isEmpty()) {
                            this.addPara("无进口订单。", paragraphGap);
                        } else {
                            this.addPara("进口量：{%s}", paragraphGap, highlightColor, "" + importItemNum);
                            this.beginGridFlipped(450.0F, 1, 40.0F, paragraphGap);
                            int gridLineIndex = 0;
                            for (TradePair tradePair : importTradePair) {
                                this.addToGrid(0, gridLineIndex++,
                                        "从 " + ((tradePair.getFromSystem() == faction) ? "本星系" : tradePair.getFromSystem().getName()) + " "
                                                + tradePair.getFromPlanet().getName() + "("
                                                + ((tradePair.getFromFaction() == faction) ? "本势力" : tradePair.getFromFaction().getDisplayName())
                                                + ") 购买", formatDemandNumber(tradePair.getItemNum()), O0OO.ÕO0000);
                            }
                            this.addGrid(smallGap);
                        }

                        if (exportTradePair.isEmpty()) {
                            this.addPara("无出口订单。", paragraphGap);
                        } else {
                            this.addPara("出口量：{%s}", paragraphGap, highlightColor, "" + exportItemNum);
                            this.beginGridFlipped(450.0F, 1, 40.0F, paragraphGap);
                            int gridLineIndex = 0;
                            for (TradePair tradePair : exportTradePair) {
                                this.addToGrid(0, gridLineIndex++,
                                        "向 " + ((tradePair.getToSystem() == faction) ? "本星系" : tradePair.getToSystem().getName()) + " "
                                                + tradePair.getToPlanet().getName() + "("
                                                + ((tradePair.getToFaction() == faction) ? "本势力" : tradePair.getToFaction().getDisplayName())
                                                + ") 出售", formatDemandNumber(tradePair.getItemNum()), O0OO.ÕO0000);
                            }
                            this.addGrid(smallGap);
                        }

                        int systemFactionSupply = 0, systemNonHostileSupply = 0, systemHostileSupply = 0;
                        int factionSupply = 0, nonHostileSupply = 0, hostileSupply = 0;
                        int systemFactionDemand = 0, systemNonHostileDemand = 0, systemHostileDemand = 0;
                        int factionDemand = 0, nonHostileDemand = 0, hostileDemand = 0;



                        systemFactionSupply = SystemEconomyService.getUnmetSupply().getOrDefault(commodityId, Collections.emptyMap()).getOrDefault(faction, Collections.emptyMap()).getOrDefault(market.getStarSystem(), 0);
                        for(Map.Entry<StarSystemAPI, Integer> si : SystemEconomyService.getUnmetSupply().getOrDefault(commodityId, Collections.emptyMap()).getOrDefault(faction, Collections.emptyMap()).entrySet()){
                            factionSupply += si.getValue();
                        }
                        for(Map.Entry<FactionAPI, Map<StarSystemAPI, Integer>> fM : SystemEconomyService.getUnmetSupply().getOrDefault(commodityId, Collections.emptyMap()).entrySet()){
                            if(fM.getKey() == faction) continue;
                            if(faction.isHostileTo(fM.getKey())){
                                systemHostileSupply += fM.getValue().getOrDefault(market.getStarSystem(), 0);
                                for(Map.Entry<StarSystemAPI, Integer> si : fM.getValue().entrySet()){
                                    hostileSupply += si.getValue();
                                }
                            } else {
                                systemNonHostileSupply += fM.getValue().getOrDefault(market.getStarSystem(), 0);
                                for(Map.Entry<StarSystemAPI, Integer> si : fM.getValue().entrySet()){
                                    nonHostileSupply += si.getValue();
                                }
                            }
                        }
                        systemFactionDemand = SystemEconomyService.getUnmetDemand().getOrDefault(commodityId, Collections.emptyMap()).getOrDefault(faction, Collections.emptyMap()).getOrDefault(market.getStarSystem(), 0);
                        for(Map.Entry<StarSystemAPI, Integer> si : SystemEconomyService.getUnmetDemand().getOrDefault(commodityId, Collections.emptyMap()).getOrDefault(faction, Collections.emptyMap()).entrySet()){
                            factionDemand += si.getValue();
                        }
                        for(Map.Entry<FactionAPI, Map<StarSystemAPI, Integer>> fM : SystemEconomyService.getUnmetDemand().getOrDefault(commodityId, Collections.emptyMap()).entrySet()){
                            if(fM.getKey() == faction) continue;
                            if(faction.isHostileTo(fM.getKey())){
                                systemHostileDemand += fM.getValue().getOrDefault(market.getStarSystem(), 0);
                                for(Map.Entry<StarSystemAPI, Integer> si : fM.getValue().entrySet()){
                                    hostileDemand += si.getValue();
                                }
                            } else {
                                systemNonHostileDemand += fM.getValue().getOrDefault(market.getStarSystem(), 0);
                                for(Map.Entry<StarSystemAPI, Integer> si : fM.getValue().entrySet()){
                                    nonHostileDemand += si.getValue();
                                }
                            }
                        }

                        this.addPara("在星系内，本势力的潜在需求为 {%s}， 非敌对势力的潜在需求为 {%s}， 敌对势力的潜在需求为 {%s}。", paragraphGap,  highlightColor, ""+systemFactionDemand, ""+systemNonHostileDemand, ""+systemHostileDemand);
                        this.addPara("在星域内，本势力的潜在需求为 {%s}， 非敌对势力的潜在需求为 {%s}， 敌对势力的潜在需求为 {%s}。", paragraphGap,  highlightColor, ""+factionDemand, ""+nonHostileDemand, ""+hostileDemand);
                        this.addPara("在星系内，本势力的过剩产能为 {%s}， 非敌对势力的过剩产能为 {%s}， 敌对势力的过剩产能为 {%s}。", paragraphGap,  highlightColor, ""+systemFactionSupply, ""+systemNonHostileSupply, ""+systemHostileSupply);
                        this.addPara("在星域内，本势力的过剩产能为 {%s}， 非敌对势力的过剩产能为 {%s}， 敌对势力的过剩产能为 {%s}。", paragraphGap,  highlightColor, ""+factionSupply, ""+nonHostileSupply, ""+hostileSupply);
                    } else {
                        this.addSectionHeading("生产 与 需求", baseColor, darkColor, Alignment.MID, paragraphGap);
                        this.addPara("{%s}", paragraphGap,  Color.red, "无信号" );
                        this.addSectionHeading("进口 与 出口", baseColor, darkColor, Alignment.MID, paragraphGap);
                        this.addPara("{%s}", paragraphGap,  Color.red, "无信号" );
                    }
                }
            }
        };
    }
}
