package com.github.alphardpaarthurnax.script.utils;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.github.alphardpaarthurnax.script.data.AsteroidsData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AsteroidsUtils {
    public static void markAsteroids(List<SectorEntityToken> ret, StarSystemAPI system, SectorEntityToken focus){
        List<SectorEntityToken> CampaignTerrain = new ArrayList<>();
        List<SectorEntityToken> CampaignAsteroid = new ArrayList<>();
        for (SectorEntityToken e : system.getAllEntities()) {
            if(CampaignTerrainAPI.class.isAssignableFrom(e.getClass())){
                if(e.getOrbitFocus() == focus){
                    CampaignTerrain.add(e);
                    ret.add(e);
                    continue;
                }
            }
            if(RingBandAPI.class.isAssignableFrom(e.getClass())) {
                if(((RingBandAPI) e).getFocus() == focus){
                    ret.add(e);
                    continue;
                }
            }
            if(AsteroidAPI.class.isAssignableFrom(e.getClass())){
                CampaignAsteroid.add(e);
                continue;
            }
        }
        for (SectorEntityToken e : CampaignAsteroid){
            for(SectorEntityToken t : CampaignTerrain){
                if(e.getOrbitFocus() == t){
                    ret.add(e);
                    break;
                }
            }
        }
    }

    public static void addAsteroidsBelt(StarSystemAPI system, SectorEntityToken focus,
                                        int numAsteroids, float radius, float width,
                                        float period, float periodOffset,
                                        String name, Color color, AsteroidsData.RingType ringType){

        WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<>();
        for (int i = 0; i < ringType.getBlockSize(); i++){
            picker.add(i);
        }

        system.addAsteroidBelt(
                focus,
                numAsteroids,
                radius,
                width,
                period * (1f - periodOffset),
                period * (1f + periodOffset),
                Terrain.ASTEROID_BELT,
                name + " 小行星带"
        );

        system.addRingBand(
                focus,
                "misc",
                ringType.getType(),
                256f,
                picker.pick(),
                color,
                width,
                radius,
                period
        );
    }
    public static void addAsteroidsBeltWithoutBackground(StarSystemAPI system, SectorEntityToken focus,
                                        int numAsteroids, float radius, float width,
                                        float period, float periodOffset, String name){
        system.addAsteroidBelt(
                focus,
                numAsteroids,
                radius,
                width,
                period * (1f - periodOffset),
                period * (1f + periodOffset),
                Terrain.ASTEROID_BELT,
                name + " 小行星带"
        );
    }
    public static void addAsteroidsBeltRandom(StarSystemAPI system, SectorEntityToken focus,
                                              int numAsteroids, float radius, float width,
                                              float period, float periodOffset,
                                              String name, Color color,int flag, AsteroidsData.RingType ringType){

        float numAsteroidsOffset = (flag & (1 << AsteroidsData.randomFlag.NumAsteroids.ordinal())) != 0 ? CCUtils.randomOffset(0.3f) : 1.0f;
        float radiusOffset = (flag & (1 << AsteroidsData.randomFlag.Radius.ordinal())) != 0 ? CCUtils.randomOffset(0.05f) : 1.0f;
        float widthOffset = (flag & (1 << AsteroidsData.randomFlag.Width.ordinal())) != 0 ? CCUtils.randomOffset(0.05f) : 1.0f;
        float _periodOffset = (flag & (1 << AsteroidsData.randomFlag.Period.ordinal())) != 0 ? CCUtils.randomOffset(0.05f) : 1.0f;
        float periodOffsetOffset = (flag & (1 << AsteroidsData.randomFlag.PeriodOffset.ordinal())) != 0 ? CCUtils.randomOffset(0.05f) : 1.0f;

        addAsteroidsBelt(system, focus, (int)Math.floor(numAsteroids * numAsteroidsOffset),
                radius * radiusOffset,width * widthOffset,
                period * _periodOffset, periodOffset * periodOffsetOffset,
                name, color, ringType
        );
    }
    public static void addAsteroidsBeltRandomWithoutBackground(StarSystemAPI system, SectorEntityToken focus,
                                              int numAsteroids, float radius, float width,
                                              float period, float periodOffset,
                                              String name,int flag){

        float numAsteroidsOffset = AsteroidsData.randomFlag.NumAsteroids.check(flag) ? CCUtils.randomOffset(0.3f) : 1.0f;
        float radiusOffset = AsteroidsData.randomFlag.Radius.check(flag) ? CCUtils.randomOffset(0.05f) : 1.0f;
        float widthOffset = AsteroidsData.randomFlag.Width.check(flag) ? CCUtils.randomOffset(0.05f) : 1.0f;
        float _periodOffset = AsteroidsData.randomFlag.Period.check(flag) ? CCUtils.randomOffset(0.05f) : 1.0f;
        float periodOffsetOffset = AsteroidsData.randomFlag.PeriodOffset.check(flag) ? CCUtils.randomOffset(0.05f) : 1.0f;

        addAsteroidsBeltWithoutBackground(system, focus, (int)Math.floor(numAsteroids * numAsteroidsOffset),
                radius * radiusOffset,width * widthOffset,
                period * _periodOffset, periodOffset * periodOffsetOffset, name
        );
    }


}
