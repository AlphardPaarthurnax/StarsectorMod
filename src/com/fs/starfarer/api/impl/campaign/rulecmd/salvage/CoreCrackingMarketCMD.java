package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.github.alphardpaarthurnax.script.CoreCrackingModPlugin;
import com.github.alphardpaarthurnax.script.services.CoreCrackingMarketServices;
import com.github.alphardpaarthurnax.script.services.RingWorldServices;
import com.github.alphardpaarthurnax.script.animation.PlanetDestructionAnimationScript;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.alphardpaarthurnax.script.data.DialogData.*;

public class CoreCrackingMarketCMD extends MarketCMD {

    private static int fuelCost = 5000;
    private static int volatilesCost = 200;
    private PlanetAPI shattered;
    /** -> {@link CoreCrackingMarketCMD#bombardCoreCracking()} */
    public static String BOMBARD_CORE_CRACKING = "mktBombardCoreCracking";
    /** -> {@link CoreCrackingMarketCMD#coreCracking()} */
    public static String BOMBARD_CORE_CRACKING_CONFIRM = "mktBombardCoreCrackingConfirm";
    /** -> dialog.dismiss(); */
    public static String CORE_CRACKING_EXIT = "mktCoreCrackingExit";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        super.execute(ruleId, dialog, params, memoryMap);

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        if(command.equals("bombardCoreCracking")){
            bombardCoreCracking();
        } else if (command.equals("coreCracking")) {
            coreCracking();
        } else if (command.equals("coreCrackingExit")) {
            coreCrackingExit();
            return true;
        }
        return true;
    }

    @Override
    protected void bombardMenu() {
        this.dialog.getVisualPanel().showImagePortion("illustrations", "bombard_prepare", 640.0F, 400.0F, 0.0F, 0.0F, 480.0F, 300.0F);
        StatBonus defender = this.market.getStats().getDynamic().getMod("ground_defenses_mod");
        float bomardBonus = Misc.getFleetwideTotalMod(this.playerFleet, "fleet_bombard_cost_reduction", 0.0F);
        String increasedBombardKey = "core_addedBombard";
        StatBonus bombardBonusStat = new StatBonus();
        if (bomardBonus > 0.0F) {
            bombardBonusStat.modifyFlat(increasedBombardKey, -bomardBonus, "遭到特遣队轰炸");
        }

        float defenderStr = (float)Math.round(defender.computeEffective(0.0F));
        defenderStr -= bomardBonus;
        if (defenderStr < 0.0F) {
            defenderStr = 0.0F;
        }

        this.temp.defenderStr = defenderStr;
        TooltipMakerAPI info = this.text.beginTooltip();
        info.setParaSmallInsignia();
        String has = this.faction.getDisplayNameHasOrHave();
        String is = this.faction.getDisplayNameIsOrAre();
        boolean hostile = this.faction.isHostileTo("player");
        boolean tOn = this.playerFleet.isTransponderOn();
        float initPad = 0.0F;
        if (!hostile) {
            info.addPara(Misc.ucFirst(this.faction.getDisplayNameWithArticle()) + " " + is + " 目前还没有敌意，如果现在展开声势浩大的轨道轰炸，无论你的应答器 处于何种状态，" + "都将无法掩盖你所实施的暴行。", initPad, this.faction.getBaseUIColor(), new String[]{this.faction.getDisplayNameWithArticleWithoutArticle()});
            initPad = opad;
        }

        info.addPara("星舰燃料很容易就能失去稳定，让其中的反物质释放其蕴含的破坏潜能。地面防御会阻碍轰炸，但实际上只需投下更多燃料就可以达成同样的轰炸效果。", initPad);
        if (bomardBonus > 0.0F) {
            info.addPara("有效的地面防御战力：{%s}", opad, highlight, new String[]{"" + (int)defenderStr});
        } else {
            info.addPara("地面防御战力：{%s}", opad, highlight, new String[]{"" + (int)defenderStr});
        }

        info.addStatModGrid(width, 50.0F, opad, small, defender, true, statPrinter(true));
        if (!bombardBonusStat.isUnmodified()) {
            info.addStatModGrid(width, 50.0F, opad, 3.0F, bombardBonusStat, true, statPrinter(false));
        }

        this.text.addTooltip();
        this.temp.bombardCost = getBombardmentCost(this.market, this.playerFleet);
        int fuel = (int)this.playerFleet.getCargo().getFuel();
        boolean canBombard = fuel >= this.temp.bombardCost;
        LabelAPI label = this.text.addPara("轰炸需要 {%s} 单位的燃料。而你现有 {%s} 单位的燃料。", highlight, new String[]{"" + this.temp.bombardCost, "" + fuel});
        label.setHighlight(new String[]{"" + this.temp.bombardCost, "" + fuel});
        label.setHighlightColors(new Color[]{canBombard ? highlight : negativeHighlight, highlight});
        this.options.clearOptions();
        this.options.addOption("准备战术轰炸", BOMBARD_TACTICAL);
        this.options.addOption("准备饱和轰炸", BOMBARD_SATURATION);
        this.options.addOption("准备地爆天星", BOMBARD_CORE_CRACKING);
        if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
            canBombard = true;
        }

        if (!canBombard) {
            this.options.setEnabled(BOMBARD_TACTICAL, false);
            this.options.setTooltip(BOMBARD_TACTICAL, "没有足够的燃料。");
            this.options.setEnabled(BOMBARD_SATURATION, false);
            this.options.setTooltip(BOMBARD_SATURATION, "没有足够的燃料。");
        }

        this.options.addOption("返回", RAID_GO_BACK);
        this.options.setShortcut(RAID_GO_BACK, 1, false, false, false, true);

        RingWorldServices.createRingWorld(market);
    }

    @Override
    protected void bombardNeverMind() {
        this.bombardMenu();
    }

    protected void bombardCoreCracking(){
        List<FactionAPI> willHostile = new ArrayList();
        willHostile.add(this.faction);
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.isPlayerFaction())
                continue;
            if (faction == this.faction)
                continue;
            if (CoreCrackingModPlugin.blacklistIds.contains(faction.getId()))
                continue;
            willHostile.add(faction);
        }

        TooltipMakerAPI info = this.text.beginTooltip();
        info.setParaSmallInsignia();
        info.addPara("通过巨像轴炮装载的超级武器进行打击。", initPad);
        info.addPara("发射的热能流能够加热行星大气，并引爆行星核心，此举将彻底的毁灭星球的地壳构造。最后，一个相对较小的冲击波会将行星炸得四分五裂。", opad);

        info.addPara("地爆天星将会对该行星造成不可挽回的毁灭",opad);
        info.addPara("这种行为被全英仙座视为{%s}，因此下列势力将立即与你{%s}：", opad, fatalWarn,"惨无人道的灭绝行为","敌对");

        if (!willHostile.isEmpty()) {
            info.setBulletedListMode(BaseIntelPlugin.INDENT);
            for (FactionAPI fac : willHostile) {
                info.addPara(Misc.ucFirst(fac.getDisplayName()), fac.getBaseUIColor(), thinPad);
            }
            info.setBulletedListMode(null);
        }

        info.addPara("{%s}", opad, fatalWarn,"该决定无法暂停");
        this.text.addTooltip();

        int fuel = (int)this.playerFleet.getCargo().getFuel();
        int volatiles = (int)this.playerFleet.getCargo().getCommodityQuantity("volatiles");
        boolean canBombard = fuel >= fuelCost && volatiles >= volatilesCost;

        LabelAPI label1 = this.text.addPara("地爆天星需要 {%s} 单位的燃料。而你现有 {%s} 单位的燃料。", highlight, new String[]{"" + fuelCost, "" + fuel});
        label1.setHighlight(new String[]{"" + fuelCost, "" + fuel});
        label1.setHighlightColors(new Color[]{fuel >= fuelCost ? highlight : negativeHighlight, highlight});
        LabelAPI label2 = this.text.addPara("地爆天星需要 {%s} 单位的挥发物。而你现有 {%s} 单位的挥发物。", highlight, new String[]{"" + volatilesCost, "" + volatiles});
        label2.setHighlight(new String[]{"" + volatilesCost, "" + volatiles});
        label2.setHighlightColors(new Color[]{volatiles >= volatilesCost ? highlight : negativeHighlight, highlight});

        this.options.clearOptions();
        this.options.addOption("确认执行地爆天星", BOMBARD_CORE_CRACKING_CONFIRM);

        if (!canBombard) {
            this.options.setEnabled(BOMBARD_CORE_CRACKING_CONFIRM, false);
            this.options.setTooltip(BOMBARD_CORE_CRACKING_CONFIRM, "没有足够的燃料/挥发物。");
        }
        this.options.addOption("放弃", BOMBARD_NEVERMIND);
        this.options.setShortcut(BOMBARD_NEVERMIND, 1, false, false, false, true);

        options.addOptionConfirmation(BOMBARD_CORE_CRACKING_CONFIRM, "这种行为被全英仙座的势力视为无法饶恕的灭绝行为，绝大部分势力都将与你为敌，" + "你确定吗?", "是的", "放弃");
    }

    protected void coreCracking() {
        this.playerFleet.getCargo().removeFuel((float)fuelCost);
        AddRemoveCommodity.addCommodityLossText("fuel", fuelCost, this.text);
        this.playerFleet.getCargo().removeCommodity("volatiles", (float)volatilesCost);
        AddRemoveCommodity.addCommodityLossText("volatiles", volatilesCost, this.text);

        shattered = CoreCrackingMarketServices.CoreCracking(market,this.faction,text,dialog);
        CoreCrackingMarketServices.hostile(text);

        this.options.clearOptions();
        this.options.addOption("退出", CORE_CRACKING_EXIT);

        PlanetDestructionAnimationScript anim = new PlanetDestructionAnimationScript(shattered);
        Global.getSector().addTransientScript(anim);
    }

    protected void coreCrackingExit(){
        this.clearTemp();
        if (this.market != null) {
            this.market.getMemoryWithoutUpdate().set("$coreCracked", true);
        }
        dialog.dismiss();
    }
}
