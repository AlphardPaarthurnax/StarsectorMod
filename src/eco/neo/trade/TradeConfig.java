package eco.neo.trade;

import com.fs.starfarer.api.Global;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.github.alphardpaarthurnax.script.CoreCrackingModPlugin.MOD_ID;

public class TradeConfig {

    private static List<TradeStrategy> defaultChain;
    private static Map<String, List<TradeStrategy>> factionChains = new HashMap<>();

    public static void load() {
        rebuildChains();
    }

    public static void rebuildChains() {
        factionChains.clear();

        JSONObject json = null;
        try {
            json = Global.getSettings().getMergedJSON("data/config/CC_trade_strategy.json");
        } catch (Exception ignored) {}

        // defaultChain: JSON → 硬编码兜底
        if (json != null) {
            JSONArray defArr = json.optJSONArray("defaultChain");
            if (defArr != null) {
                defaultChain = parseChain(defArr);
            }
        }
        if (defaultChain == null || defaultChain.isEmpty()) {
            defaultChain = Arrays.asList(TradeStrategy.FACTION, TradeStrategy.DISTANCE, TradeStrategy.PRICE);
        }

        // factionChains
        if (json != null) {
            JSONObject fo = json.optJSONObject("factionChains");
            if (fo != null) {
                Iterator<String> it = fo.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    JSONArray arr = fo.optJSONArray(key);
                    if (arr != null) {
                        factionChains.put(key, parseChain(arr));
                    }
                }
            }
        }
    }

    public static List<TradeStrategy> getChain(String factionId) {
        List<TradeStrategy> c = factionChains.get(factionId);
        return c != null ? c : defaultChain;
    }

    public static List<TradeStrategy> getDefaultChain() { return defaultChain; }
    public static Map<String, List<TradeStrategy>> getFactionChains() {
        return Collections.unmodifiableMap(factionChains);
    }

    private static TradeStrategy safeParse(String s) {
        try { return TradeStrategy.valueOf(s); }
        catch (Exception e) { return TradeStrategy.FACTION; }
    }

    private static List<TradeStrategy> parseChain(JSONArray arr) {
        List<TradeStrategy> chain = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            TradeStrategy p = safeParse(arr.optString(i, ""));
            if (!chain.contains(p)) chain.add(p);
        }
        for (TradeStrategy p : TradeStrategy.values()) {
            if (!chain.contains(p)) chain.add(p);
        }
        return chain;
    }
}
