package com.github.alphardpaarthurnax.script.services;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.github.alphardpaarthurnax.script.data.AsteroidsData;

import java.awt.*;

public class RingWorldServices {
    public static void createRingWorld(MarketAPI market){
        SectorEntityToken planet = market.getPrimaryEntity();
        StarSystemAPI system = planet.getStarSystem();
        SectorEntityToken focus = planet.getOrbitFocus();
        system.addRingBand(
                focus,
                "misc",
                AsteroidsData.RingType.Test.getType(),
                256f,
                0,
                Color.white,
                256f,
                Math.max(focus.getRadius() * 4f,AsteroidsData.minRingRadius),
                100
        );
        SectorEntityToken ring1 = system.addCustomEntity(
                "ringworld1",
                "环世界节点1",
                "station_built_from_industry",
                "tritachyon"
        );
        SectorEntityToken ring2 = system.addCustomEntity(
                "ringworld2",
                "环世界节点2",
                "station_built_from_industry",
                "tritachyon"
        );
        SectorEntityToken ring3 = system.addCustomEntity(
                "ringworld3",
                "环世界节点3",
                "station_built_from_industry",
                "tritachyon"
        );
        SectorEntityToken ring4 = system.addCustomEntity(
                "ringworld4",
                "环世界节点4",
                "station_built_from_industry",
                "tritachyon"
        );
        ring1.setCircularOrbit(
                focus,
                45f,
                Math.max(focus.getRadius() * 4f, AsteroidsData.minRingRadius),
                100f
        );
        ring2.setCircularOrbit(
                focus,
                135f,
                Math.max(focus.getRadius() * 4f, AsteroidsData.minRingRadius),
                100f
        );
        ring3.setCircularOrbit(
                focus,
                225f,
                Math.max(focus.getRadius() * 4f, AsteroidsData.minRingRadius),
                100f
        );
        ring4.setCircularOrbit(
                focus,
                315f,
                Math.max(focus.getRadius() * 4f, AsteroidsData.minRingRadius),
                100f
        );
        MarketAPI market1 = Global.getFactory().createMarket("ringworld1", "环世界节点1", 10);
        market1.setFactionId("tritachyon");
        market1.setPrimaryEntity(ring1);
        market1.addCondition(Conditions.MILD_CLIMATE);
        market1.addCondition(Conditions.HABITABLE);
        market1.getTariff().modifyFlat("default", 0.2f);
        ring1.setMarket(market1);
        MarketAPI market2 = Global.getFactory().createMarket("ringworld2", "环世界节点2", 10);
        market2.setFactionId("tritachyon");
        market2.setPrimaryEntity(ring2);
        market2.addCondition(Conditions.MILD_CLIMATE);
        market2.addCondition(Conditions.HABITABLE);
        market2.getTariff().modifyFlat("default", 0.2f);
        ring2.setMarket(market2);
        MarketAPI market3 = Global.getFactory().createMarket("ringworld3", "环世界节点3", 10);
        market3.setFactionId("tritachyon");
        market3.setPrimaryEntity(ring3);
        market3.addCondition(Conditions.MILD_CLIMATE);
        market3.addCondition(Conditions.HABITABLE);
        market3.getTariff().modifyFlat("default", 0.2f);
        ring3.setMarket(market3);
        MarketAPI market4 = Global.getFactory().createMarket("ringworld4", "环世界节点4", 10);
        market4.setFactionId("tritachyon");
        market4.setPrimaryEntity(ring4);
        market4.addCondition(Conditions.MILD_CLIMATE);
        market4.addCondition(Conditions.HABITABLE);
        market4.getTariff().modifyFlat("default", 0.2f);
        ring4.setMarket(market4);
        Global.getSector().getEconomy().addMarket(market1, true);
        Global.getSector().getEconomy().addMarket(market2, true);
        Global.getSector().getEconomy().addMarket(market3, true);
        Global.getSector().getEconomy().addMarket(market4, true);
    }
}
