package com.gmail.ryderzye.factionstax.cmd;

import com.gmail.ryderzye.factionstax.Const;
import com.gmail.ryderzye.factionstax.entity.TConf;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.util.TimeDiffUtil;
import com.massivecraft.massivecore.util.TimeUnit;
import com.massivecraft.massivecore.util.Txt;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CmdFactionsTaxInfo extends FactionsTaxCommand {
    public void perform() {
        Object title = "Tax Overview Information";
        title = Txt.titleize(title);
        message(title);
        long nowMillis = System.currentTimeMillis();
        long periodMillis = (TConf.get()).periodMillis;
        boolean enabled = TConf.get().isEnabled();
        long nextMillis = TConf.get().getNextMillisFromMillis(nowMillis);
        String periodDesc = Const.TAXES_DISABLED;
        if (enabled) {
            LinkedHashMap<TimeUnit, Long> periodCounts = TimeDiffUtil.unitcounts(periodMillis);
            periodDesc = "Every " + TimeDiffUtil.formatedVerboose(periodCounts) + Txt.parse("<i>.");
        }
        msg("<a>Taxation Period: <i>%s", new Object[] { periodDesc });
        String nextDesc = Const.TAXES_DISABLED;
        if (enabled) {
            LinkedHashMap<TimeUnit, Long> nextCounts = TimeDiffUtil.unitcounts(nextMillis - nowMillis, TimeUnit.getAllButMillis());
            nextCounts = TimeDiffUtil.limit(nextCounts, 2);
            nextDesc = TimeDiffUtil.formatedVerboose(nextCounts) + Txt.parse(" <i>from now.");
        }
        msg("<a>Next Taxation: <i>%s", new Object[] { nextDesc });
        msg("<i>All player and faction taxes are paid at the same time.");
        double upkeepBase = (TConf.get()).upkeepBase;
        double upkeepPerChunk = (TConf.get()).upkeepPerChunk;
        String baseDesc = Money.format(upkeepBase, true);
        String chunkDesc = String.valueOf(upkeepPerChunk) + " * Chunks Claimed";
        List<String> upkeepDescParts = new ArrayList<>();
        if (upkeepBase != 0.0D)
            upkeepDescParts.add(baseDesc);
        if (upkeepPerChunk != 0.0D)
            upkeepDescParts.add(chunkDesc);
        String upkeepDesc = Money.format(0.0D, true);
        if (!upkeepDescParts.isEmpty())
            upkeepDesc = Txt.implode(upkeepDescParts, " + ");
        msg("<i>Faction upkeep tax is <h>%s<i>.", new Object[] { upkeepDesc });
        String failAction = null;
        if ((TConf.get()).upkeepFailDisband) {
            failAction = "it will disband";
        } else if ((TConf.get()).upkeepFailUnclaimall) {
            failAction = "all land will be unclaimed";
        } else {
            failAction = "nothing special will happen";
        }
        msg("<i>If a faction can't afford upkeep <h>%s<i>.", new Object[] { failAction });
        String taxMinimumDesc = Money.format((TConf.get()).taxMinimum, false);
        String taxMaximumDesc = Money.format((TConf.get()).taxMaximum, true);
        msg("<i>Leader sets player tax. It varies between <h>%s<i> and <h>%s<i>.", new Object[] { taxMinimumDesc, taxMaximumDesc });
        msg("<i>Leader sets if players who can't afford tax are kicked.");
        long inactiveMillis = (TConf.get()).playerInactiveMillis;
        if (inactiveMillis <= 0L) {
            msg("<i>Players pays tax <h>regardless <i>of how long they have been offline.");
        } else {
            LinkedHashMap<TimeUnit, Long> inactiveCounts = TimeDiffUtil.unitcounts(inactiveMillis);
            String inactiveDesc = TimeDiffUtil.formatedVerboose(inactiveCounts);
            msg("<i>Players stop paying tax after being offline for <h>%s<i>.", new Object[] { inactiveDesc });
        }
        msg("<aqua>https://www.massivecraft.com/factionstax");
    }
}
