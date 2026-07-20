package eco.neo;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemEconomy {
    private final StarSystemAPI system;
    private Map<MarketAPI, PlanetEconomy> planetEconomys = new HashMap<>();

    public SystemEconomy(StarSystemAPI system){
        this.system = system;
    }
    public void updateSource(List<MarketAPI> markets) {
        for (MarketAPI market : markets) {
            if (!planetEconomys.containsKey(market)) {
                planetEconomys.put(market, new PlanetEconomy(market));
            }
        }
        planetEconomys.keySet().removeIf(m -> !markets.contains(m));

        for (PlanetEconomy pe : planetEconomys.values()) {
            pe.updateSource();
        }
    }
}
