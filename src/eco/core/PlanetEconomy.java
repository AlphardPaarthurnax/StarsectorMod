package eco.core;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.campaign.econ.reach.CommodityMarketData;
import eco.EconomyConfig;
import eco.mixin.neo.IBaseIndustryBridge;
import eco.mixin.neo.IMarketBridge;
import eco.trade.TradeDeal;
import eco.trade.TradeOffer;

import java.io.Serializable;
import java.util.*;

import static eco.core.EconomyService.computePriceMultiplier;

public class PlanetEconomy implements Serializable {
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

    private Profit profit = new Profit();
    private Map<String, Integer> actualSupply = new HashMap<>();
    private Map<String, Integer> actualDemand = new HashMap<>();
    private Map<String, TradeOffer> supply = new HashMap<>();
    private Map<String, TradeOffer> demand = new HashMap<>();
    private List<TradeDeal> importTrade = new ArrayList<>();
    private List<TradeDeal> exportTrade = new ArrayList<>();
    private Map<String, Integer> netSD = new HashMap<>();
    private Map<String, Integer> domesticDemand = new HashMap<>();
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

        for (Map.Entry<String, Integer> entry : actualSupply.entrySet()) {
            String commodityId = entry.getKey();
            int amount = Math.min(entry.getValue(), actualDemand.getOrDefault(commodityId, 0));
            if (amount > 0) {
                domesticDemand.put(commodityId, amount);
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
    public Map<String, Integer> getDomesticDemand() { return domesticDemand; }
    public Profit getProfit() {
        if (profit == null) profit = new Profit();
        return profit;
    }

    // **************** //
    // * Market 注回层  *
    // **************** //
    /**
     * 每商品的可贸易量展示 stat（注回原版 available）。
     * 结构 = eco 填写的 4 个原版 key（本地产量/进口/短缺/低可达）
     *       + 外部修改（玩家交易 eMod、其他 mod 注入、cc_econ_external）。
     * 原版经济 step 对 4 key 的写入已被 CommodityMarketDataMixin 屏蔽，
     * 玩家交易 / 其他 mod 的写入（getAvailableStat().modifyFlat 等）直接落这里。
     */
    private final Map<String, MutableStatWithTempMods> availableStats = new HashMap<>();
    /** modifyStock 专用的显示 key：stock 已即时合并，月度重算时跳过不重复累加 */
    private static final String EXTERNAL_MOD_KEY = "cc_econ_external";
    /** 原版经济 step 维护的 4 个 available key（其写入被屏蔽，读取/显示仍用） */
    private static final Set<String> VANILLA_AVAILABLE_KEYS = new HashSet<>(Arrays.asList(CommodityMarketData.KEY_LOCAL, CommodityMarketData.KEY_IMPORTS, CommodityMarketData.KEY_SHORTAGE, CommodityMarketData.KEY_LOWACCESS));
    /**
     * 每商品的可贸易量展示 stat（注回原版 available）。
     * 读取方（UI / 其他 mod）读 eco 值；写入方（玩家交易 eMod、其他 mod 的
     * modifyFlat / addTemporaryModFlat）直接落该 stat，月度合并进 stock。
     */
    public MutableStatWithTempMods getAvailableStat(String commodityId) {
        return availableStats.computeIfAbsent(commodityId, id -> new MutableStatWithTempMods(0f));
    }

    /** 可贸易量（int，与 {@link com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI#getAvailable()} 对齐） */
    public int getAvailable(String commodityId) {
        return Math.max(0, Math.round(getAvailableStat(commodityId).getModifiedValue()));
    }

    /**
     * 外部库存修改入口（mod 的 removeFromStockpile / 玩家交易等）。
     * 立即改 eco stock，并在展示 stat 上记一笔（月度重算时跳过，避免重复累加）。
     */
    public void modifyStock(String commodityId, long delta) {
        if (delta == 0) return;
        stock.merge(commodityId, delta, Long::sum);
        getAvailableStat(commodityId).modifyFlat(EXTERNAL_MOD_KEY, delta, "外部库存修改");
    }

    /** 按需求类（demand class）聚合的 eco 实际需求，注回 {@code MarketDemand.getDemandValue()} */
    public float getDemandValue(String demandClass) {
        float total = 0f;
        for (String commodityId : actualDemand.keySet()) {
            String dc = market.getCommodityData(commodityId).getDemandClass();
            if (Objects.equals(dc, demandClass)) {
                total += actualDemand.getOrDefault(commodityId, 0);
            }
        }
        return total;
    }

    /**
     * 月度注回前的 available 整理：
     * 1) 把玩家交易 / 其他 mod 对展示 stat 的修改（非 cc_econ_external、非原版 4 key）合并进 stock；
     * 2) 清空全部 mods，按 eco 数据重填 4 个原版 key（本地产量/进口/短缺），
     *    让 UI 的 available breakdown 保持原版结构但数值来自 eco。
     */
    public void updateAvailableStats() {
        // 确保 eco 涉及的商品都有展示 stat
        for (String commodityId : commodityIds) {
            getAvailableStat(commodityId);
        }
        // 1) 外部修改合并进 stock（cc_econ_external 已直接进 stock，跳过）
        for (Map.Entry<String, MutableStatWithTempMods> entry : availableStats.entrySet()) {
            long external = 0;
            for (MutableStat.StatMod mod : new ArrayList<>(entry.getValue().getFlatMods().values())) {
                if (EXTERNAL_MOD_KEY.equals(mod.source)) continue;
                if (VANILLA_AVAILABLE_KEYS.contains(mod.source)) continue;
                external += mod.value;
            }
            if (external != 0) {
                stock.merge(entry.getKey(), external, Long::sum);
            }
        }
        // 2) 清空并重填 4 key
        for (Map.Entry<String, MutableStatWithTempMods> entry : availableStats.entrySet()) {
            String commodityId = entry.getKey();
            MutableStatWithTempMods stat = entry.getValue();
            stat.unmodify();

            int supply = actualSupply.getOrDefault(commodityId, 0);
            int demand = actualDemand.getOrDefault(commodityId, 0);
            int local = Math.max(supply - demand, 0);
            int shortage = Math.max(demand - supply, 0);
            long importQty = 0;
            for (TradeDeal deal : importTrade) {
                if (deal.getItemId().equals(commodityId)) {
                    importQty += deal.getItemNum();
                }
            }
            if (local > 0) {
                stat.modifyFlat(CommodityMarketData.KEY_LOCAL, local, "本地产量");
            }
            if (importQty > 0) {
                stat.modifyFlat(CommodityMarketData.KEY_IMPORTS, importQty, "预期进口量");
            }
            if (shortage > 0) {
                stat.modifyFlat(CommodityMarketData.KEY_SHORTAGE, -shortage, "短缺");
            }
        }
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
