package com.github.alphardpaarthurnax.script;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import eco.EconomyConfig;
import eco.EconomyService;
import org.json.JSONArray;
import org.json.JSONObject;
import tests.NewSolar;

import java.util.HashSet;
import java.util.Set;

public class CoreCrackingModPlugin extends BaseModPlugin {
    public static Set<String> blacklistIds = new HashSet<>();
    public static final String MOD_ID = "CoreCracking";

    @Override
    public void onApplicationLoad() throws Exception {
        Global.getSettings().getScriptClassLoader();

        JSONArray data = Global.getSettings().getMergedSpreadsheetData("id", "data/config/genocideBlacklist.csv");
        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            blacklistIds.add(row.getString("id"));
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (Global.getSector().getStarSystem("NewSolar") != null) {
            Global.getSector().getStarSystem("NewSolar").getMemoryWithoutUpdate().set(
                    MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "new_solar_system");
        }
        registerEconomy();

        EconomyConfig.load();
    }

    @Override
    public void onNewGame() {
        SectorAPI sector = Global.getSector();
        (new NewSolar()).generate(sector);
    }

    private void registerEconomy() {
        SectorAPI sector = Global.getSector();
        if (sector == null) return;

        //sector.getListenerManager().addListener(new SystemEconomyService());
        java.util.List<EconomyService> listeners = sector.getListenerManager().getListeners(EconomyService.class);
        EconomyService economyService;
        if (listeners.isEmpty()) {
            economyService = new EconomyService();
            sector.getListenerManager().addListener(economyService);
        } else {
            economyService = listeners.get(0);
            for (int i = 1; i < listeners.size(); i++) {
                sector.getListenerManager().removeListener(listeners.get(i));
            }
        }
        economyService.activate();

    }
}
