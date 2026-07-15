package com.github.alphardpaarthurnax.script.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;
import java.util.List;

public class CCUtils {
    public static void removeEntity(StarSystemAPI system, SectorEntityToken target){
        MarketAPI market = target.getMarket();

        Global.getSector().getEconomy().removeMarket(market);
        target.setMarket(null);
        system.removeEntity(target);
        market.getConnectedEntities().clear();

        List<SectorEntityToken> toRemove = new ArrayList<>();

        for (SectorEntityToken e : system.getAllEntities()) {
            if (e == target) continue;
            if (e.getOrbitFocus() == target) {
                toRemove.add(e);
                continue;
            }
            if (e.getMarket() == market) {
                toRemove.add(e);
                continue;
            }
        }

        AsteroidsUtils.markAsteroids(toRemove,system,target);

        for (SectorEntityToken e : toRemove) {
            system.removeEntity(e);
        }
    }
    public static float randomOffset(float offset){
        return (1f - offset) + (float)Math.random() * (2f * offset);
    }
}
