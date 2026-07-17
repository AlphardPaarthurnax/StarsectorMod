package eco.mixin;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class MutableStatModGetter implements TooltipMakerAPI.StatModValueGetter{
    public String getPercentValue(MutableStat.StatMod mod) {return null;}
    public String getMultValue(MutableStat.StatMod mod) {return null;}
    public Color getModColor(MutableStat.StatMod mod) {return null;}
    public String getFlatValue(MutableStat.StatMod mod) {
        return Misc.getWithDGS(mod.value) + Strings.C;
    }
}
