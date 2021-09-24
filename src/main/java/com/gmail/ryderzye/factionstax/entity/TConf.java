package com.gmail.ryderzye.factionstax.entity;

import com.massivecraft.massivecore.collections.MassiveList;
import com.massivecraft.massivecore.store.Entity;
import java.util.List;

public class TConf extends Entity<TConf> {
    protected static transient TConf i;

    public static TConf get() {
        return i;
    }

    public boolean enabled = true;

    public double taxMaximum = 10.0D;

    public double taxMinimum = -10.0D;

    public double upkeepBase = 0.0D;

    public double upkeepPerChunk = 0.1D;

    public boolean upkeepFailUnclaimall = true;

    public boolean upkeepFailDisband = false;

    public long periodMillis = 86400000L;

    public long offsetMillis = 0L;

    public long playerInactiveMillis = 259200000L;

    public long lastMillis = 0L;

    public long pluginInstallationMillis = System.currentTimeMillis();

    public long pluginInstallationGraceMillis = 604800000L;

    public boolean installationGraceNoticeUsed = true;

    public int installationGraceNoticeDelayTicks = 0;

    public String installationGraceNoticeTitle = "FactionsTax Installed";

    public List<String> installationGraceNoticeLines = (List<String>)new MassiveList((Object[])new String[] { "<i>We just installed the Factions Expansion called FactionsTax.", "<i>Type <c>/f tax<i> to see all commands.", "<i>Type <c>/f tax i<i> for important info.", "<i>This notice will dissapear as the tax free grace period ends." });

    public int playerWarningTaxCount = 7;

    public List<String> playerWarningLines = (List<String>)new MassiveList((Object[])new String[] { "<a>Tax Warning: <b>You can't afford 1 week (<c>/f tax p<b>).", "<a>Learn to Earn: <aqua>https://www.massivecraft.com/massivemoney" });

    public int factionWarningTaxCount = 7;

    public List<String> factionWarningLines = (List<String>)new MassiveList((Object[])new String[] { "<a>Tax Warning: <b>Faction can't afford 1 week (<c>/f tax f<b>).", "<a>Suggestion: <i>Someone should <c>/f money deposit" });

    public boolean isEnabled() {
        if (!this.enabled)
            return false;
        if (this.periodMillis <= 0L)
            return false;
        return true;
    }

    public long getFirstMillis() {
        long ret = this.pluginInstallationMillis + this.pluginInstallationGraceMillis;
        ret -= ret % this.periodMillis;
        ret += this.periodMillis;
        ret += this.offsetMillis;
        return ret;
    }

    public long getInvocationFromMillis(long millis) {
        if (millis < getFirstMillis())
            return -1L;
        return (millis - getFirstMillis()) / this.periodMillis;
    }

    public long getNextInvocationFromMillis(long millis) {
        return getInvocationFromMillis(millis) + 1L;
    }

    public long getMillisFromInvocation(long invocation) {
        if (invocation < 0L)
            return 0L;
        return getFirstMillis() + invocation * this.periodMillis;
    }

    public long getNextMillisFromMillis(long millis) {
        long nextInvocation = getNextInvocationFromMillis(millis);
        long nextMillis = getMillisFromInvocation(nextInvocation);
        return nextMillis;
    }
}
