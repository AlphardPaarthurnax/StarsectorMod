package eco.core;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.campaign.econ.reach.CommodityMarketData;
import eco.EconomyConfig;
import eco.ui.IMarketBridge;
import eco.core.trade.TradeDeal;
import eco.core.trade.TradeOffer;

import java.io.Serializable;
import java.util.*;

import static eco.core.EconomyService.computePriceMultiplier;

/**
 * <p> baseSupply       满效率和
 * <p> baseDemand       满效率和
 * <p> actualSupply     *eff和
 * <p> actualDemand     *eff和
 * <p> netSD            SD和
 * */
public class PlanetEconomy implements Serializable {
    //<editor-fold desc="Inline Data">
    // ***************
    // * Inline Data *
    // ***************
    private final MarketAPI market;
    private Map<String, Integer> netSD = new HashMap<>();
    private Map<String, Integer> baseSupply = new HashMap<>();
    private Map<String, Integer> baseDemand = new HashMap<>();
    private Map<String, Integer> actualSupply = new HashMap<>();
    private Map<String, Integer> actualDemand = new HashMap<>();
    public MarketAPI getMarket(){
        return market;
    }
    public Map<String, Integer> getNetSD() { return netSD; }
    public Map<String, Integer> getAllBaseSupply() { return baseSupply; }
    public int getBaseSupply(String commodityId) { return baseSupply.getOrDefault(commodityId, 0); }
    public Map<String, Integer> getAllBaseDemand() { return baseDemand; }
    public int getBaseDemand(String commodityId) { return baseDemand.getOrDefault(commodityId, 0); }
    public Map<String, Integer> getAllActualSupply() { return actualSupply; }
    public int getActualSupply(String commodityId) { return actualSupply.getOrDefault(commodityId, 0); }
    public Map<String, Integer> getAllActualDemand() { return actualDemand; }
    public int getActualDemand(String commodityId) { return actualDemand.getOrDefault(commodityId, 0); }
    //</editor-fold>

    //<editor-fold desc="Useless Data Stream">
    // ***********************
    // * Useless Data Stream *
    // ***********************
    private final PlanetAPI planet;
    private Set<String> commodityIds = new HashSet<>();
    private Profit profit = new Profit();
    private Map<String, Integer> domesticDemand = new HashMap<>();
    public PlanetAPI getPlanet(){
        return planet;
    }
    public String getPlanetName() { return planet != null ? planet.getName() : "(station)"; }
    public int getMarketSize() { return market.getSize(); }
    public String getMarketName() { return market.getName(); }
    public String getFactionId() { return market.getFaction().getId(); }
    public Set<String> getCommodityIds() {
        return commodityIds;
    }
    public Profit getProfit() {
        if (profit == null) profit = new Profit();
        return profit;
    }
    public Map<String, Integer> getDomesticDemand() { return domesticDemand; }
    //</editor-fold>

    //<editor-fold desc="UI Data Stream">
    // ******************
    // * UI Data Stream *
    // ******************
    private Map<String, MutableStatWithTempMods> available = new HashMap<>();
    public Map<String, MutableStatWithTempMods> getAllAvailable() {
        return available;
    }
    public MutableStatWithTempMods getAvailable(String commodityId) {
        return available.get(commodityId);
    }
    //</editor-fold>

    //<editor-fold desc="Logic Data Stream">
    // *********************
    // * Logic Data Stream *
    // *********************
    private Map<Industry, IndustryEconomy> industryEconomys = new HashMap<>();
    private Map<String, Long> stock = new HashMap<>();
    private Map<String, Float> prices = new HashMap<>();
    private Map<String, TradeOffer> supply = new HashMap<>();
    private Map<String, TradeOffer> demand = new HashMap<>();
    private List<TradeDeal> importTrade = new ArrayList<>();
    private List<TradeDeal> exportTrade = new ArrayList<>();
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
    public void addStock(String commodityId, long delta) {
        stock.merge(commodityId, delta, Long::sum);
    }
    //</editor-fold>;


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
        for(IndustryEconomy industryEco : industryEconomys.values()){
            industryEco.updateSource(getPeopleScale(market.getSize()));
            industryEco.updateReferenceSupplyDemand();

            for(Map.Entry<String, Integer> SI : industryEco.getAllRefSupply().entrySet()){
                baseSupply.merge(SI.getKey(), SI.getValue(), Integer::sum);
            }
            for(Map.Entry<String, Integer> DI : industryEco.getAllRefDemand().entrySet()){
                baseDemand.merge(DI.getKey(), DI.getValue(), Integer::sum);
            }
        }
        // 更新
        if (stock.isEmpty()){
            for(IndustryEconomy industryEco : industryEconomys.values()){
                for(Map.Entry<String, Integer> SI : industryEco.getAllRefSupply().entrySet()){
                    stock.merge(SI.getKey(), (long) SI.getValue(), Long::sum);
                }
                for(Map.Entry<String, Integer> DI : industryEco.getAllRefDemand().entrySet()){
                    stock.merge(DI.getKey(), (long) DI.getValue(), Long::sum);
                }
            }
        }
        for(Map.Entry<String, Long> SL : stock.entrySet()){
            if (!prices.containsKey(SL.getKey())) {
                prices.put(SL.getKey(), Global.getSettings().getCommoditySpec(SL.getKey()).getBasePrice());
            }
        }
        // init Stock & Prices
        Map<String, Float> effList = new HashMap<>();
        for(Map.Entry<String, Long> SL : stock.entrySet()){
            effList.put(SL.getKey(), (float) ((double) SL.getValue() / baseDemand.getOrDefault(SL.getKey(),0)));
        }
        // efflist
        for(Map.Entry<Industry, IndustryEconomy> industryEco : industryEconomys.entrySet()){
            industryEco.getValue().updateEfficiency(effList);
            industryEco.getValue().updateSupplyDemand();
            updateStock(industryEco.getValue());
        }
        for (IndustryEconomy ie : industryEconomys.values()) {
            commodityIds.addAll(ie.getCommodityIds());
        }
    }
    private void updateStock(IndustryEconomy industryEconomy){
        for(Map.Entry<String, Integer> supplySI : industryEconomy.getAllSupply().entrySet()){
            stock.merge(supplySI.getKey(),(long) supplySI.getValue(),Long::sum);
        }
        if(!industryEconomy.isOverload()){
            for(Map.Entry<String, Integer> demandSI : industryEconomy.getAllDemand().entrySet()){
                stock.merge(demandSI.getKey(),(long) -demandSI.getValue(),Long::sum);
            }
        }
    }
    public void updateSupplyDemand(){
        netSD.clear();
        supply.clear();
        demand.clear();
        actualSupply.clear();
        actualDemand.clear();
        domesticDemand.clear();
        importTrade.clear();
        exportTrade.clear();
        available.clear();

        Map<String, MutableStat> demandMStB = new HashMap<>();

        for (IndustryEconomy ie : industryEconomys.values()) {
            for (Map.Entry<String, Integer> s : ie.getAllSupply().entrySet()) {
                actualSupply.merge(s.getKey(), s.getValue(), Integer::sum);
                netSD.merge(s.getKey(), s.getValue(), Integer::sum);
            }
            for (Map.Entry<String, Integer> d : ie.getAllRefDemand().entrySet()) {
                netSD.merge(d.getKey(), -d.getValue(), Integer::sum);
                demandMStB.computeIfAbsent(market.getCommodityData(d.getKey()).getDemandClass(), i -> new MutableStat(0))
                        .modifyFlat("c3ore_" + ie.getIndustry().getCurrentName(), d.getValue(), ie.getIndustry().getCurrentName());
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
        for (Map.Entry<String, Integer> entry : actualSupply.entrySet()) {
            String commodityId = entry.getKey();
            int amount = Math.min(entry.getValue(), actualDemand.getOrDefault(commodityId, 0));
            if (amount > 0) {
                domesticDemand.put(commodityId, amount);
            }
        }
        for(Map.Entry<String, MutableStat> SMS : demandMStB.entrySet()){
            // 直写 vanilla MarketDemand.getDemand() 堆（不再走 bridge/mixin）：
            // 先移除该需求堆上所有上月的 c3ore_* 修饰（产业可能消失/改名/停供，旧 key 不会
            // 被 vanilla 清除），再写入本月各产业聚合后的需求。
            MutableStat demandStat = market.getDemand(SMS.getKey()).getDemand();
            for (String source : new ArrayList<>(demandStat.getFlatMods().keySet())) {
                if (source.startsWith("c3ore_")) {
                    demandStat.unmodify(source);
                }
            }
            for (Map.Entry<String, MutableStat.StatMod> mod : SMS.getValue().getFlatMods().entrySet()) {
                demandStat.modifyFlat(mod.getKey(), mod.getValue().value, mod.getValue().desc);
            }
        }

        Map<String, Map<FactionAPI, Integer>> importSFI = new HashMap<>();
        Map<String, Map<FactionAPI, Integer>> exportSFI = new HashMap<>();
        for(TradeDeal tradeDeal : importTrade) {
            importSFI.computeIfAbsent(tradeDeal.getItemId(), i -> new HashMap<>())
                    .merge(tradeDeal.getFromFaction(), tradeDeal.getItemNum(), Integer::sum);
        }
        for(TradeDeal tradeDeal : exportTrade) {
            exportSFI.computeIfAbsent(tradeDeal.getItemId(), i -> new HashMap<>())
                    .merge(tradeDeal.getToFaction(), tradeDeal.getItemNum(), Integer::sum);
        }

        for(String commodityId : commodityIds) {
            MutableStatWithTempMods MSWTM = new MutableStatWithTempMods(0);
            MSWTM.modifyFlat(CommodityMarketData.KEY_LOCAL, actualSupply.getOrDefault(commodityId,0),"本地产量");

            Map<FactionAPI, Integer> importFI = importSFI.getOrDefault(commodityId, new HashMap<>());
            for(Map.Entry<FactionAPI, Integer> FI : importFI.entrySet()){
                MSWTM.modifyFlat(CommodityMarketData.KEY_IMPORTS + "_" + FI.getKey().getId(), FI.getValue(),"从 " + FI.getKey().getDisplayName() + " 进口");
            }

            Map<FactionAPI, Integer> exportFI = exportSFI.getOrDefault(commodityId, new HashMap<>());
            for(Map.Entry<FactionAPI, Integer> FI : exportFI.entrySet()){
                MSWTM.modifyFlat(CommodityMarketData.KEY_SHORTAGE + "_" + FI.getKey().getId(), -FI.getValue(),"向 " + FI.getKey().getDisplayName() + " 出口");
            }

            MSWTM.modifyFlat(CommodityMarketData.KEY_LOWACCESS, -actualDemand.getOrDefault(commodityId,0),"本地消耗");

            available.put(commodityId, MSWTM);
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
        allIds.addAll(baseDemand.keySet());
        allIds.addAll(stock.keySet());

        for (String cid : allIds) {
            float sysPrice = systemPrices.getOrDefault(cid,
                    Global.getSettings().getCommoditySpec(cid).getBasePrice());
            int s = actualSupply.getOrDefault(cid, 0);
            int d = baseDemand.getOrDefault(cid, 0);
            long st = stock.getOrDefault(cid, 0L);
            float mult = computePriceMultiplier(s, d, st);
            prices.put(cid, sysPrice * mult);
        }

        for (IndustryEconomy ie : industryEconomys.values()) {
            ie.updateProfit();
        }
    }
    public void updatePlanetProfit() {
        getProfit().clear();

        for (IndustryEconomy ie : industryEconomys.values()) {
            getProfit().addIncome(ie.getIncome());
            getProfit().addUpkeep(ie.getUpkeep());
        }
        for (TradeDeal deal : exportTrade) {
            getProfit().addExport(deal.getExportProfit());
        }
        for (TradeDeal deal : importTrade) {
            getProfit().addImportCost(deal.getImportCost());
        }
        for(Map.Entry<String, Integer> dd : domesticDemand.entrySet()){
            getProfit().addTax(dd.getValue() * getPrice(dd.getKey()) * EconomyConfig.getInternalTradeTaxRate());
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
    public void updateCollectData() {
        for(IndustryEconomy ie : industryEconomys.values()) {
            ie.updateCollectData();
        }
        if (market instanceof IMarketBridge) {
            ((IMarketBridge) market).ECON$dataUpdate(this);
        }
    }
}
