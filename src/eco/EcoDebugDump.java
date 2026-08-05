package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import eco.core.*;
import eco.mutable.BridgedMutableCommodityQuantity;
import eco.trade.TradeDeal;
import eco.trade.TradeOffer;
import eco.trade.TradeStrategy;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class EcoDebugDump {
    private static int lastDumpedMonth = -1;

    public static void dump() {
        int month = Global.getSector().getClock().getMonth();
        if (month == lastDumpedMonth) return;
        lastDumpedMonth = month;

        String path = new File("D:\\Starsector\\corecracking_debug.html").getAbsolutePath();

        try (OutputStreamWriter ow = new OutputStreamWriter(
                new FileOutputStream(path), Charset.forName("UTF-8"))) {
            ow.write(buildHtml(month));
        } catch (Exception e) {
            Global.getLogger(EcoDebugDump.class).error("Dump failed", e);
        }
    }

    private static String buildHtml(int month) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>CC Economy Debug</title><style>");
        sb.append(":root{--bg:#1a1a2e;--card:#16213e;--text:#e0e0e0;--dim:#888;--green:#4caf50;--yellow:#ffc107;--red:#f44336;--orange:#ff9800;--cyan:#00bcd4;--stock:#7c4dff;--border:#2a2a4a;--th:#0d1b3e}");
        sb.append("body{font-family:'Consolas','Courier New',monospace;font-size:13px;background:var(--bg);color:var(--text);margin:0;padding:16px}");
        sb.append("*{box-sizing:border-box}");
        sb.append("details{cursor:pointer;margin-left:18px;border-left:2px solid var(--border);padding-left:12px}");
        sb.append("summary{padding:6px 0;outline:none;user-select:none}");
        sb.append("summary::marker{color:var(--dim);font-size:11px}");
        sb.append(".level-0{margin-left:0;border-left:none;padding-left:0}");
        sb.append(".sys-header{color:var(--cyan);font-size:15px;font-weight:bold}");
        sb.append(".planet-header{color:#81c784;font-size:14px}");
        sb.append(".indi-header{color:#ce93d8;font-size:13px}");
        sb.append(".tag{display:inline-block;padding:1px 6px;border-radius:3px;font-size:11px;margin:0 2px}");
        sb.append(".tag-overload{background:var(--red);color:#fff}");
        sb.append(".tag-shortage{background:var(--orange);color:#000}");
        sb.append(".tag-overloadShortage{background:#d32f2f;color:#fff}");
        sb.append(".tag-fight{background:#e91e63;color:#fff}");
        sb.append(".bar-wrap{display:inline-block;width:80px;height:10px;background:#333;border-radius:2px;vertical-align:middle;margin:0 4px;overflow:hidden}");
        sb.append(".bar-fill{display:block;height:100%;border-radius:2px}");
        sb.append(".bar-green{background:var(--green)}.bar-yellow{background:var(--yellow)}.bar-red{background:var(--red)}");
        sb.append("table{width:100%;border-collapse:collapse;margin:6px 0 12px;font-size:12px}");
        sb.append("table.data-table{margin-left:24px;width:auto;min-width:60%}");
        sb.append("table.mod-table{margin-left:24px;width:auto;min-width:70%}");
        sb.append("table.trade-table{margin-left:24px;width:auto;min-width:60%}");
        sb.append("th{background:var(--th);color:var(--dim);padding:4px 8px;text-align:left;font-weight:normal;border-bottom:2px solid var(--border);white-space:nowrap}");
        sb.append("td{padding:3px 8px;border-bottom:1px solid var(--border);white-space:nowrap}");
        sb.append("tr:nth-child(even) td{background:rgba(255,255,255,0.02)}");
        sb.append("td.commodity{color:var(--cyan)}");
        sb.append("td.pos{color:var(--green)}");
        sb.append("td.neg{color:var(--red)}");
        sb.append("td.zero{color:var(--dim)}");
        sb.append("td.trade-intra{color:var(--green);font-size:11px}");
        sb.append("td.trade-inter{color:var(--yellow);font-size:11px}");
        sb.append("td.mod-type{color:var(--dim);font-size:11px}");
        sb.append("td.mod-key{color:#80cbc4}");
        sb.append("td.mod-val{color:var(--yellow)}");
        sb.append("td.mod-desc{color:var(--dim);font-size:11px}");
        sb.append("h1{color:var(--cyan);border-bottom:2px solid var(--border);padding-bottom:8px;margin:0 0 16px}");
        sb.append(".subtitle{color:var(--dim);font-size:12px}");
        sb.append(".section-label{color:var(--dim);font-size:11px;margin:6px 0 2px 24px}");
        sb.append(".kv{margin:1px 0;padding-left:24px;font-size:12px}");
        sb.append(".kv-label{color:var(--dim)}.kv-stock{color:var(--stock)}.kv-green{color:var(--green)}.kv-red{color:var(--red)}.kv-yellow{color:var(--yellow)}.kv-cyan{color:var(--cyan)}");
        sb.append("</style></head><body>");
        sb.append("<h1>CC Economy Debug</h1>");
        long marketCount = Global.getSector().getEconomy().getMarketsCopy().stream().filter(MarketAPI::isInEconomy).count();
        sb.append("<div class=\"subtitle\">Month ").append(month).append(" &mdash; ").append(marketCount).append(" markets in economy</div><br>");
        sb.append("<div class=\"subtitle\">");
        sb.append("<button onclick=\"document.querySelectorAll('details').forEach(function(d){d.open=true})\">\u5c55\u5f00\u5168\u90e8</button> ");
        sb.append("<button onclick=\"document.querySelectorAll('details').forEach(function(d){d.open=false})\">\u6298\u53e0\u5168\u90e8</button> ");
        sb.append("</div><br>");

        sb.append(dumpGlobal());
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String dumpGlobal() {
        GlobalEconomy ge = GlobalEconomy.getInstance();
        Map<StarSystemAPI, SystemEconomy> systems = ge.getAllSystemEconomys();
        StringBuilder sb = new StringBuilder();

        sb.append("<br><details><summary><span style=\"color:var(--yellow)\">Trade Strategy Config</span></summary><table>");
        sb.append("<tr><th>faction</th><th>chain</th></tr>");
        sb.append("<tr><td class=\"commodity\">default</td><td>").append(chainToStr(EconomyConfig.getDefaultTradeChain())).append("</td></tr>");
        for (Map.Entry<String, List<TradeStrategy>> e : EconomyConfig.getFactionTradeChains().entrySet()) {
            sb.append("<tr><td class=\"commodity\">").append(esc(e.getKey())).append("</td>");
            sb.append("<td>").append(chainToStr(e.getValue())).append("</td></tr>");
        }
        sb.append("</table></details>");

        sb.append("<details><summary><span style=\"color:var(--yellow)\">Economy Profit Config</span></summary>");
        sb.append("<div class=\"kv\"><span class=\"kv-label\">internalTradeTaxRate:</span> ")
                .append(String.format("%.2f%%", EconomyConfig.getInternalTradeTaxRate() * 100f)).append("</div>");
        sb.append("<div class=\"kv\"><span class=\"kv-label\">freightCostPerCargoSpacePerLY:</span> ")
                .append(String.format("%.2f", EconomyConfig.getFreightCostPerCargoSpacePerLY()))
                .append(" credits / cargo / LY</div>");
        sb.append("</details>");

        Map<String, Float> gp = ge.getGlobalPrices();
        if (!gp.isEmpty()) {
            sb.append("<br><details><summary><span style=\"color:var(--yellow)\">Global Prices</span></summary>");
            sb.append("<table class=\"trade-table\">");
            sb.append("<tr><th>commodityId</th><th>price</th></tr>");
            List<String> gids = new ArrayList<>(gp.keySet());
            Collections.sort(gids);
            for (String cid : gids) {
                sb.append("<tr><td class=\"commodity\">").append(esc(cid)).append("</td>");
                sb.append("<td>").append(String.format("%.1f", gp.get(cid))).append("</td></tr>");
            }
            sb.append("</table></details>");
        }

        Map<FactionAPI, Profit> geFP = ge.getFactionProfitBreakdowns();
        if (!geFP.isEmpty()) {
            sb.append("<br><details><summary><span style=\"color:var(--yellow)\">Global Faction Profits</span></summary>");
            sb.append(dumpFactionProfitTable(geFP));
            sb.append("</details>");
        }

        if (!ge.getAllSupply().isEmpty() || !ge.getAllDemand().isEmpty()) {
            sb.append("<br><details><summary><span style=\"color:var(--orange)\">Global Unmatched</span></summary>");
            if (!ge.getAllSupply().isEmpty()) {
                sb.append("<div class=\"section-label\">Unmatched Supply:</div>");
                sb.append(dumpAggregateOfferTable(ge.getAllSupply(), true));
            }
            if (!ge.getAllDemand().isEmpty()) {
                sb.append("<div class=\"section-label\">Unmatched Demand:</div>");
                sb.append(dumpAggregateOfferTable(ge.getAllDemand(), false));
            }
            sb.append("</details>");
        }

        sb.append("<details class=\"level-0\" open><summary><span class=\"sys-header\">GlobalEconomy</span> &mdash; ").append(systems.size()).append(" systems</summary>");
        for (SystemEconomy se : systems.values()) {
            sb.append(dumpSystem(se));
        }
        sb.append("</details>");
        return sb.toString();
    }

    private static String dumpSystem(SystemEconomy se) {
        Map<MarketAPI, PlanetEconomy> planets = se.getAllPlanetEconomys();
        StringBuilder sb = new StringBuilder();
        String sysName = se.getSystem() != null ? se.getSystem().getName() : "?";
        sb.append("<details><summary><span class=\"sys-header\">SystemEconomy</span>: ").append(esc(sysName)).append(" &mdash; ").append(planets.size()).append(" planets</summary>");

        if (se.getSovereigntyFaction() != null) {
            sb.append("<div class=\"kv\"><span class=\"kv-label\">sovereignty:</span> ");
            sb.append(esc(se.getSovereigntyFaction().getDisplayName()));
            if (se.isInSovereigntyFight())
                sb.append(" <span class=\"tag tag-fight\">FIGHT</span>");
            sb.append("</div>");
        }

        if (!se.getCommodityIds().isEmpty()) {
            sb.append("<div class=\"kv\"><span class=\"kv-label\">commodityIds:</span> [");
            List<String> sorted = new ArrayList<>(se.getCommodityIds());
            Collections.sort(sorted);
            for (int i = 0; i < sorted.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("<span class=\"kv-cyan\">").append(esc(sorted.get(i))).append("</span>");
            }
            sb.append("]</div>");
        }

        Map<FactionAPI, Profit> seFP = se.getFactionProfitBreakdowns();
        if (!seFP.isEmpty()) {
            sb.append("<div class=\"section-label\">Faction Profits:</div>");
            sb.append(dumpFactionProfitTable(seFP));
        }

        if (!se.getAllSupply().isEmpty()) {
            sb.append("<div class=\"section-label\">System Unmatched Supply:</div>");
            sb.append(dumpAggregateOfferTable(se.getAllSupply(), true));
        }
        if (!se.getAllDemand().isEmpty()) {
            sb.append("<div class=\"section-label\">System Unmatched Demand:</div>");
            sb.append(dumpAggregateOfferTable(se.getAllDemand(), false));
        }

        for (PlanetEconomy pe : planets.values()) {
            sb.append(dumpPlanet(pe));
        }
        sb.append("</details>");
        return sb.toString();
    }

    private static String dumpPlanet(PlanetEconomy pe) {
        StringBuilder sb = new StringBuilder();
        String label = pe.getMarketName() + " &mdash; " + pe.getPlanetName() + " (size=" + pe.getMarketSize() + ") &mdash; " + pe.getFactionId();
        sb.append("<details><summary><span class=\"planet-header\">PlanetEconomy</span>: ").append(esc(label));
        sb.append(" <span class=\"kv-label\">planetProfit:</span>");
        float pp = pe.getProfit().getNetProfit();
        sb.append(pp >= 0 ? "<span class=\"pos\">+" + fmtNumFloat(pp) + "</span>"
                : "<span class=\"neg\">" + fmtNumFloat(pp) + "</span>");
        sb.append("</summary>");

        sb.append("<div class=\"section-label\">Profit Breakdown:</div>");
        sb.append(dumpProfitBreakdownTable(pe.getProfit()));

        Set<String> allIds = new TreeSet<>();
        allIds.addAll(pe.getAllStock().keySet());
        allIds.addAll(pe.getNetSD().keySet());
        allIds.addAll(pe.getAllActualSupply().keySet());
        allIds.addAll(pe.getAllActualDemand().keySet());
        allIds.addAll(pe.getDomesticDemand().keySet());
        allIds.addAll(pe.getAllBaseSupply().keySet());
        allIds.addAll(pe.getAllBaseDemand().keySet());
        allIds.addAll(pe.getAllPrice().keySet());

        if (!allIds.isEmpty()) {
            sb.append("<table class=\"data-table\">");
            sb.append("<tr><th>commodityId</th><th>stock</th><th>netSD</th><th>actualS</th><th>actualD</th><th>domesticD</th><th>baseS</th><th>baseD</th><th>price</th></tr>");
            for (String cid : allIds) {
                long stock = pe.getAllStock().getOrDefault(cid, 0L);
                int net = pe.getNetSD().getOrDefault(cid, 0);
                int as = pe.getAllActualSupply().getOrDefault(cid, 0);
                int ad = pe.getAllActualDemand().getOrDefault(cid, 0);
                int domestic = pe.getDomesticDemand().getOrDefault(cid, 0);
                int bs = pe.getAllBaseSupply().getOrDefault(cid, 0);
                int bd = pe.getAllBaseDemand().getOrDefault(cid, 0);
                float price = pe.getAllPrice().getOrDefault(cid, 0f);
                String netClass = net > 0 ? "pos" : (net < 0 ? "neg" : "zero");
                String netStr = net >= 0 ? ("+" + net) : Integer.toString(net);
                if (net == 0) netStr = "0";
                sb.append("<tr>");
                sb.append("<td class=\"commodity\">").append(esc(cid)).append("</td>");
                sb.append("<td>").append(fmtNum(stock)).append("</td>");
                sb.append("<td class=\"").append(netClass).append("\">").append(netStr).append("</td>");
                sb.append("<td>").append(as).append("</td>");
                sb.append("<td>").append(ad).append("</td>");
                sb.append("<td>").append(domestic).append("</td>");
                sb.append("<td>").append(bs).append("</td>");
                sb.append("<td>").append(bd).append("</td>");
                sb.append("<td>").append(String.format("%.1f", price)).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        if (!pe.getCommodityIds().isEmpty()) {
            sb.append("<div class=\"kv\"><span class=\"kv-label\">commodityIds:</span> [");
            List<String> sorted = new ArrayList<>(pe.getCommodityIds());
            Collections.sort(sorted);
            for (int i = 0; i < sorted.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("<span class=\"kv-cyan\">").append(esc(sorted.get(i))).append("</span>");
            }
            sb.append("]</div>");
        }

        Map<String, TradeOffer> supplyOffers = pe.getSupplyOffers();
        sb.append("<div class=\"section-label\">Supply Offers");
        sb.append(supplyOffers.isEmpty() ? " (none)" : ":");
        sb.append("</div>");
        if (!supplyOffers.isEmpty()) {
            sb.append(dumpTradeOfferTable(supplyOffers, true));
        }

        Map<String, TradeOffer> demandOffers = pe.getDemandOffers();
        sb.append("<div class=\"section-label\">Demand Offers");
        sb.append(demandOffers.isEmpty() ? " (none)" : ":");
        sb.append("</div>");
        if (!demandOffers.isEmpty()) {
            sb.append(dumpTradeOfferTable(demandOffers, false));
        }

        List<TradeDeal> exportTrades = pe.getExportTrade();
        sb.append("<div class=\"section-label\">Deals (export)");
        sb.append(exportTrades.isEmpty() ? " (none)" : ":");
        sb.append("</div>");
        if (!exportTrades.isEmpty()) {
            sb.append(dumpTradeDealTable(exportTrades));
        }

        List<TradeDeal> importTrades = pe.getImportTrade();
        sb.append("<div class=\"section-label\">Deals (import)");
        sb.append(importTrades.isEmpty() ? " (none)" : ":");
        sb.append("</div>");
        if (!importTrades.isEmpty()) {
            sb.append(dumpTradeDealTable(importTrades));
        }

        for (Map.Entry<Industry, IndustryEconomy> ie : pe.getAllIndustryEconomy().entrySet()) {
            sb.append(dumpIndustry(ie.getValue()));
        }

        sb.append("</details>");
        return sb.toString();
    }

    private static String dumpIndustry(IndustryEconomy ie) {
        StringBuilder sb = new StringBuilder();
        String name = ie.getIndustry().getCurrentName() + " (" + ie.getIndustry().getId() + ")";
        float eff = ie.getEfficiency();
        String barColor;
        if (eff >= 1.0f) barColor = "bar-green";
        else if (eff >= 0.5f) barColor = "bar-yellow";
        else barColor = "bar-red";
        int barPct = Math.min(100, Math.max(0, (int)(eff * 100)));

        StringBuilder tags = new StringBuilder();
        if (ie.isOverload()) tags.append("<span class=\"tag tag-overload\">OVERLOAD</span>");
        if (!ie.getOverloadShortages().isEmpty())
            tags.append("<span class=\"tag tag-overloadShortage\">ovlShort:[")
                    .append(String.join(",", ie.getOverloadShortages())).append("]</span>");
        if (!ie.getShortages().isEmpty())
            tags.append("<span class=\"tag tag-shortage\">short:[")
                    .append(String.join(",", ie.getShortages())).append("]</span>");

        sb.append("<details><summary><span class=\"indi-header\">IndustryEconomy</span>: ").append(esc(name))
                .append("  eff=").append(String.format("%.0f%%", eff * 100))
                .append(" <span class=\"bar-wrap\"><span class=\"bar-fill ").append(barColor)
                .append("\" style=\"width:").append(barPct).append("%\"></span></span>")
                .append(" income=").append(fmtNumFloat(ie.getIncome()))
                .append(" upkeep=").append(fmtNumFloat(ie.getUpkeep()))
                .append(" net=").append(fmtNumFloat(ie.getProfit()))
                .append(" peopleScale=").append(String.format("%.2f", ie.getPeopleScale()))
                .append(" ").append(tags)
                .append("</summary>");

        sb.append("<table class=\"data-table\">");
        sb.append("<tr><th>income</th><th>upkeep</th><th>expectedProfit</th><th>profit</th><th>modIncome</th><th>modUpkeep</th></tr>");
        sb.append("<tr><td class=\"pos\">").append(fmtNumFloat(ie.getIncome())).append("</td>")
                .append("<td class=\"neg\">").append(fmtNumFloat(ie.getUpkeep())).append("</td>")
                .append("<td>").append(fmtNumFloat(ie.getExpectedProfit())).append("</td>")
                .append("<td class=\"").append(ie.getProfit() >= 0f ? "pos" : "neg").append("\">")
                .append(fmtNumFloat(ie.getProfit())).append("</td>")
                .append("<td>").append(fmtNumFloat(ie.getModIncome().getModifiedValue())).append("</td>")
                .append("<td>").append(fmtNumFloat(ie.getModUpkeep().getModifiedValue())).append("</td></tr></table>");

        sb.append("<div class=\"section-label\">modIncome details:</div>");
        sb.append(buildStatTable("income", ie.getModIncome()));
        sb.append("<div class=\"section-label\">modUpkeep details:</div>");
        sb.append(buildStatTable("upkeep", ie.getModUpkeep()));

        Set<String> allIds = new TreeSet<>();
        allIds.addAll(ie.getAllSupply().keySet());
        allIds.addAll(ie.getAllDemand().keySet());
        allIds.addAll(ie.getAllRefSupply().keySet());
        allIds.addAll(ie.getAllRefDemand().keySet());

        if (!allIds.isEmpty()) {
            sb.append("<table class=\"data-table\">");
            sb.append("<tr><th>commodityId</th><th>supply</th><th>demand</th><th>baseSupply</th><th>baseDemand</th></tr>");
            for (String cid : allIds) {
                int s = ie.getAllSupply().getOrDefault(cid, 0);
                int d = ie.getAllDemand().getOrDefault(cid, 0);
                int bs = ie.getAllRefSupply().getOrDefault(cid, 0);
                int bd = ie.getAllRefDemand().getOrDefault(cid, 0);
                sb.append("<tr>");
                sb.append("<td class=\"commodity\">").append(esc(cid)).append("</td>");
                sb.append("<td>").append(s == 0 ? "<span class=\"zero\">0</span>" : String.valueOf(s)).append("</td>");
                sb.append("<td>").append(d == 0 ? "<span class=\"zero\">0</span>" : String.valueOf(d)).append("</td>");
                sb.append("<td>").append(bs == 0 ? "<span class=\"zero\">0</span>" : String.valueOf(bs)).append("</td>");
                sb.append("<td>").append(bd == 0 ? "<span class=\"zero\">0</span>" : String.valueOf(bd)).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        if (!ie.getCommodityIds().isEmpty()) {
            sb.append("<div class=\"kv\"><span class=\"kv-label\">commodityIds:</span> [");
            List<String> sorted = new ArrayList<>(ie.getCommodityIds());
            Collections.sort(sorted);
            for (int i = 0; i < sorted.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("<span class=\"kv-cyan\">").append(esc(sorted.get(i))).append("</span>");
            }
            sb.append("]</div>");
        }

        Map<String, BridgedMutableCommodityQuantity> modSupply = ie.getAllModSupply();
        if (!modSupply.isEmpty()) {
            sb.append("<div class=\"section-label\">modSupply details:</div>");
            sb.append(buildModTable(modSupply));
        }

        Map<String, BridgedMutableCommodityQuantity> modDemand = ie.getAllModDemand();
        if (!modDemand.isEmpty()) {
            sb.append("<div class=\"section-label\">modDemand details:</div>");
            sb.append(buildModTable(modDemand));
        }

        sb.append("</details>");
        return sb.toString();
    }

    private static String dumpTradeOfferTable(Map<String, TradeOffer> offers, boolean isSupply) {
        String qtyColor = isSupply ? "pos" : "neg";
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"trade-table\">");
        sb.append("<tr><th>commodityId</th><th>quantity</th><th>price</th></tr>");
        List<String> ids = new ArrayList<>(offers.keySet());
        Collections.sort(ids);
        for (String cid : ids) {
            TradeOffer o = offers.get(cid);
            sb.append("<tr>");
            sb.append("<td class=\"commodity\">").append(esc(cid)).append("</td>");
            sb.append("<td class=\"").append(qtyColor).append("\">")
                    .append(String.format("%+d", o.getItemNum())).append("</td>");
            sb.append("<td>").append(String.format("%.1f", o.getItemPrice())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String dumpAggregateOfferTable(Map<String, List<TradeOffer>> aggregate, boolean isSupply) {
        String qtyColor = isSupply ? "pos" : "neg";
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"trade-table\">");
        sb.append("<tr><th>commodityId</th><th>planet</th><th>quantity</th><th>price</th></tr>");
        List<String> ids = new ArrayList<>(aggregate.keySet());
        Collections.sort(ids);
        for (String cid : ids) {
            for (TradeOffer o : aggregate.get(cid)) {
                sb.append("<tr>");
                sb.append("<td class=\"commodity\">").append(esc(cid)).append("</td>");
                sb.append("<td>").append(esc(o.getPlanetEconomy().getMarketName())).append("</td>");
                sb.append("<td class=\"").append(qtyColor).append("\">")
                        .append(String.format("%+d", o.getItemNum())).append("</td>");
                sb.append("<td>").append(String.format("%.1f", o.getItemPrice())).append("</td>");
                sb.append("</tr>");
            }
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String dumpTradeDealTable(List<TradeDeal> deals) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"trade-table\">");
        sb.append("<tr><th>commodityId</th><th>from</th><th>to</th><th>quantity</th><th>export price</th><th>cargo/unit</th><th>cargo volume</th><th>distance LY</th><th>freight deducted</th><th>export profit</th><th>import cost</th><th>scope</th></tr>");
        for (TradeDeal d : deals) {
            boolean intra = d.getFromStarSystem() == d.getToStarSystem();
            sb.append("<tr>");
            sb.append("<td class=\"commodity\">").append(esc(d.getItemId())).append("</td>");
            sb.append("<td>").append(esc(d.getFromPlanetEconomy().getMarketName()))
                    .append(" (").append(esc(d.getFromPlanetEconomy().getFactionId())).append(")</td>");
            sb.append("<td>").append(esc(d.getToPlanetEconomy().getMarketName()))
                    .append(" (").append(esc(d.getToPlanetEconomy().getFactionId())).append(")</td>");
            sb.append("<td class=\"pos\">").append(String.valueOf(d.getItemNum())).append("</td>");
            sb.append("<td>").append(String.format("%.1f", d.getItemPrice())).append("</td>");
            sb.append("<td>").append(String.format("%.2f", d.getCargoSpacePerUnit())).append("</td>");
            sb.append("<td>").append(String.format("%.2f", d.getCargoVolume())).append("</td>");
            sb.append("<td>").append(String.format("%.2f", d.getDistanceLY())).append("</td>");
            sb.append("<td class=\"neg\">").append(fmtNumFloat(d.getFreightCost())).append("</td>");
            sb.append("<td class=\"").append(d.getExportProfit() >= 0f ? "pos" : "neg").append("\">")
                    .append(fmtNumFloat(d.getExportProfit())).append("</td>");
            sb.append("<td class=\"neg\">").append(fmtNumFloat(d.getImportCost())).append("</td>");
            sb.append("<td class=\"").append(intra ? "trade-intra" : "trade-inter").append("\">")
                    .append(intra ? "INTRA" : "INTER").append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String dumpFactionProfitTable(Map<FactionAPI, Profit> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"trade-table\">");
        sb.append("<tr><th>faction</th><th>+ export profit (after freight)</th><th>+ internal tax</th><th>+ income</th><th>- imports</th><th>- upkeep</th><th>net</th></tr>");
        List<Map.Entry<FactionAPI, Profit>> sorted = new ArrayList<>(values.entrySet());
        sorted.sort((a, b) -> Float.compare(b.getValue().getNetProfit(), a.getValue().getNetProfit()));
        for (Map.Entry<FactionAPI, Profit> entry : sorted) {
            Profit value = entry.getValue();
            sb.append("<tr><td>").append(esc(entry.getKey().getDisplayName())).append("</td>");
            appendProfitCells(sb, value);
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String dumpProfitBreakdownTable(Profit value) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"trade-table\">");
        sb.append("<tr><th>+ export profit (after freight)</th><th>+ internal tax</th><th>+ income</th><th>- imports</th><th>- upkeep</th><th>net</th></tr><tr>");
        appendProfitCells(sb, value);
        sb.append("</tr></table>");
        return sb.toString();
    }

    private static void appendProfitCells(StringBuilder sb, Profit value) {
        sb.append("<td class=\"").append(value.getExport() >= 0f ? "pos" : "neg").append("\">")
                .append(fmtNumFloat(value.getExport())).append("</td>");
        sb.append("<td class=\"pos\">").append(fmtNumFloat(value.getTax())).append("</td>");
        sb.append("<td class=\"pos\">").append(fmtNumFloat(value.getIncome())).append("</td>");
        sb.append("<td class=\"neg\">").append(fmtNumFloat(value.getImportCost())).append("</td>");
        sb.append("<td class=\"neg\">").append(fmtNumFloat(value.getUpkeep())).append("</td>");
        float net = value.getNetProfit();
        sb.append("<td class=\"").append(net >= 0f ? "pos" : "neg").append("\">")
                .append(fmtNumFloat(net)).append("</td>");
    }

    private static String buildStatTable(String name, MutableStat stat) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"mod-table\">");
        sb.append("<tr><th>stat</th><th>base</th><th>modified</th><th>type</th><th>key</th><th>value</th><th>desc</th></tr>");

        boolean first = true;
        for (Map.Entry<String, MutableStat.StatMod> entry : stat.getFlatMods().entrySet()) {
            sb.append(buildStatRow(name, stat, first, "flt", entry.getKey(), entry.getValue()));
            first = false;
        }
        for (Map.Entry<String, MutableStat.StatMod> entry : stat.getPercentMods().entrySet()) {
            sb.append(buildStatRow(name, stat, first, "pct", entry.getKey(), entry.getValue()));
            first = false;
        }
        for (Map.Entry<String, MutableStat.StatMod> entry : stat.getMultMods().entrySet()) {
            sb.append(buildStatRow(name, stat, first, "mul", entry.getKey(), entry.getValue()));
            first = false;
        }
        if (first) {
            sb.append("<tr><td class=\"commodity\">").append(esc(name)).append("</td>")
                    .append("<td>").append(String.format("%.1f", stat.getBaseValue())).append("</td>")
                    .append("<td>").append(String.format("%.1f", stat.getModifiedValue())).append("</td>")
                    .append("<td class=\"zero\" colspan=\"4\">(no modifiers)</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String buildStatRow(String name, MutableStat stat, boolean first, String type,
                                       String key, MutableStat.StatMod mod) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr><td class=\"commodity\">").append(esc(name)).append("</td>");
        if (first) {
            sb.append("<td>").append(String.format("%.1f", stat.getBaseValue())).append("</td>")
                    .append("<td>").append(String.format("%.1f", stat.getModifiedValue())).append("</td>");
        } else {
            sb.append("<td class=\"zero\"></td><td class=\"zero\"></td>");
        }
        sb.append("<td class=\"mod-type\">").append(type).append("</td>")
                .append("<td class=\"mod-key\">").append(esc(key)).append("</td>")
                .append("<td class=\"mod-val\">").append(String.format("%+.2f", mod.value)).append("</td>")
                .append("<td class=\"mod-desc\">").append(esc(mod.desc != null ? mod.desc : "")).append("</td></tr>");
        return sb.toString();
    }

    private static String buildModTable(Map<String, ? extends MutableCommodityQuantity> modMap) {
        StringBuilder sb = new StringBuilder();
        boolean hasAny = false;
        for (MutableCommodityQuantity mcq : modMap.values()) {
            MutableStat qty = mcq.getQuantity();
            if (!qty.getFlatMods().isEmpty() || !qty.getPercentMods().isEmpty() || !qty.getMultMods().isEmpty()) {
                hasAny = true;
                break;
            }
        }

        if (!hasAny) {
            sb.append("<table class=\"mod-table\"><tr><td class=\"zero\">(no modifiers)</td></tr></table>");
            return sb.toString();
        }

        sb.append("<table class=\"mod-table\">");
        sb.append("<tr><th>commodityId</th><th>base</th><th>type</th><th>key</th><th>value</th><th>desc</th></tr>");
        for (Map.Entry<String, ? extends MutableCommodityQuantity> entry : modMap.entrySet()) {
            String cid = entry.getKey();
            MutableCommodityQuantity mcq = entry.getValue();
            MutableStat qty = mcq.getQuantity();
            float base = qty.getBaseValue();

            for (Map.Entry<String, MutableStat.StatMod> m : qty.getFlatMods().entrySet()) {
                sb.append(buildModRow(cid, base, "flt", m.getKey(), m.getValue()));
                base = -1;
            }
            for (Map.Entry<String, MutableStat.StatMod> m : qty.getPercentMods().entrySet()) {
                sb.append(buildModRow(cid, base, "pct", m.getKey(), m.getValue()));
                base = -1;
            }
            for (Map.Entry<String, MutableStat.StatMod> m : qty.getMultMods().entrySet()) {
                sb.append(buildModRow(cid, base, "mul", m.getKey(), m.getValue()));
                base = -1;
            }
            if (qty.getFlatMods().isEmpty() && qty.getPercentMods().isEmpty() && qty.getMultMods().isEmpty()) {
                sb.append("<tr>");
                sb.append("<td class=\"commodity\">").append(esc(cid)).append("</td>");
                sb.append("<td>").append(String.format("%.1f", base)).append("</td>");
                sb.append("<td class=\"zero\" colspan=\"4\">(no modifiers)</td>");
                sb.append("</tr>");
            }
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String buildModRow(String cid, float base, String type, String key, MutableStat.StatMod mod) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td class=\"commodity\">").append(esc(cid)).append("</td>");
        if (base >= 0) {
            sb.append("<td>").append(String.format("%.1f", base)).append("</td>");
        } else {
            sb.append("<td class=\"zero\"></td>");
        }
        sb.append("<td class=\"mod-type\">").append(type).append("</td>");
        sb.append("<td class=\"mod-key\">").append(esc(key)).append("</td>");
        sb.append("<td class=\"mod-val\">").append(String.format("%+.2f", mod.value)).append("</td>");
        String desc = mod.desc != null ? mod.desc : "";
        sb.append("<td class=\"mod-desc\">").append(esc(desc)).append("</td>");
        sb.append("</tr>");
        return sb.toString();
    }

    private static String fmtNum(long n) {
        if (n >= 1_000_000_000) return String.format("%.1fB", n / 1_000_000_000f);
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000f);
        if (n >= 1_000) return String.format("%.1fK", n / 1_000f);
        return Long.toString(n);
    }

    private static String fmtNumFloat(float v) {
        if (Math.abs(v) >= 1_000_000f) return String.format("%.1fM", v / 1_000_000f);
        if (Math.abs(v) >= 1_000f)     return String.format("%.1fK", v / 1_000f);
        return String.format("%.0f", v);
    }

    private static String esc(String s) {
        if (s == null) return "(null)";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String chainToStr(List<TradeStrategy> chain) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chain.size(); i++) {
            if (i > 0) sb.append(" <span class=\"kv-label\">→</span> ");
            sb.append("<span class=\"kv-cyan\">").append(chain.get(i).name()).append("</span>");
        }
        return sb.toString();
    }
}
