package eco.core.trade;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import eco.core.PlanetEconomy;

public class TradeOffer {
    private PlanetEconomy planetEconomy;
    private String itemId;
    private int itemNum;
    private float itemPrice;
    public TradeOffer(PlanetEconomy planetEconomy, String itemId, int itemNum, float itemPrice){
        this.planetEconomy = planetEconomy;
        this.itemId = itemId;
        this.itemNum = itemNum;
        this.itemPrice = itemPrice;
    }
    public PlanetEconomy getPlanetEconomy() { return planetEconomy; }
    public StarSystemAPI getStarSystem() { return planetEconomy.getMarket().getStarSystem(); }
    public MarketAPI getMarket() { return planetEconomy.getMarket(); }
    public FactionAPI getFaction() { return planetEconomy.getMarket().getFaction(); }
    public String getItemId() {
        return itemId;
    }
    public int getItemNum() {
        return itemNum;
    }
    public float getItemPrice() {
        return itemPrice;
    }
    public void addItemNum(int itemNum) { this.itemNum += itemNum;}
}
