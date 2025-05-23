package iu.sna.domain.nfr_taxonomy;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NFRTaxonomyItem {
    private final Map<String, Integer> effects;

    public final String name;

    public NFRTaxonomyItem(String name, JSONObject config) {
        this.effects = new HashMap<>();
        this.name = name;

        for (String item : config.keySet())
            this.effects.put(item, config.getInt(item));
    }

    public boolean evaluate(Map<String, Double> topLayerItems) {
        double totalEffect = 0;
        for (String nfr : effects.keySet())
            totalEffect += topLayerItems.get(nfr) * effects.get(nfr);

        return totalEffect > 0;
    }
}
