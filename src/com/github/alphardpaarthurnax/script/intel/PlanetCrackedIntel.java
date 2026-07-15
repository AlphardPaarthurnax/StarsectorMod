package com.github.alphardpaarthurnax.script.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.Set;

import static com.github.alphardpaarthurnax.script.data.DialogData.*;

public class PlanetCrackedIntel extends BaseIntelPlugin {
    private final String systemName;
    private final String planetName;
    private final String factionName;
    private final FactionAPI doFaction;
    private final SectorEntityToken newPlanet;
    private final CampaignClockAPI time;

    public PlanetCrackedIntel(String systemName, String planetName, String factionName, FactionAPI doFaction, SectorEntityToken newPlanet) {
        this.systemName = systemName;
        this.planetName = planetName;
        this.factionName = factionName;
        this.doFaction = doFaction;
        this.newPlanet = newPlanet;
        this.time = Global.getSector().getClock();
    }

    @Override
    protected String getName() {
        return planetName + "已被完全摧毁";
    }
    @Override
    public String getIcon() {
        return doFaction.getCrest();
    }
    @Override
    public FactionAPI getFactionForUIColors() {
        return doFaction;
    }
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(doFaction.getId());
        return tags;
    }
    @Override
    public String getSortString() {
        return "地爆天星";
    }
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return newPlanet;
    }
    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara(
                "%s %s(%s)于%s被%s使用地爆天星摧毁",
                opad,
                highlight,
                systemName,
                planetName,
                factionName,
                time.getDateString(),
                doFaction.getDisplayName()
        );
        info.addPara("{%s}",opad,fatalWarn,"该星球已在烈焰中毁灭");
        info.addPara(
                "发生时间：%s",
                opad,
                highlight,
                time.getDateString()
        );
    }
}
