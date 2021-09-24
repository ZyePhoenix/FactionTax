package com.gmail.ryderzye.factionstax.entity;

import com.massivecraft.factions.entity.*;
import com.gmail.ryderzye.factionstax.Const;
import com.gmail.ryderzye.factionstax.FactionsTax;
import com.gmail.ryderzye.factionstax.TaxKeyComparator;
import com.gmail.ryderzye.factionstax.cmd.arg.TypeTaxKey;
import com.massivecraft.massivecore.collections.MassiveList;
import com.massivecraft.massivecore.collections.MassiveListDef;
import com.massivecraft.massivecore.collections.MassiveMap;
import com.massivecraft.massivecore.collections.MassiveTreeMapDef;
import com.massivecraft.massivecore.mixin.MixinDisplayName;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.store.Entity;
import com.massivecraft.massivecore.store.EntityInternal;
import com.massivecraft.massivecore.util.Txt;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;

public class TFaction extends Entity<TFaction> {
    public static final transient int HISTORY_LENGTH = 7;

    public static final transient double DEFAULT_TAX = 0.0D;

    public static final transient String YES = Txt.parse("<g>YES");

    public static final transient String NO = Txt.parse("<b>NO");

    public static TFaction get(Object oid) {
        return (TFaction)TFactionColl.get().get(oid);
    }

    public static TFaction get(Faction faction) {
        return (TFaction)TFactionColl.get().get(faction.getId(), true);
    }

    public TFaction load(TFaction that) {
        this.playerHistory = that.playerHistory;
        this.factionHistory = that.factionHistory;
        this.rules = that.rules;
        return this;
    }

    public boolean isDefault() {
        if (hasPlayerHistory())
            return false;
        if (hasFactionHistory())
            return false;
        if (hasRules())
            return false;
        return true;
    }

    private MassiveListDef<Double> playerHistory = new MassiveListDef();

    private MassiveListDef<Double> factionHistory = new MassiveListDef();

    private MassiveTreeMapDef<String, Double, TaxKeyComparator> rules = new MassiveTreeMapDef(TaxKeyComparator.get());

    public List<Double> getPlayerHistory() {
        return (List<Double>)this.playerHistory;
    }

    public void setPlayerHistory(Collection<Double> playerHistory) {
        this.playerHistory = new MassiveListDef(playerHistory);
    }

    public boolean hasPlayerHistory() {
        return hasHistory(getPlayerHistory());
    }

    public String getPlayerHistoryDesc() {
        return getHistoryDesc(getPlayerHistory());
    }

    public void trimPlayerHistory() {
        if (trimHistory(getPlayerHistory()))
            changed();
    }

    public void addPlayerHistory(double element) {
        addToHistory(getPlayerHistory(), element);
    }

    public List<Double> getFactionHistory() {
        return (List<Double>)this.factionHistory;
    }

    public void setFactionHistory(Collection<Double> factionHistory) {
        this.factionHistory = new MassiveListDef(factionHistory);
    }

    public boolean hasFactionHistory() {
        return hasHistory(getFactionHistory());
    }

    public String getFactionHistoryDesc() {
        return getHistoryDesc(getFactionHistory());
    }

    public void trimFactionHistory() {
        if (trimHistory(getFactionHistory()))
            changed();
    }

    public void addFactionHistory(double element) {
        addToHistory(getFactionHistory(), element);
    }

    public void addToHistory(List<Double> history, double element) {
        MassiveList massiveList = new MassiveList(history);
        history.add(0, Double.valueOf(element));
        trimHistory(history);
        changed(massiveList, history);
    }

    public static boolean hasHistory(List<Double> entries) {
        return (entries != null && !entries.isEmpty());
    }

    public static boolean trimHistory(List<Double> entries) {
        return trimHistory(entries, 7);
    }

    public static boolean trimHistory(List<Double> entries, int maxlen) {
        if (entries == null)
            return false;
        if (entries.size() <= maxlen)
            return false;
        if (maxlen <= 0) {
            entries.clear();
        } else {
            entries.subList(maxlen, entries.size()).clear();
        }
        return true;
    }

    public static String getHistoryDesc(List<Double> entries) {
        if (entries == null || entries.isEmpty())
            return Const.NULL_HISTORY;
        double sum = 0.0D;
        List<String> entryDescs = new LinkedList<>();
        for (Double entry : entries) {
            if (entry == null)
                continue;
            sum += entry.doubleValue();
            String entryDesc = Txt.parse("<h>%s", new Object[] { Money.format(entry.doubleValue(), false) });
            entryDescs.add(entryDesc);
        }
        String sumDesc = Txt.parse("<h>%s", new Object[] { Money.format(sum, true) });
        return Txt.implode(entryDescs, Txt.parse(" <i>+ ")) + Txt.parse(" <i>= ") + sumDesc;
    }

    public boolean hasRules() {
        return !this.rules.isEmpty();
    }

    public Map<String, Double> getRules() {
        return (Map)this.rules;
    }

    public double getDefaultTax() {
        Double ret = (Double)this.rules.get("default");
        if (ret != null)
            return Money.prepare(ret.doubleValue());
        return 0.0D;
    }

    public Map<String, Double> getRulesWithDefault() {
        MassiveMap<String, Double> massiveMap = new MassiveMap((Map)this.rules);
        massiveMap.put("default", Double.valueOf(getDefaultTax()));
        return (Map<String, Double>)massiveMap;
    }

    public boolean setTax(String key, Double value) {
        if (key.equals("all")) {
            if (value == null || value.equals(Double.valueOf(0.0D))) {
                if (this.rules.isEmpty())
                    return false;
                this.rules.clear();
                changed();
                return true;
            }
            MassiveTreeMapDef<String, Double, TaxKeyComparator> target = new MassiveTreeMapDef(TaxKeyComparator.get(), "default", value, new Object[0]);
            if (target.equals(this.rules))
                return false;
            this.rules = target;
            changed();
            return true;
        }
        if (value == null || (key.equals("default") && value.equals(Double.valueOf(0.0D)))) {
            boolean bool = (this.rules.remove(key) != null);
            if (bool)
                changed();
            return bool;
        }
        boolean ret = !value.equals(this.rules.put(key, value));
        if (ret)
            changed();
        return ret;
    }

    public double getTax(MPlayer mplayer) {
        if (mplayer == null)
            throw new NullPointerException("mplayer");
        if (!hasRules())
            return getDefaultTax();
        String id = mplayer.getId();
        Double test = null;
        test = getRules().get(id);
        if (test != null)
            return Money.prepare(test.doubleValue());
        Rank rel = mplayer.getRank();
        String relString = rel.toString().toLowerCase();
        test = getRules().get(relString);
        if (test != null)
            return Money.prepare(test.doubleValue());
        return getDefaultTax();
    }

    public static String getTaxRuleKeyDesc(String taxKey, Object watcherObject) {
        if (TypeTaxKey.specials.contains(taxKey))
            return Txt.parse("<k>%s", new Object[] { Txt.upperCaseFirst(taxKey.toLowerCase()) });
        return ChatColor.WHITE.toString() + MixinDisplayName.get().getDisplayName(taxKey, watcherObject);
    }

    public static String getTaxRuleValueDesc(double taxValue) {
        return Txt.parse("<v>%s", new Object[] { Money.format(taxValue, false) });
    }

    public static String getTaxRuleDesc(String taxKey, double taxValue, Object watcherObject) {
        String taxRuleKeyDesc = getTaxRuleKeyDesc(taxKey, watcherObject);
        String taxRuleValueDesc = getTaxRuleValueDesc(taxValue);
        return Txt.parse("%s %s", new Object[] { taxRuleValueDesc, taxRuleKeyDesc });
    }

    public static String getTaxRuleDesc(Map.Entry<String, Double> rule, Object watcherObject) {
        return getTaxRuleDesc(rule.getKey(), ((Double)rule.getValue()).doubleValue(), watcherObject);
    }

    public double getUpkeepTax() {
        int chunkCount = BoardColl.get().getCount(getId());
        return getUpkeepTax(chunkCount);
    }

    public double getUpkeepTax(int chunkCount) {
        Faction faction = Faction.get(getId());
        MFlag flag = FactionsTax.getFlagTaxfree();
        if (faction.getFlag(flag))
            return 0.0D;
        double ret = (TConf.get()).upkeepBase + chunkCount * (TConf.get()).upkeepPerChunk;
        ret = Money.prepare(ret);
        return ret;
    }

    public String getUpkeepTaxDesc(int chunkCount) {
        double upkeepTax = getUpkeepTax(chunkCount);
        return getUpkeepTaxDesc(upkeepTax);
    }

    public String getUpkeepTaxDesc(double upkeepTax) {
        Faction faction = Faction.get(getId());
        MFlag flag = FactionsTax.getFlagTaxfree();
        if (faction.getFlag(flag))
            return Const.TAXFREE_TAX_DESC;
        double bank = Money.get(faction);
        return Txt.parse("<h>%s<i> / <h>%s<i> in faction bank.", new Object[] { Money.format(upkeepTax, true), Money.format(bank, true) });
    }
}
