package eco;

import com.fs.starfarer.api.Global;
import eco.core.trade.TradeStrategy;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class EconomyConfig {
    private static final float DEFAULT_INTERNAL_TRADE_TAX_RATE = 0.10f;
    private static final float DEFAULT_FREIGHT_COST_PER_CARGO_SPACE_PER_LY = 1f;
    private static final List<TradeStrategy> FALLBACK_TRADE_CHAIN = Collections.unmodifiableList(Arrays.asList(
            TradeStrategy.FACTION, TradeStrategy.DISTANCE, TradeStrategy.PRICE));

    private static float internalTradeTaxRate = DEFAULT_INTERNAL_TRADE_TAX_RATE;
    private static float freightCostPerCargoSpacePerLY = DEFAULT_FREIGHT_COST_PER_CARGO_SPACE_PER_LY;
    private static List<TradeStrategy> defaultTradeChain = FALLBACK_TRADE_CHAIN;
    private static final Map<String, List<TradeStrategy>> factionTradeChains = new HashMap<>();

    private EconomyConfig() {}

    public static void load() {
        internalTradeTaxRate = DEFAULT_INTERNAL_TRADE_TAX_RATE;
        freightCostPerCargoSpacePerLY = DEFAULT_FREIGHT_COST_PER_CARGO_SPACE_PER_LY;
        defaultTradeChain = FALLBACK_TRADE_CHAIN;
        factionTradeChains.clear();

        try {
            JSONObject json = Global.getSettings().getMergedJSON("data/config/CC_economy.json");
            internalTradeTaxRate = nonNegative(json.optDouble(
                    "internalTradeTaxRate", internalTradeTaxRate));
            freightCostPerCargoSpacePerLY = nonNegative(json.optDouble(
                    "freightCostPerCargoSpacePerLY", freightCostPerCargoSpacePerLY));

            JSONArray defaultChainJson = json.optJSONArray("defaultTradeChain");
            if (defaultChainJson != null) {
                List<TradeStrategy> parsed = parseTradeChain(defaultChainJson);
                if (!parsed.isEmpty()) defaultTradeChain = parsed;
            }

            JSONObject factionChainsJson = json.optJSONObject("factionTradeChains");
            if (factionChainsJson != null) {
                Iterator<String> factionIds = factionChainsJson.keys();
                while (factionIds.hasNext()) {
                    String factionId = factionIds.next();
                    JSONArray chainJson = factionChainsJson.optJSONArray(factionId);
                    if (chainJson != null) {
                        factionTradeChains.put(factionId, parseTradeChain(chainJson));
                    }
                }
            }
        } catch (Exception e) {
            Global.getLogger(EconomyConfig.class).warn("Failed to load CC_economy.json; using defaults", e);
        }
    }

    private static float nonNegative(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0f;
        return (float) Math.max(0d, value);
    }

    public static float getInternalTradeTaxRate() { return internalTradeTaxRate; }
    public static float getFreightCostPerCargoSpacePerLY() { return freightCostPerCargoSpacePerLY; }
    public static List<TradeStrategy> getTradeChain(String factionId) {
        List<TradeStrategy> chain = factionTradeChains.get(factionId);
        return chain != null ? chain : defaultTradeChain;
    }
    public static List<TradeStrategy> getDefaultTradeChain() { return defaultTradeChain; }
    public static Map<String, List<TradeStrategy>> getFactionTradeChains() {
        return Collections.unmodifiableMap(factionTradeChains);
    }

    private static List<TradeStrategy> parseTradeChain(JSONArray json) {
        List<TradeStrategy> chain = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            try {
                TradeStrategy strategy = TradeStrategy.valueOf(json.optString(i, ""));
                if (!chain.contains(strategy)) chain.add(strategy);
            } catch (IllegalArgumentException ignored) {}
        }
        for (TradeStrategy strategy : TradeStrategy.values()) {
            if (!chain.contains(strategy)) chain.add(strategy);
        }
        return Collections.unmodifiableList(chain);
    }
}
