package eco.trade;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import eco.EconomyConfig;
import eco.core.PlanetEconomy;

public class TradeDeal {
    private PlanetEconomy fromPlanetEconomy;
    private PlanetEconomy toPlanetEconomy;
    private String itemId;
    private int itemNum;
    private float itemPrice;
    private float distanceLY;
    public TradeDeal(TradeOffer supplyOffer, TradeOffer demandOffer){
        this.fromPlanetEconomy = supplyOffer.getPlanetEconomy();
        this.toPlanetEconomy = demandOffer.getPlanetEconomy();
        this.itemId = supplyOffer.getItemId();
        this.itemNum = Math.min(supplyOffer.getItemNum(), -demandOffer.getItemNum());
        this.itemPrice = Math.max(0f, supplyOffer.getItemPrice());
        this.distanceLY = calculateDistanceLY(supplyOffer, demandOffer);
        supplyOffer.addItemNum(-itemNum);
        demandOffer.addItemNum(itemNum);
        fromPlanetEconomy.addExportTrade(this);
        toPlanetEconomy.addImportTrade(this);
    }

    private static float calculateDistanceLY(TradeOffer supplyOffer, TradeOffer demandOffer) {
        if (supplyOffer.getStarSystem() == demandOffer.getStarSystem()) return 0f;
        return Math.max(0f, Misc.getDistanceLY(
                supplyOffer.getMarket().getLocationInHyperspace(),
                demandOffer.getMarket().getLocationInHyperspace()));
    }
    public PlanetEconomy getFromPlanetEconomy() { return fromPlanetEconomy; }
    public StarSystemAPI getFromStarSystem() { return fromPlanetEconomy.getMarket().getStarSystem(); }
    public MarketAPI getFromMarket() { return fromPlanetEconomy.getMarket(); }
    public FactionAPI getFromFaction() { return fromPlanetEconomy.getMarket().getFaction(); }
    public PlanetEconomy getToPlanetEconomy() { return toPlanetEconomy; }
    public StarSystemAPI getToStarSystem() { return toPlanetEconomy.getMarket().getStarSystem(); }
    public MarketAPI getToMarket() { return toPlanetEconomy.getMarket(); }
    public FactionAPI getToFaction() { return toPlanetEconomy.getMarket().getFaction(); }
    public String getItemId() {
        return itemId;
    }
    public int getItemNum() {
        return itemNum;
    }
    public float getItemPrice() {
        return itemPrice;
    }
    public float getDistanceLY() { return distanceLY; }
    public float getCargoSpacePerUnit() {
        return Math.max(1f, Global.getSettings().getCommoditySpec(itemId).getCargoSpace());
    }
    public float getCargoVolume() {
        return itemNum * getCargoSpacePerUnit();
    }
    public float getFreightCost() {
        return EconomyConfig.getFreightCostPerCargoSpacePerLY() * distanceLY * getCargoVolume();
    }
    public float getExportProfit() { return itemPrice * itemNum - getFreightCost(); }
    public float getImportCost() { return itemPrice * itemNum; }
}
