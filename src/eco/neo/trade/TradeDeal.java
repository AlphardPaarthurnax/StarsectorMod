package eco.neo.trade;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import eco.neo.PlanetEconomy;

public class TradeDeal {
    private PlanetEconomy fromPlanetEconomy;
    private PlanetEconomy toPlanetEconomy;
    private String itemId;
    private int itemNum;
    public float itemPrice;
    public TradeDeal(TradeOffer supplyOffer, TradeOffer demandOffer){
        this.fromPlanetEconomy = supplyOffer.getPlanetEconomy();
        this.toPlanetEconomy = demandOffer.getPlanetEconomy();
        this.itemId = supplyOffer.getItemId();
        this.itemNum = Math.min(supplyOffer.getItemNum(), -demandOffer.getItemNum());
        this.itemPrice = demandOffer.getItemPrice() - supplyOffer.getItemPrice();
        supplyOffer.addItemNum(-itemNum);
        demandOffer.addItemNum(itemNum);
        fromPlanetEconomy.addExportTrade(this);
        toPlanetEconomy.addImportTrade(this);
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
}
