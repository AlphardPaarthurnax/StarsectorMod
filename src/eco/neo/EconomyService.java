package eco.neo;

import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;

public class EconomyService implements EconomyTickListener {
    private GlobalEconomy globalEconomy = new GlobalEconomy();
    @Override
    public void reportEconomyTick(int iterIndex) {

    }
    @Override
    public void reportEconomyMonthEnd() {
        globalEconomy.updateSource();
    }
}
