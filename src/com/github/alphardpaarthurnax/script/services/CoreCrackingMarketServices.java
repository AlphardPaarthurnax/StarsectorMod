package com.github.alphardpaarthurnax.script.services;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.github.alphardpaarthurnax.script.CoreCrackingModPlugin;
import com.github.alphardpaarthurnax.script.data.AsteroidsData;
import com.github.alphardpaarthurnax.script.intel.PlanetCrackedIntel;
import com.github.alphardpaarthurnax.script.utils.AsteroidsUtils;
import com.github.alphardpaarthurnax.script.utils.CCUtils;

import java.awt.*;

public class CoreCrackingMarketServices {

    public static PlanetAPI CoreCracking(MarketAPI market, FactionAPI faction, TextPanelAPI text, InteractionDialogAPI dialog){
        SectorEntityToken planet = market.getPrimaryEntity();

        StarSystemAPI system = planet.getStarSystem();

        float angle = planet.getCircularOrbitAngle();
        float orbitRadius = planet.getCircularOrbitRadius();
        float period = planet.getCircularOrbitPeriod();
        float radius = planet.getRadius();
        String planetName = planet.getName();
        String planetId = planet.getId();
        SectorEntityToken focus = planet.getOrbitFocus();

        CCUtils.removeEntity(system,planet);

        AsteroidsUtils.addAsteroidsBeltRandom(
                system, focus, 150, orbitRadius,
                radius, period, 0.5f,
                planetName + " 碎片", Color.white,
                AsteroidsData.randomFlag.NumAsteroids.getFlag() |
                        AsteroidsData.randomFlag.Width.getFlag() |
                        AsteroidsData.randomFlag.Period.getFlag() |
                        AsteroidsData.randomFlag.PeriodOffset.getFlag()
                , AsteroidsData.RingType.Asteroids
        );

        PlanetAPI shattered = system.addPlanet(
                "shattered_" + planetId,
                focus,
                "破碎的" + planetName,
                "lava",
                angle,
                radius * 0.5f,
                orbitRadius,
                period
        );

        AsteroidsUtils.addAsteroidsBeltRandomWithoutBackground(
                system, shattered, 10, radius,
                radius * 0.5f, 20f, 0.5f,
                shattered.getName() + " 环",
                AsteroidsData.randomFlag.NumAsteroids.getFlag() |
                        AsteroidsData.randomFlag.Radius.getFlag() |
                        AsteroidsData.randomFlag.Width.getFlag() |
                        AsteroidsData.randomFlag.Period.getFlag() |
                        AsteroidsData.randomFlag.PeriodOffset.getFlag()
        );

        PlanetCrackedIntel intel = new PlanetCrackedIntel(system.getName(),planetName,faction.getDisplayName(),Global.getSector().getPlayerFaction(),shattered);
        Global.getSector().getIntelManager().addIntel(intel, true, text);
        tests.bigevent.BaseSuperEvent.show(planetName);

        return shattered;
    }
    public static void hostile(TextPanelAPI text){
        CustomRepImpact impact = new CustomRepImpact();
        impact.delta = -999f;
        impact.ensureAtBest = RepLevel.VENGEFUL;
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.isPlayerFaction())
                continue;
            if (CoreCrackingModPlugin.blacklistIds.contains(faction.getId()))
                continue;

            Global.getSector().adjustPlayerReputation(
                    new CoreReputationPlugin.RepActionEnvelope(
                            CoreReputationPlugin.RepActions.CUSTOM,
                            impact, null, text, true, true),
                    faction.getId());
        }
    }
}
