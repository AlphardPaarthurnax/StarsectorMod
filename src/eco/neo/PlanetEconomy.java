package eco.neo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import eco.neo.trade.TradeDeal;
import eco.neo.trade.TradeOffer;

import java.util.*;

public class PlanetEconomy {
    private final PlanetAPI planet;
    private final MarketAPI market;
    private Map<Industry, IndustryEconomy> industryEconomys = new HashMap<>();
    private Map<String, Long> stock = new HashMap<>();
    private Map<String, Float> prices = new HashMap<>();
    private Set<String> commodityIds = new HashSet<>();
    public PlanetAPI getPlanet(){
        return planet;
    }
    public MarketAPI getMarket(){
        return market;
    }
    public IndustryEconomy getIndustryEconomy(Industry industry){
        return industryEconomys.getOrDefault(industry, new IndustryEconomy(industry));
    }
    public Map<Industry, IndustryEconomy> getAllIndustryEconomy(){
        return industryEconomys;
    }
    public Long getStock(String commodityId){
        return stock.getOrDefault(commodityId, 0L);
    }
    public Map<String, Long> getAllStock(){
        return stock;
    }
    public Float getPrice(String commodityId) {
        if(!prices.containsKey(commodityId)){
            prices.put(commodityId, Global.getSettings().getCommoditySpec(commodityId).getBasePrice());
        }
        return prices.getOrDefault(commodityId, 0f);
    }
    public Map<String, Float> getAllPrice() {
        return prices;
    }
    public Set<String> getCommodityIds() {
        return commodityIds;
    }

    private float planetProfit = 0;
    private Map<String, Integer> actualSupply = new HashMap<>();
    private Map<String, Integer> actualDemand = new HashMap<>();
    private Map<String, TradeOffer> supply = new HashMap<>();
    private Map<String, TradeOffer> demand = new HashMap<>();
    private List<TradeDeal> importTrade = new ArrayList<>();
    private List<TradeDeal> exportTrade = new ArrayList<>();
    private Map<String, Integer> netSD = new HashMap<>();
    private Map<String, Integer> baseSupply = new HashMap<>();
    private Map<String, Integer> baseDemand = new HashMap<>();
    public PlanetEconomy(MarketAPI market){
        this.market = market;
        this.planet = market.getPlanetEntity();
    }
    public void updateSource(){
        baseSupply.clear();
        baseDemand.clear();
        commodityIds.clear();

        for (Industry industry : market.getIndustries()) {
            if (!industryEconomys.containsKey(industry)) {
                industryEconomys.put(industry, new IndustryEconomy(industry));
            }
        }
        Set<Industry> currentSet = new HashSet<>(market.getIndustries());
        industryEconomys.keySet().removeIf(ind -> !currentSet.contains(ind));
        // 同步
        for(Map.Entry<Industry, IndustryEconomy> industryEco : industryEconomys.entrySet()){
            industryEco.getValue().updateSource(getPeopleScale(market.getSize()));

            Map<String, Map<String, Integer>> base = industryEco.getValue().getReferenceSupplyDemand();
            for(Map.Entry<String, Integer> SI : base.get("s").entrySet()){
                baseSupply.merge(SI.getKey(), SI.getValue(), Integer::sum);
            }
            for(Map.Entry<String, Integer> DI : base.get("d").entrySet()){
                baseDemand.merge(DI.getKey(), DI.getValue(), Integer::sum);
            }
        }
        // 更新
        if (stock.isEmpty()){
            for(Map.Entry<Industry, IndustryEconomy> industryEco : industryEconomys.entrySet()){
                Map<String, Map<String, Integer>> reference = industryEco.getValue().getReferenceSupplyDemand();
                for(Map.Entry<String, Integer> SI : reference.get("s").entrySet()){
                    stock.merge(SI.getKey(), (long) SI.getValue(), Long::sum);
                }
                for(Map.Entry<String, Integer> DI : reference.get("d").entrySet()){
                    stock.merge(DI.getKey(), (long) DI.getValue(), Long::sum);
                }
            }
        }
        for(Map.Entry<String, Long> SL : stock.entrySet()){
            if (!prices.containsKey(SL.getKey())) {
                prices.put(SL.getKey(), Global.getSettings().getCommoditySpec(SL.getKey()).getBasePrice());
            }
        }
        // initStock
        Map<String, Float> effList = new HashMap<>();
        for(Map.Entry<String, Long> SL : stock.entrySet()){
            effList.put(SL.getKey(), (float) ((double) SL.getValue() / baseDemand.getOrDefault(SL.getKey(),0)));
        }
        // efflist
        for(Map.Entry<Industry, IndustryEconomy> industryEco : industryEconomys.entrySet()){
            industryEco.getValue().updateEfficiency(effList);
            industryEco.getValue().updateSupplyDemand();
            if(!industryEco.getValue().isOverload()){
                updateStock(industryEco.getValue());
            }
        }
        for (IndustryEconomy ie : industryEconomys.values()) {
            commodityIds.addAll(ie.getCommodityIds());
        }
    }
    private void updateStock(IndustryEconomy industryEconomy){
        for(Map.Entry<String, Integer> supplySI : industryEconomy.getAllSupply().entrySet()){
            stock.merge(supplySI.getKey(),(long) supplySI.getValue(),Long::sum);
        }
        for(Map.Entry<String, Integer> demandSI : industryEconomy.getAllDemand().entrySet()){
            stock.merge(demandSI.getKey(),(long) -demandSI.getValue(),Long::sum);
        }
    }
    public void updateSupplyDemand(){
        netSD.clear();
        supply.clear();
        demand.clear();
        actualSupply.clear();
        actualDemand.clear();
        importTrade.clear();
        exportTrade.clear();

        for (IndustryEconomy ie : industryEconomys.values()) {
            for (Map.Entry<String, Integer> s : ie.getAllSupply().entrySet()) {
                actualSupply.merge(s.getKey(), s.getValue(), Integer::sum);
                netSD.merge(s.getKey(), s.getValue(), Integer::sum);
            }
            Map<String, Map<String, Integer>> ref = ie.getReferenceSupplyDemand();
            for (Map.Entry<String, Integer> d : ref.get("d").entrySet()) {
                netSD.merge(d.getKey(), -d.getValue(), Integer::sum);
            }
            for (Map.Entry<String, Integer> d : ie.getAllDemand().entrySet()){
                actualDemand.merge(d.getKey(), d.getValue(), Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> netSDSI : netSD.entrySet()){
            if(netSDSI.getValue() > 0){
                supply.put(netSDSI.getKey(), new TradeOffer(this, netSDSI.getKey(), netSDSI.getValue(), getPrice(netSDSI.getKey())));
            } else if(netSDSI.getValue() < 0){
                demand.put(netSDSI.getKey(), new TradeOffer(this, netSDSI.getKey(), netSDSI.getValue(), getPrice(netSDSI.getKey())));
            }
        }
    }
    public void updateTradeStock() {
        for (TradeDeal d : importTrade) {
            stock.merge(d.getItemId(), (long) d.getItemNum(), Long::sum);
        }
        for (TradeDeal d : exportTrade) {
            stock.merge(d.getItemId(), (long) -d.getItemNum(), Long::sum);
        }
    }
    public void updatePlanetPrices(Map<String, Float> systemPrices) {
        prices.clear();
        Set<String> allIds = new HashSet<>();
        allIds.addAll(actualSupply.keySet());
        allIds.addAll(actualDemand.keySet());
        allIds.addAll(stock.keySet());

        for (String cid : allIds) {
            float sysPrice = systemPrices.getOrDefault(cid,
                    Global.getSettings().getCommoditySpec(cid).getBasePrice());
            int s = actualSupply.getOrDefault(cid, 0);
            int d = actualDemand.getOrDefault(cid, 0);
            long st = stock.getOrDefault(cid, 0L);
            float mult = PriceCalculator.computePriceMultiplier(s, d, st);
            prices.put(cid, sysPrice * mult);
        }

        for (IndustryEconomy ie : industryEconomys.values()) {
            ie.updateProfit(prices);
        }
    }
    public void updatePlanetProfit() {
        planetProfit = 0;
        for (IndustryEconomy ie : industryEconomys.values()) {
            planetProfit += ie.getProfit();
        }
    }
    private static float getPeopleScale(int size) {
        if (size <= 1) return 0.01f;
        if (size == 2) return 0.10f;
        if (size == 3) return 1.0f;

        float result = 1.0f;
        final float r = 0.772f;
        for (int s = 4; s <= size; s++) {
            result *= (1.0f + 9.0f * (float) Math.pow(r, s - 4));
        }
        return result;
    }

    public Map<String, Integer> getNetSD() { return netSD; }
    public Map<String, Integer> getBaseSupply() { return baseSupply; }
    public Map<String, Integer> getBaseDemand() { return baseDemand; }
    public String getPlanetName() { return planet != null ? planet.getName() : "(station)"; }
    public int getMarketSize() { return market.getSize(); }
    public String getMarketName() { return market.getName(); }
    public String getFactionId() { return market.getFaction().getId(); }
    public Map<String, TradeOffer> getSupplyOffers() { return supply; }
    public Map<String, TradeOffer> getDemandOffers() { return demand; }
    public List<TradeDeal> getImportTrade() { return importTrade; }
    public List<TradeDeal> getExportTrade() { return exportTrade; }
    public void addImportTrade(TradeDeal tradeDeal){
        importTrade.add(tradeDeal);
    }
    public void addExportTrade(TradeDeal tradeDeal){
        exportTrade.add(tradeDeal);
    }
    public Map<String, Integer> getAllActualSupply() { return actualSupply; }
    public Map<String, Integer> getAllActualDemand() { return actualDemand; }
    public float getPlanetProfit() { return planetProfit; }
}
