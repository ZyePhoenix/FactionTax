package com.gmail.ryderzye.factionstax;

import com.gmail.ryderzye.factionstax.cmd.arg.TypeTaxKey;
import com.massivecraft.massivecore.comparator.ComparatorCombined;
import com.massivecraft.massivecore.comparator.ComparatorNaturalOrder;
import com.massivecraft.massivecore.comparator.ComparatorWithList;
import java.util.Comparator;

public class TaxKeyComparator extends ComparatorCombined<String> {
    private static TaxKeyComparator i = new TaxKeyComparator();

    public static TaxKeyComparator get() {
        return i;
    }

    public TaxKeyComparator() {
        super(new Comparator[] { (Comparator)(new ComparatorWithList(TypeTaxKey.specials))
                .setReversed(true),
                (Comparator)ComparatorNaturalOrder.get() });
        setSmart(true);
    }
}
