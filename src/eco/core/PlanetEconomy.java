package eco.core;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketDemandAPI;
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
import static eco.core.EconomyService.getPeopleScale;

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
    private Map<String, Integer> refSupply = new HashMap<>();
    private Map<String, Integer> refDemand = new HashMap<>();
    private Map<String, Integer> actualSupply = new HashMap<>();
    private Map<String, Integer> actualDemand = new HashMap<>();
    public MarketAPI getMarket(){
        return market;
    }
    public Map<String, Integer> getNetSD() { return netSD; }
    public Map<String, Integer> getAllBaseSupply() { return refSupply; }
    public int getBaseSupply(String commodityId) { return refSupply.getOrDefault(commodityId, 0); }
    public Map<String, Integer> getAllBaseDemand() { return refDemand; }
    public int getBaseDemand(String commodityId) { return refDemand.getOrDefault(commodityId, 0); }
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
    private Profit profit = new Profit();
    private Map<String, Integer> domesticDemand = new HashMap<>();
    public PlanetAPI getPlanet(){
        return planet;
    }
    public String getPlanetName() { return planet != null ? planet.getName() : "(station)"; }
    public int getMarketSize() { return market.getSize(); }
    public String getMarketName() { return market.getName(); }
    public String getFactionId() { return market.getFaction().getId(); }
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
    private Map<String, TradeOffer> supplyTradeOffer = new HashMap<>();
    private Map<String, TradeOffer> demandTradeOffer = new HashMap<>();
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
    public Map<String, TradeOffer> getSupplyOffers() { return supplyTradeOffer; }
    public Map<String, TradeOffer> getDemandOffers() { return demandTradeOffer; }
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

    private static final String MONTH_TIMER_KEY = "cc_debug_monthtimer";
    private static final String DEBUG_MONTH_TIMER_SUB = "cc_debug_monthtimer_sub";
    private static final String MARKET_DEMAND_PREFIX = "c3ore_";
    private Map<String, Float> efficiencyList = new HashMap<>();

    public PlanetEconomy(MarketAPI market){
        this.market = market;
        this.planet = market.getPlanetEntity();
    }
    public void preUpdate() {
        efficiencyList.clear();
        refSupply.clear();
        refDemand.clear();
        importTrade.clear();
        exportTrade.clear();

        // 同步 industry
        for (Industry industry : market.getIndustries()) {
            if (!industryEconomys.containsKey(industry)) {
                industryEconomys.put(industry, new IndustryEconomy(industry));
            }
        }
        Set<Industry> currentSet = new HashSet<>(market.getIndustries());
        industryEconomys.keySet().removeIf(ind -> !currentSet.contains(ind));

        // 从 IE 更新数据
        for(IndustryEconomy industryEco : industryEconomys.values()){

            industryEco.preUpdate(getPeopleScale(market.getSize()));

            for(Map.Entry<String, Integer> SI : industryEco.getAllRefSupply().entrySet()){
                refSupply.merge(SI.getKey(), SI.getValue(), Integer::sum);
            }
            for(Map.Entry<String, Integer> DI : industryEco.getAllRefDemand().entrySet()){
                refDemand.merge(DI.getKey(), DI.getValue(), Integer::sum);
            }
        }

        // 初始化 库存/价格
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
    }
    public void Update() {
        netSD.clear();
        supplyTradeOffer.clear();
        demandTradeOffer.clear();
        actualSupply.clear();
        actualDemand.clear();
        domesticDemand.clear();

        for(IndustryEconomy industryEconomy : industryEconomys.values()){
            industryEconomy.Update(this);

            // 库存更新 - SD
            for(Map.Entry<String, Integer> supplySI : industryEconomy.getAllSupply().entrySet()){
                stock.merge(supplySI.getKey(),(long) supplySI.getValue(),Long::sum);
            }
            if(!industryEconomy.isOverload()){
                for(Map.Entry<String, Integer> demandSI : industryEconomy.getAllDemand().entrySet()){
                    stock.merge(demandSI.getKey(),(long) -demandSI.getValue(),Long::sum);
                }
            }
        }

        // 真实值与期望值
        for (IndustryEconomy ie : industryEconomys.values()) {
            for (Map.Entry<String, Integer> s : ie.getAllSupply().entrySet()) {
                actualSupply.merge(s.getKey(), s.getValue(), Integer::sum);
                netSD.merge(s.getKey(), s.getValue(), Integer::sum);
            }
            for (Map.Entry<String, Integer> d : ie.getAllRefDemand().entrySet()) {
                netSD.merge(d.getKey(), -d.getValue(), Integer::sum);
            }
            for (Map.Entry<String, Integer> d : ie.getAllDemand().entrySet()){
                actualDemand.merge(d.getKey(), d.getValue(), Integer::sum);
            }
        }

        // 生成 TradeOffer
        for (Map.Entry<String, Integer> netSDSI : netSD.entrySet()){
            if(netSDSI.getValue() > 0){
                supplyTradeOffer.put(netSDSI.getKey(), new TradeOffer(this, netSDSI.getKey(), netSDSI.getValue(), getPrice(netSDSI.getKey())));
            } else if(netSDSI.getValue() < 0){
                demandTradeOffer.put(netSDSI.getKey(), new TradeOffer(this, netSDSI.getKey(), netSDSI.getValue(), getPrice(netSDSI.getKey())));
            }
        }

        // 计算内销
        for (Map.Entry<String, Integer> entry : actualSupply.entrySet()) {
            String commodityId = entry.getKey();
            int amount = Math.min(entry.getValue(), actualDemand.getOrDefault(commodityId, 0));
            if (amount > 0) {
                domesticDemand.put(commodityId, amount);
            }
        }
    }
    public void postUpdate(Map<String, Float> systemPrices) {
        prices.clear();
        getProfit().clear();

        // 价格
        Set<String> allIds = new HashSet<>();
        allIds.addAll(actualSupply.keySet());
        allIds.addAll(refDemand.keySet());
        allIds.addAll(stock.keySet());
        for (String cid : allIds) {
            float sysPrice = systemPrices.getOrDefault(cid,
                    Global.getSettings().getCommoditySpec(cid).getBasePrice());
            int s = actualSupply.getOrDefault(cid, 0);
            int d = refDemand.getOrDefault(cid, 0);
            long st = stock.getOrDefault(cid, 0L);
            float mult = computePriceMultiplier(s, d, st);
            prices.put(cid, sysPrice * mult);
        }

        // IE 后更新
        for (IndustryEconomy ie : industryEconomys.values()) {
            ie.postUpdate();
        }

        // 利润
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

        // 市场需求 MutableStat
        postUpdateMarketDemandStats();

        // 商品可用量 MutableStatWithTempMods
        postUpdateAvailableStats();

        if (market instanceof IMarketBridge) {
            ((IMarketBridge) market).ECON$dataUpdate(this);
        }
    }

    private void postUpdateMarketDemandStats() {
        Map<String, Map<String, Integer>> demandByClass = new HashMap<>();
        Map<String, Map<String, String>> demandDescriptions = new HashMap<>();
        Set<String> demandClasses = new HashSet<>();

        // 收集市场中全部需求类别，确保产业消失后也能清理遗留的 c3ore_* 修正。
        for (CommodityOnMarketAPI commodity : market.getAllCommodities()) {
            if (commodity.getDemandClass() != null) {
                demandClasses.add(commodity.getDemandClass());
            }
        }

        for (IndustryEconomy ie : industryEconomys.values()) {
            String industryName = ie.getIndustry().getCurrentName();
            String source = MARKET_DEMAND_PREFIX + industryName;

            for (Map.Entry<String, Integer> entry : ie.getAllRefDemand().entrySet()) {
                CommodityOnMarketAPI commodity = market.getCommodityData(entry.getKey());
                if (commodity == null || commodity.getDemandClass() == null) continue;

                String demandClass = commodity.getDemandClass();
                demandClasses.add(demandClass);
                demandByClass.computeIfAbsent(demandClass, key -> new LinkedHashMap<>())
                        .merge(source, entry.getValue(), Integer::sum);
                demandDescriptions.computeIfAbsent(demandClass, key -> new HashMap<>())
                        .put(source, industryName);
            }
        }

        for (String demandClass : demandClasses) {
            MarketDemandAPI marketDemand = market.getDemand(demandClass);
            if (marketDemand == null) continue;

            MutableStat demandStat = marketDemand.getDemand();

            if (demandStat.getFlatStatMod(MONTH_TIMER_KEY) == null) {
                demandStat.modifyFlat(MONTH_TIMER_KEY, -1);
            }
            if (demandStat.getFlatStatMod(MONTH_TIMER_KEY).getValue() == GlobalEconomy.getInstance().getMonth())
                continue;

            for (String source : new ArrayList<>(demandStat.getFlatMods().keySet())) {
                if (source.startsWith(MARKET_DEMAND_PREFIX)) {
                    demandStat.unmodify(source);
                }
            }

            Map<String, Integer> classDemand = demandByClass.getOrDefault(demandClass, Collections.emptyMap());
            Map<String, String> classDescriptions = demandDescriptions.getOrDefault(demandClass, Collections.emptyMap());
            for (Map.Entry<String, Integer> entry : classDemand.entrySet()) {
                demandStat.modifyFlat(entry.getKey(), entry.getValue(), classDescriptions.get(entry.getKey()));
            }

            demandStat.unmodify(MONTH_TIMER_KEY);
            demandStat.unmodify(DEBUG_MONTH_TIMER_SUB);
            demandStat.modifyFlat(MONTH_TIMER_KEY, GlobalEconomy.getInstance().getMonth());
            demandStat.modifyFlat(DEBUG_MONTH_TIMER_SUB, -GlobalEconomy.getInstance().getMonth());
        }
    }

    private void postUpdateAvailableStats() {
        Map<String, Map<FactionAPI, Integer>> importsByCommodity = new HashMap<>();
        Map<String, Map<FactionAPI, Integer>> exportsByCommodity = new HashMap<>();
        Set<String> allIds = new HashSet<>();
        allIds.addAll(actualSupply.keySet());
        allIds.addAll(actualDemand.keySet());
        allIds.addAll(stock.keySet());

        for (TradeDeal tradeDeal : importTrade) {
            allIds.add(tradeDeal.getItemId());
            importsByCommodity.computeIfAbsent(tradeDeal.getItemId(), key -> new HashMap<>())
                    .merge(tradeDeal.getFromFaction(), tradeDeal.getItemNum(), Integer::sum);
        }
        for (TradeDeal tradeDeal : exportTrade) {
            allIds.add(tradeDeal.getItemId());
            exportsByCommodity.computeIfAbsent(tradeDeal.getItemId(), key -> new HashMap<>())
                    .merge(tradeDeal.getToFaction(), tradeDeal.getItemNum(), Integer::sum);
        }

        // 清空引用表后，CommodityOnMarketMixin 会回落到原版 getter；随后保存原版 Stat 引用。
        available.clear();
        for (String commodityId : allIds) {
            CommodityOnMarketAPI commodity = market.getCommodityData(commodityId);
            if (commodity == null) continue;

            MutableStatWithTempMods availableStat = commodity.getAvailableStat();
            if (availableStat == null) continue;
            available.put(commodityId, availableStat);

            if (availableStat.getFlatStatMod(MONTH_TIMER_KEY) == null) {
                availableStat.modifyFlat(MONTH_TIMER_KEY, -1);
            }
            if (availableStat.getFlatStatMod(MONTH_TIMER_KEY).getValue() == GlobalEconomy.getInstance().getMonth())
                continue;

            for (String source : new ArrayList<>(availableStat.getFlatMods().keySet())) {
                availableStat.unmodify(source);
            }

            availableStat.modifyFlat(CommodityMarketData.KEY_LOCAL,
                    actualSupply.getOrDefault(commodityId, 0), "本地产量");

            for (Map.Entry<FactionAPI, Integer> entry : importsByCommodity
                    .getOrDefault(commodityId, Collections.emptyMap()).entrySet()) {
                FactionAPI faction = entry.getKey();
                availableStat.modifyFlat(CommodityMarketData.KEY_IMPORTS + "_" + faction.getId(),
                        entry.getValue(), "从 " + faction.getDisplayName() + " 进口");
            }

            for (Map.Entry<FactionAPI, Integer> entry : exportsByCommodity
                    .getOrDefault(commodityId, Collections.emptyMap()).entrySet()) {
                FactionAPI faction = entry.getKey();
                availableStat.modifyFlat(CommodityMarketData.KEY_SHORTAGE + "_" + faction.getId(),
                        -entry.getValue(), "向 " + faction.getDisplayName() + " 出口");
            }

            availableStat.modifyFlat(CommodityMarketData.KEY_LOWACCESS,
                    -actualDemand.getOrDefault(commodityId, 0), "本地消耗");

            availableStat.unmodify(MONTH_TIMER_KEY);
            availableStat.unmodify(DEBUG_MONTH_TIMER_SUB);
            availableStat.modifyFlat(MONTH_TIMER_KEY, GlobalEconomy.getInstance().getMonth());
            availableStat.modifyFlat(DEBUG_MONTH_TIMER_SUB, -GlobalEconomy.getInstance().getMonth());
        }
    }
    // 对IE
    public float getEfficiency(String commodityID) {
        if(!efficiencyList.containsKey(commodityID)) {
            efficiencyList.put(commodityID, (float) ((double) stock.getOrDefault(commodityID, 0L) / refDemand.getOrDefault(commodityID,0)));
        }
        return efficiencyList.get(commodityID);
    }
    // 对SE
    public void updateTradeStock() {
        for (TradeDeal d : importTrade) {
            stock.merge(d.getItemId(), (long) d.getItemNum(), Long::sum);
        }
        for (TradeDeal d : exportTrade) {
            stock.merge(d.getItemId(), (long) -d.getItemNum(), Long::sum);
        }
    }
}
