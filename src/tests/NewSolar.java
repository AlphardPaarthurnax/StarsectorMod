package tests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.github.alphardpaarthurnax.script.data.AsteroidsData;
import com.github.alphardpaarthurnax.script.utils.AsteroidsUtils;

import java.awt.*;
import java.util.ArrayList;

public class NewSolar {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("NewSolar");

        PlanetAPI star = system.initStar("new_solar", "star_yellow", 1700f, 40000f, 40000f, 500f);
        star.setName("新太阳");
        system.setBackgroundTextureFilename("graphics/CoreCracking/backgrounds/pcp_background1.png");

        PlanetAPI mercury = system.addPlanet("new_mercury", star, "新水星", "rocky_metallic", 0f, 57.44f, 2400f, 58.65f);
        PlanetAPI venus = system.addPlanet("new_venus", star, "新金星", "barren_venuslike", 0f, 142.48f, 3200f, 224.70f);

        PlanetAPI earth = system.addPlanet("new_earth", star, "新地球", "terran", 0f, 150f, 4200f, 365.25f);
        PlanetAPI moon = system.addPlanet("new_moon", earth, "新月球", "barren2", 90f, 40.92f, 300f, 27.32f);

        PlanetAPI mars = system.addPlanet("new_mars", star, "新火星", "barren-desert", 0f, 79.80f, 5000f, 686.97f);

        WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<>();
        picker.add(0);
        picker.add(1);
        for(int r = 6300+128 ; r < 7000-128 ; r += 256){
            AsteroidsData.RingType ringType;
            if(picker.pick()==0) {
                ringType = AsteroidsData.RingType.Asteroids;
            } else {
                ringType = AsteroidsData.RingType.Dust;
            }
            AsteroidsUtils.addAsteroidsBelt(system,star,100,r,256,800f,10f,"新火-木小行星带", Color.white, ringType);
        }

        PlanetAPI jupiter = system.addPlanet("new_jupiter", star, "新木星", "gas_giant", 0f, 1645.99f / 2f, 9000f, 365.25f * 11.862f);
        PlanetAPI saturn = system.addPlanet("new_saturn", star, "新土星", "gas_giant", 0f, 1371.02f / 2f, 12000f, 365.25f * 29.457f);

        for(int r = 14000+128 ; r < 15000-128 ; r += 256){
            AsteroidsData.RingType ringType;
            if(picker.pick()==0) {
                ringType = AsteroidsData.RingType.Ice;
            } else {
                ringType = AsteroidsData.RingType.Dust;
            }
            AsteroidsUtils.addAsteroidsBelt(system,star,100,r,256,365.25f * 50f,365.25f,"新土-天王小行星带", Color.white, ringType);
        }

        PlanetAPI uranus = system.addPlanet("new_uranus", star, "新天王星", "ice_giant", 0f, 597.12f / 2f, 17000f, 365.25f * 84.021f);
        PlanetAPI neptune = system.addPlanet("new_neptune", star, "新海王星", "ice_giant", 0f, 579.70f / 2f, 20000f, 365.25f * 164.8f);

        for(int r = 22000+128 ; r < 23000-128 ; r += 256){
            AsteroidsData.RingType ringType;
            if(picker.pick()==0) {
                ringType = AsteroidsData.RingType.Ice;
            } else {
                ringType = AsteroidsData.RingType.Dust;
            }
            AsteroidsUtils.addAsteroidsBelt(system,star,100,r,256,365.25f * 170f,365.25f * 20f,"新远太阳系小行星带", Color.white, ringType);
        }

        EconomyAPI econ = Global.getSector().getEconomy();
        MarketAPI capitalMarket = Global.getFactory().createMarket("my_capital_market", "New Earth", 6);
        capitalMarket.setFactionId("pcp");
        capitalMarket.setPrimaryEntity(earth);
        capitalMarket.getTariff().modifyFlat("generator", 0.15f);

        ArrayList<String> capitalIndustries = new ArrayList<>();
        capitalIndustries.add(Industries.POPULATION);
        capitalIndustries.add(Industries.MEGAPORT);
        capitalIndustries.add(Industries.FARMING);
        capitalIndustries.add(Industries.LIGHTINDUSTRY);
        capitalIndustries.add(Industries.GROUNDDEFENSES);
        capitalIndustries.add(Industries.PATROLHQ);
        capitalIndustries.add(Industries.ORBITALSTATION);

        ArrayList<String> capitalConditions = new ArrayList<>();
        capitalConditions.add(Conditions.HABITABLE);
        capitalConditions.add(Conditions.POPULATION_6);
        capitalConditions.add(Conditions.FARMLAND_RICH);
        capitalConditions.add(Conditions.ORGANICS_COMMON);
        capitalConditions.add(Conditions.REGIONAL_CAPITAL);

        ArrayList<String> capitalSubmarkets = new ArrayList<>();
        capitalSubmarkets.add(Submarkets.SUBMARKET_OPEN);
        capitalSubmarkets.add(Submarkets.SUBMARKET_STORAGE);
        capitalSubmarkets.add(Submarkets.SUBMARKET_BLACK);

        earth.setMarket(capitalMarket);
        econ.addMarket(capitalMarket, false);

        for (String ind : capitalIndustries) capitalMarket.addIndustry(ind);
        for (String con : capitalConditions) capitalMarket.addCondition(con);
        for (String sub : capitalSubmarkets) capitalMarket.addSubmarket(sub);

        capitalMarket.reapplyConditions();
        earth.getMemoryWithoutUpdate().set(MemFlags.STORY_CRITICAL, true);

        /*JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("my_jump_point", "MyStar Jump-point");
        jumpPoint.setCircularOrbit(star, 0f, 6000f, 360f);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);*/

        system.addCustomEntity("my_sensor", "MyStar Sensor Array", "sensor_array", "pcp").setCircularOrbit(star,0f,5000f,120f);
        system.addCustomEntity("my_nav", "MyStar Navigation Array", "nav_buoy", "pcp").setCircularOrbit(star,120f,6000f,120f);
        system.addCustomEntity("my_relay", "MyStar Communication Relay", "comm_relay", "pcp").setCircularOrbit(star,240f,7000f,120f);

        StarSystemGenerator.addSystemwideNebula(system, StarAge.YOUNG);
        system.autogenerateHyperspaceJumpPoints(true, true);

        Global.getSector().addScript(new MySystemMusicScript(system));

        cleanup(system);
    }

    private void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, radius + minRadius * 0.5f, 0f, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, radius + minRadius * 0.5f, 0f, 360f, 0.25f);
    }
}
