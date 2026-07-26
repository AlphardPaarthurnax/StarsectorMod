package eco.neo;

import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;

public class EconomyService implements EconomyTickListener {
    private GlobalEconomy globalEconomy = GlobalEconomy.getInstance();
    @Override
    public void reportEconomyTick(int iterIndex) {

    }
    @Override
    public void reportEconomyMonthEnd() {
        globalEconomy.updateSource();
        globalEconomy.matchTrade();
        globalEconomy.updateSupplyDemand();
        globalEconomy.updateTradeStock();
        globalEconomy.updatePrices();
        EcoDebugDump.dump();
    }
}
