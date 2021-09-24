package com.gmail.ryderzye.factionstax;

import com.massivecraft.massivecore.util.Txt;

public class Const {
    public static final String BASENAME = "factionstax";

    public static final String BASENAME_ = "factionstax_";

    public static final String COLLECTION_TFACTION = "factionstax_faction";

    public static final String COLLECTION_TCONF = "factionstax_mconf";

    public static final int TAX_UNIT_LIMIT = 2;

    public static final String PERM_ID_TAX = "tax";

    public static final int PERM_PRIORITY_TAX = 16500;

    public static final String FLAG_ID_TAXKICK = "taxkick";

    public static final String FLAG_ID_TAXFREE = "taxfree";

    public static final int FLAG_PRIORITY_TAXKICK = 2500;

    public static final int FLAG_PRIORITY_TAXFREE = 2510;

    public static final int SHOW_PRIORITY_FACTIONSTAX_PLAYERTAX = 7500;

    public static final int SHOW_PRIORITY_FACTIONSTAX_UPKEEPTAX = 7510;

    public static final String SHOW_ID_FACTIONSTAX_PLAYERTAX = "factionstax_playertax";

    public static final String SHOW_ID_FACTIONSTAX_UPKEEPTAX = "factionstax_upkeeptax";

    public static final String NULL_HISTORY = Txt.parse("<silver><italic>No history available.");

    public static final String TAXES_DISABLED = Txt.parse("<silver><italic>Taxes are disabled at the moment.");

    public static final String NEVER = Txt.parse("<silver><italic>Never.");

    public static final String YES = Txt.parse("<g>YES");

    public static final String NO = Txt.parse("<b>NO");

    public static final String TAXFREE_TAX_DESC = Txt.parse("<silver><italic>None, this faction has the taxfree flag.");

    public static final String NOTAX_RULES_DESC = Txt.parse("<silver><italic>None, this faction doesn't tax it's players.");
}
