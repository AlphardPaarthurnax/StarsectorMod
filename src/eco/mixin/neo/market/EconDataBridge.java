package eco.mixin.neo.market;

import eco.core.PlanetEconomy;

public interface EconDataBridge {
    void ECON$dataUpdate(PlanetEconomy economy, String id);
}
