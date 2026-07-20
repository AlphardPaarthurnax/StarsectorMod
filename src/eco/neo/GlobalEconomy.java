package eco.neo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.*;

public class GlobalEconomy{
    private Map<StarSystemAPI, SystemEconomy> systemEconomys = new HashMap<>();
    public GlobalEconomy(){

    }

    public void updateSource(){
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        allMarkets.removeIf(m -> !m.isInEconomy() || m.getStarSystem() == null);//TODO 空间站

        Map<StarSystemAPI, List<MarketAPI>> systemGroups = new LinkedHashMap<>();
        for (MarketAPI market : allMarkets) {
            systemGroups.computeIfAbsent(market.getStarSystem(), k -> new ArrayList<>()).add(market);
        }

        systemEconomys.keySet().removeIf(sys -> !systemGroups.containsKey(sys));
        for (Map.Entry<StarSystemAPI, List<MarketAPI>> entry : systemGroups.entrySet()) {
            if (!systemEconomys.containsKey(entry.getKey())) {
                systemEconomys.put(entry.getKey(), new SystemEconomy(entry.getKey()));
            }
            systemEconomys.get(entry.getKey()).updateSource(entry.getValue());
        }
        //创建 SystemEconomy
    }

}
